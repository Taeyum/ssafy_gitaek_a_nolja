use TDD;

-- 외래 키 제약 조건을 잠시 비활성화 (테이블 초기화 순서와 관계없이 실행하기 위함)
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 테이블 데이터 초기화 (TRUNCATE)
TRUNCATE TABLE Users;
TRUNCATE TABLE Region;
TRUNCATE TABLE POI;
TRUNCATE TABLE Trip;
TRUNCATE TABLE Chat_Room;
TRUNCATE TABLE Trip_Participant;
TRUNCATE TABLE Chat_Message;

-- 외래 키 제약 조건을 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 사용자 (Users)
INSERT INTO Users (user_id, email, password, nickname, created_at) VALUES
(1, 'cheolsu@example.com', 'hashed_password_123', '여행가철수', '2025-11-01 10:00:00'),
(2, 'younghee@example.com', 'hashed_password_123', '뚜벅이영희', '2025-11-02 11:00:00'),
(3, 'minsu@example.com', 'hashed_password_123', '맛집탐방민수', '2025-11-03 12:00:00');

-- 2. 지역 (Region) - (계층 구조 포함)
INSERT INTO Region (region_id, name, parent_region_id) VALUES
(1, '대구광역시', NULL),
(2, '경상북도', NULL),
(3, '서울광역시', NULL),
(10, '중구', 1),   -- 대구광역시 중구
(11, '남구', 1),   -- 대구광역시 남구
(20, '경주시', 2),  -- 경상북도 경주시
(30, '종로구', 3);  -- 서울특별시 종로구

-- 3. 관광지 (POI)
INSERT INTO POI (poi_id, name, description, address, latitude, longitude, region_id) VALUES
(101, '동성로 스파크', '대구 시내 중심가의 복합 쇼핑몰 및 관람차', '대구광역시 중구 동성로 6길 61', 35.8689, 128.5997, 10), -- 대구 중구
(102, '앞산전망대', '대구 시내를 한눈에 볼 수 있는 야경 명소', '대구광역시 남구 앞산순환로 453', 35.8354, 128.5830, 11), -- 대구 남구
(201, '불국사', '신라 시대의 찬란한 불교 문화를 간직한 사찰', '경상북도 경주시 불국로 385', 35.7903, 129.3320, 20), -- 경주
(202, '황리단길', '경주의 구시가지에 위치한 특색 있는 카페와 상점 거리', '경상북도 경주시 포석로 1080', 35.8380, 129.2132, 20); -- 경주

-- 4. 여행 (Trip) - (방장: 철수, 영희)
INSERT INTO Trip (trip_id, title, description, start_date, end_date, region_id, host_user_id, max_participants, created_at) VALUES
(501, '[대구] 12월 10일, 앞산 야경 보러가실 분!', '12월 10일 저녁에 앞산전망대 같이 올라가실 분 구합니다. 케이블카 타고 가요!', '2025-12-10', '2025-12-10', 1, 1, 4, '2025-11-05 14:00:00'), -- 방장: 철수(1), 지역: 대구(1)
(502, '경주 1박 2일, 황리단길/불국사 같이 투어해요', '12월 12~13일 경주 여행합니다. 20대 여성분만!', '2025-12-12', '2025-12-13', 20, 2, 3, '2025-11-06 17:00:00'); -- 방장: 영희(2), 지역: 경주(20)


-- 5. 채팅방 (Chat_Room) - (Trip 생성과 동시에 생성되어야 함)
INSERT INTO Chat_Room (chat_room_id, trip_id, created_at) VALUES
(701, 501, '2025-11-05 14:00:01'), -- 대구 야경 여행(501) 채팅방
(702, 502, '2025-11-06 17:00:01'); -- 경주 1박 2일(502) 채팅방

-- 6. 여행 참가자 (Trip_Participant) - (M:N 관계)
-- (방장은 자동으로 참가되도록 로직을 짜는 것이 좋지만, 여기서는 수동 삽입)

-- 501번 여행 (방장: 철수, 참가자: 영희)
INSERT INTO Trip_Participant (trip_id, user_id, joined_at) VALUES
(501, 1, '2025-11-05 14:00:01'), -- 방장 철수(1) 참가
(501, 2, '2025-11-06 09:00:00'); -- 참가자 영희(2) 참가

-- 502번 여행 (방장: 영희, 참가자: 민수)
INSERT INTO Trip_Participant (trip_id, user_id, joined_at) VALUES
(502, 2, '2025-11-06 17:00:01'), -- 방장 영희(2) 참가
(502, 3, '2025-11-07 10:00:00'); -- 참가자 민수(3) 참가

-- 7. 채팅 메시지 (Chat_Message)
-- (701번 채팅방: 대구 야경 여행)
INSERT INTO Chat_Message (message_id, chat_room_id, user_id, message_type, content, sent_at) VALUES
(1001, 701, 1, 'TEXT', '안녕하세요! 10일 저녁 7시에 케이블카 앞에서 뵐까요?', '2025-11-06 09:01:00'), -- 철수
(1002, 701, 2, 'TEXT', '네! 좋아요. 날씨 추울까요?', '2025-11-06 09:02:00'), -- 영희
(1003, 701, 1, 'TEXT', '아마 밤이라 꽤 추울 것 같아요. 핫팩 챙겨오세요!', '2025-11-06 09:03:00'); -- 철수

-- (702번 채팅방: 경주 여행)
INSERT INTO Chat_Message (message_id, chat_room_id, user_id, message_type, content, sent_at) VALUES
(1004, 702, 2, 'TEXT', '안녕하세요 민수님! 12일 KTX역에서 10시에 뵙겠습니다 :)', '2025-11-07 10:01:00'), -- 영희
(1005, 702, 3, 'TEXT', '네 방장님! 저도 시간 맞춰 가겠습니다. 기대되네요!', '2025-11-07 10:05:00'); -- 민수

COMMIT;

SELECT
    t.title AS '여행 이름',
    u.nickname AS '참가자 닉네임',
    tp.joined_at AS '가입일'
FROM
    Trip_Participant AS tp
JOIN
    Trip AS t ON tp.trip_id = t.trip_id
JOIN
    Users AS u ON tp.user_id = u.user_id
WHERE
    t.trip_id = 501; -- 501번 = '대구 야경' 여행
    
SELECT
    t.title AS '여행 이름',
    u.nickname AS '작성자',
    cm.content AS '메시지 내용',
    cm.sent_at AS '전송 시간'
FROM
    Trip AS t
JOIN
    Chat_Room AS cr ON t.trip_id = cr.trip_id
JOIN
    Chat_Message AS cm ON cr.chat_room_id = cm.chat_room_id
JOIN
    Users AS u ON cm.user_id = u.user_id
WHERE
    t.trip_id = 502; -- 502번 = '경주 1박 2일' 여행


SELECT
    parent.name AS '상위 지역',
    child.name AS '하위 지역',
    p.name AS '관광지명'
FROM
    Region AS parent
JOIN
    Region AS child ON parent.region_id = child.parent_region_id
LEFT JOIN -- (관광지가 없는 '하위 지역'도 표시하기 위해 LEFT JOIN)
    POI AS p ON child.region_id = p.region_id
WHERE
    parent.name = '대구광역시';