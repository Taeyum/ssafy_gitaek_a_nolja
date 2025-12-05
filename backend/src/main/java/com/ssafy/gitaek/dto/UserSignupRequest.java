package com.ssafy.gitaek.dto;

import com.ssafy.gitaek.model.User;
import lombok.Data;

@Data
public class UserSignupRequest {

    private String email;
    private String password;
    private String nickname;

    public User toEntity() {
        User user = new User();
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setNickname(this.nickname);
        return user;
    }
}