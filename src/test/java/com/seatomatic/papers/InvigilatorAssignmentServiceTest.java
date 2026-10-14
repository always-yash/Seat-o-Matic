package com.seatomatic.papers;

import com.seatomatic.core.UserAccount;
import com.seatomatic.core.UserAccountRepository;
import com.seatomatic.exams.Exam;
import com.seatomatic.exams.ExamRepository;
import com.seatomatic.exams.Room;
import com.seatomatic.exams.RoomRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InvigilatorAssignmentServiceTest {

    @Autowired
    private InvigilatorAssignmentService assignmentService;

    @Autowired
    private InvigilatorAssignmentRepository assignmentRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserAccountRepository userRepository;

    private Exam exam1;
    private Exam exam2;
    private Room room1;
    private Room room2;
    private UserAccount invigilator1;
    private UserAccount invigilator2;
    private UserAccount facultyUser;
    private UserAccount adminUser;
    private UserAccount inactiveInvigilator;

    @BeforeEach
    void setUp() {
        room1 = roomRepository.save(new Room("Test-R101", "Main Hall", 5, 5, 20));
        room2 = roomRepository.save(new Room("Test-R102", "Science Block", 5, 5, 20));

        exam1 = examRepository.save(new Exam("TEST-101", "Algorithm Design", "Algorithms",
                LocalDateTime.now().plusDays(2), 20, room1));
        exam2 = examRepository.save(new Exam("TEST-102", "Database Systems", "Databases",
                LocalDateTime.now().plusDays(3), 20, room2));

        invigilator1 = userRepository.save(new UserAccount("invig.active1", "hashed_pass_1234", UserAccount.Role.INVIGILATOR));
        invigilator2 = userRepository.save(new UserAccount("invig.active2", "hashed_pass_1234", UserAccount.Role.INVIGILATOR));

        facultyUser = userRepository.save(new UserAccount("faculty.test", "hashed_pass_1234", UserAccount.Role.FACULTY));
        adminUser = userRepository.save(new UserAccount("admin.test", "hashed_pass_1234", UserAccount.Role.ADMIN));

        inactiveInvigilator = new UserAccount("invig.paused", "hashed_pass_1234", UserAccount.Role.INVIGILATOR);
        inactiveInvigilator.setActive(false);
        inactiveInvigilator = userRepository.save(inactiveInvigilator);
    }

    @Test
    void assignInvigilator_Success() {
        var view = assignmentService.assign(exam1.getId(), room1.getId(), invigilator1.getId());

        assertNotNull(view);
        assertNotNull(view.id());
        assertEquals(exam1.getId(), view.examId());
        assertEquals(room1.getId(), view.roomId());
        assertEquals(invigilator1.getId(), view.invigilatorId());
        assertEquals("invig.active1", view.invigilatorUsername());

        assertTrue(assignmentService.isInvigilatorAssignedToExam("invig.active1", exam1.getId()));
    }

    @Test
    void assignInvigilator_Fails_WhenUserIsNotInvigilatorRole() {
        IllegalArgumentException facultyEx = assertThrows(IllegalArgumentException.class, () ->
                assignmentService.assign(exam1.getId(), room1.getId(), facultyUser.getId()));
        assertTrue(facultyEx.getMessage().contains("INVIGILATOR"));

        IllegalArgumentException adminEx = assertThrows(IllegalArgumentException.class, () ->
                assignmentService.assign(exam1.getId(), room1.getId(), adminUser.getId()));
        assertTrue(adminEx.getMessage().contains("INVIGILATOR"));
    }

    @Test
    void assignInvigilator_Fails_WhenAccountIsInactive() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                assignmentService.assign(exam1.getId(), room1.getId(), inactiveInvigilator.getId()));
        assertTrue(ex.getMessage().contains("paused or inactive"));
    }

    @Test
    void assignInvigilator_Fails_WhenRoomDoesNotBelongToExam() {
        // room2 is assigned to exam2, not exam1
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                assignmentService.assign(exam1.getId(), room2.getId(), invigilator1.getId()));
        assertTrue(ex.getMessage().contains("not assigned to this examination"));
    }

    @Test
    void assignInvigilator_ReassignsRoomToDifferentInvigilator() {
        var first = assignmentService.assign(exam1.getId(), room1.getId(), invigilator1.getId());
        assertEquals("invig.active1", first.invigilatorUsername());

        var reassigned = assignmentService.assign(exam1.getId(), room1.getId(), invigilator2.getId());
        assertEquals(first.id(), reassigned.id());
        assertEquals("invig.active2", reassigned.invigilatorUsername());

        assertTrue(assignmentService.isInvigilatorAssignedToExam("invig.active2", exam1.getId()));
        assertFalse(assignmentService.isInvigilatorAssignedToExam("invig.active1", exam1.getId()));
    }

    @Test
    void removeAssignment_Success() {
        var view = assignmentService.assign(exam1.getId(), room1.getId(), invigilator1.getId());
        assertTrue(assignmentRepository.existsById(view.id()));

        assignmentService.remove(view.id());
        assertFalse(assignmentRepository.existsById(view.id()));
        assertFalse(assignmentService.isInvigilatorAssignedToExam("invig.active1", exam1.getId()));
    }

    @Test
    void findForInvigilator_ReturnsOnlyTheirAssignments() {
        assignmentService.assign(exam1.getId(), room1.getId(), invigilator1.getId());
        assignmentService.assign(exam2.getId(), room2.getId(), invigilator2.getId());

        var list1 = assignmentService.findForInvigilator("invig.active1");
        assertEquals(1, list1.size());
        assertEquals("TEST-101", list1.get(0).examCode());

        var list2 = assignmentService.findForInvigilator("invig.active2");
        assertEquals(1, list2.size());
        assertEquals("TEST-102", list2.get(0).examCode());
    }

    @Test
    void listCoverage_ReturnsAllExamRoomsWithAccurateStatus() {
        assignmentService.assign(exam1.getId(), room1.getId(), invigilator1.getId());

        var coverage = assignmentService.listCoverage();
        assertTrue(coverage.size() >= 2);

        var exam1Coverage = coverage.stream().filter(c -> c.examId().equals(exam1.getId())).findFirst().orElseThrow();
        assertTrue(exam1Coverage.assigned());
        assertEquals("invig.active1", exam1Coverage.invigilatorUsername());

        var exam2Coverage = coverage.stream().filter(c -> c.examId().equals(exam2.getId())).findFirst().orElseThrow();
        assertFalse(exam2Coverage.assigned());
        assertNull(exam2Coverage.invigilatorUsername());
    }

    @Test
    void isInvigilatorAssignedToExamAndRoom_ValidatesAccurately() {
        assignmentService.assign(exam1.getId(), room1.getId(), invigilator1.getId());
        assertTrue(assignmentService.isInvigilatorAssignedToExamAndRoom("invig.active1", exam1.getId(), room1.getId()));
        assertFalse(assignmentService.isInvigilatorAssignedToExamAndRoom("invig.active1", exam2.getId(), room2.getId()));
        assertFalse(assignmentService.isInvigilatorAssignedToExamAndRoom("invig.active2", exam1.getId(), room1.getId()));
        assertFalse(assignmentService.isInvigilatorAssignedToExamAndRoom("nonexistent", exam1.getId(), room1.getId()));
    }
}
