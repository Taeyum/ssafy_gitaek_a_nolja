package com.ssafy.gitaek.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PoiDto {
    private int poiId;
    private String name;
    private String description;
    private String address;
    private BigDecimal latitude;  // 위도
    private BigDecimal longitude; // 경도
    private String thumbnailUrl;
    private int regionId;         // 외래키
}