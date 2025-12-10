package com.ssafy.gitaek.mapper;

import com.ssafy.gitaek.dto.PoiDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PoiMapper {
    void insertPoi(PoiDto poiDto);
    // [추가] 조회 메서드
    List<PoiDto> selectPois(int regionId);
}