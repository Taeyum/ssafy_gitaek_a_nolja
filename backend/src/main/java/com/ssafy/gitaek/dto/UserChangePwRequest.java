package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class UserChangePwRequest {
    private String currentPassword; 
    private String newPassword;     
}