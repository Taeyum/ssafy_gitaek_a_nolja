package com.ssafy.gitaek.service;

import com.ssafy.gitaek.dto.PoiDto; // ★ PoiDto import 확인
import com.ssafy.gitaek.mapper.PoiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference; // ★ 리스트 변환용 import 추가

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Autowired
    private PoiMapper poiMapper;

    // 1. 관광지 설명 생성 (기존 유지)
    public String getPlaceDescription(String placeName, String address) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", String.format(
                    "여행 가이드로서 '%s' (%s)에 대한 3줄 요약 설명과 해시태그 3개를 작성해줘. 말투는 친근한 해요체. 텍스트만 줘.",
                    placeName, address));
            messages.add(msg);

            bodyMap.put("messages", messages);
            String jsonBody = mapper.writeValueAsString(bodyMap);

            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            System.out.println(">>> GMS 요청 시작 (HttpClient): " + model);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> responseMap = mapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            } else {
                System.err.println(">>> GMS 실패 (" + response.statusCode() + "): " + response.body());
                return "AI 연결 실패 (보안 정책 차단)";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "서버 에러 발생";
        }
    }

    // 2. 여행 코스 추천 기능 (RAG 방식: DB 데이터 주입)
    public String getAiItinerary(String destination, int totalDays, String style) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // [Step 1] DB에서 해당 지역의 장소 목록을 먼저 가져옴 (최대 80개)
            List<String> availablePlaces = poiMapper.findNamesByKeyword(destination);

            if (availablePlaces.isEmpty()) {
                System.out.println(">>> 해당 지역 DB 데이터 없음: " + destination);
                return null; // 데이터가 없으면 AI 요청도 안 함 (비용 절약)
            }

            // 목록을 콤마로 연결된 문자열로 변환 (예: "해운대, 광안리, 태종대...")
            String placeListString = String.join(", ", availablePlaces);

            // [Step 2] 프롬프트에 목록을 주입하고 "여기서만 골라"라고 지시
            String prompt = String.format(
                    "여행지: '%s', 기간: '%d일', 스타일: '%s'. " +
                            "아래 제공된 **[가능한 장소 목록]**에 있는 장소들 중에서만 선택하여 코스를 짜줘. " +
                            "목록에 없는 장소는 절대 포함하지 마. " +
                            "결과는 설명 멘트 없이 오직 JSON Array로만 줘. " +
                            "\n\n[가능한 장소 목록]: %s" +
                            "\n\nJSON 형식: [{\"day\": 1, \"title\": \"장소명(목록에있는이름그대로)\", \"memo\": \"한줄설명\"}, ...]",
                    destination, totalDays, style, placeListString
            );

            // ... (이하 HTTP 요청 부분은 기존과 완전히 동일) ...

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", prompt);
            messages.add(msg);

            bodyMap.put("messages", messages);
            bodyMap.put("temperature", 0.3); // 창의성 낮춤 (있는거에서 고르라고)

            String jsonBody = mapper.writeValueAsString(bodyMap);

            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(30)) // 데이터가 많아져서 처리 시간 늘림
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("User-Agent", "Mozilla/5.0 ...")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            System.out.println(">>> AI 코스 생성 요청 (RAG): " + destination);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> responseMap = mapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");

                content = content.replaceAll("```json", "").replaceAll("```", "").trim();

                // [Step 3] 결과 매핑 (이제 이름이 DB에 있는게 확실하므로 매칭률 매우 높음)
                List<Map<String, Object>> aiPlanList = mapper.readValue(content, new TypeReference<List<Map<String, Object>>>(){});
                List<Map<String, Object>> resultList = new ArrayList<>();

                for (Map<String, Object> plan : aiPlanList) {
                    String title = (String) plan.get("title");
                    PoiDto realPlace = poiMapper.searchPoiByTitle(title);

                    if (realPlace != null) {
                        plan.put("poiId", realPlace.getPoiId());
                        plan.put("lat", realPlace.getLatitude());
                        plan.put("lng", realPlace.getLongitude());
                        plan.put("address", realPlace.getAddress());
                        plan.put("title", realPlace.getName());
                        resultList.add(plan);
                    }
                }
                return mapper.writeValueAsString(resultList);

            } else {
                System.err.println(">>> GMS 실패: " + response.body());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}