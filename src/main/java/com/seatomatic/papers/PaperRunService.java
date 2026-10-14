package com.seatomatic.papers;

import com.seatomatic.analysis.AuditService;
import com.seatomatic.exams.Exam;
import com.seatomatic.exams.Room;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaperRunService {
    private final PaperRunRepository runs;
    private final AuditService audit;
    private final int bufferCopies;
    public PaperRunService(PaperRunRepository runs, AuditService audit,
                           @Value("${seatomatic.papers.buffer-copies:2}") int bufferCopies) {
        this.runs = runs; this.audit = audit; this.bufferCopies = bufferCopies;
    }
    @Transactional(readOnly = true) public List<PaperRun> findAll() { return runs.findAll(); }
    @Transactional(readOnly = true) public long count() { return runs.count(); }
    @Transactional(readOnly = true) public java.util.Optional<PaperRun> findById(Long id) { return runs.findById(id); }
    @Transactional public PaperRun prepareRequirement(Exam exam, Room room, int candidates) {
        var existing = runs.findByExamIdAndRoomIdAndSetCode(exam.getId(), room.getId(), "SET-A");
        if (existing.isPresent()) {
            PaperRun run = existing.get();
            if (run.updateRequirement(candidates, bufferCopies)) {
            audit.record("PAPER_REQUIREMENT_UPDATED", "Paper run", exam.getCode() + " · " + room.getName() + " · " + candidates + " copies + " + bufferCopies + " buffer");
                return runs.save(run);
            }
            return run;
        }
        PaperRun run = runs.save(new PaperRun(exam, room, "SET-A", candidates, bufferCopies));
        audit.record("PAPER_RUN_CREATED", "Paper run", exam.getCode() + " · " + room.getName() + " · " + candidates + " copies + " + bufferCopies + " buffer");
        return run;
    }
    @Transactional public PaperRun transition(Long id, PaperRun.Status next) {
        PaperRun run = runs.findById(id).orElseThrow(() -> new IllegalArgumentException("Paper run not found."));
        run.transitionTo(next);
        audit.record("PAPER_" + next, "Paper run", run.getExam().getCode() + " · " + run.getRoom().getName());
        return run;
    }
}
