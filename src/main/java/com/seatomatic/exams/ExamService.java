package com.seatomatic.exams;

import com.seatomatic.analysis.AuditService;
import com.seatomatic.student.Student;
import com.seatomatic.student.StudentService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamService {
    private final ExamRepository exams;
    private final RoomRepository rooms;
    private final ExamCandidateRepository candidates;
    private final StudentService students;
    private final AuditService audit;
    public ExamService(ExamRepository exams, RoomRepository rooms, ExamCandidateRepository candidates,
                       StudentService students, AuditService audit) {
        this.exams = exams; this.rooms = rooms; this.candidates = candidates; this.students = students; this.audit = audit;
    }
    @Transactional(readOnly = true) public List<Exam> findExams() { return exams.findAll(); }
    @Transactional(readOnly = true) public Exam findExam(Long id) { return requireExam(id); }
    @Transactional(readOnly = true) public List<Exam> findUpcoming() { return exams.findTop5ByOrderByStartsAtAsc(); }
    @Transactional(readOnly = true) public long countExams() { return exams.count(); }
    @Transactional(readOnly = true) public long countRooms() { return rooms.count(); }
    @Transactional(readOnly = true) public List<Room> findRooms() { return rooms.findAll(); }
    @Transactional(readOnly = true) public Room findRoom(Long id) { return rooms.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found.")); }
    @Transactional(readOnly = true) public List<Room> findFeaturedRooms() { return rooms.findTop4ByOrderByNameAsc(); }
    @Transactional(readOnly = true) public List<ExamCandidate> findCandidates(Long examId) {
        requireExam(examId); return candidates.findByExamIdOrderByStudentFullName(examId);
    }
    @Transactional public Room createRoom(CreateRoom input) {
        long gridSize = (long) input.rows() * input.columns();
        if (input.capacity() > gridSize) throw new IllegalArgumentException("Capacity cannot exceed the room grid.");
        Room room = rooms.save(new Room(input.name(), input.building(), input.rows(), input.columns(), input.capacity()));
        audit.record("ROOM_CREATED", "Room", room.getName() + " · " + room.getCapacity() + " seats");
        return room;
    }
    @Transactional public Exam createExam(CreateExam input) {
        Room room = rooms.findById(input.roomId()).orElseThrow(() -> new IllegalArgumentException("Room not found."));
        if (input.expectedStudents() > room.getCapacity()) throw new IllegalArgumentException("Expected students cannot exceed the selected room's capacity.");
        Exam exam = exams.save(new Exam(input.code(), input.name(), input.subject(), input.startsAt(), input.expectedStudents(), room));
        audit.record("EXAM_CREATED", "Examination", exam.getCode() + " · " + exam.getName());
        return exam;
    }
    @Transactional public ExamCandidate registerCandidate(Long examId, String studentId) {
        Exam exam = requireExam(examId);
        if (exam.isRegistrationClosed()) throw new IllegalArgumentException("Candidate registration is closed because a seating plan has been generated.");
        if (candidates.findByExamIdOrderByStudentFullName(examId).size() >= exam.getExpectedStudents()) {
            throw new IllegalArgumentException("The registered roster already matches the expected student count.");
        }
        Student student = students.findActive(studentId);
        if (exam.getRoom() != null && candidates.findByExamIdOrderByStudentFullName(examId).size() >= exam.getRoom().getCapacity()) {
            throw new IllegalArgumentException("The candidate roster cannot exceed room capacity.");
        }
        ExamCandidate candidate = candidates.save(new ExamCandidate(exam, student));
        audit.record("CANDIDATE_REGISTERED", "Examination", exam.getCode() + " · " + student.getStudentId());
        return candidate;
    }
    @Transactional public void closeCandidateRegistration(Long examId) {
        Exam exam = requireExam(examId);
        if (!exam.isRegistrationClosed()) {
            exam.closeCandidateRegistration();
            audit.record("CANDIDATE_REGISTRATION_CLOSED", "Examination", exam.getCode());
        }
    }
    private Exam requireExam(Long id) { return exams.findById(id).orElseThrow(() -> new IllegalArgumentException("Examination not found.")); }
    public record CreateRoom(String name, String building, int rows, int columns, int capacity) { }
    public record CreateExam(String code, String name, String subject, LocalDateTime startsAt, int expectedStudents, Long roomId) { }
}
