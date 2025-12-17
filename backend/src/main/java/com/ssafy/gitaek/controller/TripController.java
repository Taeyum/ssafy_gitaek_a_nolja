package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.TripCreateRequest;
import com.ssafy.gitaek.dto.TripScheduleDto;
import com.ssafy.gitaek.jwt.CustomUserDetails; // ★ 필수 Import
import com.ssafy.gitaek.model.Trip;
import com.ssafy.gitaek.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    // 여행 생성
    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody TripCreateRequest request, 
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        try {
            Trip newTrip = tripService.createTrip(request, userDetails.getUser().getUserId());
            return new ResponseEntity<>(newTrip, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 내 여행 목록
    @GetMapping("/my")
    public ResponseEntity<?> getMyTrips(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        try {
            List<Trip> trips = tripService.getMyTrips(userDetails.getUser().getUserId());
            return new ResponseEntity<>(trips, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("조회 오류", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 여행 삭제
    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTrip(@PathVariable int tripId, 
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        try {
            tripService.deleteTrip(tripId, userDetails.getUser().getUserId());
            return new ResponseEntity<>("여행이 삭제되었습니다.", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("삭제 오류", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 일정 추가 (로그인 필요 시 userDetails 추가 가능, 현재는 누구나 가능으로 둠)
    @PostMapping("/schedule")
    public ResponseEntity<?> addSchedule(@RequestBody TripScheduleDto dto) {
        try {
            tripService.addSchedule(dto);
            return ResponseEntity.ok("일정 추가 성공!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("실패: " + e.getMessage());
        }
    }

    @GetMapping("/{tripId}/schedules")
    public ResponseEntity<?> getSchedules(@PathVariable int tripId) {
        return ResponseEntity.ok(tripService.getSchedules(tripId));
    }

    @DeleteMapping("/{tripId}/schedules")
    public ResponseEntity<?> deleteSchedule(@PathVariable int tripId, @RequestParam int tripDay, @RequestParam int poiId) {
        tripService.deleteSchedule(tripId, tripDay, poiId);
        return ResponseEntity.ok("삭제 성공");
    }

    // 수정 권한 요청 (세션 -> 토큰)
    @PostMapping("/{tripId}/edit/request")
    public ResponseEntity<?> requestEdit(@PathVariable int tripId, 
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();

        boolean success = tripService.requestEdit(tripId, userDetails.getUser().getUserId());
        return success ? ResponseEntity.ok("권한 획득") : ResponseEntity.status(409).body("다른 사용자가 수정 중");
    }

    // 수정 해제
    @PostMapping("/{tripId}/edit/release")
    public ResponseEntity<?> releaseEdit(@PathVariable int tripId, 
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();

        tripService.releaseEdit(tripId, userDetails.getUser().getUserId());
        return ResponseEntity.ok("권한 해제");
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<?> getTrip(@PathVariable int tripId) {
        Trip trip = tripService.getTrip(tripId);
        return trip != null ? ResponseEntity.ok(trip) : ResponseEntity.notFound().build();
    }
    
    // 초대 코드로 입장
    @PostMapping("/join")
    public ResponseEntity<?> joinTrip(@RequestBody Map<String, String> map, 
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        String code = map.get("inviteCode");
        try {
            Trip joinedTrip = tripService.joinTripByCode(code, userDetails.getUser().getUserId());
            return ResponseEntity.ok(joinedTrip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 여행 나가기
    @PostMapping("/{tripId}/leave")
    public ResponseEntity<?> leaveTrip(@PathVariable int tripId, 
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        try {
            tripService.leaveTrip(tripId, userDetails.getUser().getUserId());
            return ResponseEntity.ok("여행에서 나갔습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("오류 발생");
        }
    }
}