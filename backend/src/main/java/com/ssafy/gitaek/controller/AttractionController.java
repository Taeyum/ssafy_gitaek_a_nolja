package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.AiPlanRequest;
import com.ssafy.gitaek.dto.PoiDto;
import com.ssafy.gitaek.mapper.PoiMapper;
import com.ssafy.gitaek.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    @Autowired
    private PoiMapper poiMapper;

    @Autowired
    private AiService aiService;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.mobility.url}")
    private String kakaoMobilityUrl;


    // 전체 조회 또는 지역별 조회
    // 요청 예시: /api/attractions?areaCode=1 (서울만 조회)
    // 요청 예시: /api/attractions (전체 조회)
    @GetMapping
    public ResponseEntity<List<PoiDto>> getAttractions(
            @RequestParam(value = "areaCode", defaultValue = "0") int areaCode) {

        List<PoiDto> list = poiMapper.selectPois(areaCode);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{poiId}/description")
    public ResponseEntity<String> getPoiDescription(@PathVariable int poiId) {
        // 1. DB 조회
        PoiDto poi = poiMapper.selectPoiById(poiId);
        if (poi == null) return ResponseEntity.notFound().build();

        String currentDesc = poi.getDescription();

        // 2. 캐싱 확인 로직 수정
        // 내용이 있고(null 아님) AND "실패/오류" 같은 단어가 없을 때만 DB 내용 반환
        if (currentDesc != null && !currentDesc.isEmpty()
                && !currentDesc.contains("실패")
                && !currentDesc.contains("오류")
                && !currentDesc.contains("html")) { // 아까 그 html 에러 태그 방지
            return ResponseEntity.ok(currentDesc);
        }

        // 3. 내용이 없거나, 에러 메시지가 저장되어 있다면 -> AI 다시 호출
        System.out.println(">>> AI 설명 재생성 요청: " + poi.getName());
        String aiDesc = aiService.getPlaceDescription(poi.getName(), poi.getAddress(),poi.getContentTypeId());

        // 4. 제대로 된 응답일 때만 DB 업데이트
        if (!aiDesc.contains("실패") && !aiDesc.contains("오류")) {
            poiMapper.updatePoiDescription(poiId, aiDesc);
        }

        return ResponseEntity.ok(aiDesc);
    }

    @GetMapping("/bounds")
    public ResponseEntity<?> getAttractionsByBounds(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLng,
            @RequestParam double maxLng,
            @RequestParam(defaultValue = "12") int contentTypeId // 기본값: 관광지(12)
    ) {
        List<PoiDto> list = poiMapper.getAttractionsByBounds(minLat, maxLat, minLng, maxLng, contentTypeId);
        return ResponseEntity.ok(list);
    }

    // ★ AI 코스 생성 요청 (POST /api/attractions/ai-plan)
    @PostMapping("/ai-plan")
    public ResponseEntity<?> getAiPlan(@RequestBody AiPlanRequest request) {
        String jsonResult = aiService.getAiItinerary(
                request.getDestination(),
                request.getTotalDays(),
                request.getStyle()
        );

        if (jsonResult == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("AI 코스 생성 실패");
        }

        // JSON String을 그대로 반환 (프론트에서 JSON.parse 할 것임)
        return ResponseEntity.ok(jsonResult);
    }

    /**
     * 카카오 모빌리티 길찾기 API 중계
     */
    @GetMapping("/path")
    public ResponseEntity<?> getPath(@RequestParam String start, @RequestParam String end) {

        // 1. 프로퍼티에서 가져온 URL 사용
        // (가독성을 위해 String.format 사용 추천)
        String url = String.format("%s?origin=%s&destination=%s&priority=RECOMMEND",
                kakaoMobilityUrl, start, end);

        // 2. 프로퍼티에서 가져온 API 키 사용
        HttpHeaders headers = new HttpHeaders();
        // ★ "KakaoAK " (공백 포함) 뒤에 변수를 붙임
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 3. 요청 전송
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return ResponseEntity.ok(response.getBody());
    }
}