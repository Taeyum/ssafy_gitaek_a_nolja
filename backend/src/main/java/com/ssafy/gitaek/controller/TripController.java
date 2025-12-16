package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.TripCreateRequest;
import com.ssafy.gitaek.dto.TripScheduleDto;
import com.ssafy.gitaek.model.Trip;
import com.ssafy.gitaek.model.User;
import com.ssafy.gitaek.service.TripService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // 일정 추가 API
    @PostMapping("/schedule")
    public ResponseEntity<?> addSchedule(@RequestBody TripScheduleDto dto) {
        try {
            tripService.addSchedule(dto);
            return ResponseEntity.ok("일정 추가 성공!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("일정 추가 실패: " + e.getMessage());
        }
    }

    @GetMapping("/{tripId}/schedules")
    public ResponseEntity<?> getSchedules(@PathVariable int tripId) {
        try {
            List<TripScheduleDto> schedules = tripService.getSchedules(tripId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("일정 조회 실패");
        }
    }

    // 일정 삭제 API
    // 요청 모양: DELETE /api/trips/{tripId}/schedules?tripDay=1&poiId=123
    @DeleteMapping("/{tripId}/schedules")
    public ResponseEntity<?> deleteSchedule(
            @PathVariable int tripId,
            @RequestParam int tripDay,
            @RequestParam int poiId) {
        try {
            tripService.deleteSchedule(tripId, tripDay, poiId);
            return ResponseEntity.ok("일정 삭제 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("삭제 실패");
        }
    }

    // 수정 권한 요청
    @PostMapping("/{tripId}/edit/request")
    public ResponseEntity<?> requestEdit(@PathVariable int tripId, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).build();

        boolean success = tripService.requestEdit(tripId, user.getUserId());
        if (success) {
            return ResponseEntity.ok("수정 권한 획득");
        } else {
            return ResponseEntity.status(409).body("다른 사용자가 수정 중입니다.");
        }
    }

    // 수정 종료
    @PostMapping("/{tripId}/edit/release")
    public ResponseEntity<?> releaseEdit(@PathVariable int tripId, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).build();

        tripService.releaseEdit(tripId, user.getUserId());
        return ResponseEntity.ok("수정 권한 해제");
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<?> getTrip(@PathVariable int tripId) {
        try {
            Trip trip = tripService.getTrip(tripId);
            if (trip != null) {
                return ResponseEntity.ok(trip);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("조회 실패");
        }
    }
    // 초대 코드로 입장 API
    @PostMapping("/join")
    public ResponseEntity<?> joinTrip(@RequestBody Map<String, String> map, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        String code = map.get("inviteCode");
        try {
            Trip joinedTrip = tripService.joinTripByCode(code, user.getUserId());
            return ResponseEntity.ok(joinedTrip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [추가] 여행 나가기 API: POST /api/trips/{tripId}/leave
    @PostMapping("/{tripId}/leave")
    public ResponseEntity<?> leaveTrip(@PathVariable int tripId, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        try {
            tripService.leaveTrip(tripId, user.getUserId());
            return ResponseEntity.ok("여행에서 나갔습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("오류 발생");
        }
    }
}