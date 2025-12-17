package com.ssafy.gitaek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate; // ★ 만능 SQL 도구 추가
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ssafy.gitaek.mapper.UserMapper;
import com.ssafy.gitaek.model.User;

@SpringBootApplication
public class GitaekApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitaekApplication.class, args);
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate; // ★ 이걸로 직접 DB를 수정합니다

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            String adminEmail = "admin@ssafy.com";
            
            // 1. 관리자가 존재하는지 확인
            User existingAdmin = userMapper.selectUserByEmail(adminEmail);
            
            if (existingAdmin != null) {
                // ★ 이미 존재하면 -> 비밀번호를 '1234'로 강제 업데이트! (SQL 직접 실행)
                System.out.println(">>> [SYSTEM] 기존 관리자 발견! 비밀번호를 '1234'로 강제 초기화합니다.");
                
                String newPw = passwordEncoder.encode("1234");
                jdbcTemplate.update("UPDATE Users SET password = ?, role = 'ADMIN' WHERE email = ?", newPw, adminEmail);
                
                System.out.println(">>> [SYSTEM] 관리자 계정 복구 완료 (비번: 1234)");
            } else {
                // 2. 없으면 -> 새로 생성
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("1234"));
                admin.setNickname("슈퍼관리자");
                admin.setPhoneNumber("010-0000-0000");
                admin.setRole("ADMIN");
                
                userMapper.insertUser(admin);
                System.out.println(">>> [SYSTEM] 관리자 계정 생성 완료 (비번: 1234)");
            }
        };
    }
}