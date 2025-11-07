package com.ssafy.exam.model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssafy.exam.util.DBUtil;

import com.ssafy.exam.model.dto.TripPlan;

public class TripPlanDaoImpl implements TripPlanDao {
    private static final TripPlanDaoImpl INSTANCE = new TripPlanDaoImpl();

    private TripPlanDaoImpl() {
    }

    public static TripPlanDaoImpl getInstance() {
        return INSTANCE;
    }
    @Override
    public void ensureTables() {
        try (Connection conn = DBUtil.getConnection()) {
            String schema = resolveSchema(conn);
            if (!tableExists(conn, schema, "trip_plan")) {
                createTripPlanTable(conn);
            }
            if (!tableExists(conn, schema, "trip_plan_item")) {
                createTripPlanItemTable(conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure trip plan tables", e);
        }
    }

    private boolean tableExists(Connection conn, String schema, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void createTripPlanTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE trip_plan (" +
                "plan_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(200) NOT NULL, " +
                "start_date DATE NULL, " +
                "end_date DATE NULL, " +
                "budget VARCHAR(50), " +
                "memo TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private void createTripPlanItemTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE trip_plan_item (" +
                "plan_item_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "plan_id INT NOT NULL, " +
                "order_index INT NOT NULL, " +
                "place_id VARCHAR(50), " +
                "place_title VARCHAR(200), " +
                "address VARCHAR(200), " +
                "visit_day DATE, " +
                "start_time VARCHAR(10), " +
                "end_time VARCHAR(10), " +
                "memo TEXT, " +
                "mapx DOUBLE, " +
                "mapy DOUBLE, " +
                "local_id VARCHAR(64), " +
                "CONSTRAINT fk_trip_plan_item_plan FOREIGN KEY (plan_id) REFERENCES trip_plan (plan_id) ON DELETE CASCADE" +
                ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private String resolveSchema(Connection conn) throws SQLException {
        String schema = conn.getCatalog();
        if (schema == null || schema.isBlank()) {
            schema = conn.getSchema();
        }
        if (schema == null || schema.isBlank()) {
            throw new SQLException("Unable to resolve database schema for connection");
        }
        return schema;
    }

    @Override
    public int insertPlan(Connection conn, TripPlan plan) throws SQLException {
        String sql = "INSERT INTO trip_plan (title, start_date, end_date, budget, memo, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, plan.getTitle());
            setNullableDate(ps, 2, plan.getStartDate());
            setNullableDate(ps, 3, plan.getEndDate());
            ps.setString(4, plan.getBudget());
            ps.setString(5, plan.getMemo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated key for trip_plan");
    }

    @Override
    public void updatePlan(Connection conn, TripPlan plan) throws SQLException {
        String sql = "UPDATE trip_plan SET title = ?, start_date = ?, end_date = ?, budget = ?, memo = ?, updated_at = NOW() " +
                "WHERE plan_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getTitle());
            setNullableDate(ps, 2, plan.getStartDate());
            setNullableDate(ps, 3, plan.getEndDate());
            ps.setString(4, plan.getBudget());
            ps.setString(5, plan.getMemo());
            ps.setInt(6, plan.getPlanId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deletePlanItems(Connection conn, int planId) throws SQLException {
        String sql = "DELETE FROM trip_plan_item WHERE plan_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.executeUpdate();
        }
    }

    @Override
    public void insertPlanItem(Connection conn, int planId, TripPlan.PlanItem item) throws SQLException {
        String sql = "INSERT INTO trip_plan_item " +
                "(plan_id, order_index, place_id, place_title, address, visit_day, start_time, end_time, memo, mapx, mapy, local_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, planId);
            ps.setInt(2, item.getOrderIndex() == null ? 0 : item.getOrderIndex());
            ps.setString(3, item.getPlaceId());
            ps.setString(4, item.getTitle());
            ps.setString(5, item.getAddress());
            setNullableDate(ps, 6, item.getDay());
            ps.setString(7, item.getStartTime());
            ps.setString(8, item.getEndTime());
            ps.setString(9, item.getMemo());
            if (item.getMapx() == null) {
                ps.setNull(10, Types.DOUBLE);
            } else {
                ps.setDouble(10, item.getMapx());
            }
            if (item.getMapy() == null) {
                ps.setNull(11, Types.DOUBLE);
            } else {
                ps.setDouble(11, item.getMapy());
            }
            ps.setString(12, item.getLocalId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setPlanItemId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public List<TripPlan> selectAll(Connection conn) throws SQLException {
        String sql = "SELECT p.plan_id, p.title, p.start_date, p.end_date, p.budget, p.memo, p.created_at, p.updated_at, " +
                "i.plan_item_id, i.order_index, i.place_id, i.place_title, i.address, i.visit_day, i.start_time, i.end_time, i.memo AS item_memo, " +
                "i.mapx, i.mapy, i.local_id " +
                "FROM trip_plan p " +
                "LEFT JOIN trip_plan_item i ON p.plan_id = i.plan_id " +
                "ORDER BY p.plan_id, i.order_index";
        Map<Integer, TripPlan> planMap = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int planId = rs.getInt("plan_id");
                TripPlan plan = planMap.computeIfAbsent(planId, id -> {
                    TripPlan p = new TripPlan();
                    p.setPlanId(id);
                    p.setItems(new ArrayList<>());
                    return p;
                });

                plan.setTitle(rs.getString("title"));
                Date startDate = rs.getDate("start_date");
                plan.setStartDate(startDate != null ? startDate.toString() : null);
                Date endDate = rs.getDate("end_date");
                plan.setEndDate(endDate != null ? endDate.toString() : null);
                plan.setBudget(rs.getString("budget"));
                plan.setMemo(rs.getString("memo"));
                plan.setCreatedAt(rs.getString("created_at"));
                plan.setUpdatedAt(rs.getString("updated_at"));

                int itemId = rs.getInt("plan_item_id");
                if (!rs.wasNull()) {
                    TripPlan.PlanItem item = new TripPlan.PlanItem();
                    item.setPlanItemId(itemId);
                    String localId = rs.getString("local_id");
                    if (localId == null || localId.isBlank()) {
                        localId = "plan-item-" + itemId;
                    }
                    item.setLocalId(localId);
                    int orderIndex = rs.getInt("order_index");
                    item.setOrderIndex(rs.wasNull() ? null : orderIndex);
                    item.setPlaceId(rs.getString("place_id"));
                    item.setTitle(rs.getString("place_title"));
                    item.setAddress(rs.getString("address"));
                    Date visitDay = rs.getDate("visit_day");
                    item.setDay(visitDay != null ? visitDay.toString() : null);
                    item.setStartTime(rs.getString("start_time"));
                    item.setEndTime(rs.getString("end_time"));
                    item.setMemo(rs.getString("item_memo"));
                    double mapx = rs.getDouble("mapx");
                    if (!rs.wasNull()) {
                        item.setMapx(mapx);
                    }
                    double mapy = rs.getDouble("mapy");
                    if (!rs.wasNull()) {
                        item.setMapy(mapy);
                    }
                    plan.addItem(item);
                }
            }
        }
        return new ArrayList<>(planMap.values());
    }

    private void setNullableDate(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.DATE);
            return;
        }
        try {
            LocalDate parsed = LocalDate.parse(value);
            ps.setDate(index, Date.valueOf(parsed));
        } catch (DateTimeParseException e) {
            throw new SQLException("Invalid date value: " + value, e);
        }
    }
}
