package com.ssafy.gitaek.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ReviewDto {
    private int reviewId;
    private int userId;
    private String title;
    private String content;
    private String thumbnail; // 사진 URL
    private int hit;
    private int likeCount;
    private String createdAt;
    
    // 작성자 정보 (조인해서 가져옴)
    private String writerName; 
    
    // 프론트 표시용
    private boolean isMine;
    
    @JsonProperty("isLiked")
    private boolean isLiked; // 내가 좋아요 눌렀는지
    
    // 기존 필드 아래에 추가
    private int tripId;      // 연결된 여행 ID
    private String tripTitle; // 연결된 여행 제목 (화면 표시용)
}