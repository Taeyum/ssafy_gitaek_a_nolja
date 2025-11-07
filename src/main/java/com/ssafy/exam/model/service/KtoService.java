package com.ssafy.exam.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KtoService {
    // 한국관광공사 KorService2 기본 경로
    private static final String BASE = "https://apis.data.go.kr/B551011/KorService2";

    /** ★ 여기에 발급받은 서비스 키 넣으세요.
     *  - 가능하면 '디코드된(=그냥 평문)' 키를 넣는 걸 권장합니다.
     *  - 만약 이미 % 기호가 잔뜩 들어간 '인코드된' 키만 있다면 SERVICE_KEY_IS_ENCODED = true 로 바꾸세요.
     */
    private static final String SERVICE_KEY = "cQnZa9/ukSdD9DCmFAWPA7UckJbTzKXW7v1enO60rbPA25la+RpDesdEbU+rDzuFNIOI68ofM4lGrrUvSnuNmA==";
    private static final boolean SERVICE_KEY_IS_ENCODED = false; // 인코드된 키면 true

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    // 하드코딩 키를 쓰므로 생성자 불필요(원하면 오버로드 추가 가능)
    public KtoService() {}

    private void appendParam(StringBuilder qs, String key, String value) {
        if (value == null) return;
        if (qs.length() > 0) qs.append('&');

        // serviceKey만 예외 처리(이미 인코드된 키면 그대로 붙이고, 아니면 인코딩)
        if ("serviceKey".equals(key)) {
            qs.append(key).append('=')
              .append(SERVICE_KEY_IS_ENCODED ? value : URLEncoder.encode(value, StandardCharsets.UTF_8));
        } else {
            qs.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
              .append('=')
              .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
    }

    private JsonNode call(String path, Map<String, String> q) throws Exception {
        StringBuilder qs = new StringBuilder();
        appendParam(qs, "serviceKey", SERVICE_KEY);      // ★ 키 자동 포함
        appendParam(qs, "MobileOS", "ETC");
        appendParam(qs, "MobileApp", "EnjoyTrip");
        appendParam(qs, "_type", "json");
        if (q != null) {
            for (var e : q.entrySet()) appendParam(qs, e.getKey(), e.getValue());
        }

        URI uri = URI.create(BASE + "/" + path + "?" + qs);
        HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new RuntimeException("HTTP " + res.statusCode());

        JsonNode root = om.readTree(res.body());
        JsonNode header = root.path("response").path("header");
        if (header.has("resultCode") && !"0000".equals(header.get("resultCode").asText())) {
            throw new RuntimeException("KTO API error: " + header.path("resultMsg").asText(""));
        }
        return root;
    }

    /** 시도/시군구 코드 */
    public JsonNode areaCode(String areaCode) throws Exception {
        Map<String, String> q = new LinkedHashMap<>();
        q.put("numOfRows", "120");
        q.put("pageNo", "1");
        if (areaCode != null && !areaCode.isBlank()) q.put("areaCode", areaCode);
        return call("areaCode2", q);
    }

    /** 지역 기반 목록 (관광지/숙박/음식점/문화시설/공연/코스/쇼핑 등) */
    public JsonNode areaBasedList(String areaCode, String sigunguCode, String contentTypeId,
                                  String pageNo, String numOfRows) throws Exception {
        Map<String, String> q = new LinkedHashMap<>();
        q.put("numOfRows", (numOfRows == null || numOfRows.isBlank()) ? "20" : numOfRows);
        q.put("pageNo", (pageNo == null || pageNo.isBlank()) ? "1" : pageNo);
        if (areaCode != null && !areaCode.isBlank()) q.put("areaCode", areaCode);
        if (sigunguCode != null && !sigunguCode.isBlank()) q.put("sigunguCode", sigunguCode);
        if (contentTypeId != null && !contentTypeId.isBlank()) q.put("contentTypeId", contentTypeId);
        return call("areaBasedList2", q);
    }
}
