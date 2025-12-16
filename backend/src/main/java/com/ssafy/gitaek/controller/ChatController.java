package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.ChatMessageDto;
import com.ssafy.gitaek.model.User;
import com.ssafy.gitaek.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> getMessages(@PathVariable int tripId, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).build();

        List<ChatMessageDto> list = chatService.getMessages(tripId, user.getUserId());
        return ResponseEntity.ok(list);
    }

    // 메시지 전송
    @PostMapping("/{tripId}")
    public ResponseEntity<?> sendMessage(@PathVariable int tripId, @RequestBody Map<String, String> body, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).build();

        String content = body.get("content");
        chatService.sendMessage(tripId, user.getUserId(), content);
        return ResponseEntity.ok("전송 완료");
    }
}