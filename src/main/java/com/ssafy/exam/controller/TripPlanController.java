package com.ssafy.exam.controller;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exam.model.dto.TripPlan;
import com.ssafy.exam.model.service.TripPlanService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/plan", loadOnStartup = 1)
public class TripPlanController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;

    private final TripPlanService service = TripPlanService.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = getActionParameter(req, resp);
        switch (action) {
        case "list-json" -> writePlanList(resp);
        case "plan-form", "index" -> forward(req, resp, "/plan.jsp");
        default -> forward(req, resp, "/plan.jsp");
        }
    }

    private void writePlanList(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        mapper.writeValue(resp.getWriter(), service.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = getActionParameter(req, resp);
        switch (action) {
        case "save-plan" -> savePlan(req, resp);
        default -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported action: " + action);
        }
    }

    private void savePlan(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        TripPlan plan = mapper.readValue(req.getReader(), TripPlan.class);
        try {
            TripPlan saved = service.save(plan);
            resp.setContentType("application/json; charset=UTF-8");
            mapper.writeValue(resp.getWriter(), Map.of("planId", saved.getPlanId()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json; charset=UTF-8");
            mapper.writeValue(resp.getWriter(), Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Failed to save trip plan"));
        }
    }
}
