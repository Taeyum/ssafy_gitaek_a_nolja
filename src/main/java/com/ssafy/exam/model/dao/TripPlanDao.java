package com.ssafy.exam.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ssafy.exam.model.dto.TripPlan;

public interface TripPlanDao {

    int insertPlan(TripPlan plan);

    void updatePlan(TripPlan plan);

    void deletePlanItems(int planId);

    void insertPlanItem(@Param("planId") int planId, @Param("item") TripPlan.PlanItem item);

    List<TripPlan> selectAll();
}
