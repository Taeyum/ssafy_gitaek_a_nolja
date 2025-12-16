package com.ssafy.gitaek.service;

import com.ssafy.gitaek.dto.ChatMessageDto;
import com.ssafy.gitaek.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMapper chatMapper;

    @Transactional
    public void sendMessage(int tripId, int userId, String content) {
        // 1. 채팅방 ID 찾기
        Integer roomId = chatMapper.findChatRoomIdByTripId(tripId);

        // 2. 없으면 만들기 (안전장치)
        if (roomId == null) {
            chatMapper.createChatRoom(tripId);
            roomId = chatMapper.findChatRoomIdByTripId(tripId);
        }

        // 3. 메시지 저장
        chatMapper.insertMessage(roomId, userId, content);
    }

    public List<ChatMessageDto> getMessages(int tripId, int currentUserId) {
        // 1. 채팅방 없으면 빈 리스트 반환 (혹은 생성)
        Integer roomId = chatMapper.findChatRoomIdByTripId(tripId);
        if (roomId == null) {
            chatMapper.createChatRoom(tripId); // 조회 시에도 없으면 생성
            return List.of();
        }

        List<ChatMessageDto> list = chatMapper.selectMessages(tripId);

        // 내가 보낸 메시지인지 표시 (프론트 디자인용)
        for (ChatMessageDto msg : list) {
            msg.setMine(msg.getUserId() == currentUserId);
        }
        return list;
    }
}