package com.ssafy.gitaek.service;

import com.ssafy.gitaek.dto.PoiDto;
import com.ssafy.gitaek.dto.TourApiDto;
import com.ssafy.gitaek.mapper.PoiMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@Service
public class OpenApiService {

    private final PoiMapper poiMapper;

    @Value("${tour.api.key}")
    private String serviceKey;

    @Value("${tour.api.baseUrl}")
    private String apiUrl;

    public OpenApiService(PoiMapper poiMapper) {
        this.poiMapper = poiMapper;
    }

    @Transactional
    public String fetchAndSaveAllAttractions() {
        RestTemplate restTemplate = new RestTemplate();

        // 테스트를 위해 서울(1)만 먼저 돌려보세요
        int[] areaCodes = {1, 2, 3, 4, 5, 6, 7, 8, 31, 32, 33, 34, 35, 36, 37, 38, 39};
        int totalSaved = 0;

        for (int areaCode : areaCodes) {
            try {
                // 1. URI 생성 (인코딩 방지 모드)
                URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl + "/areaBasedList2")
                        .queryParam("serviceKey", serviceKey) // %가 포함된 인코딩 키를 넣음
                        .queryParam("numOfRows", 100)
                        .queryParam("pageNo", 1)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "GitaekApp")
                        .queryParam("_type", "json")
                        .queryParam("areaCode", areaCode)
                        .queryParam("contentTypeId", 12)
                        .build(true) // [핵심] true를 넣어서 "이미 인코딩 되었다"고 알림 -> 자바가 건드리지 않음
                        .toUri();

                System.out.println("요청 URL: " + uri);

                // 2. 요청 전송
                TourApiDto response = restTemplate.getForObject(uri, TourApiDto.class);

                // 3. 응답 처리
                if (response != null && response.getResponse() != null &&
                        response.getResponse().getBody() != null &&
                        response.getResponse().getBody().getItems() != null) {

                    List<TourApiDto.Item> items = response.getResponse().getBody().getItems().getItem();
                    if (items != null) {
                        for (TourApiDto.Item item : items) {
                            PoiDto poi = new PoiDto();
                            poi.setName(item.getTitle());
                            poi.setAddress(item.getAddr1());

                            poi.setLatitude(item.getMapy() != null ? BigDecimal.valueOf(item.getMapy()) : BigDecimal.ZERO);
                            poi.setLongitude(item.getMapx() != null ? BigDecimal.valueOf(item.getMapx()) : BigDecimal.ZERO);

                            String img = item.getFirstimage();
                            if (img == null || img.isEmpty()) img = "";
                            poi.setThumbnailUrl(img);

                            poi.setRegionId(areaCode);

                            poiMapper.insertPoi(poi);
                            totalSaved++;
                        }
                    }
                }
                System.out.println("지역코드 " + areaCode + " 저장 완료.");

            } catch (Exception e) {
                System.out.println("지역코드 " + areaCode + " 실패: " + e.getMessage());
                e.printStackTrace();
                continue;
            }
        }
        return "총 " + totalSaved + "개의 데이터 저장 완료!";
    }
}