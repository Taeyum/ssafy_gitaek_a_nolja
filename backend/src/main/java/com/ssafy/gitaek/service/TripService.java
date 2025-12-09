package com.ssafy.gitaek.service;

import com.ssafy.gitaek.dto.TripCreateRequest;
import com.ssafy.gitaek.mapper.TripMapper;
import com.ssafy.gitaek.model.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

        tripMapper.insertTrip(trip);
        tripMapper.insertParticipant(trip.getTripId(), ownerId, "HOST");

        return trip;
    }

    public List<Trip> getMyTrips(int userId) {
        return tripMapper.selectMyTrips(userId);
    }

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
}