package com.ssafy.gitaek.model;

import lombok.Data;

@Data
public class User {
    private int userId;
    private String email;
    private String password;
    private String nickname;
    private String createdAt;
    
    // ★ [필수] DB와 짝을 맞추기 위해 추가함 (이거 없으면 에러남!)
    private String phoneNumber; 
    
    // 시큐리티 권한 (USER, ADMIN)
    private String role; 
}