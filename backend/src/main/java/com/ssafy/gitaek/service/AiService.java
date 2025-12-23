package com.ssafy.gitaek.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.gitaek.dto.PoiDto;
import com.ssafy.gitaek.mapper.PoiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

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

    private final ObjectMapper mapper = new ObjectMapper();

    // 지역 코드 매핑
    private static final Map<String, Integer> AREA_CODES = new HashMap<>();
    static {
        AREA_CODES.put("서울", 1); AREA_CODES.put("인천", 2); AREA_CODES.put("대전", 3);
        AREA_CODES.put("대구", 4); AREA_CODES.put("광주", 5); AREA_CODES.put("부산", 6);
        AREA_CODES.put("울산", 7); AREA_CODES.put("세종", 8);
        AREA_CODES.put("경기", 31); AREA_CODES.put("강원", 32); AREA_CODES.put("충북", 33);
        AREA_CODES.put("충남", 34); AREA_CODES.put("경북", 35); AREA_CODES.put("경남", 36);
        AREA_CODES.put("전북", 37); AREA_CODES.put("전남", 38); AREA_CODES.put("제주", 39);
        AREA_CODES.put("강릉", 32); AREA_CODES.put("속초", 32); AREA_CODES.put("여수", 38);
        AREA_CODES.put("전주", 37); AREA_CODES.put("경주", 35);
    }

    // ==================================================================================
    // 1. AI 여행 계획 생성 (유형별 시간 고정 + 부족한 항목 강제 주입)
    // ==================================================================================
    public String getAiItinerary(String destination, int totalDays, String style) {
        try {
            int areaCode = getAreaCode(destination);
            int searchAreaCode = (areaCode != 0) ? areaCode : 1; // 기본값 서울

            // 1. 해당 지역 데이터 미리 로드
            List<PoiDto> regionPois = poiMapper.selectPois(searchAreaCode);

            // 프롬프트: 하루 관광지 3곳, 식사 2곳, 숙소 1곳 명시
            String prompt = String.format(
                    "Create a detailed %d-day trip plan for %s (South Korea). Style: %s.\n" +
                            "CRITICAL RULES:\n" +
                            "1. Output MUST be a single JSON Array of objects.\n" +
                            "2. Structure: [{\"day\":1, \"title\":\"ExactName\", \"type\":\"visit/meal/stay\", \"memo\":\"desc\"}, ...]\n" +
                            "3. QUANTITY REQUIREMENTS (Per Day):\n" +
                            "   - At least 3 'visit' (Attractions)\n" +
                            "   - At least 2 'meal' (Lunch, Dinner)\n" +
                            "   - EXACTLY 1 'stay' (Accommodation) for Day 1 to Day %d (Last day excludes hotel).\n" +
                            "4. NAMES: Use EXACT Korean official names from KakaoMap (No English, No explanations in title).\n" +
                            "5. Ensure Day 1 hotel is near Day 2 start point.",
                    totalDays, destination, style, (totalDays - 1)
            );

            String content = callOpenAi(prompt);
            List<Map<String, Object>> validList = new ArrayList<>();

            if (content != null) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
                List<Map<String, Object>> aiRawList = mapper.readValue(content, new TypeReference<List<Map<String, Object>>>(){});

                // 3. DB 매칭
                for (Map<String, Object> plan : aiRawList) {
                    String title = (String) plan.get("title");
                    PoiDto matched = findBestMatch(regionPois, title);
                    if (matched != null) {
                        addPlaceToList(validList, plan, matched);
                    }
                }
            }

            // 4. 하루 일정 재조립 (부족한 거 채우고 -> 시간 순서 강제 정렬)
            List<Map<String, Object>> finalItinerary = new ArrayList<>();
            double[] prevCoords = null;

            for (int day = 1; day <= totalDays; day++) {
                int currentDay = day;
                List<Map<String, Object>> dayItems = validList.stream()
                        .filter(item -> (int)item.get("day") == currentDay)
                        .collect(Collectors.toList());

                // ★ [수정] arrangeDaySchedule에 totalDays 전달하여 숙소 필터링 제어
                List<Map<String, Object>> sortedDay = arrangeDaySchedule(dayItems, regionPois, currentDay, prevCoords, totalDays);

                finalItinerary.addAll(sortedDay);

                if (!sortedDay.isEmpty()) {
                    // 마지막 장소가 숙소일 확률이 높음 -> 다음날의 시작점(prevCoords)으로 설정
                    Map<String, Object> last = sortedDay.get(sortedDay.size() - 1);
                    prevCoords = new double[]{(double)last.get("lat"), (double)last.get("lng")};
                }
            }

            return mapper.writeValueAsString(finalItinerary);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================================================================================
    // 2. 하루 일정 조립기 (슬롯 기반 스케줄링)
    // ==================================================================================

    private List<Map<String, Object>> arrangeDaySchedule(
            List<Map<String, Object>> dayItems,
            List<PoiDto> regionPois,
            int day,
            double[] prevCoords,
            int totalDays) { // ★ totalDays 추가됨

        List<Map<String, Object>> result = new ArrayList<>();

        // 1. 유형별 분류
        List<Map<String, Object>> meals = new ArrayList<>();
        List<Map<String, Object>> stays = new ArrayList<>();
        List<Map<String, Object>> visits = new ArrayList<>();

        for (Map<String, Object> item : dayItems) {
            String type = (String) item.get("type");
            if ("meal".equals(type) || type.contains("음식")) meals.add(item);
            else if ("stay".equals(type) || type.contains("숙소")) stays.add(item);
            else visits.add(item);
        }

        // 2. [부족분 강제 채우기]
        // ★ 식사 2끼, 관광지 3곳 이상, 숙소 1곳(마지막날 제외) 필수 보장
        fillMissingType(meals, regionPois, 39, 2, day, "meal", "맛집");

        if (day < totalDays) { // 마지막 날이 아니면 숙소 필수
            fillMissingType(stays, regionPois, 32, 1, day, "stay", "추천 숙소");
        }

        fillMissingType(visits, regionPois, 12, 3, day, "visit", "관광 명소");


        // =========================================================
        // 3. 시작 위치 결정 로직 (PrevCoords 활용)
        // =========================================================
        double currentLat, currentLng;

        if (day > 1 && prevCoords != null) {
            // [2일차 이후]: 어제 숙소(prevCoords) 위치가 오늘의 시작 기준점
            currentLat = prevCoords[0];
            currentLng = prevCoords[1];
        } else {
            // [1일차]: '역', '터미널', '공항'이 있으면 거기가 시작점
            Map<String, Object> startNode = visits.stream()
                    .filter(v -> {
                        String t = (String)v.get("title");
                        return t.matches(".*(역|터미널|공항).*");
                    })
                    .findFirst().orElse(null);

            if (startNode != null) {
                currentLat = (double) startNode.get("lat");
                currentLng = (double) startNode.get("lng");
            } else if (!visits.isEmpty()) {
                currentLat = (double) visits.get(0).get("lat");
                currentLng = (double) visits.get(0).get("lng");
            } else {
                currentLat = 37.5665; // 기본값 (서울 등)
                currentLng = 126.9780;
            }
        }

        // 4. 슬롯 배치 시작 (Greedy: 현재 위치에서 가장 가까운 곳 찾기)

        // (1) 오전 관광 (10:00)
        // -> 점심(12:00)과 겹치지 않게 1곳만 배치하거나 시간을 조절
        int time = 10;
        int morningSlots = 1; // 10시 한 타임만 배정 (12시는 밥 먹어야 함)

        for (int i = 0; i < morningSlots; i++) {
            if (visits.isEmpty()) break;

            // 현재 위치(숙소 등)에서 가장 가까운 관광지 선택
            Map<String, Object> v = findNearestAndRemove(currentLat, currentLng, visits);

            if (v != null) {
                v.put("time", String.format("%02d:00", time));
                result.add(v);

                // 이동했으니 현재 위치 갱신
                currentLat = (double) v.get("lat");
                currentLng = (double) v.get("lng");
                time += 2; // 12시가 됨
            }
        }

        // (2) 점심 식사 (12:00)
        Map<String, Object> lunch = findNearestAndRemove(currentLat, currentLng, meals);
        if (lunch != null) {
            lunch.put("time", "12:00");
            result.add(lunch);
            currentLat = (double) lunch.get("lat");
            currentLng = (double) lunch.get("lng");
        }

        time = 14; // 오후 시작

        // (3) 숙소 미리 확보 (동선의 마지막 종착지용 - 리스트에서만 빼둠)
        Map<String, Object> accommodation = findNearestAndRemove(currentLat, currentLng, stays);

        // (4) 오후 관광 (14:00 ~ 18:00)
        while (!visits.isEmpty()) {
            if (time >= 18) break; // 저녁 시간 전까지만

            Map<String, Object> v = findNearestAndRemove(currentLat, currentLng, visits);
            if (v != null) {
                v.put("time", String.format("%02d:00", time));
                result.add(v);
                currentLat = (double) v.get("lat");
                currentLng = (double) v.get("lng");
                time += 2;
            } else break;
        }

        // (5) 저녁 식사 (18:00)
        Map<String, Object> dinner = findNearestAndRemove(currentLat, currentLng, meals);
        if (dinner != null) {
            dinner.put("time", "18:00");
            result.add(dinner);
            currentLat = (double) dinner.get("lat");
            currentLng = (double) dinner.get("lng");
        }

        // (6) 야간 관광 (남은 관광지가 있다면 19:30에 하나 더)
        if (!visits.isEmpty()) {
            Map<String, Object> nightVisit = findNearestAndRemove(currentLat, currentLng, visits);
            if (nightVisit != null) {
                nightVisit.put("time", "19:30");
                result.add(nightVisit);
                currentLat = (double) nightVisit.get("lat");
                currentLng = (double) nightVisit.get("lng");
            }
        }

        // (7) 숙소 배치 (20:00 또는 마지막 일정 후)
        if (accommodation != null) {
            accommodation.put("time", "21:00"); // 넉넉하게 21시
            result.add(accommodation);
            // ★ 오늘의 마지막 위치 갱신 (다음날 시작점)
            currentLat = (double) accommodation.get("lat");
            currentLng = (double) accommodation.get("lng");
        }

        return result;
    }

    // ==================================================================================
    // 3. 유틸리티 메서드
    // ==================================================================================

    // AiService.java의 addPlaceToList 메서드 보강
    private void addPlaceToList(List<Map<String, Object>> list, Map<String, Object> item, PoiDto place) {
        // 중복 체크
        boolean exists = list.stream().anyMatch(i -> (int)i.get("poiId") == place.getPoiId());
        if (exists) return;

        item.put("poiId", place.getPoiId());
        item.put("lat", place.getLatitude().doubleValue());
        item.put("lng", place.getLongitude().doubleValue());
        item.put("address", place.getAddress());
        item.put("title", place.getName());

        // ★ [추가] DB에서 가져온 진짜 타입 정보로 덮어쓰기 (AI가 틀렸을 수도 있으므로)
        // 숙소(32)면 stay, 음식점(39)이면 meal, 나머지는 visit
        if (place.getContentTypeId() == 32) item.put("type", "stay");
        else if (place.getContentTypeId() == 39) item.put("type", "meal");

        list.add(item);
    }

    private Map<String, Object> findNearestAndRemove(double lat, double lng, List<Map<String, Object>> candidates) {
        if (candidates.isEmpty()) return null;

        Map<String, Object> nearest = null;
        double minDist = Double.MAX_VALUE;
        int bestIdx = -1;

        for (int i = 0; i < candidates.size(); i++) {
            Map<String, Object> item = candidates.get(i);
            double pLat = (double) item.get("lat");
            double pLng = (double) item.get("lng");
            double dist = Math.pow(lat - pLat, 2) + Math.pow(lng - pLng, 2);

            if (dist < minDist) {
                minDist = dist;
                nearest = item;
                bestIdx = i;
            }
        }

        if (bestIdx != -1) {
            candidates.remove(bestIdx);
        }
        return nearest;
    }

    private void fillMissingType(List<Map<String, Object>> targetList, List<PoiDto> regionPois,
                                 int contentTypeId, int requiredCount, int day, String typeStr, String memo) {
        if (targetList.size() >= requiredCount) return;

        List<PoiDto> candidates = regionPois.stream()
                .filter(p -> p.getContentTypeId() == contentTypeId)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) return;
        Collections.shuffle(candidates);

        int needed = requiredCount - targetList.size();
        for (int i = 0; i < needed; i++) {
            if (candidates.isEmpty()) break;
            PoiDto p = candidates.remove(0);

            Map<String, Object> item = new HashMap<>();
            item.put("day", day);
            item.put("type", typeStr);
            item.put("title", p.getName());
            item.put("memo", memo + " (AI 추천)");

            addPlaceToList(targetList, item, p);
        }
    }

    private PoiDto findBestMatch(List<PoiDto> regionPois, String title) {
        if (title == null || title.isEmpty()) return null;

        String cleanTitle = title.replaceAll(" ", "");

        for (PoiDto p : regionPois) {
            if (p.getName().equals(title) || p.getName().replaceAll(" ", "").equals(cleanTitle)) {
                return p;
            }
        }

        for (PoiDto p : regionPois) {
            if (p.getName().contains(title) || title.contains(p.getName())) {
                return p;
            }
        }

        return null;
    }

    private int getAreaCode(String destination) {
        for (Map.Entry<String, Integer> entry : AREA_CODES.entrySet()) {
            if (destination.contains(entry.getKey())) return entry.getValue();
        }
        return 0;
    }

    public String getPlaceDescription(String placeName, String address, int contentTypeId) {
        try {
            String prompt = "Describe " + placeName + " in Korean (max 3 sentences).";
            String content = callOpenAi(prompt);
            return (content != null) ? content.trim() : "설명 없음";
        } catch (Exception e) { return "오류"; }
    }

    private String callOpenAi(String prompt) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a helpful travel assistant. Output JSON only."),
                    Map.of("role", "user", "content", prompt)
            ));
            body.put("temperature", 0.7);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                Map<String, Object> map = mapper.readValue(res.body(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) map.get("choices");
                Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                return (String) msg.get("content");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}