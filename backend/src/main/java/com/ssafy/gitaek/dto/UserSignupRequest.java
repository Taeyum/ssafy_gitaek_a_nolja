package com.ssafy.gitaek.dto;

import com.ssafy.gitaek.model.User;
import lombok.Data;

@Data
public class UserSignupRequest {

    private String email;
    private String password;
    private String nickname;
    
    // ★ [수정] 갈치 기능(전화번호) 추가
    private String phoneNumber; 

    public User toEntity() {
        User user = new User();
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setNickname(this.nickname);
        user.setPhoneNumber(this.phoneNumber);
        
        // ★ [수정] 시큐리티 권한 설정 (기본값 USER)
        user.setRole("USER"); 
        return user;
    }
}