package com.seatomatic.papers;

import com.seatomatic.core.SeatingPlan;
import com.seatomatic.core.SeatingPlanRepository;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvigilatorAssignmentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private InvigilatorAssignmentService assignmentService;

    @Autowired
    private SeatingPlanRepository seatingPlanRepository;

    private Exam exam1;
    private Exam exam2;
    private Room room1;
    private Room room2;
    private UserAccount invigilatorUser;
    private InvigilatorAssignmentService.AssignmentView assignment1;
    private SeatingPlan plan1;
    private SeatingPlan plan2;

    @BeforeEach
    void setUp() {
        room1 = roomRepository.save(new Room("Hall-A", "Main Building", 6, 6, 30));
        room2 = roomRepository.save(new Room("Hall-B", "Science Building", 6, 6, 30));

        exam1 = examRepository.save(new Exam("EX-SEC-1", "Security Fundamentals", "Cybersecurity",
                LocalDateTime.now().plusDays(1), 20, room1));
        exam2 = examRepository.save(new Exam("EX-SEC-2", "Software Engineering", "SE",
                LocalDateTime.now().plusDays(2), 20, room2));

        invigilatorUser = userRepository.save(new UserAccount("assigned.invigilator", "hashed_password_123", UserAccount.Role.INVIGILATOR));

        assignment1 = assignmentService.assign(exam1.getId(), room1.getId(), invigilatorUser.getId());

        plan1 = seatingPlanRepository.save(new SeatingPlan(exam1));
        plan2 = seatingPlanRepository.save(new SeatingPlan(exam2));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_CanListAllAssignments() throws Exception {
        mockMvc.perform(get("/api/v1/invigilator-assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_CanAssignInvigilator() throws Exception {
        UserAccount invig2 = userRepository.save(new UserAccount("new.invig", "hashed_password_123", UserAccount.Role.INVIGILATOR));

        String payload = String.format("{\"examId\": %d, \"roomId\": %d, \"userId\": %d}",
                exam2.getId(), room2.getId(), invig2.getId());

        mockMvc.perform(post("/api/v1/invigilator-assignments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invigilatorUsername").value("new.invig"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_CanRemoveAssignment() throws Exception {
        mockMvc.perform(delete("/api/v1/invigilator-assignments/" + assignment1.id())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "faculty", roles = {"FACULTY"})
    void faculty_CannotAssignInvigilator_Returns403() throws Exception {
        String payload = String.format("{\"examId\": %d, \"roomId\": %d, \"userId\": %d}",
                exam2.getId(), room2.getId(), invigilatorUser.getId());

        mockMvc.perform(post("/api/v1/invigilator-assignments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "faculty", roles = {"FACULTY"})
    void faculty_CannotRemoveAssignment_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/invigilator-assignments/" + assignment1.id())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "faculty", roles = {"FACULTY"})
    void faculty_CannotListAssignmentsEndpoint_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/invigilator-assignments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assigned.invigilator", roles = {"INVIGILATOR"})
    void invigilator_CannotAssignOrRemove_Returns403() throws Exception {
        String payload = String.format("{\"examId\": %d, \"roomId\": %d, \"userId\": %d}",
                exam2.getId(), room2.getId(), invigilatorUser.getId());

        mockMvc.perform(post("/api/v1/invigilator-assignments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/invigilator-assignments/" + assignment1.id())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assigned.invigilator", roles = {"INVIGILATOR"})
    void invigilator_CanViewOwnAssignments() throws Exception {
        mockMvc.perform(get("/api/v1/invigilator-assignments/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].examCode").value("EX-SEC-1"));
    }

    @Test
    @WithMockUser(username = "faculty", roles = {"FACULTY"})
    void faculty_CannotCallMyAssignments_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/invigilator-assignments/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assigned.invigilator", roles = {"INVIGILATOR"})
    void invigilator_CanViewAssignedExamPlan_Success() throws Exception {
        mockMvc.perform(get("/workspace/plans/" + plan1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "assigned.invigilator", roles = {"INVIGILATOR"})
    void invigilator_CannotViewUnassignedExamPlan_Returns403() throws Exception {
        mockMvc.perform(get("/workspace/plans/" + plan2.getId()))
                .andExpect(status().isForbidden());
    }
}
