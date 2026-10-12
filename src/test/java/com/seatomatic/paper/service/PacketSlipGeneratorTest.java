package com.seatomatic.paper.service;

import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.dto.PacketSlipDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PacketSlipGeneratorTest {

    private PacketSlipGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PacketSlipGenerator();
    }

    @Test
    @DisplayName("Slip Generator: Generates accurate slip metrics and student counts")
    void testGenerateSlipAccurateMetrics() {
        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 45, 5);
        dist.setId(1001L);
        dist.setExamId(50L);
        dist.setRoomCode("LH-201");
        dist.setSubjectId(301L);
        dist.setSubjectCode("CS301");
        dist.setSubjectName("Distributed Systems");
        dist.setSetCode("Set B");

        PacketSlipDTO slip = generator.generateSlip(dist, "Final Examination 2026", "2026-11-15", "Dr. Alan Turing");

        assertNotNull(slip);
        assertEquals(1001L, slip.getDistributionId());
        assertEquals(50L, slip.getExamId());
        assertEquals("Final Examination 2026", slip.getExamTitle());
        assertEquals("2026-11-15", slip.getExamDate());
        assertEquals("LH-201", slip.getRoomCode());
        assertEquals("CS301", slip.getSubjectCode());
        assertEquals("Distributed Systems", slip.getSubjectName());
        assertEquals("Set B", slip.getSetCode());
        assertEquals(40, slip.getStudentCount(), "Student count must be total required (45) minus buffer (5)");
        assertEquals(5, slip.getBufferCount());
        assertEquals(45, slip.getTotalCopies());
        assertEquals("Dr. Alan Turing", slip.getInvigilatorName());
        assertNotNull(slip.getGeneratedAt());
    }

    @Test
    @DisplayName("Slip Generator: Safe fallbacks on null metadata")
    void testGenerateSlipNullSafetyFallbacks() {
        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 20, 2);
        dist.setId(1002L);
        dist.setExamId(50L);

        PacketSlipDTO slip = generator.generateSlip(dist, null, null, null);

        assertNotNull(slip);
        assertEquals("Examination #50", slip.getExamTitle());
        assertEquals("N/A", slip.getExamDate());
        assertEquals("Room #101", slip.getRoomCode());
        assertEquals("SUBJ", slip.getSubjectCode());
        assertEquals("Set A", slip.getSetCode());
        assertEquals("Unassigned", slip.getInvigilatorName());
    }

    @Test
    @DisplayName("Slip Generator: Formats readable ASCII slip with required layout sections")
    void testFormatAsciiSlip() {
        PaperDistribution dist = new PaperDistribution(10L, 101L, 501L, 30, 2);
        dist.setId(1003L);
        dist.setExamId(50L);
        dist.setRoomCode("AUD-1");
        dist.setSubjectCode("MA101");
        dist.setSubjectName("Calculus");
        dist.setSetCode("Set A");

        PacketSlipDTO slip = generator.generateSlip(dist, "Midterm 2026", "2026-10-20", "Prof. Gauss");
        String ascii = generator.formatAsciiSlip(slip);

        assertNotNull(ascii);
        assertTrue(ascii.contains("AUD-1"));
        assertTrue(ascii.contains("MA101 - Calculus"));
        assertTrue(ascii.contains("Students   : 28"));
        assertTrue(ascii.contains("Buffer     : 2"));
        assertTrue(ascii.contains("Total Pkts : 30"));
        assertTrue(ascii.contains("Prof. Gauss"));
    }

    @Test
    @DisplayName("Slip Generator: Batch generates slips for multiple room distributions")
    void testBatchGeneration() {
        PaperDistribution d1 = new PaperDistribution(10L, 101L, 501L, 30, 2);
        d1.setId(1L);
        PaperDistribution d2 = new PaperDistribution(10L, 102L, 501L, 25, 2);
        d2.setId(2L);

        List<PacketSlipDTO> batch = generator.generateBatchSlips(List.of(d1, d2), "Midterm 2026", "2026-10-20");

        assertEquals(2, batch.size());
        assertEquals(1L, batch.get(0).getDistributionId());
        assertEquals(2L, batch.get(1).getDistributionId());
    }
}
