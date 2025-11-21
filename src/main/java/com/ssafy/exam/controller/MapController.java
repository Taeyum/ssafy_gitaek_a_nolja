package com.ssafy.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController {

    @GetMapping("/map")
    public String showMapPage() {
        return "map"; // ViewResolver가 /map.jsp로 변환합니다.
    }

}
