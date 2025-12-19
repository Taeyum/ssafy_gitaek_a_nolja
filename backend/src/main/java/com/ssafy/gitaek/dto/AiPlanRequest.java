package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class AiPlanRequest {
    private String destination; // 예: 부산
    private int totalDays;      // 예: 3 (3일치) -> 숫자로 변경!
    private String style;       // 예: 맛집 탐방
}