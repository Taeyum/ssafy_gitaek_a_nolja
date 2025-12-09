package com.ssafy.gitaek.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.gitaek.model.User;
import com.ssafy.gitaek.service.UserService;
import com.ssafy.gitaek.dto.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> signup(@RequestBody UserSignupRequest signupDto) {
        try {
            userService.signup(signupDto.toEntity());
            return new ResponseEntity<>("회원가입 성공!", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("회원가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest loginDto,
                                   HttpSession session,
                                   HttpServletResponse response) {

        User user = userService.login(loginDto.getEmail(), loginDto.getPassword());

        if (user != null) {
            session.setAttribute("loginUser", user);

            Cookie cookie = new Cookie("rememberEmail", loginDto.getEmail());
            if (loginDto.isRememberId()) {
                cookie.setMaxAge(60 * 60 * 24 * 7);
                cookie.setPath("/");
            } else {
                cookie.setMaxAge(0);
                cookie.setPath("/");
            }
            response.addCookie(cookie);

            user.setPassword(null);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("로그인 실패", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return new ResponseEntity<>("로그아웃 되었습니다.", HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser != null) {
            return new ResponseEntity<>(loginUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("로그인 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        if (exists) {
            return new ResponseEntity<>("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("사용 가능한 이메일입니다.", HttpStatus.OK);
        }
    }

    @PostMapping("/password-recovery")
    public ResponseEntity<?> findPw(@RequestBody UserFindPwRequest request) {
        String tempPw = userService.findPassword(request.getEmail(), request.getNickname());

        if (tempPw != null) {
            return new ResponseEntity<>("임시 비밀번호: " + tempPw, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("정보가 일치하는 회원이 없습니다.", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePw(@RequestBody UserChangePwRequest request, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        boolean result = userService.changePassword(loginUser.getUserId(),
                                                    request.getCurrentPassword(),
                                                    request.getNewPassword());

        if (result) {
            loginUser.setPassword(request.getNewPassword());
            session.setAttribute("loginUser", loginUser);
            return new ResponseEntity<>("비밀번호 변경 성공!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("현재 비밀번호가 틀렸습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    // [8] 닉네임 중복 체크: GET /api/users/check/{nickname}
    @GetMapping("/check/{nickname}")
    public ResponseEntity<?> checkNickname(@PathVariable String nickname) {
        boolean exists = userService.isNicknameExists(nickname);
        // exists가 true면 중복(사용불가), false면 사용가능
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    // [9] 회원 정보 수정: PUT /api/users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @RequestBody User user, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getUserId() != userId) {
            return new ResponseEntity<>("권한이 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        user.setUserId(userId); // ID 강제 주입
        userService.updateUserInfo(user);

        // 세션 정보도 갱신 (중요! 안하면 새로고침 전까지 옛날 닉네임 보임)
        loginUser.setNickname(user.getNickname());
        // loginUser.setPhone(user.getPhone()); // 전화번호 있다면
        session.setAttribute("loginUser", loginUser);

        return new ResponseEntity<>("회원 정보가 수정되었습니다.", HttpStatus.OK);
    }

    // [10] 회원 탈퇴: DELETE /api/users/{userId}
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId, @RequestBody UserLoginRequest request, HttpSession session) {
        // 본인 확인 (request body에 password가 들어있다고 가정)
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getUserId() != userId) {
            return new ResponseEntity<>("권한이 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        boolean result = userService.deleteUser(userId, request.getPassword());
        if (result) {
            session.invalidate(); // 로그아웃 처리
            return new ResponseEntity<>("회원 탈퇴가 완료되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}