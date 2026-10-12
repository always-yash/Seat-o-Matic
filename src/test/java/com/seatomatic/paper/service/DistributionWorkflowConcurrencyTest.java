package com.seatomatic.paper.service;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.exception.ValidationException;
import com.seatomatic.paper.dao.PaperDistributionDao;
import com.seatomatic.paper.dao.QuestionPaperDao;
import com.seatomatic.paper.model.DistributionStatus;
import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.QuestionPaper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Concurrency test suite verifying race condition prevention and optimistic version locking.
 */
class DistributionWorkflowConcurrencyTest {

    private PaperDistributionDao mockDao;
    private QuestionPaperDao mockPaperDao;
    private AuditPublisher mockAudit;
    private DistributionWorkflowService service;

    @BeforeEach
    void setUp() {
        mockDao = mock(PaperDistributionDao.class);
        mockPaperDao = mock(QuestionPaperDao.class);
        mockAudit = mock(AuditPublisher.class);
        service = new DistributionWorkflowService(mockDao, mockPaperDao, mockAudit);
    }

    @Test
    @DisplayName("Concurrency: Simultaneous state updates detect conflict and reject second thread")
    void testConcurrentModificationRejection() throws Exception {
        Long distId = 100L;
        PaperDistribution dist = new PaperDistribution(1L, 101L, 501L, 50, 5);
        dist.setId(distId);
        dist.setStatus(DistributionStatus.PENDING);
        dist.setVersion(0);

        QuestionPaper paper = new QuestionPaper(10L, 1L, "Set A", 100);
        paper.setId(501L);

        when(mockDao.findById(distId)).thenAnswer(inv -> {
            PaperDistribution d = new PaperDistribution(1L, 101L, 501L, 50, 5);
            d.setId(distId);
            d.setStatus(DistributionStatus.PENDING);
            d.setVersion(0);
            return Optional.of(d);
        });
        when(mockPaperDao.findById(501L)).thenReturn(Optional.of(paper));

        // First thread succeeds with version 0 -> 1; Second thread fails because version is already 1
        when(mockDao.updateStatusWithVersion(eq(distId), eq(DistributionStatus.PREPARED), eq(0)))
                .thenReturn(1);
        when(mockDao.updateStatusWithVersion(eq(distId), eq(DistributionStatus.PREPARED), eq(1)))
                .thenReturn(0);

        // Thread 1 succeeds
        assertDoesNotThrow(() -> service.markPreparedWithOptimisticLock(distId, 0, 1L));

        // Thread 2 with stale version throws ValidationException
        ValidationException ex = assertThrows(ValidationException.class, () ->
                service.markPreparedWithOptimisticLock(distId, 1, 2L));

        assertTrue(ex.getMessage().contains("Concurrent modification detected"));
    }

    @Test
    @DisplayName("Concurrency: Multi-threaded dispatch race condition results in exactly 1 winner")
    void testMultiThreadedDispatchRaceCondition() throws Exception {
        Long distId = 200L;
        PaperDistribution dist = new PaperDistribution(1L, 102L, 502L, 40, 4);
        dist.setId(distId);
        dist.setStatus(DistributionStatus.PREPARED);
        dist.setVersion(1);

        when(mockDao.findById(distId)).thenReturn(Optional.of(dist));

        AtomicInteger winnerCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        // Simulate atomic database version update: exactly one thread gets row count 1
        when(mockDao.updateDistributedWithVersion(eq(distId), eq(40), eq(DistributionStatus.DISTRIBUTED), eq(1)))
                .thenAnswer(invocation -> winnerCount.compareAndSet(0, 1) ? 1 : 0);

        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final long actorId = 10L + i;
            executor.submit(() -> {
                try {
                    latch.await();
                    service.markDistributedWithOptimisticLock(distId, 40, 1, actorId);
                } catch (ValidationException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, winnerCount.get(), "Exactly one thread must succeed in transitioning packet");
        assertEquals(threads - 1, conflictCount.get(), "All other concurrent callers must receive conflict rejection");
    }
}
