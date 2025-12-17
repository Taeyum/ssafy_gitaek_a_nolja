package com.ssafy.gitaek.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TripCreateRequest {
    private String title;
    private int duration;
    private int maxParticipants;
    private String style;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    // ★ [추가] 프론트엔드에서 계산해서 보낸 종료일을 받기 위해 필드 추가!
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate; 
}