package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.model.User;
import com.ssafy.gitaek.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminController {

    @Autowired
    private UserService userService;

    // 관리자 권한 체크용 헬퍼 메서드
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // 1. 회원 목록 조회
    @GetMapping
    public ResponseEntity<?> listUsers(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) String keyword,
                                       HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
        
        Map<String, Object> result = userService.getUserList(page, type, keyword);
        return ResponseEntity.ok(result);
    }

    // 2. 상태 변경 (정지 등) & 관리자 승격 (같이 사용 가능)
    // 요청 예시: { "role": "BANNED" } 또는 { "role": "ADMIN" }
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> changeRole(@PathVariable int userId, 
                                        @RequestBody Map<String, String> body,
                                        HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");

        String newRole = body.get("role");
        userService.changeUserRole(userId, newRole);
        return ResponseEntity.ok("권한이 " + newRole + "(으)로 변경되었습니다.");
    }

    // 3. 비밀번호 초기화
    @PutMapping("/{userId}/reset-pw")
    public ResponseEntity<?> resetPw(@PathVariable int userId, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");

        userService.resetUserPassword(userId);
        return ResponseEntity.ok("비밀번호가 1234로 초기화되었습니다.");
    }
}