package com.ssafy.gitaek.model;
import lombok.Data;

@Data
public class User {
    private int userId;
    private String email;
    private String password;
    private String nickname;
    private String createdAt;
}