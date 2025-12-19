# Trip Backend

## 기술 스택

- Language: Java 17
- Runtime: Jakarta Servlet 6.0, JSP 4.0, JSTL 3.0
- Build: Maven (`pom.xml`), WAR 패키징
- Database: MySQL 8 (기본 URL `jdbc:mysql://localhost:3306/ssafy_trip`)

**데이터베이스 및 환경 설정**

- DB 계정과 스키마를 준비해 주세요.
- `ssafy_trip` 스키마를 생성하고 사용자 권한을 부여합니다.
- application.properties 설정
- TourAPI 인증키 
- tour.api.key="여기에 키 입력" (인코딩 키)
- KorService2
- tour.api.baseUrl=https://apis.data.go.kr/B551011/KorService2
- 이후 데이터 수집 실행 메서드  http://localhost:8080/api/admin/load-data 실행
#

## ERD

- **ERD**

  ![ERD](./img/DB/TDD3.png)

- **Users**: 서비스에 가입한 사용자 계정 정보를 관리하는 기본 테이블입니다.
- **Region**: 여행/관광지가 속한 지역 정보를 관리하고, 자기참조로 시·군·구 같은 계층 구조를 표현합니다.
- **POI**: 관광지(명소)의 이름, 설명, 주소, 좌표, 썸네일과 어느 지역에 속하는지 저장하는 테이블입니다.
- **Trip**: 여행 제목, 기간, 설명, 지역, 호스트, 최대 인원 등 하나의 “여행 방(여행 일정)” 정보를 담는 핵심 엔티티입니다.
- **Chat_Room**: 각 여행계획에 1:1로 매핑되는 채팅방 정보를 저장해, 여행별 채팅 공간을 구분합니다.
- **Trip_Participant**: 어떤 사용자가 어떤 여행에 참가했는지 M:N 관계를 풀어주는 참가자 매핑 테이블입니다.
- **Chat_Message**: 채팅방에서 누가, 언제, 어떤 내용을 보냈는지 메시지 단위로 기록하는 로그 테이블입니다.

5.  **API 명세서 작성**:
    - **노션 링크**: https://www.notion.so/2b2af5125192800fad5acf5bab04a860

### 11월 28일 주요 변경 사항

1. 기능

랜딩 페이지: 시작하기 버튼으로 대시보드 진입.

지도(Map): 카카오맵 연동, 검색 시 이동(moveCamera).

검색: 가짜 데이터 검색 -> 리스트 출력 -> [추가] 버튼 시 모달 오픈.

일정 추가: 모달에서 날짜/시간 선택 -> Pinia Store 저장 -> 자동 정렬.

일정 관리: 드래그 앤 드롭으로 순서 변경, 삭제 기능.

권한: 수정 권한 요청/완료 로직 (UI 제어).

구조 (Pinia 도입 완료):

tripStore.js가 중앙 관제탑 역할을 하며 데이터(itinerary)를 잡고 있습니다.

PlanningDashboard.vue(입력)와 ItineraryList.vue(출력)는 서로 직접 대화하지 않고 Store를 통해서만 소통합니다.

Front-end 작성 중 입니다. 기존의 front는 사용하지 않을 예정입니다.

해당 결과물은 현재 gitaek-anolja-front.zip 에 담겨있습니다.

### 12월 1일 ~ 12월 5일 백엔드_로그인_관련 주요 변경 사항


1. 기능 (Feature)

회원가입, 로그인 (아이디 기억하기 구현.), 내 정보 조회, 로그아웃, 비밀번호 관리 찾기, 임시 비밀번호 발급 및 변경

2. 구조 및 기술 (Structure & Tech)

RESTful API 리팩토링: 기존 '/api/user/signup' 등의 URL을 자원 중심의 '/api/users' (복수형) 및 HTTP Method('POST', 'PUT')로 전면 재설계.

DTO 패턴 도입 (Security): 'UserDto' 하나로 통일하지 않고, 'UserLoginRequest', 'UserSignupRequest' 등으로 클래스를 세분화하여 오버포스팅 방지 및 보안 강화.
-> 이유: 로그인이나 가입마다 필요한 데이터와 검증 규칙이 다른데, 하나로 몽뚤그리면 불필요한 데이터 노출로 인해 해킹 위험이 생길 수도 있고,  코드 수정시 범용성을 고려하여 유지보수를 위함.

model폴더 내 User를  따로 둚.
-> 이유: Model은 DB 테이블과 100% 똑같은 '원본'이라 함부로 건드리면 안 되고, DTO는 화면 요구사항에 따라 막 변해도 되는 포장지 역할을 하기에 분리함.

CORS 설정: Vue.js 연동을 대비하여 'WebMvcConfig' 도입.

3. 테스트 및 현황 (Status)

Postman 설치 이슈 대안으로(박기택 노트북에 윈도우 버전이 안맞아서 설치가 안 됨) 'static/index.html' 및 Talend API Tester 자체 테스트 페이지 구축 완료.

[가입] → [로그인] → [세션 확인] → [비번 변경] → [로그아웃]으로 이어지는 전체 시나리오 테스트 통과 (100% 완료).


4. Vue.js와 최초 연결 성공

: 기존 작업자의 컴퓨터에서 포트번호가 달라서 오류가 떴었으나 이후 포트번호만 'resoureces/application.properties'에서 변경하니 오류없이 로그인, 회원가입, 로그아웃 등 동작 정상작동 확인. 

5. 금일 업데이트 이후, 추가하거나, 수정해야 할 부분 고민

: 현재 최우선 순위인 구현에 초점을 맞추느라, 뷰와 연동성 문제에 대해서 성공을 했지만, 보안 관련해서는 취약하다. 여행 기능을 추가할지, 아니면 로그인 보안 기능을 좀 더 추가할지에 대해 우선순위적으로 판단하는게 맞다고 생각되지만, 우리의 코드와 메이저 로그인 기능 관련 코드(네이버,카카오 등)에 대해 비교 분석해 보았고, Spring Security, 유효성 검증 등 나아가야 할 방향성을 노션 프로젝트/로그인/ 1205에 정리해보았다. 



# Trip Backend

## 기술 스택

- Language: Java 17
- Runtime: Jakarta Servlet 6.0, JSP 4.0, JSTL 3.0
- Build: Maven (`pom.xml`), WAR 패키징
- Database: MySQL 8 (기본 URL `jdbc:mysql://localhost:3306/ssafy_trip`)

**데이터베이스 및 환경 설정**

- DB 계정과 스키마를 준비해 주세요.
- `ssafy_trip` 스키마를 생성하고 사용자 권한을 부여합니다.
- application.properties 설정
- TourAPI 인증키 
- tour.api.key="여기에 키 입력" (인코딩 키)
- KorService2
- tour.api.baseUrl=https://apis.data.go.kr/B551011/KorService2
- 이후 데이터 수집 실행 메서드  http://localhost:8080/api/admin/load-data 실행
#

## ERD

- **ERD**

  ![ERD](./img/DB/TDD3.png)

- **Users**: 서비스에 가입한 사용자 계정 정보를 관리하는 기본 테이블입니다.
- **Region**: 여행/관광지가 속한 지역 정보를 관리하고, 자기참조로 시·군·구 같은 계층 구조를 표현합니다.
- **POI**: 관광지(명소)의 이름, 설명, 주소, 좌표, 썸네일과 어느 지역에 속하는지 저장하는 테이블입니다.
- **Trip**: 여행 제목, 기간, 설명, 지역, 호스트, 최대 인원 등 하나의 “여행 방(여행 일정)” 정보를 담는 핵심 엔티티입니다.
- **Chat_Room**: 각 여행계획에 1:1로 매핑되는 채팅방 정보를 저장해, 여행별 채팅 공간을 구분합니다.
- **Trip_Participant**: 어떤 사용자가 어떤 여행에 참가했는지 M:N 관계를 풀어주는 참가자 매핑 테이블입니다.
- **Chat_Message**: 채팅방에서 누가, 언제, 어떤 내용을 보냈는지 메시지 단위로 기록하는 로그 테이블입니다.

5.  **API 명세서 작성**:
    - **노션 링크**: https://www.notion.so/2b2af5125192800fad5acf5bab04a860

제공해주신 프로젝트 정보와 개발 로그(11월 28일 ~ 12월 19일)를 바탕으로, 요청하신 **'좋은 README'의 9가지 필수 항목**을 완벽하게 갖춘 `README.md`를 작성해 드립니다.

그대로 복사해서 사용하실 수 있도록 마크다운 형식으로 정리했습니다.

---

# ✈️ 기택 아놀자 (Gitaek Anolja)

> **친구들과 함께 실시간으로 떠나는 여행 계획 협업 플랫폼** > "복잡한 엑셀과 지도 앱은 이제 그만! 지도 위에서 친구들과 동시에 여행 코스를 그려보세요."

**기택 아놀자**는 여러 사용자가 하나의 여행 계획을 공유하고 실시간으로 수정할 수 있는 웹 애플리케이션입니다. 공공데이터포털의 관광지 API와 Kakao Map API를 활용하여 직관적인 경로 설계를 지원하며, **JWT 기반의 강력한 보안**, **초대 코드를 통한 간편한 입장**, **동시성 제어(Locking)** 기술을 통해 충돌 없는 협업 경험을 제공합니다.

---

## 📑 목차

1. [프로젝트 설치 및 실행 방법](https://www.google.com/search?q=%23-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%84%A4%EC%B9%98-%EB%B0%8F-%EC%8B%A4%ED%96%89-%EB%B0%A9%EB%B2%95)
2. [기술 스택](https://www.google.com/search?q=%23-%EA%B8%B0%EC%88%A0-%EC%8A%A4%ED%83%9D)
3. [주요 기능 및 사용법 (Usage)](https://www.google.com/search?q=%23-%EC%A3%BC%EC%9A%94-%EA%B8%B0%EB%8A%A5-%EB%B0%8F-%EC%82%AC%EC%9A%A9%EB%B2%95-usage)
4. [프로젝트 상태 및 로드맵](https://www.google.com/search?q=%23-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%83%81%ED%83%9C-%EB%B0%8F-%EB%A1%9C%EB%93%9C%EB%A7%B5)
5. [개발 히스토리 (Change Log)](https://www.google.com/search?q=%23-%EA%B0%9C%EB%B0%9C-%ED%9E%88%EC%8A%A4%ED%86%A0%EB%A6%AC-change-log)
6. [테스트](https://www.google.com/search?q=%23-%ED%85%8C%EC%8A%A4%ED%8A%B8)
7. [기여 방법 (Contributing)](https://www.google.com/search?q=%23-%EA%B8%B0%EC%97%AC-%EB%B0%A9%EB%B2%95-contributing)
8. [저자 및 기여자](https://www.google.com/search?q=%23-%EC%A0%80%EC%9E%90-%EB%B0%8F-%EA%B8%B0%EC%97%AC%EC%9E%90)
9. [라이선스](https://www.google.com/search?q=%23-%EB%9D%BC%EC%9D%B4%EC%84%A0%EC%8A%A4)

---

## 🚀 프로젝트 설치 및 실행 방법

이 프로젝트를 로컬 환경에서 실행하기 위해 다음 단계들을 순서대로 진행해 주세요.

### 1. 사전 요구 사항 (Prerequisites)

* **Java 17** 이상
* **Spring Boot 3.3.5**
* **MySQL 8.0**
* **Node.js** (LTS 버전) & **Vue 3**
* **API Key**: 공공데이터포털(TourAPI), Kakao Map API Key

### 2. 데이터베이스 설정 (Database Setup)

MySQL에 접속하여 스키마를 생성하고, 제공된 SQL 스크립트를 실행합니다.

```sql
-- 스키마 생성
CREATE DATABASE ssafy_trip DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 테이블 및 기초 데이터 생성 (프로젝트 루트의 sql 파일 참조)
source ./ssafy_trip.sql;

```

### 3. 백엔드 설정 (Backend)

`src/main/resources/application.properties` 파일을 본인의 환경에 맞게 수정합니다.

```properties
# MySQL 설정
spring.datasource.url=jdbc:mysql://localhost:3306/ssafy_trip?serverTimezone=UTC&useUnicode=true&characterEncoding=utf8
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

# API Key 설정
tour.api.key=YOUR_TOUR_API_ENCODING_KEY

```

이후 Maven을 통해 프로젝트를 빌드하고 실행합니다.

### 4. 프론트엔드 설정 (Frontend)

프론트엔드 디렉토리로 이동하여 의존성을 설치하고 실행합니다.

```bash
npm install
npm run dev

```

---

## 📖 주요 기능 및 사용법 (Usage)

### 1. 보안이 강화된 회원 관리

* **JWT 인증**: 로그인 시 Access Token을 발급받아 `localStorage`에 저장하며, Axios Interceptor가 모든 요청 헤더에 토큰을 자동 주입합니다.
* **암호화**: `BCrypt`를 사용하여 비밀번호를 암호화하여 저장합니다.

### 2. 여행 그룹 및 초대

* **그룹 생성**: 여행 제목, 기간, 인원을 설정하여 방을 생성합니다.
* **초대 코드 입장**: 생성 시 부여된 **8자리 랜덤 UUID 코드**를 입력하면 검색 없이 즉시 여행 그룹에 합류할 수 있습니다.

### 3. 실시간 동시 편집 (Concurrency Control)

* **편집 권한 요청**: 여러 명이 동시에 수정하여 데이터가 꼬이는 것을 방지하기 위해, **'수정 권한 요청'** 버튼을 눌러 권한(Lock)을 획득한 사용자만 일정을 변경할 수 있습니다.
* **읽기 전용 모드**: 다른 사용자가 수정 중일 때 나머지 인원은 읽기 전용 모드로 전환됩니다.

### 4. 지도 기반 일정 관리

* **관광지 검색 및 추가**: 카카오맵에서 관광지를 검색하고, 마커의 **[+] 버튼**을 눌러 일정에 즉시 추가할 수 있습니다.
* **드래그 앤 드롭**: `vuedraggable`을 활용하여 여행 코스의 순서를 직관적으로 변경할 수 있습니다.

### 5. 관리자(Admin) 기능

* **데이터 수집**: `/api/admin/load-data`를 통해 공공데이터포털의 최신 관광지 정보를 DB에 적재합니다.
* **회원 관리**: 악성 유저 정지, 권한 부여, 비밀번호 초기화 기능을 제공합니다.

---

## 🚧 프로젝트 상태 및 로드맵

**현재 상태 (Status): v1.0 (Stable Beta)**

* 핵심 기능(여행 생성, 지도 연동, 동시성 제어, JWT 보안) 구현 완료.
* 전체 시나리오 테스트 통과.

---

## 📅 개발 히스토리 (Change Log)

### 2024.12.19 (Hotfix & Refactoring)

* **보안 이슈 해결**: 비밀번호 변경 시 클라이언트 측 토큰을 즉시 파기(로그아웃)하도록 원자성 보장.
* **회원 탈퇴 로직 개선**: 방장(Owner)이 탈퇴 시 생성한 여행 그룹을 먼저 삭제(`Cascade`)하도록 트랜잭션 처리하여 FK 오류 해결.
* **로그아웃 버그 수정**: 탈퇴 후 서버로 불필요한 로그아웃 요청을 보내지 않도록 클라이언트 로직 수정.

### 2024.12.17 (Security Overhaul)

* **Spring Security + JWT 도입**: Session 기반에서 Stateless 아키텍처로 전면 전환.
* **Filter Chain 적용**: `JwtAuthenticationFilter`를 통해 모든 API 요청 검증.
* **비밀번호 암호화**: `BCryptPasswordEncoder` 적용.

### 2024.12.14 (Core Features)

* **여행 로직 완성**: 초대 코드(`UUID`) 입장, 기간 자동 계산 로직 구현.
* **동시성 제어**: `current_editor_id`를 활용한 편집 권한 Locking 메커니즘 구현.
* **지도 연동**: Kakao Map Event Bus 구현 및 TourAPI 데이터 적재 로직(`OpenApiService`) 완성.

### 2024.12.09 (Admin Features)

* **관리자 기능**: `AdminController` 분리, 회원 상태 변경(정지/승격), 데이터 수집 기능 구현.

### 2024.12.01 ~ 12.05 (Auth & DTO)

* **REST API 리팩토링**: 자원 중심 URL 설계 (`/api/users`).
* **DTO 패턴 도입**: `UserLoginRequest` 등으로 클래스 세분화하여 오버포스팅 방지.

---

## 🧪 테스트

* **API Test**: `Talend API Tester`를 사용하여 회원가입 → 로그인 → 세션 확인 → 정보 변경 → 로그아웃 시나리오 100% 통과.
* **Frontend Test**: Pinia Store 상태 관리 및 컴포넌트 간 이벤트 전달(Event Bus) 검증 완료.

---

---

## 👤 저자 및 기여자

* **은태현** (Lead Developer)
* **박기택**

---
