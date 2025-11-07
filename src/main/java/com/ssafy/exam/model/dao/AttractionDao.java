package com.ssafy.exam.model.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssafy.exam.util.DBUtil;

public class AttractionDao {
    private static final AttractionDao INSTANCE = new AttractionDao();

    private AttractionDao() {
    }

    public static AttractionDao getInstance() {
        return INSTANCE;
    }

    public List<Map<String, Object>> search(Integer areaCode, Integer sigunguCode, Integer contentTypeId,
                                            int offset, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT content_id, title, addr1, addr2, tel, latitude, longitude, first_image1, first_image2, ");
        sql.append("area_code, si_gun_gu_code, content_type_id FROM attractions WHERE 1 = 1");

        List<Object> params = new ArrayList<>();
        if (areaCode != null) {
            sql.append(" AND area_code = ?");
            params.add(areaCode);
        }
        if (sigunguCode != null) {
            sql.append(" AND si_gun_gu_code = ?");
            params.add(sigunguCode);
        }
        if (contentTypeId != null) {
            sql.append(" AND content_type_id = ?");
            params.add(contentTypeId);
        }
        sql.append(" ORDER BY title");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("contentid", rs.getInt("content_id"));
                    row.put("title", rs.getString("title"));
                    row.put("addr1", rs.getString("addr1"));
                    row.put("addr2", rs.getString("addr2"));
                    row.put("tel", rs.getString("tel"));
                    BigDecimal lat = rs.getBigDecimal("latitude");
                    BigDecimal lon = rs.getBigDecimal("longitude");
                    row.put("mapy", lat != null ? lat.doubleValue() : null);
                    row.put("mapx", lon != null ? lon.doubleValue() : null);
                    row.put("firstimage", rs.getString("first_image1"));
                    row.put("firstimage2", rs.getString("first_image2"));
                    row.put("areaCode", rs.getObject("area_code"));
                    row.put("sigunguCode", rs.getObject("si_gun_gu_code"));
                    row.put("contentTypeId", rs.getObject("content_type_id"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search attractions", e);
        }
        return result;
    }
}
