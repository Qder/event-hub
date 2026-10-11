package org.example.eventhub.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PasswordTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void encode_samePassword_producesDifferentHashes() {
        String raw = "123456";
        String hash1 = passwordEncoder.encode(raw);
        String hash2 = passwordEncoder.encode(raw);

        System.out.println("hash1 = " + hash1);
        System.out.println("hash2 = " + hash2);

        // 两次哈希完全不同（盐值不同）
        assertThat(hash1).isNotEqualTo(hash2);
        // 但都能校验通过
        assertThat(passwordEncoder.matches(raw, hash1)).isTrue();
        assertThat(passwordEncoder.matches(raw, hash2)).isTrue();
    }

    @Test
    void matches_wrongPassword_returnsFalse() {
        String hash = passwordEncoder.encode("correct");
        assertThat(passwordEncoder.matches("wrong", hash)).isFalse();
        assertThat(passwordEncoder.matches("correct", hash)).isTrue();
    }

    @Test
    void bcryptHashFormat() {
        String hash = passwordEncoder.encode("123456");
        System.out.println("hash = " + hash);
        // BCrypt 哈希以 $2a$ 开头，长度固定 60
        assertThat(hash).startsWith("$2a$");
        assertThat(hash).hasSize(60);
    }
}