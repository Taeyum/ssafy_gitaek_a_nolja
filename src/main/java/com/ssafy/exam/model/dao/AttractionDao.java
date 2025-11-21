package com.ssafy.exam.model.dao;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface AttractionDao {
    List<Map<String, Object>> search(@Param("areaCode") Integer areaCode,
                                     @Param("sigunguCode") Integer sigunguCode,
                                     @Param("contentTypeId") Integer contentTypeId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);
}
