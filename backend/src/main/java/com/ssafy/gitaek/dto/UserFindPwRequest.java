package com.ssafy.gitaek.dto;

import lombok.Data;

// [이 폴더의 범용성]
// 프론트엔드와 백엔드 간에 데이터를 주고받을 때 사용하는 객체(DTO)들의 집합.
// DB 스키마(Entity)가 직접 노출되는 것을 방지하고, 화면에서 필요한 데이터만 선별하여 통신 효율성과 보안성을 높이는 역할을 함.
@Data
public class UserFindPwRequest {

    // [이 코드의 목적]
    // 비밀번호를 분실한 사용자가 계정을 찾기 위해 입력한 본인 확인 정보를 서버로 전달하는 객체.
    // 이 정보가 DB에 존재하면 임시 비밀번호를 발송하거나 비밀번호 재설정 페이지로 안내함.

    // [현재 코드 있는 내용]
    // - 사용자 식별 및 연락 수단: 이메일(email)
    // - 보조 확인 수단: 닉네임(nickname)
    private String email;
    private String nickname;

    // [추가로 들어갈 수 있는 내용]
    // - 실명 확인: 동명이인 구분을 위한 이름(name) 필드
    // - 연락처 인증: 휴대폰 번호(phone) 및 인증 번호(authCode)
    // - 보안 질문: "나의 보물 1호는?" 같은 보안 질문에 대한 답변(securityAnswer)
}