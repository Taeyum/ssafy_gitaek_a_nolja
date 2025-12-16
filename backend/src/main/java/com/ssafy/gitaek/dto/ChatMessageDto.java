package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private Long messageId;
    private int tripId;       // 어느 여행방인지
    private int userId;       // 누가 보냈는지
    private String content;   // 내용
    private String sentAt;    // 보낸 시간 (String으로 변환해서 전송)

    // 보낸 사람 정보 (화면에 띄워야 함)
    private String senderName;
    private String senderProfileImg;
    private boolean isMine;   // 내가 보낸 건지 여부 (프론트 편의용)
}