package com.ssafy.exam.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.exam.model.service.LocalTripDataService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final LocalTripDataService localService;

    @Autowired
    public ApiController(LocalTripDataService localService) {
        this.localService = localService;
    }

    @GetMapping("/areas")
    public Map<String, Object> handleAreas(@RequestParam(required = false) String areaCode) {
        List<Map<String, Object>> data = localService.loadAreas(areaCode);
        return wrapResponse(data);
    }

    @GetMapping("/sigungus")
    public Map<String, Object> handleSigungus(@RequestParam String areaCode) {
        List<Map<String, Object>> data = localService.loadSigungus(areaCode);
        return wrapResponse(data);
    }

    @GetMapping("/places")
    public Map<String, Object> handlePlaces(@RequestParam(required = false) String areaCode,
                                            @RequestParam(required = false) String sigunguCode,
                                            @RequestParam(required = false) String contentTypeId,
                                            @RequestParam(required = false) String pageNo,
                                            @RequestParam(required = false) String numOfRows) {
        List<Map<String, Object>> data = localService.searchAttractions(areaCode, sigunguCode, contentTypeId, pageNo, numOfRows);
        return wrapResponse(data);
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
}
