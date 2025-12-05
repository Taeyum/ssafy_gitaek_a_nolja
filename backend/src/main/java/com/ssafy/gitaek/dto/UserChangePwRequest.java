package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class UserChangePwRequest {
    private String currentPassword; // 현재 비밀번호 (확인용)
    private String newPassword;     // 바꿀 비밀번호
}