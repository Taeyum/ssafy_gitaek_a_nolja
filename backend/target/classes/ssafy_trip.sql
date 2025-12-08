DROP DATABASE IF EXISTS ssafy_trip;
CREATE DATABASE ssafy_trip DEFAULT CHARACTER SET utf8mb4;
USE ssafy_trip;

CREATE TABLE Users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(100) NOT NULL,
    role        VARCHAR(20) DEFAULT 'ROLE_USER', -- ★ 추가됨 (ROLE_USER, ROLE_ADMIN)
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

<<<<<<< HEAD
-- [방법 2] 이미 테이블이 있다면 (이거 실행):
ALTER TABLE Users ADD COLUMN role VARCHAR(20) DEFAULT 'ROLE_USER';

-- [2] 지역 (Region)
=======
>>>>>>> b4014e7a4cba316cdf9c5536248f3c8e5c92d9e3
CREATE TABLE Region (
    region_id   INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    parent_region_id INT
);

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

CREATE TABLE Trip (
    trip_id         INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    owner_id        INT NOT NULL,
    max_participants INT DEFAULT 4,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    region_id       INT NOT NULL
);

CREATE TABLE Trip_Participant (
    trip_id     INT NOT NULL,
    user_id     INT NOT NULL,
    joined_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    role        VARCHAR(20) DEFAULT 'MEMBER',
    PRIMARY KEY (trip_id, user_id)
);

CREATE TABLE Trip_Schedule (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_day    INT,
    visit_order INT,
    memo        TEXT,
    trip_id     INT NOT NULL,
    poi_id      INT NOT NULL
);

CREATE TABLE Chat_Room (
    chat_room_id INT AUTO_INCREMENT PRIMARY KEY,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    trip_id      INT NOT NULL
);

CREATE TABLE Chat_Message (
    message_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    content      TEXT NOT NULL,
    sent_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    chat_room_id INT NOT NULL,
    user_id      INT NOT NULL
);

ALTER TABLE POI ADD CONSTRAINT FK_POI_Region FOREIGN KEY (region_id) REFERENCES Region(region_id);

ALTER TABLE Trip ADD CONSTRAINT FK_Trip_Owner FOREIGN KEY (owner_id) REFERENCES Users(user_id);
ALTER TABLE Trip ADD CONSTRAINT FK_Trip_Region FOREIGN KEY (region_id) REFERENCES Region(region_id);

ALTER TABLE Trip_Participant ADD CONSTRAINT FK_TP_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id);
ALTER TABLE Trip_Participant ADD CONSTRAINT FK_TP_User FOREIGN KEY (user_id) REFERENCES Users(user_id);

ALTER TABLE Trip_Schedule ADD CONSTRAINT FK_TS_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id);
ALTER TABLE Trip_Schedule ADD CONSTRAINT FK_TS_POI FOREIGN KEY (poi_id) REFERENCES POI(poi_id);

ALTER TABLE Chat_Room ADD CONSTRAINT FK_CR_Trip FOREIGN KEY (trip_id) REFERENCES Trip(trip_id);

ALTER TABLE Chat_Message ADD CONSTRAINT FK_CM_Room FOREIGN KEY (chat_room_id) REFERENCES Chat_Room(chat_room_id);
ALTER TABLE Chat_Message ADD CONSTRAINT FK_CM_User FOREIGN KEY (user_id) REFERENCES Users(user_id);