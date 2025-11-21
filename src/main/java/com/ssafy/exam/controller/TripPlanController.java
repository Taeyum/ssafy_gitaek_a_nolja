package com.ssafy.exam.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ssafy.exam.model.dto.TripPlan;
import com.ssafy.exam.model.service.TripPlanService;

@Controller
@RequestMapping("/plan")
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @Autowired
    public TripPlanController(TripPlanService tripPlanService) {
        this.tripPlanService = tripPlanService;
    }

    @GetMapping(value = { "", "/", "/index", "/plan-form" })
    public String showPlanPage() {
        // ViewResolver가 "plan"을 "/plan.jsp"로 변환해줍니다.
        return "plan";
    }

    @GetMapping("/list-json")
    @ResponseBody
    public ResponseEntity<List<TripPlan>> getPlanList() {
        try {
            List<TripPlan> plans = tripPlanService.findAll();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/save-plan")
    public ResponseEntity<Map<String, Object>> savePlan(@RequestBody TripPlan plan) {
        try {
            TripPlan savedPlan = tripPlanService.save(plan);
            return ResponseEntity.ok(Map.of("planId", savedPlan.getPlanId()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to save trip plan"));
        }
    }
}
