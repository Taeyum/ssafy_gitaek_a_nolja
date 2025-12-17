package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.*;
import com.ssafy.gitaek.jwt.CustomUserDetails;
import com.ssafy.gitaek.jwt.JwtTokenProvider;
import com.ssafy.gitaek.model.User;
import com.ssafy.gitaek.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // ★ 토큰 발급기

    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder; // ★ 인증 매니저

    // 회원가입
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

    // ★ 로그인: 세션 대신 JWT 토큰 발급
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest loginDto) {
        try {
            // 1. ID/PW로 인증 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

            // 2. 실제 검증 (비밀번호 체크 등)
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 3. 인증 정보로 JWT 토큰 생성
            String jwt = jwtTokenProvider.createToken(authentication);

            // 4. 토큰 반환 (클라이언트는 이걸 받아서 저장해야 함)
            return ResponseEntity.ok(jwt);
            
        } catch (Exception e) {
            return new ResponseEntity<>("아이디 또는 비밀번호를 확인하세요.", HttpStatus.UNAUTHORIZED);
        }
    }

    // 로그아웃 (JWT는 서버에 세션이 없으므로, 클라이언트가 토큰을 버리면 끝)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return new ResponseEntity<>("로그아웃 되었습니다. (토큰 삭제 필요)", HttpStatus.OK);
    }

    // ★ 내 정보 조회: 세션 대신 @AuthenticationPrincipal 사용
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            User user = userDetails.getUser();
            user.setPassword(null); // 보안상 비번 가림
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("로그인 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        return exists ? new ResponseEntity<>("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT)
                      : new ResponseEntity<>("사용 가능한 이메일입니다.", HttpStatus.OK);
    }

    @PostMapping("/password-recovery")
    public ResponseEntity<?> findPw(@RequestBody UserFindPwRequest request) {
        String tempPw = userService.findPassword(request.getEmail(), request.getNickname());
        return tempPw != null ? new ResponseEntity<>("임시 비밀번호: " + tempPw, HttpStatus.OK)
                              : new ResponseEntity<>("정보가 일치하는 회원이 없습니다.", HttpStatus.NOT_FOUND);
    }

    // ★ 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<?> changePw(@RequestBody UserChangePwRequest request, 
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        boolean result = userService.changePassword(userDetails.getUser().getUserId(),
                                                    request.getCurrentPassword(),
                                                    request.getNewPassword());
        if (result) {
            return new ResponseEntity<>("비밀번호 변경 성공!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("현재 비밀번호가 틀렸습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}