package org.example.eventhub.interceptor;

import org.example.eventhub.common.UserContext;
import org.example.eventhub.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtInterceptorTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    void noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validToken_shouldPassInterceptor() throws Exception {
        String token = jwtUtil.generateToken(1L, "T001");

        // 没有 id=1 的活动，会返回业务异常（"活动不存在"），但拦截器已经放行
        MvcResult result = mockMvc.perform(get("/api/events/999999")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        // 不是 401，说明拦截器放行了
        assertThat(result.getResponse().getStatus()).isNotEqualTo(401);
    }

    @Test
    void loginEndpoint_shouldBeExcluded() throws Exception {
        // 登录接口不需要 token，即使没有 Authorization 头也不应该 401
        // 这里故意发一个错误请求体，看返回的是参数校验错误，不是 401
        MvcResult result = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/auth/login")
                                .contentType("application/json")
                                .content("{}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(401);
    }
}