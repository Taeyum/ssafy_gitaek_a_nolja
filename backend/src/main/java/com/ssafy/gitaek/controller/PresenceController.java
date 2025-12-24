package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.UserLoginRequest; // 간단한 유저 정보 담을 클래스로 활용 (혹은 Map)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class PresenceController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 실시간 접속자 저장소 (SessionID : 유저정보)
    // 동시성 문제를 위해 ConcurrentHashMap 사용
    private static final Map<String, Map<String, String>> onlineUsers = new ConcurrentHashMap<>();

    // 1. 유저 입장 (프론트에서 /pub/board/join 으로 보냄)
    @MessageMapping("/board/join")
    public void joinBoard(@Payload Map<String, String> userInfo, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        
        // 세션에 유저 정보 저장 (나중에 나갈 때 확인용)
        headerAccessor.getSessionAttributes().put("userInfo", userInfo);
        
        // 목록에 추가
        onlineUsers.put(sessionId, userInfo);

        // 전체에게 현재 접속자 명단 브로드캐스트
        broadcastOnlineUsers();
    }

    // 2. 유저 퇴장 (연결 끊김 감지)
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        
        if (onlineUsers.containsKey(sessionId)) {
            onlineUsers.remove(sessionId);
            broadcastOnlineUsers(); // 갱신된 목록 전송
        }
    }

    // 접속자 명단 전체 뿌리기
    private void broadcastOnlineUsers() {
        // Map의 value들만 모아서 리스트로 변환
        messagingTemplate.convertAndSend("/sub/board/online", new ArrayList<>(onlineUsers.values()));
    }
}