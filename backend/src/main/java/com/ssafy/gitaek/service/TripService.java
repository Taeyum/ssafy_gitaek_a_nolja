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
        trip.setMaxParticipants(request.getMaxParticipants());
        trip.setOwnerId(ownerId);
        trip.setRegionId(0);

        // 1. 시작일 설정
        LocalDate start = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();
        trip.setStartDate(start);

        // 2. ★ [핵심 수정] 종료일 결정 로직
        // 프론트에서 endDate를 줬으면 그걸 쓰고, 안 줬으면 duration으로 계산 (안전장치 포함)
        if (request.getEndDate() != null) {
            trip.setEndDate(request.getEndDate());
        } else {
            // 기간이 0이나 음수면 최소 1일로 보정
            int safeDuration = Math.max(1, request.getDuration());
            trip.setEndDate(start.plusDays(safeDuration - 1));
        }

        // 초대 코드 생성
        String randomCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        trip.setInviteCode(randomCode);

        tripMapper.insertTrip(trip);
        tripMapper.insertParticipant(trip.getTripId(), ownerId, "HOST");

        return trip;
    }

    // ... (나머지 코드는 건드릴 필요 없이 그대로 유지) ...
    @Transactional
    public Trip joinTripByCode(String code, int userId) throws Exception {
        Trip trip = tripMapper.selectTripByInviteCode(code);
        if (trip == null) throw new Exception("유효하지 않은 초대 코드입니다.");

        int count = tripMapper.checkParticipant(trip.getTripId(), userId);
        if (count > 0) return trip;

        if (trip.getCurrentParticipants() >= trip.getMaxParticipants()) throw new Exception("정원이 초과되었습니다.");

        tripMapper.insertParticipant(trip.getTripId(), userId, "MEMBER");
        return trip;
    }

    public List<Trip> getMyTrips(int userId) {
        return tripMapper.selectMyTrips(userId);
    }

    public Trip getTrip(int tripId) { return tripMapper.selectTripById(tripId); }

    @Transactional
    public void deleteTrip(int tripId, int userId) {
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip == null) throw new RuntimeException("존재하지 않는 여행입니다.");
        if (trip.getOwnerId() != userId) throw new RuntimeException("삭제 권한이 없습니다.");

        tripMapper.deleteTripParticipants(tripId);
        tripMapper.deleteTrip(tripId);
    }

    @Transactional
    public void addSchedule(TripScheduleDto dto) {
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

    @Transactional
    public boolean requestEdit(int tripId, int userId) {
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip.getCurrentEditorId() == null || trip.getCurrentEditorId() == 0 || trip.getCurrentEditorId() == userId) {
            tripMapper.updateCurrentEditor(tripId, userId);
            return true;
        }
        return false;
    }

    @Transactional
    public void releaseEdit(int tripId, int userId) {
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip != null && trip.getCurrentEditorId() != null && trip.getCurrentEditorId() == userId) {
            tripMapper.updateCurrentEditor(tripId, null);
        }
    }
    
    @Transactional
    public void leaveTrip(int tripId, int userId) {
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip == null) throw new RuntimeException("여행이 존재하지 않습니다.");
        if (trip.getOwnerId() == userId) throw new RuntimeException("방장은 나갈 수 없습니다.");
        tripMapper.deleteParticipant(tripId, userId);
    }
}