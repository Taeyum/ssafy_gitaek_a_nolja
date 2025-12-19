package com.ssafy.gitaek.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper; // JSON 처리를 위한 Jackson 라이브러리

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

    public String getPlaceDescription(String placeName, String address) {
        try {
            // 1. JSON Body 생성 (ObjectMapper 사용으로 문법 오류 원천 차단)
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("model", model); // gpt-4o-mini

            // ★ 가장 호환성 좋은 'user' 역할 사용
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", String.format(
                    "여행 가이드로서 '%s' (%s)에 대한 3줄 요약 설명과 해시태그 3개를 작성해줘. 말투는 친근한 해요체. 텍스트만 줘.",
                    placeName, address));
            messages.add(msg);

            bodyMap.put("messages", messages);
            String jsonBody = mapper.writeValueAsString(bodyMap);

            // 2. 최신 HttpClient 생성 (Cloudflare 통과용)
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1) // 안정성을 위해 1.1 강제
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // 3. 요청 생성 (헤더 완벽 구비)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim()) // 공백 제거
                    // ★ 봇 탐지 회피용 헤더
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            System.out.println(">>> GMS 요청 시작 (HttpClient): " + model);

            // 4. 전송 및 응답 받기
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 5. 결과 확인
            if (response.statusCode() == 200) {
                // 성공 시 파싱
                Map<String, Object> responseMap = mapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            } else {
                // 실패 시 에러 로그 출력
                System.err.println(">>> GMS 실패 (" + response.statusCode() + "): " + response.body());
                return "AI 연결 실패 (보안 정책 차단)";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "서버 에러 발생";
        }
    }
}