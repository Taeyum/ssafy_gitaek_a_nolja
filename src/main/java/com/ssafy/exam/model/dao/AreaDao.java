package com.ssafy.exam.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssafy.exam.util.DBUtil;

public class AreaDao {
    private static final AreaDao INSTANCE = new AreaDao();

    private AreaDao() {
    }

    public static AreaDao getInstance() {
        return INSTANCE;
    }

    public List<Map<String, Object>> selectSidos() {
        String sql = "SELECT sido_code, sido_name FROM sidos ORDER BY sido_code";
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("code", rs.getInt("sido_code"));
                row.put("name", rs.getString("sido_name"));
                result.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sidos", e);
        }
        return result;
    }

    public List<Map<String, Object>> selectGuguns(int sidoCode) {
        String sql = "SELECT gugun_code, gugun_name FROM guguns WHERE sido_code = ? ORDER BY gugun_name";
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sidoCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", rs.getInt("gugun_code"));
                    row.put("name", rs.getString("gugun_name"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load guguns", e);
        }
        return result;
    }
}
