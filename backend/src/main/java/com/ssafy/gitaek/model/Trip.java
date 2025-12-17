package com.ssafy.gitaek.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Trip {
    private int tripId;
    private String title;
    private String style;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int ownerId;
    private int maxParticipants;
    private int regionId;
    
    // DB 조회용 (테이블엔 없지만 Mapper가 채워줄 필드)
    private int currentParticipants; 
    
    // ★ 갈치 기능 (동시 편집 Lock & 초대 코드) 확인 완료!
    private Integer currentEditorId; 
    private String inviteCode; 
}