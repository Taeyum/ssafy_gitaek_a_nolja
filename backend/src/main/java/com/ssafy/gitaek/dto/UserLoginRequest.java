package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String email;
    private String password;
    private boolean rememberId;

}