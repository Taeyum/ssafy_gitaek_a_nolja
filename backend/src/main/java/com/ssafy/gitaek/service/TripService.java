package com.ssafy.gitaek.service;

import com.ssafy.gitaek.dto.TripCreateRequest;
import com.ssafy.gitaek.dto.TripScheduleDto;
import com.ssafy.gitaek.mapper.TripMapper;
import com.ssafy.gitaek.model.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TripService {

    @Autowired
    private TripMapper tripMapper;

    @Transactional
    public Trip createTrip(TripCreateRequest request, int ownerId) {
        Trip trip = new Trip();
        trip.setTitle(request.getTitle());

        trip.setStyle(request.getStyle());
        trip.setDescription("");

        trip.setMaxParticipants(request.getMaxMembers());
        trip.setOwnerId(ownerId);

        LocalDate start = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();
        trip.setStartDate(start);
        trip.setEndDate(start.plusDays(request.getDuration() - 1));

        // ★ [추가] 랜덤 초대 코드 생성 (8자리 대문자)
        String randomCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        trip.setInviteCode(randomCode);

        tripMapper.insertTrip(trip);
        tripMapper.insertParticipant(trip.getTripId(), ownerId, "HOST");

        return trip;
    }

    // 초대 코드로 입장하기
    @Transactional
    public Trip joinTripByCode(String code, int userId) throws Exception {
        Trip trip = tripMapper.selectTripByInviteCode(code);
        if (trip == null) {
            throw new Exception("유효하지 않은 초대 코드입니다.");
        }

        // 2. 이미 참가 중인지 확인
        int count = tripMapper.checkParticipant(trip.getTripId(), userId);
        if (count > 0) {
            return trip; // 이미 참가 중이면 그냥 성공 처리
        }

        // 3. 인원 꽉 찼는지 확인 (선택 사항)
        if (trip.getCurrentParticipants() >= trip.getMaxParticipants()) throw new Exception("정원이 초과되었습니다.");

        // 4. 참가자로 등록
        tripMapper.insertParticipant(trip.getTripId(), userId, "MEMBER");
        return trip;
    }


    public List<Trip> getMyTrips(int userId) {
        return tripMapper.selectMyTrips(userId);
    }

    public Trip getTrip(int tripId) { return tripMapper.selectTripById(tripId); }

    @Transactional
    public void deleteTrip(int tripId, int userId) {
        // 1. 여행 정보 가져오기
        Trip trip = tripMapper.selectTripById(tripId);

        if (trip == null) {
            throw new RuntimeException("존재하지 않는 여행입니다.");
        }

        // 2. 방장인지 확인
        if (trip.getOwnerId() != userId) {
            throw new RuntimeException("삭제 권한이 없습니다. (방장만 삭제 가능)");
        }

        // 3. 삭제 진행 (자식 테이블 -> 부모 테이블 순서)
        tripMapper.deleteTripParticipants(tripId);
        // (만약 일정이나 채팅 테이블이 있다면 여기서 같이 지워야 함)
        tripMapper.deleteTrip(tripId);
    }

    @Transactional
    public void addSchedule(TripScheduleDto dto) {
        // 순서(visitOrder)나 날짜(tripDay) 로직이 필요하면 여기서 계산
        // 일단 기본값으로 저장한다고 가정
        if(dto.getTripDay() == 0) dto.setTripDay(1);
        tripMapper.registSchedule(dto);
    }

    public List<TripScheduleDto> getSchedules(int tripId) {
        return tripMapper.selectSchedulesByTripId(tripId);
    }

    @Transactional
    public void deleteSchedule(int tripId, int tripDay, int poiId) {
        tripMapper.deleteSchedule(tripId, tripDay, poiId);
    }

    // 수정 권한 요청 (Lock 걸기)
    @Transactional
    public boolean requestEdit(int tripId, int userId) {
        Trip trip = tripMapper.selectTripById(tripId);

        // 1. 아무도 수정 중이 아니거나, 내가 이미 수정 중이면 -> 승인
        if (trip.getCurrentEditorId() == null || trip.getCurrentEditorId() == 0 || trip.getCurrentEditorId() == userId) {
            tripMapper.updateCurrentEditor(tripId, userId);
            return true;
        }

        // 2. 다른 사람이 수정 중이면 -> 거절
        return false;
    }

    // 수정 종료 (Lock 해제)
    @Transactional
    public void releaseEdit(int tripId, int userId) {
        // 내가 잡고 있을 때만 해제 가능 (혹은 강제 해제 로직 필요 시 수정)
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip != null && trip.getCurrentEditorId() != null && trip.getCurrentEditorId() == userId) {
            tripMapper.updateCurrentEditor(tripId, null); // null로 초기화
        }
    }

    // [추가] 여행 나가기 (멤버용)
    @Transactional
    public void leaveTrip(int tripId, int userId) {
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip == null) throw new RuntimeException("여행이 존재하지 않습니다.");

        // 방장이 나가려고 하면 막음
        if (trip.getOwnerId() == userId) {
            throw new RuntimeException("방장은 나갈 수 없습니다. 여행을 삭제해주세요.");
        }

        tripMapper.deleteParticipant(tripId, userId);
    }
}