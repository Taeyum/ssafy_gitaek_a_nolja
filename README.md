
# ✈️ Trip Backend (Gitaek Anolja)

> **친구들과 함께 실시간으로 떠나는 여행 계획 협업 플랫폼**
> "복잡한 엑셀과 지도 앱은 이제 그만! 지도 위에서 친구들과 동시에 여행 코스를 그려보세요."

**Trip Backend**는 Vue.js 프론트엔드와 연동되어 작동하는 Spring Boot 기반의 백엔드 프로젝트입니다. 공공데이터포털의 관광지 API(TourAPI)와 Kakao Map API를 활용하여 직관적인 경로 설계를 지원하며, **JWT 기반의 보안**, **RESTful API 설계**, 그리고 **데이터 동시성 제어**를 통해 안정적인 협업 경험을 제공합니다.

---

## 🛠 프로젝트 소개 및 기술 스택

### 백엔드 (Backend)

* **Language**: Java 17
* **Framework**: Spring Boot 3.3.5
* **Build Tool**: Maven
* **Database**: MySQL 8.0
* **Libraries**: Spring Security, JWT, Lombok, MyBatis (or JPA)

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

* **은태현** - *Frontend & Trip Backend & AI*
* **박기택** - *User Backend & Document*

---
