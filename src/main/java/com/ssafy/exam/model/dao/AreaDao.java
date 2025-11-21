package com.ssafy.exam.model.dao;

import java.util.List;
import java.util.Map;

public interface AreaDao {
    List<Map<String, Object>> selectSidos();
    List<Map<String, Object>> selectGuguns(int sidoCode);
}
