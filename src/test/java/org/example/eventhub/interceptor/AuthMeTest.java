package org.example.eventhub.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthMeTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private StudentService studentService;
    @Autowired private ObjectMapper objectMapper;

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    private Long createStudent() {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setStudentNo("ME_" + UUID.randomUUID().toString().substring(0, 8));
        req.setName("当前用户测试");
        req.setPhone("13800000000");
        return studentService.createStudent(req);
    }

    @Test
    void me_shouldReturnCurrentUser() throws Exception {
        Long studentId = createStudent();
        String token = jwtUtil.generateToken(studentId, "T001");

        MvcResult result = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        String json = result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("响应: " + json);

        // 断言响应里包含正确的 name 和 studentNo
        assertThat(json).contains("\"code\":0");
        assertThat(json).contains("\"name\":\"当前用户测试\"");
        assertThat(json).contains("\"id\":" + studentId);
    }

    @Test
    void me_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .status().isUnauthorized());
    }

    @Test
    void me_withInvalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .status().isUnauthorized());
    }
}