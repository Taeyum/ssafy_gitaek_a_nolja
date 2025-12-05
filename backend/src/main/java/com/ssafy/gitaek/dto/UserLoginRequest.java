package com.ssafy.gitaek.dto;

import lombok.Data;

// [이 폴더의 범용성]
// 클라이언트(Vue.js)와 서버(Spring Boot) 간의 데이터 교환 규격을 정의하는 객체(DTO)들의 모음.
// DB 테이블 형태인 Entity와 분리되어 있어, 내부 로직이 변경되더라도 프론트엔드 통신 규격(API)을 일정하게 유지시켜 주는 완충 지대 역할을 함.
@Data
public class UserLoginRequest {

    // [이 코드의 목적]
    // 사용자가 로그인 폼에 입력한 정보를 컨트롤러(Controller)가 받기 위한 포장지(Request Body) 역할.
    // 서비스 계층으로 로그인 처리에 필요한 데이터만 선별하여 전달함.

    // [현재 코드 있는 내용]
    // - 사용자 식별 정보: 이메일(email), 비밀번호(password)
    // - 편의 기능 데이터: 로그인 화면의 '아이디 저장' 체크박스 값 (rememberId)
    private String email;
    private String password;
    private boolean rememberId;

    // [추가로 들어갈 수 있는 내용]
    // - 입력값 검증: @Email(형식 체크), @NotBlank(필수값 체크) 등 유효성 검사 어노테이션
    // - 보안 강화: 봇 방지를 위한 리캡차(reCAPTCHA) 토큰, 2차 인증 코드
    // - 소셜 로그인 확장: 소셜 제공자 정보 (String provider), 소셜용 액세스 토큰
}