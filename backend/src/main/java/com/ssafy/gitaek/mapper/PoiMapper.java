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
}