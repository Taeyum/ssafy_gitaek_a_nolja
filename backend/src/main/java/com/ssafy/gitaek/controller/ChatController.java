package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.ChatMessageDto;
import com.ssafy.gitaek.jwt.CustomUserDetails;
import com.ssafy.gitaek.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 1. [유지] 기존 목록 조회 (HTTP GET)
    // 채팅방 입장 시 과거 기록을 불러옵니다.
    @GetMapping("/api/chat/{tripId}")
    public ResponseEntity<?> getMessages(@PathVariable int tripId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();

        // 기존 로직 유지
        List<ChatMessageDto> list = chatService.getMessages(tripId, userDetails.getUser().getUserId());
        return ResponseEntity.ok(list);
    }

    // 2. [변경] 실시간 메시지 전송 (STOMP)
    // 클라이언트가 "/pub/chat/message"로 보내면 실행됨
    // (기존의 @PostMapping은 삭제하거나 이걸로 대체)
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDto message) {
        // 1. DB 저장 (기존 서비스 로직)
        chatService.sendMessage(message.getTripId(), message.getUserId(), message.getContent());

        // 2. ★ [여기가 핵심] 서버 시간이 뭐든 상관없이 무조건 '서울 시간'으로 구하기
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // 3. 구한 시간을 "HH:mm" 문자열로 바꿔서 DTO에 넣기
        message.setSentAt(nowKst.format(DateTimeFormatter.ofPattern("HH:mm")));

        // 4. 구독자들에게 전송
        messagingTemplate.convertAndSend("/sub/chat/" + message.getTripId(), message);
    }
}