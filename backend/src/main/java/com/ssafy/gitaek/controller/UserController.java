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
}