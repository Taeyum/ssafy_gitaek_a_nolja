package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.TripCreateRequest;
import com.ssafy.gitaek.model.Trip;
import com.ssafy.gitaek.model.User;
import com.ssafy.gitaek.service.TripService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips") // ★ 프론트엔드 api/trip.js랑 매칭되는 주소
public class TripController {

    @Autowired
    private TripService tripService;

    // 여행 생성: POST /api/trips
    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody TripCreateRequest request, HttpSession session) {
        // 1. 로그인 확인 (세션에서 정보 꺼내기)
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요한 서비스입니다.", HttpStatus.UNAUTHORIZED); // 401 에러
        }

        // 2. 서비스 호출 (여행 만들기 + 방장 참여시키기)
        try {
            Trip newTrip = tripService.createTrip(request, loginUser.getUserId());
            return new ResponseEntity<>(newTrip, HttpStatus.CREATED); // 201 성공!
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("여행 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // 내 여행 목록 조회: GET /api/trips/my
    @GetMapping("/my")
    public ResponseEntity<?> getMyTrips(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            List<Trip> trips = tripService.getMyTrips(loginUser.getUserId());
            return new ResponseEntity<>(trips, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("목록 조회 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 여행 삭제: DELETE /api/trips/{tripId}
    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTrip(@PathVariable int tripId, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            tripService.deleteTrip(tripId, loginUser.getUserId());
            return new ResponseEntity<>("여행이 삭제되었습니다.", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("삭제 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}