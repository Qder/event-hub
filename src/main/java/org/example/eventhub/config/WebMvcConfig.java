package org.example.eventhub.config;

import lombok.RequiredArgsConstructor;
import org.example.eventhub.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")                    // 拦截所有 API
                .excludePathPatterns(
                        "/api/auth/login",                     // 登录接口放行
                        "/api/auth/register"                   // 如果以后有注册接口也放行
                );
    }
}