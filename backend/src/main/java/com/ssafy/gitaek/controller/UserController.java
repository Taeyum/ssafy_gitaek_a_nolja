package com.ssafy.gitaek.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.gitaek.dto.UserChangePwRequest;
import com.ssafy.gitaek.dto.UserFindPwRequest;
import com.ssafy.gitaek.dto.UserLoginRequest;
import com.ssafy.gitaek.dto.UserSignupRequest;
import com.ssafy.gitaek.jwt.CustomUserDetails;
import com.ssafy.gitaek.jwt.JwtTokenProvider;
import com.ssafy.gitaek.model.User;
import com.ssafy.gitaek.service.UserService;

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
    
 // ★ 회원 탈퇴
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId,
                                        @RequestBody Map<String, String> body,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 1. 로그인 정보 확인
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        // 2. 본인 확인 (토큰의 ID와 요청한 ID가 같은지)
        if (userDetails.getUser().getUserId() != userId) {
            return new ResponseEntity<>("본인의 계정만 탈퇴할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 3. 비밀번호 꺼내기 (프론트에서 { data: { password: "..." } } 로 보냄)
        String password = body.get("password");

        // 4. 서비스 호출
        boolean isDeleted = userService.deleteUser(userId, password);

        if (isDeleted) {
            return new ResponseEntity<>("회원 탈퇴가 완료되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    // ★ [추가 1] 닉네임 중복 체크
    // 요청: GET /api/users/check/{nickname}
    @GetMapping("/check/{nickname}")
    public ResponseEntity<?> checkNickname(@PathVariable("nickname") String nickname) {
        // 서비스에서 중복 여부 확인 (true: 중복, false: 사용가능)
        boolean isDuplicate = userService.checkNickname(nickname);
        return ResponseEntity.ok(isDuplicate);
    }

    // ★ [추가 2] 회원 정보 수정 (닉네임 변경)
    // 요청: PUT /api/users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo(@PathVariable int userId,
                                            @RequestBody Map<String, String> body,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 1. 로그인 확인
        if (userDetails == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        // 2. 본인 확인 (토큰 주인 == 수정하려는 대상)
        if (userDetails.getUser().getUserId() != userId) {
            return new ResponseEntity<>("본인의 정보만 수정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 3. 닉네임 꺼내기
        String newNickname = body.get("nickname");

        // 4. 업데이트 수행
        try {
            userService.updateNickname(userId, newNickname);
            return ResponseEntity.ok("회원 정보가 수정되었습니다.");
        } catch (Exception e) {
            return new ResponseEntity<>("수정 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}