-- ==========================================
-- 1. 데이터베이스 초기화
-- ==========================================
DROP DATABASE IF EXISTS ssafy_trip;
CREATE DATABASE ssafy_trip DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ssafy_trip;

-- ==========================================
-- 2. 테이블 생성
-- ==========================================

-- [1] 유저 (Users) - ★ phone_number 추가됨!
CREATE TABLE Users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),  -- ★ 자바 코드와 맞추기 위해 추가
    role        VARCHAR(20) DEFAULT 'USER',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- [2] 지역 (Region)
CREATE TABLE Region (
    region_id   INT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    parent_region_id INT
);

-- [3] 관광지 (POI)
CREATE TABLE POI (
    poi_id          INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    address         VARCHAR(255),
    latitude        DECIMAL(10, 8),
    longitude       DECIMAL(11, 8),
    thumbnail_url   VARCHAR(2048),
    region_id       INT NOT NULL
);

-- [4] 여행 (Trip)
CREATE TABLE Trip (
    trip_id           INT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    style             VARCHAR(50),
    description       TEXT,
    start_date        DATE NOT NULL,
    end_date          DATE NOT NULL,
    owner_id          INT NOT NULL,
    max_participants  INT DEFAULT 4,
    region_id         INT NOT NULL,
    current_editor_id INT DEFAULT NULL, 
    invite_code       VARCHAR(50) DEFAULT NULL,
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- [5] 여행 참가자 (Trip_Participant)
CREATE TABLE Trip_Participant (
    trip_id     INT NOT NULL,
    user_id     INT NOT NULL,
    joined_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    role        VARCHAR(20) DEFAULT 'MEMBER',
    PRIMARY KEY (trip_id, user_id)
);

-- [6] 여행 일정 상세 (Trip_Schedule)
CREATE TABLE Trip_Schedule (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_day    INT,
    visit_order INT,
    memo        TEXT,
    trip_id     INT NOT NULL,
    poi_id      INT NOT NULL,
    schedule_time VARCHAR(10) DEFAULT '12:00'
);

-- [7] 채팅방 (Chat_Room)
CREATE TABLE Chat_Room (
    chat_room_id INT AUTO_INCREMENT PRIMARY KEY,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    trip_id      INT NOT NULL
);

-- [8] 채팅 메시지 (Chat_Message)
CREATE TABLE Chat_Message (
    message_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    content      TEXT NOT NULL,
    sent_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    chat_room_id INT NOT NULL,
    user_id      INT NOT NULL
);

-- ==========================================
-- 3. 외래키(Foreign Key) 설정
-- ==========================================

ALTER TABLE POI ADD CONSTRAINT FK_POI_Region FOREIGN KEY (region_id) REFERENCES Region(region_id);

ALTER TABLE Trip ADD CONSTRAINT FK_Trip_Owner FOREIGN KEY (owner_id) REFERENCES Users(user_id);
ALTER TABLE Trip ADD CONSTRAINT FK_Trip_Region FOREIGN KEY (region_id) REFERENCES Region(region_id);
ALTER TABLE Trip ADD CONSTRAINT FK_Trip_Editor FOREIGN KEY (current_editor_id) REFERENCES Users(user_id) ON DELETE SET NULL;

ALTER TABLE Trip_Participant ADD CONSTRAINT FK_TP_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id) ON DELETE CASCADE;
ALTER TABLE Trip_Participant ADD CONSTRAINT FK_TP_User FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE;

ALTER TABLE Trip_Schedule ADD CONSTRAINT FK_TS_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id) ON DELETE CASCADE;
ALTER TABLE Trip_Schedule ADD CONSTRAINT FK_TS_POI FOREIGN KEY (poi_id) REFERENCES POI(poi_id);

ALTER TABLE Chat_Room ADD CONSTRAINT FK_CR_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id) ON DELETE CASCADE;

ALTER TABLE Chat_Message ADD CONSTRAINT FK_CM_Room FOREIGN KEY (chat_room_id) REFERENCES Chat_Room(chat_room_id) ON DELETE CASCADE;
ALTER TABLE Chat_Message ADD CONSTRAINT FK_CM_User FOREIGN KEY (user_id) REFERENCES Users(user_id);


-- ==========================================
-- 4. 초기 데이터 삽입
-- ==========================================
INSERT INTO Region (region_id, name) VALUES
(1, '서울'), (2, '인천'), (3, '대전'), (4, '대구'), (5, '광주'), (6, '부산'), (7, '울산'), (8, '세종'),
(31, '경기'), (32, '강원'), (33, '충북'), (34, '충남'), (35, '경북'), (36, '경남'), (37, '전북'), (38, '전남'), (39, '제주'),
(0, '전국');

-- 기본 관리자 및 테스트 계정 생성
-- (비밀번호는 암호화되지 않은 예시입니다. 실제 로그인 시에는 암호화된 값이 필요할 수 있습니다)
--INSERT INTO Users (email, password, nickname, phone_number, role) VALUES 
--('admin@ssafy.com', '$2a$10$YourEncryptedPasswordHere', '관리자', '010-0000-0000', 'ADMIN'), 
--('ssafy@ssafy.com', '$2a$10$YourEncryptedPasswordHere', '김싸피', '010-1234-5678', 'USER');

-- ★ [추가 요청] 테스트 계정을 강제로 ADMIN으로 승격시키기
-- (만약 위 INSERT 문에 admin@test.com이 없다면 0 rows affected가 뜨겠지만, 
--  나중에 회원가입 후 이 줄만 따로 실행하면 관리자가 됩니다.)
--UPDATE Users SET role = 'ADMIN' WHERE email = 'admin@test.com';