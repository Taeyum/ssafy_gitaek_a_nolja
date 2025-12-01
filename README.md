# Trip Backend

## 주요 기능
- 회원 관리: 회원가입, 로그인, 비밀번호 찾기, 정보 수정/탈퇴, remember-me 쿠키 처리
- 여행 일정: `plan.jsp` UI와 `/plan` API를 통한 일정 저장 및 목록 조회(JSON)
- 지역 데이터 API: `/api` 엔드포인트로 시·군·구 및 관광지 검색
- 지도/미디어 뷰: Kakao 지도 연동 `map.jsp`
- 요청 수명주기 보조: `LoggingFilter`, `MyContextListener`

## 기술 스택
- Language: Java 17
- Runtime: Jakarta Servlet 6.0, JSP 4.0, JSTL 3.0
- Build: Maven (`pom.xml`), WAR 패키징
- Database: MySQL 8 (기본 URL `jdbc:mysql://localhost:3306/ssafy_trip`)

**데이터베이스 설정**
- DB 계정과 스키마를 준비해 주세요.
- `ssafy_trip` 스키마를 생성하고 사용자 권한을 부여합니다.
- 필요하면 `src/main/java/com/ssafy/exam/util/DBUtil.java`의 `URL`, `USER`, `PASSWORD` 값을 환경에 맞춰 수정합니다.

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
    *   **노션 링크**: https://www.notion.so/2b2af5125192800fad5acf5bab04a860


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