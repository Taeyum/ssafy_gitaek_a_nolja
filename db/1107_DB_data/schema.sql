use TDD;

CREATE DATABASE IF NOT EXISTS TDD;
USE TDD;

DROP TABLE IF EXISTS `Trip_Participant`;
DROP TABLE IF EXISTS `Chat_Message`;
DROP TABLE IF EXISTS `Chat_Room`;
DROP TABLE IF EXISTS `POI`;
DROP TABLE IF EXISTS `Trip`;
DROP TABLE IF EXISTS `Users`;
DROP TABLE IF EXISTS `Region`;

-- 1. Users (다른 테이블이 참조하는 부모 테이블)
CREATE TABLE `Users` (
    `user_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NULL UNIQUE,
    `password` VARCHAR(255) NULL,
    `nickname` VARCHAR(100) NULL UNIQUE,
    `created_at` DATETIME NULL
);

-- 2. Region (다른 테이블이 참조하며, 자기 자신도 참조)
CREATE TABLE `Region` (
    `region_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `parent_region_id` INT NULL, /* 최상위 지역은 부모가 NULL이어야 하므로 NULL 허용 */
    CONSTRAINT `FK_Region_TO_Region` FOREIGN KEY (`parent_region_id`) REFERENCES `Region` (`region_id`)
);

-- 3. POI (Region을 참조)
CREATE TABLE `POI` (
    `poi_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `address` VARCHAR(255) NULL,
    `latitude` DECIMAL(10, 8) NULL,
    `longitude` DECIMAL(11, 8) NULL,
    `thumbnail_url` VARCHAR(2048) NULL,
    `region_id` INT NOT NULL,
    CONSTRAINT `FK_POI_TO_Region` FOREIGN KEY (`region_id`) REFERENCES `Region` (`region_id`)
);

-- 4. Trip (Users와 Region을 참조)
CREATE TABLE `Trip` (
    `trip_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `host_user_id` INT NOT NULL,
    `max_participants` INT NULL,
    `created_at` DATETIME NULL,
    `region_id` INT NOT NULL,
    CONSTRAINT `FK_Trip_TO_Users_Host` FOREIGN KEY (`host_user_id`) REFERENCES `Users` (`user_id`),
    CONSTRAINT `FK_Trip_TO_Region` FOREIGN KEY (`region_id`) REFERENCES `Region` (`region_id`)
);

-- 5. Chat_Room (Trip을 1:1로 참조)
CREATE TABLE `Chat_Room` (
    `chat_room_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `created_at` DATETIME NULL,
    `trip_id` INT NOT NULL UNIQUE, /* 하나의 Trip은 하나의 채팅방만 가짐 (UNIQUE) */
    CONSTRAINT `FK_Chat_Room_TO_Trip` FOREIGN KEY (`trip_id`) REFERENCES `Trip` (`trip_id`)
        ON DELETE CASCADE /* Trip이 삭제되면 채팅방도 삭제 */
);

-- 6. Chat_Message (Chat_Room과 Users를 참조)
CREATE TABLE `Chat_Message` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `message_type` VARCHAR(20) NULL DEFAULT 'TEXT',
    `content` TEXT NOT NULL,
    `sent_at` DATETIME NULL,
    `chat_room_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    CONSTRAINT `FK_Message_TO_Chat_Room` FOREIGN KEY (`chat_room_id`) REFERENCES `Chat_Room` (`chat_room_id`)
        ON DELETE CASCADE, /* 채팅방이 삭제되면 메시지도 삭제 */
    CONSTRAINT `FK_Message_TO_Users` FOREIGN KEY (`user_id`) REFERENCES `Users` (`user_id`)
);

-- 7. Trip_Participant (Trip과 Users를 참조하는 M:N 테이블)
CREATE TABLE `Trip_Participant` (
    `trip_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `joined_at` DATETIME NULL,
    PRIMARY KEY (`trip_id`, `user_id`), /* 두 컬럼을 묶어 기본 키로 설정 */
    CONSTRAINT `FK_Participant_TO_Trip` FOREIGN KEY (`trip_id`) REFERENCES `Trip` (`trip_id`)
        ON DELETE CASCADE, /* Trip이 삭제되면 참가자 목록도 삭제 */
    CONSTRAINT `FK_Participant_TO_Users` FOREIGN KEY (`user_id`) REFERENCES `Users` (`user_id`)
        ON DELETE CASCADE /* 사용자가 탈퇴하면 참가자 목록에서 삭제 */
);


