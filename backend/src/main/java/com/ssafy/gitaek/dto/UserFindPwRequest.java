package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class UserFindPwRequest {

    private String email;
    private String nickname;

}