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

## 서비스 화면
- **서비스화면**  
  ![서비스화면](./img/메인/메인.png)
- **로그인페이지**  
  ![로그인페이지](./img/메인/로그인페이지.png)
- **회원가입**  
  ![회원가입](./img/메인/회원가입.png)
- **관리자로그인**  
  ![관리자로그인](./img/메인/관리자로그인.png)
- **로그아웃 및 쿠키 처리**  
  ![로그아웃](./img/메인/로그아웃.png)
- **비밀번호 찾기**  
  ![비밀번호 찾기](./img/메인/비밀번호%20찾기.png)
- **회원 목록 조회**  
  ![회원목록조회](./img/메인/회원목록조회.png)
- **멤버 DB**  
  ![멤버DB](./img/메인/멤버DB.png)

## 서비스 조회 화면
- **관광지 조회**  
  ![관광지조회](./img/관광지조회/관광지조회.png)
- **나만의 여행 계획**  
  ![나만의여행계획](./img/관광지조회/나만의%20여행계획.png)
- **여행 계획 저장**  
  ![여행계획저장](./img/관광지조회/여행%20계획%20저장.png)
- **여행 계획 DB**  
  ![여행계획DB](./img/관광지조회/여행계획DB.png)
- **여행 계획 정보 DB**  
  ![여행계획정보DB](./img/관광지조회/여행세부계획DB.png)

## Usecase

- **기본관리**

  ![기본관리](./img/usecase/기본관리.png)

- **관광지조회**

  ![관광지조회](./img/usecase/관광지조회.png)

- **여행계획**
  
  ![여행계획](./img/usecase/여행계획.png)

## 클래스 다이어그램

- **Class UML**

   ![UML](./img/uml/uml.png)


## ERD

- **ERD**

  ![ERD](./img/DB/TDD3.png)