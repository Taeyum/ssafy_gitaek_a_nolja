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

        // 1. 전국 지역 코드
        int[] areaCodes = {1, 2, 3, 4, 5, 6, 7, 8, 31, 32, 33, 34, 35, 36, 37, 38, 39};

        // 2. 관광 타입 (12:관광지, 14:문화시설, 32:숙박, 39:음식점)
        int[] contentTypes = {12, 14, 32, 39};

        int totalSaved = 0;
        int numOfRows = 1000; // 한 번에 가져올 개수

        System.out.println(">>> [ADMIN] 전국 데이터 수집 시작 (기존 설정 URL 사용)...");

        for (int areaCode : areaCodes) {
            for (int contentTypeId : contentTypes) {
                int pageNo = 1;

                // ★ 페이지 무한 반복 (데이터 끝날 때까지)
                while (true) {
                    try {
                        // ★ [수정됨] 하드코딩 제거 -> 선생님의 apiUrl 설정 그대로 사용
                        // areaBasedList1 이 보통 좌표정보가 정확해서 추천하지만,
                        // 만약 에러나면 areaBasedList2 로 바꾸셔도 됩니다. (기존 코드는 2였음)
                        URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl + "/areaBasedList2")
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("numOfRows", numOfRows)
                                .queryParam("pageNo", pageNo)
                                .queryParam("MobileOS", "ETC")
                                .queryParam("MobileApp", "GitaekApp")
                                .queryParam("_type", "json")
                                .queryParam("areaCode", areaCode)
                                .queryParam("contentTypeId", contentTypeId)
                                .build(true) // ★ [중요] 선생님 코드대로 true 유지 (인코딩된 키 보호)
                                .toUri();

                        TourApiDto response = restTemplate.getForObject(uri, TourApiDto.class);

                        if (response != null && response.getResponse() != null &&
                                response.getResponse().getBody() != null &&
                                response.getResponse().getBody().getItems() != null) {

                            List<TourApiDto.Item> items = response.getResponse().getBody().getItems().getItem();

                            // 데이터가 없으면 종료
                            if (items == null || items.isEmpty()) {
                                break;
                            }

                            for (TourApiDto.Item item : items) {
                                try {
                                    PoiDto poi = new PoiDto();
                                    poi.setName(item.getTitle());
                                    poi.setAddress(item.getAddr1());

                                    // 좌표 처리
                                    if (item.getMapy() != null && item.getMapx() != null) {
                                        poi.setLatitude(BigDecimal.valueOf(item.getMapy()));
                                        poi.setLongitude(BigDecimal.valueOf(item.getMapx()));
                                    } else {
                                        poi.setLatitude(BigDecimal.ZERO);
                                        poi.setLongitude(BigDecimal.ZERO);
                                    }

                                    String img = item.getFirstimage();
                                    poi.setThumbnailUrl((img != null && !img.isEmpty()) ? img : "");

                                    // 타입/지역 저장
                                    poi.setContentTypeId(contentTypeId); // 필요시 주석 해제
                                    poi.setRegionId(areaCode);

                                    poiMapper.insertPoi(poi);
                                    totalSaved++;

                                } catch (Exception e) {
                                    // 중복 등 개별 실패는 무시하고 계속 진행
                                }
                            }

                            System.out.println("✅ 저장중.. Area:" + areaCode + " Type:" + contentTypeId + " Page:" + pageNo + " (누적: " + totalSaved + ")");

                            // 가져온 개수가 요청 개수보다 적으면 마지막 페이지임
                            if (items.size() < numOfRows) {
                                break;
                            }
                            pageNo++;

                        } else {
                            // 응답이 비정상일 때
                            break;
                        }

                    } catch (Exception e) {
                        System.out.println("❌ 요청 실패 (Area:" + areaCode + "): " + e.getMessage());
                        // 에러가 나도 멈추지 않고 다음 루프로 넘어감 (break로 해당 타입만 중단)
                        break;
                    }
                } // end while
            } // end for contentType
        } // end for areaCode

        return "🎉 데이터 수집 완료! 총 " + totalSaved + "개의 장소를 저장했습니다.";
    }
}