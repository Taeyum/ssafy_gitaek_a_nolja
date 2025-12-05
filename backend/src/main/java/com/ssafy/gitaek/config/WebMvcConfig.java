package com.ssafy.gitaek.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 주소에 대해
                .allowedOrigins("http://localhost:5173", "http://localhost:8080") // ★ 중요:보통 Vite 쓰면 5173, Webpack이면 8080 등  Vue가 실행되는 주소 적기
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // ★ 핵심: 쿠키(세션ID)를 주고받겠다!
    }
}