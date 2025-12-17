package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class UserFindPwRequest {
    // 비밀번호 찾기 시 입력받는 정보 (UserService의 로직과 일치해야 함)
    private String email;
    private String nickname;
}