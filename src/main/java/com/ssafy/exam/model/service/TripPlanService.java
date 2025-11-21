package com.ssafy.exam.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.exam.model.dao.TripPlanDao;
import com.ssafy.exam.model.dto.TripPlan;
import com.ssafy.exam.model.dto.TripPlan.PlanItem;

@Service
public class TripPlanService {

    private final TripPlanDao dao;

    @Autowired
    public TripPlanService(TripPlanDao dao) {
        this.dao = dao;
    }

    @Transactional
    public TripPlan save(TripPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("plan must not be null");
        }
        if (plan.getItems() == null) {
            plan.setItems(new ArrayList<>());
        }

        normaliseItems(plan);

        if (plan.getPlanId() > 0) {
            dao.updatePlan(plan);
            dao.deletePlanItems(plan.getPlanId());
        } else {
            dao.insertPlan(plan); // The planId will be set by mybatis keyProperty
        }

        for (PlanItem item : plan.getItems()) {
            if (item == null) continue;
            dao.insertPlanItem(plan.getPlanId(), item);
        }

        return plan;
    }

    @Transactional(readOnly = true)
    public List<TripPlan> findAll() {
        return dao.selectAll();
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
}
