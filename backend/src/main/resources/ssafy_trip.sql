-- 1. 데이터베이스 초기화 (기존꺼 삭제 후 재생성)
DROP DATABASE IF EXISTS ssafy_trip;
CREATE DATABASE ssafy_trip DEFAULT CHARACTER SET utf8mb4;
USE ssafy_trip;

-- ==========================================
-- 2. 테이블 생성 (순서 중요: 부모 테이블 먼저)
-- ==========================================

-- [1] 유저 (Users) - 가장 먼저 생성
CREATE TABLE Users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,  -- ★ AUTO_INCREMENT 필수
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(100) NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- [2] 지역 (Region)
CREATE TABLE Region (
    region_id   INT AUTO_INCREMENT PRIMARY KEY, -- ★ AUTO_INCREMENT
    name        VARCHAR(100) NOT NULL,
    parent_region_id INT
);

-- [3] 관광지 (POI)
CREATE TABLE POI (
    poi_id          INT AUTO_INCREMENT PRIMARY KEY, -- ★ AUTO_INCREMENT
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
    trip_id         INT AUTO_INCREMENT PRIMARY KEY, -- ★ AUTO_INCREMENT
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    owner_id        INT NOT NULL,   -- 방장 (User FK)
    max_participants INT DEFAULT 4,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    region_id       INT NOT NULL
);

-- [5] 여행 참가자 (Trip_Participant) - M:N 관계
CREATE TABLE Trip_Participant (
    trip_id     INT NOT NULL,
    user_id     INT NOT NULL,
    joined_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    role        VARCHAR(20) DEFAULT 'MEMBER', -- 방장(HOST)인지 구분
    PRIMARY KEY (trip_id, user_id)
);

-- [6] 여행 일정 상세 (Trip_Schedule)
CREATE TABLE Trip_Schedule (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- ★ AUTO_INCREMENT
    trip_day    INT,            -- 1일차, 2일차...
    visit_order INT,            -- 방문 순서
    memo        TEXT,
    trip_id     INT NOT NULL,
    poi_id      INT NOT NULL
);

-- [7] 채팅방 (Chat_Room)
CREATE TABLE Chat_Room (
    chat_room_id INT AUTO_INCREMENT PRIMARY KEY, -- ★ AUTO_INCREMENT
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    trip_id      INT NOT NULL
);

-- [8] 채팅 메시지 (Chat_Message)
CREATE TABLE Chat_Message (
    message_id   BIGINT AUTO_INCREMENT PRIMARY KEY, -- ★ AUTO_INCREMENT
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

-- Trip_Participant -> Trip, User
ALTER TABLE Trip_Participant ADD CONSTRAINT FK_TP_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id);
ALTER TABLE Trip_Participant ADD CONSTRAINT FK_TP_User FOREIGN KEY (user_id) REFERENCES Users(user_id);

-- Trip_Schedule -> Trip, POI
ALTER TABLE Trip_Schedule ADD CONSTRAINT FK_TS_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id);
ALTER TABLE Trip_Schedule ADD CONSTRAINT FK_TS_POI FOREIGN KEY (poi_id) REFERENCES POI(poi_id);

-- Chat_Room -> Trip
ALTER TABLE Chat_Room ADD CONSTRAINT FK_CR_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id);

-- Chat_Message -> Chat_Room, User
ALTER TABLE Chat_Message ADD CONSTRAINT FK_CM_Room FOREIGN KEY (chat_room_id) REFERENCES Chat_Room(chat_room_id);
ALTER TABLE Chat_Message ADD CONSTRAINT FK_CM_User FOREIGN KEY (user_id) REFERENCES Users(user_id);