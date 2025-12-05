package com.ssafy.gitaek.dto;

import com.ssafy.gitaek.model.User;
import lombok.Data;

// [이 폴더의 범용성]
// 데이터 전송 객체(DTO)를 관리하는 위치로, Controller와 Service 계층 간의 데이터 교환을 담당함.
// Entity(DB 테이블)와 완벽히 분리되어 있어, DB 설계가 바뀌어도 API 스펙(프론트엔드 통신 규격)을 보호하는 역할을 수행함.
@Data
public class UserSignupRequest {

    // [이 코드의 목적]
    // 클라이언트(프론트엔드)로부터 '회원가입' 시 필요한 데이터를 받아오는 객체.
    // 입력받은 데이터를 캡슐화하고, 최종적으로 DB 저장을 위해 User Entity로 변환하는 기능까지 포함함.
    
    // [현재 코드 있는 내용]
    // - 회원가입 핵심 데이터 3가지: 이메일(ID 역할), 비밀번호, 닉네임
    // - 편의 메서드: DTO 데이터를 User Entity로 즉시 변환하는 toEntity() 메서드
    private String email;
    private String password;
    private String nickname;

    // [추가로 들어갈 수 있는 내용]
    // - 유효성 검증 어노테이션: @Email, @NotBlank, @Size(min=8) 등 (잘못된 데이터 방지)
    // - 추가 정보: 연락처(phoneNumber), 주소(address), 프로필 이미지 URL
    // - 약관 동의: 마케팅 수신 동의 여부 (boolean marketingAgreed)
    // - 비밀번호 확인: 프론트엔드 확인용 필드 (String passwordConfirm)

    // DTO -> Model 변환 메서드 (편의상 추가)
    public User toEntity() {
        User user = new User();
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setNickname(this.nickname);
        return user;
    }
}