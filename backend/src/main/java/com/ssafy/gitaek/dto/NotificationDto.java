package com.ssafy.gitaek.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String type;      // "EDIT", "ENTRY", "INFO" 등 알림 종류
    private String message;   // "OO님이 해운대를 추가했습니다."
    private String senderName; // 알림을 발생시킨 사람
}