package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.PoiDto;
import com.ssafy.gitaek.mapper.PoiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    @Autowired
    private PoiMapper poiMapper;

    // 전체 조회 또는 지역별 조회
    // 요청 예시: /api/attractions?areaCode=1 (서울만 조회)
    // 요청 예시: /api/attractions (전체 조회)
    @GetMapping
    public ResponseEntity<List<PoiDto>> getAttractions(
            @RequestParam(value = "areaCode", defaultValue = "0") int areaCode) {

        List<PoiDto> list = poiMapper.selectPois(areaCode);
        return ResponseEntity.ok(list);
    }
}