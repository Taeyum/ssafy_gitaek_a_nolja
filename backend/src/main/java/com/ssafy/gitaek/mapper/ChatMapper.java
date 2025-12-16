package com.ssafy.gitaek.mapper;

import com.ssafy.gitaek.dto.ChatMessageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatMapper {
    // 1. 채팅방 존재 여부 확인
    Integer findChatRoomIdByTripId(int tripId);

    // 2. 채팅방 생성
    void createChatRoom(int tripId);

    // 3. 메시지 저장
    void insertMessage(@Param("roomId") int roomId, @Param("userId") int userId, @Param("content") String content);

    // 4. 메시지 목록 조회
    List<ChatMessageDto> selectMessages(int tripId);
}