
# ✈️ Gitaek Anolja

> **Gitaek Anolja와 함께 실시간으로 떠나는 여행 계획 협업 플랫폼**
> "지도 위에서 친구들과 동시에 여행 코스를 그려보세요."

**Gitaek Anolja**는 Vue.js 프론트엔드와 연동되어 작동하는 Spring Boot 기반의 백엔드 프로젝트입니다. 공공데이터포털의 관광지 API(TourAPI)와 Kakao Map API를 활용하여 직관적인 경로 설계를 지원하며, **JWT 기반의 보안**, **RESTful API 설계**, 그리고 **데이터 동시성 제어**를 통해 안정적인 협업 경험을 제공합니다.

---

## 🛠 프로젝트 소개 및 기술 스택

### 백엔드 (Backend)

* **Language**: Java 17
* **Framework**: Spring Boot 3.3.5
* **Build Tool**: Maven
* **Database**: MySQL 8.0
* **Libraries**: Spring Security, JWT, Lombok, MyBatis

### 프론트엔드 (Frontend - Separate Repository)

* **Framework**: Vue.js 3
* **State Management**: Pinia
* **External API**: Kakao Map API, TourAPI (KorService2)

---

## 🚀 데이터베이스 및 환경 설정

- DB 계정과 스키마를 준비해 주세요.
- `ssafy_trip` 스키마를 생성하고 사용자 권한을 부여합니다.
- application.properties 설정
- TourAPI 인증키 
- tour.api.key="여기에 키 입력" (인코딩 키)
- KorService2
- tour.api.baseUrl=https://apis.data.go.kr/B551011/KorService2
- 이후 데이터 수집 실행 메서드  http://localhost:8080/api/admin/load-data 실행


---

## 💾 데이터베이스 모델링 (ERD)

  ![ERD](./img/DB/TDD3.png)

- **Users**: 서비스에 가입한 사용자 계정 정보를 관리하는 기본 테이블입니다.
- **Region**: 여행/관광지가 속한 지역 정보를 관리하고, 자기참조로 시·군·구 같은 계층 구조를 표현합니다.
- **POI**: 관광지(명소)의 이름, 설명, 주소, 좌표, 썸네일과 어느 지역에 속하는지 저장하는 테이블입니다.
- **Trip**: 여행 제목, 기간, 설명, 지역, 호스트, 최대 인원 등 하나의 “여행 방(여행 일정)” 정보를 담는 핵심 엔티티입니다.
- **Chat_Room**: 각 여행계획에 1:1로 매핑되는 채팅방 정보를 저장해, 여행별 채팅 공간을 구분합니다.
- **Trip_Participant**: 어떤 사용자가 어떤 여행에 참가했는지 M:N 관계를 풀어주는 참가자 매핑 테이블입니다.
- **Chat_Message**: 채팅방에서 누가, 언제, 어떤 내용을 보냈는지 메시지 단위로 기록하는 로그 테이블입니다.


---

## 📖 주요 기능 및 사용법 (Usage)

### 1. 보안 및 인증 (Security & Auth)

* **JWT 도입**: 기존 세션 방식에서 Stateless한 JWT 토큰 방식으로 전환하여 보안 강화.
* **DTO 분리**: `UserLoginRequest`, `UserSignupRequest` 등으로 요청 객체를 세분화하여 오버포스팅 방지.
* **비밀번호 암호화**: BCrypt를 이용한 단방향 암호화 적용.

### 2. 여행 계획 및 지도 (Trip & Map)

* **실시간 경로 편집**: Pinia Store를 활용한 중앙 상태 관리로 일정 추가/삭제/순서 변경(Drag & Drop) 반영.
* **동시성 제어**: 다수의 사용자가 동시에 수정할 때 데이터 충돌을 방지하기 위한 편집 권한 로직 구현.
* **관광지 데이터**: TourAPI를 통해 수집된 데이터를 기반으로 지역별/유형별 검색 제공.

### 3. 관리자 기능 (Admin)

* **데이터 적재**: 공공데이터포털의 방대한 관광지 데이터를 DB에 자동 적재하는 API 제공.
* **회원 관리**: 악성 유저 관리 및 전체 회원 조회 기능.

---

## API 명세서 작성

Postman 및 Talend API Tester를 활용하여 주요 시나리오 테스트를 완료했습니다.

* **API 명세서**: [Notion Link](https://www.notion.so/2b2af5125192800fad5acf5bab04a860)



---

## 🚧 프로젝트 상태 및 로드맵

**현재 상태: Beta v1.0**

* 핵심 기능(인증, 여행 생성, 지도 연동) 구현 완료.
* Vue.js 프론트엔드와 연동 테스트 완료. 

---

## 👤 저자 및 기여자

* **은태현** - *Project Leader & Frontend & Trip Backend & AI*
* **박기택** - *User Backend & Document*

---

## 📅 개발 히스토리 (Change Log)

### 2024.12.19 (Hotfix & Refactoring)

**보안 이슈 해결 (Security)**

* 비밀번호 변경 성공 시, 서버 로그아웃 처리와 동시에 클라이언트 측 토큰을 즉시 파기하여 원자성(Atomicity) 보장.

**회원 탈퇴 로직 개선 (Transaction)**

* 문제: 방장(Owner)이 탈퇴할 경우 FK 제약조건(FK_Trip_Owner)으로 인해 DB 에러 발생.
* 해결: UserService 트랜잭션 내에서
  `방장이 소유한 여행 그룹 전체 삭제(Cascade) → 회원 정보 삭제`
  순서로 처리하여 오류 해결.

**로그아웃 버그 수정**

* 회원 탈퇴 후 이미 계정이 삭제된 상태에서 서버로 로그아웃 요청을 보내 발생하는 401/500 에러 해결
  (클라이언트 단독 토큰 파기 로직 적용).

---

### 2024.12.17 (Security Overhaul)

**Spring Security + JWT 도입**

* 기존 Session 기반 인증을 Stateless 아키텍처로 전면 전환.
* JwtTokenProvider 및 JwtAuthenticationFilter를 구현하여 모든 API 요청 헤더 검증.

**프론트엔드 인증 처리**

* Axios Interceptor: 프론트엔드에서 모든 요청 시 `Bearer {token}` 자동 주입 구현.

**암호화 적용**

* BCryptPasswordEncoder를 적용하여 비밀번호 DB 저장 시 평문 저장 방지.

---

### 2024.12.14 (Core Features: Trip & Map)

**여행 로직 완성**

* 초대 코드(Invite Code): UUID 기반 8자리 랜덤 코드로 검색 없는 즉시 입장 구현.
* 기간 자동 계산: 종료일 미입력 시 Duration 기준으로 날짜 자동 산출.

**동시성 제어 (Concurrency)**

* `current_editor_id` 컬럼을 활용한 Locking 메커니즘 구현
  (한 명이 수정 중일 때 타인은 읽기 전용).

**지도 및 데이터 연동**

* TourAPI 적재: OpenApiService를 통해 공공데이터포털의 관광지 데이터를 수집 및 POI 테이블 적재.
* Event Bus: Kakao Map 마커 이벤트와 Vue 컴포넌트 간 데이터 전달 체계 구축.

---

### 2024.12.09 (Admin Features)

**관리자 기능 구현**

* AdminController 분리 (SRP 원칙 준수).
* 회원 상태 변경 기능 구현:

  * 악성 유저 정지(BANNED)
  * 정지 해제(USER)
  * 관리자 승격(ADMIN)
* 비밀번호 초기화 기능 및 MyBatis Mapper(count, updateRole) 고도화.

---

### 2024.12.01 ~ 12.05 (Auth & DTO Refactoring)

**REST API 리팩토링**

* URL 설계 변경:
  `/api/user/signup → /api/users`
  (자원 중심, HTTP Method 활용).

**DTO 패턴 도입**

* UserLoginRequest, UserSignupRequest 등으로 클래스를 세분화하여
  오버포스팅 방지 및 보안 강화.

**테스트 완료**

* Talend API Tester를 활용하여
  `[가입 → 로그인 → 정보수정 → 로그아웃]`
  전체 시나리오 100% 통과.

---

### 2024.11.28 (Frontend Foundation)

**초기 구조 설정**

* Pinia(tripStore.js) 도입으로 중앙 상태 관리 체계 구축.
* Kakao Map 연동 및 Drag & Drop 일정 관리 UI 구현.

---