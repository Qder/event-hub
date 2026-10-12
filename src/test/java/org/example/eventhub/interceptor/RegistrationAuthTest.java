package org.example.eventhub.interceptor;

import org.example.eventhub.common.UserContext;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.service.StudentService;
import org.example.eventhub.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrationAuthTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private StudentService studentService;

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    private Long createStudent() {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setStudentNo("RA_" + UUID.randomUUID().toString().substring(0, 8));
        req.setName("报名认证测试");
        req.setPhone("13800000000");
        return studentService.createStudent(req);
    }

    @Test
    void register_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/events/1/registrations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withToken_shouldNotReturn401() throws Exception {
        Long studentId = createStudent();
        String token = jwtUtil.generateToken(studentId, "T001");

        // 活动 1 可能不存在，会返回业务异常，但不是 401
        mockMvc.perform(post("/api/events/1/registrations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}