package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String email;
    private String password;
    
    // 프론트엔드 호환용 (JWT에서는 보통 클라이언트가 처리하지만 필드는 유지)
    private boolean rememberId; 
}