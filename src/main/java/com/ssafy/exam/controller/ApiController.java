package com.ssafy.exam.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.exam.model.service.LocalTripDataService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/api"}, loadOnStartup = 1)
public class ApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;

    private final LocalTripDataService localService = LocalTripDataService.getInstance();
    private final ObjectMapper om = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = getActionParameter(req, resp); // 기본 "index"
        switch (action) {
            case "areas" -> handleAreas(req, resp);       // 시도 목록 or 시군구 목록
            case "sigungus" -> handleSigungus(req, resp); // 특정 시도의 시군구
            case "places" -> handlePlaces(req, resp);     // 관광지 목록
            case "map" -> forward(req, resp, "/map.jsp");
            case "index" -> forward(req, resp, "/index.jsp");
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action: " + action);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    private void handleAreas(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String areaCode = req.getParameter("areaCode");
        List<Map<String, Object>> data = localService.loadAreas(areaCode);
        writeJson(resp, wrapResponse(data));
    }

    private void handleSigungus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String areaCode = req.getParameter("areaCode");
        List<Map<String, Object>> data = localService.loadSigungus(areaCode);
        writeJson(resp, wrapResponse(data));
    }

    private void handlePlaces(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String areaCode = req.getParameter("areaCode");
        String sigunguCode = req.getParameter("sigunguCode");
        String contentTypeId = req.getParameter("contentTypeId");
        String pageNo = req.getParameter("pageNo");
        String numOfRows = req.getParameter("numOfRows");
        List<Map<String, Object>> data = localService.searchAttractions(areaCode, sigunguCode, contentTypeId, pageNo, numOfRows);
        writeJson(resp, wrapResponse(data));
    }

    private Map<String, Object> wrapResponse(List<Map<String, Object>> items) {
        return Map.of(
                "response", Map.of(
                        "body", Map.of(
                                "items", Map.of(
                                        "item", items
                                )
                        )
                )
        );
    }

    private void writeJson(HttpServletResponse resp, Object json) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        om.writeValue(resp.getWriter(), json);
    }
}
