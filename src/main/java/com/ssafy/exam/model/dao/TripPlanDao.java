package com.ssafy.exam.model.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.ssafy.exam.model.dto.TripPlan;

public interface TripPlanDao {
    void ensureTables();

    int insertPlan(Connection conn, TripPlan plan) throws SQLException;

    void updatePlan(Connection conn, TripPlan plan) throws SQLException;

    void deletePlanItems(Connection conn, int planId) throws SQLException;

    void insertPlanItem(Connection conn, int planId, TripPlan.PlanItem item) throws SQLException;

    List<TripPlan> selectAll(Connection conn) throws SQLException;
}
