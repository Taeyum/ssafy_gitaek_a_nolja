-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';


-- -----------------------------------------------------
-- Schema ssafy_trip
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `ssafy_trip` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `ssafy_trip` ;

-- -----------------------------------------------------
-- Table `ssafy_trip`.`sidos`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_trip`.`sidos` ;

CREATE TABLE IF NOT EXISTS `ssafy_trip`.`sidos` (
  `no` int NOT NULL AUTO_INCREMENT  comment '시도번호',
  `sido_code` int NOT NULL comment '시도코드',
  `sido_name` varchar(20) DEFAULT NULL comment '시도이름',
  PRIMARY KEY (`no`),
  UNIQUE INDEX `sido_code_UNIQUE` (`sido_code` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 18
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci comment '시도정보테이블';


-- -----------------------------------------------------
-- Table `ssafy_trip`.`guguns`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_trip`.`guguns` ;

CREATE TABLE IF NOT EXISTS `ssafy_trip`.`guguns` (
  `no` int NOT NULL AUTO_INCREMENT comment '구군번호',
  `sido_code` int NOT NULL comment '시도코드',
  `gugun_code` int NOT NULL comment '구군코드',
  `gugun_name` varchar(20) DEFAULT NULL comment '구군이름',
  PRIMARY KEY (`no`),
  INDEX `guguns_sido_to_sidos_cdoe_fk_idx` (`sido_code` ASC) VISIBLE,
  INDEX `gugun_code_idx` (`gugun_code` ASC) VISIBLE,
  CONSTRAINT `guguns_sido_to_sidos_cdoe_fk`
    FOREIGN KEY (`sido_code`)
    REFERENCES `ssafy_trip`.`sidos` (`sido_code`))
ENGINE = InnoDB
AUTO_INCREMENT = 235
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
comment '구군정보테이블';


-- -----------------------------------------------------
-- Table `ssafy_trip`.`contenttypes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_trip`.`contenttypes` ;

CREATE TABLE IF NOT EXISTS `ssafy_trip`.`contenttypes` (
  `content_type_id` int NOT NULL comment '콘텐츠타입번호',
  `content_type_name` varchar(45) DEFAULT NULL comment '콘텐츠타입이름',
  PRIMARY KEY (`content_type_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci comment '콘텐츠타입정보테이블';


-- -----------------------------------------------------
-- Table `ssafy_trip`.`attractions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_trip`.`attractions` ;

CREATE TABLE IF NOT EXISTS `ssafy_trip`.`attractions` (
  `no` int NOT NULL AUTO_INCREMENT  comment '명소코드',
  `content_id` int DEFAULT NULL comment '콘텐츠번호',
  `title` varchar(500) DEFAULT NULL comment '명소이름',
  `content_type_id` int DEFAULT NULL comment '콘텐츠타입',
  `area_code` int DEFAULT NULL comment '시도코드',
  `si_gun_gu_code` int DEFAULT NULL comment '구군코드',
  `first_image1` varchar(100) DEFAULT NULL comment '이미지경로1',
  `first_image2` varchar(100) DEFAULT NULL comment '이미지경로2',
  `map_level` int DEFAULT NULL comment '줌레벨',
  `latitude` decimal(20,17) DEFAULT NULL comment '위도',
  `longitude` decimal(20,17) DEFAULT NULL comment '경도',
  `tel` varchar(20) DEFAULT NULL comment '전화번호',
  `addr1` varchar(100) DEFAULT NULL comment '주소1',
  `addr2` varchar(100) DEFAULT NULL comment '주소2',
  `homepage` varchar(1000) DEFAULT NULL comment '홈페이지',
  `overview` varchar(10000) DEFAULT NULL comment '설명',
  PRIMARY KEY (`no`),
  INDEX `attractions_typeid_to_types_typeid_fk_idx` (`content_type_id` ASC) VISIBLE,
  INDEX `attractions_sido_to_sidos_code_fk_idx` (`area_code` ASC) VISIBLE,
  INDEX `attractions_sigungu_to_guguns_gugun_fk_idx` (`si_gun_gu_code` ASC) VISIBLE,
  CONSTRAINT `attractions_area_to_sidos_code_fk`
    FOREIGN KEY (`area_code`)
    REFERENCES `ssafy_trip`.`sidos` (`sido_code`),
  CONSTRAINT `attractions_sigungu_to_guguns_gugun_fk`
    FOREIGN KEY (`si_gun_gu_code`)
    REFERENCES `ssafy_trip`.`guguns` (`gugun_code`),
  CONSTRAINT `attractions_typeid_to_types_typeid_fk`
    FOREIGN KEY (`content_type_id`)
    REFERENCES `ssafy_trip`.`contenttypes` (`content_type_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 56644
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
comment '명소정보테이블';


-- -----------------------------------------------------
-- Table `ssafy_trip`.`Member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_trip`.`Member` ;

CREATE TABLE IF NOT EXISTS `ssafy_trip`.`Member` (
  `mno` INT NOT NULL AUTO_INCREMENT COMMENT '회원번호',
  `name` VARCHAR(100) NOT NULL COMMENT '이름',
  `email` VARCHAR(100) NOT NULL COMMENT '이메일',
  `password` VARCHAR(255) NOT NULL COMMENT '비밀번호',
  `role` VARCHAR(50) NULL DEFAULT 'USER' COMMENT '역할',
  PRIMARY KEY (`mno`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '회원정보테이블';


-- -----------------------------------------------------
-- Table `ssafy_trip`.`trip_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_trip`.`trip_plan` ;

CREATE TABLE IF NOT EXISTS `ssafy_trip`.`trip_plan` (
  `plan_id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  `budget` VARCHAR(50) NULL,
  `memo` TEXT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`plan_id`))
ENGINE = InnoDB
COMMENT = '여행계획테이블';


-- -----------------------------------------------------
-- Table `ssafy_trip`.`trip_plan_item`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_trip`.`trip_plan_item` ;

CREATE TABLE IF NOT EXISTS `ssafy_trip`.`trip_plan_item` (
  `plan_item_id` INT NOT NULL AUTO_INCREMENT,
  `plan_id` INT NOT NULL,
  `order_index` INT NOT NULL,
  `place_id` VARCHAR(50) NULL,
  `place_title` VARCHAR(200) NULL,
  `address` VARCHAR(200) NULL,
  `visit_day` DATE NULL,
  `start_time` VARCHAR(10) NULL,
  `end_time` VARCHAR(10) NULL,
  `memo` TEXT NULL,
  `mapx` DOUBLE NULL,
  `mapy` DOUBLE NULL,
  `local_id` VARCHAR(64) NULL,
  PRIMARY KEY (`plan_item_id`),
  INDEX `fk_trip_plan_item_plan_idx` (`plan_id` ASC) VISIBLE,
  CONSTRAINT `fk_trip_plan_item_plan`
    FOREIGN KEY (`plan_id`)
    REFERENCES `ssafy_trip`.`trip_plan` (`plan_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
COMMENT = '여행계획항목테이블';


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;


