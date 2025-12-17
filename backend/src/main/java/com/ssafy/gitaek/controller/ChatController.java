package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.ChatMessageDto;
import com.ssafy.gitaek.jwt.CustomUserDetails; // ★ 필수 Import
import com.ssafy.gitaek.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 목록 조회
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getMessages(@PathVariable int tripId, 
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();

        // 토큰에 있는 내 ID(userId)를 넘겨줘서 '내가 쓴 글'인지 판별
        List<ChatMessageDto> list = chatService.getMessages(tripId, userDetails.getUser().getUserId());
        return ResponseEntity.ok(list);
    }

    // 메시지 전송
    @PostMapping("/{tripId}")
    public ResponseEntity<?> sendMessage(@PathVariable int tripId, 
                                         @RequestBody Map<String, String> body, 
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();

        String content = body.get("content");
        chatService.sendMessage(tripId, userDetails.getUser().getUserId(), content);
        return ResponseEntity.ok("전송 완료");
    }
}