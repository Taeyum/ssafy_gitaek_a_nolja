-- ==========================================
-- 1. 데이터베이스 초기화 (주의: 기존 데이터 모두 삭제됨)
-- ==========================================
DROP DATABASE IF EXISTS ssafy_trip;
CREATE DATABASE ssafy_trip DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ssafy_trip;

-- ==========================================
-- 2. 테이블 생성
-- ==========================================

-- [1] 유저 (Users)
CREATE TABLE Users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(100) NOT NULL,
    role        VARCHAR(20) DEFAULT 'USER', -- 초기값 USER, 관리자는 ADMIN
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- [2] 지역 (Region)
CREATE TABLE Region (
    region_id   INT PRIMARY KEY, -- TourAPI 코드와 일치시키기 위해 AUTO_INCREMENT 제거
    name        VARCHAR(100) NOT NULL,
    parent_region_id INT
);

-- [3] 관광지 (POI)
CREATE TABLE POI (
    poi_id          INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    address         VARCHAR(255),
    latitude        DECIMAL(10, 8), -- 위도
    longitude       DECIMAL(11, 8), -- 경도
    thumbnail_url   VARCHAR(2048),
    region_id       INT NOT NULL
);

-- [4] 여행 (Trip) - ★ 수정됨 (current_editor_id 추가)
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
    current_editor_id INT DEFAULT NULL, -- ★ [추가] 현재 수정 중인 유저 ID (동시성 제어용)
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
    poi_id      INT NOT NULL
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

-- POI -> Region
ALTER TABLE POI ADD CONSTRAINT FK_POI_Region FOREIGN KEY (region_id) REFERENCES Region(region_id);

-- Trip -> User (Owner), Region
ALTER TABLE Trip ADD CONSTRAINT FK_Trip_Owner FOREIGN KEY (owner_id) REFERENCES Users(user_id);
ALTER TABLE Trip ADD CONSTRAINT FK_Trip_Region FOREIGN KEY (region_id) REFERENCES Region(region_id);
-- (선택 사항: current_editor_id도 외래키로 걸어두면 유저 삭제 시 안전합니다)
ALTER TABLE Trip ADD CONSTRAINT FK_Trip_Editor FOREIGN KEY (current_editor_id) REFERENCES Users(user_id) ON DELETE SET NULL;

-- Trip_Participant -> Trip, User (ON DELETE CASCADE 추가: 여행 삭제 시 참가자도 자동 삭제)
ALTER TABLE Trip_Participant ADD CONSTRAINT FK_TP_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id) ON DELETE CASCADE;
ALTER TABLE Trip_Participant ADD CONSTRAINT FK_TP_User FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE;

-- Trip_Schedule -> Trip, POI (ON DELETE CASCADE 추가: 여행 삭제 시 일정도 자동 삭제)
ALTER TABLE Trip_Schedule ADD CONSTRAINT FK_TS_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id) ON DELETE CASCADE;
ALTER TABLE Trip_Schedule ADD CONSTRAINT FK_TS_POI FOREIGN KEY (poi_id) REFERENCES POI(poi_id);

-- Chat_Room -> Trip (ON DELETE CASCADE 추가)
ALTER TABLE Chat_Room ADD CONSTRAINT FK_CR_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id) ON DELETE CASCADE;

-- Chat_Message -> Chat_Room, User
ALTER TABLE Chat_Message ADD CONSTRAINT FK_CM_Room FOREIGN KEY (chat_room_id) REFERENCES Chat_Room(chat_room_id) ON DELETE CASCADE;
ALTER TABLE Chat_Message ADD CONSTRAINT FK_CM_User FOREIGN KEY (user_id) REFERENCES Users(user_id);


-- ==========================================
-- 4. 초기 필수 데이터 (Region) 삽입
-- ==========================================
-- TourAPI 지역 코드와 매칭 (서울=1, 인천=2 ... 제주=39)
INSERT INTO Region (region_id, name) VALUES
(1, '서울'),
(2, '인천'),
(3, '대전'),
(4, '대구'),
(5, '광주'),
(6, '부산'),
(7, '울산'),
(8, '세종'),
(31, '경기'),
(32, '강원'),
(33, '충북'),
(34, '충남'),
(35, '경북'),
(36, '경남'),
(37, '전북'),
(38, '전남'),
(39, '제주'),
(0, '전국'); -- 전국 검색용 0번 추가 (선택사항)

-- 테스트용 관리자 계정 (비번: 1234)
INSERT INTO Users (email, password, nickname, role) VALUES 
('admin@ssafy.com', '1234', '관리자', 'ADMIN'),
('ssafy@ssafy.com', '1234', '김싸피', 'USER');

-- 확인
SELECT * FROM Region;
SELECT * FROM Users;