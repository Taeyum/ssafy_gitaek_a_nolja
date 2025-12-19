package com.ssafy.gitaek.mapper;

import com.ssafy.gitaek.dto.PoiDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PoiMapper {
    void insertPoi(PoiDto poiDto);
    // [추가] 조회 메서드
    List<PoiDto> selectPois(int regionId);

    // 단일 조회
    PoiDto selectPoiById(int poiId);
    // 설명 업데이트
    void updatePoiDescription(@Param("poiId") int poiId, @Param("description") String description);

    // 이름으로 POI 검색 (가장 비슷한 1개)
    PoiDto searchPoiByTitle(@Param("keyword") String keyword);

    // 특정 지역(키워드)의 관광지 이름 목록 조회 (AI 제공용)
    List<String> findNamesByKeyword(@Param("keyword") String keyword);
}