package com.ssafy.gitaek.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class BoardDto {
    private int boardId;
    private int userId;
    private String title;
    private String content;
    private int hit;
    private String createdAt;
    
    // 작성자 닉네임 (조인해서 가져옴)
    private String writerName; 
    // 본인 글 여부 (프론트 표시용)
    private boolean isMine;
    
    
    
    // [추가]
    private int likeCount;   // 좋아요 총 개수
    
    @JsonProperty("isLiked")
    private boolean isLiked; // 현재 로그인한 사람이 좋아요 눌렀는지 여부
}