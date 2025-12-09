package com.ssafy.gitaek.model;
import lombok.Data;

@Data
public class User {
    private int userId;
    private String email;
    private String password;
    private String nickname;
    private String role; // ★ 추가됨
    private String createdAt;
    private String role; // 관리자 권한을 위한 추가
}