package com.ssafy.gitaek.dto;

import lombok.Data;

@Data
public class TripScheduleDto {
    private Long scheduleId;
    private int tripId;
    private int poiId;
    private int tripDay;
    private int visitOrder;
    private String memo;
    private String scheduleTime;
    // ★ [추가] 조회할 때 필요한 관광지 정보 (DB에서 조인해서 가져올 예정)
    private String placeName;
    private String placeAddress;
    private Double placeLat;
    private Double placeLng;
    // ★ [이 부분이 빠져 있어서 사진을 못 가져왔습니다! 추가해주세요]
    private String thumbnailUrl;
}

