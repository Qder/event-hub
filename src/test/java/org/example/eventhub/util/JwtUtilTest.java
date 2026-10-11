package org.example.eventhub.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAndParse_shouldRoundTrip() {
        String token = jwtUtil.generateToken(123L, "T2026001");
        System.out.println("token = " + token);

        // token 是三段，用 . 分隔
        assertThat(token.split("\\.")).hasSize(3);

        // 能解析出 studentId
        Long studentId = jwtUtil.getStudentId(token);
        assertThat(studentId).isEqualTo(123L);

        // Claims 里有 studentNo
        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.get("studentNo")).isEqualTo("T2026001");
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(1L, "T001");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken(1L, "T001");
        // 篡改签名（最后一段）
        String tampered = token.substring(0, token.length() - 3) + "xxx";
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_garbage_returnsFalse() {
        assertThat(jwtUtil.validateToken("not.a.jwt")).isFalse();
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    void differentUser_differentToken() {
        String t1 = jwtUtil.generateToken(1L, "T001");
        String t2 = jwtUtil.generateToken(2L, "T002");
        assertThat(t1).isNotEqualTo(t2);
        assertThat(jwtUtil.getStudentId(t1)).isEqualTo(1L);
        assertThat(jwtUtil.getStudentId(t2)).isEqualTo(2L);
    }
}