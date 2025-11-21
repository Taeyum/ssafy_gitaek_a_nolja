package com.ssafy.exam.model.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.exam.model.dao.AreaDao;
import com.ssafy.exam.model.dao.AttractionDao;

@Service
public class LocalTripDataService {

    private final AreaDao areaDao;
    private final AttractionDao attractionDao;

    @Autowired
    public LocalTripDataService(AreaDao areaDao, AttractionDao attractionDao) {
        this.areaDao = areaDao;
        this.attractionDao = attractionDao;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> loadAreas(String areaCode) {
        if (areaCode == null || areaCode.isBlank()) {
            return areaDao.selectSidos();
        }
        try {
            int code = Integer.parseInt(areaCode);
            return areaDao.selectGuguns(code);
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> loadSigungus(String areaCode) {
        return loadAreas(areaCode);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchAttractions(String areaCode, String sigunguCode,
                                                       String contentTypeId, String pageNo, String numOfRows) {
        Integer area = parseInteger(areaCode);
        Integer sigungu = parseInteger(sigunguCode);
        Integer contentType = parseInteger(contentTypeId);

        int page = parseInteger(pageNo, 1);
        int rows = parseInteger(numOfRows, 20);
        if (page < 1) {
            page = 1;
        }
        if (rows <= 0) {
            rows = 20;
        }
        int offset = (page - 1) * rows;

        return attractionDao.search(area, sigungu, contentType, offset, rows);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInteger(String value, int defaultValue) {
        Integer parsed = parseInteger(value);
        return parsed != null ? parsed : defaultValue;
    }
}
