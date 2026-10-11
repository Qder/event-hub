package org.example.eventhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 强度参数 10，是生产环境推荐的平衡值
        // 数字越大越安全，但计算越慢（每 +1 慢一倍）
        return new BCryptPasswordEncoder(10);
    }
}