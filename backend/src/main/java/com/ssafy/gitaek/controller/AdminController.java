package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.service.OpenApiService;
import com.ssafy.gitaek.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin") // ★ 1. 전체 기본 주소 변경 (SecurityConfig와 일치!)
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private OpenApiService openApiService;

    // --- [1] 회원 관리 기능 (URL에 /users 추가) ---

    // 1. 회원 목록 조회
    // 최종 주소: GET /api/admin/users
    @GetMapping("/users") // ★ 명확하게 'users'를 붙여줍니다.
    public ResponseEntity<?> listUsers(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) String keyword) {
        Map<String, Object> result = userService.getUserList(page, type, keyword);
        return ResponseEntity.ok(result);
    }

    // 2. 권한 변경
    // 최종 주소: PUT /api/admin/users/{userId}/role
    @PutMapping("/users/{userId}/role") // ★ 여기도 /users 추가
    public ResponseEntity<?> changeRole(@PathVariable int userId, 
                                        @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        userService.changeUserRole(userId, newRole);
        return ResponseEntity.ok("권한이 " + newRole + "(으)로 변경되었습니다.");
    }

    // 3. 비밀번호 초기화
    // 최종 주소: PUT /api/admin/users/{userId}/reset-pw
    @PutMapping("/users/{userId}/reset-pw") // ★ 여기도 /users 추가
    public ResponseEntity<?> resetPw(@PathVariable int userId) {
        userService.resetUserPassword(userId);
        return ResponseEntity.ok("비밀번호가 1234로 초기화되었습니다.");
    }

    // --- [2] 기타 관리자 기능 ---

    // 4. 데이터 수집
    // 최종 주소: GET /api/admin/load-data (users 안 붙음 -> 깔끔!)
    @GetMapping("/load-data")
    public ResponseEntity<String> loadData() {
        System.out.println(">>> [ADMIN] 관광지 데이터 수집 시작...");
        
        try {
            String result = openApiService.fetchAndSaveAllAttractions();
            return ResponseEntity.ok("✅ 데이터 수집 완료: " + result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("💥 수집 실패: " + e.getMessage());
        }
    }
}