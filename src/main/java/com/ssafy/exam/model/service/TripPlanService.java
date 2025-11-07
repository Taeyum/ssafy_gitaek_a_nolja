package com.ssafy.exam.model.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ssafy.exam.model.dao.TripPlanDao;
import com.ssafy.exam.model.dao.TripPlanDaoImpl;
import com.ssafy.exam.model.dto.TripPlan;
import com.ssafy.exam.model.dto.TripPlan.PlanItem;
import com.ssafy.exam.util.DBUtil;

public class TripPlanService {
    private static final TripPlanService INSTANCE = new TripPlanService();

    private final TripPlanDao dao = TripPlanDaoImpl.getInstance();

    private TripPlanService() {
    }

    public static TripPlanService getInstance() {
        return INSTANCE;
    }

    public TripPlan save(TripPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("plan must not be null");
        }
        if (plan.getItems() == null) {
            plan.setItems(new ArrayList<>());
        }

        normaliseItems(plan);

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            if (plan.getPlanId() > 0) {
                dao.updatePlan(conn, plan);
                dao.deletePlanItems(conn, plan.getPlanId());
            } else {
                int planId = dao.insertPlan(conn, plan);
                plan.setPlanId(planId);
            }

            for (PlanItem item : plan.getItems()) {
                dao.insertPlanItem(conn, plan.getPlanId(), item);
            }

            conn.commit();
            return plan;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Failed to save trip plan", e);
        } finally {
            closeQuietly(conn);
        }
    }

    public List<TripPlan> findAll() {
        try (Connection conn = DBUtil.getConnection()) {
            return dao.selectAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load trip plans", e);
        }
    }

    private void normaliseItems(TripPlan plan) {
        int order = 1;
        for (PlanItem item : plan.getItems()) {
            if (item == null) {
                continue;
            }
            if (item.getLocalId() == null || item.getLocalId().isBlank()) {
                item.setLocalId(UUID.randomUUID().toString());
            }
            item.setOrderIndex(order++);
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }
}
