package com.ssafy.gitaek.mapper;

import com.ssafy.gitaek.dto.TripScheduleDto;
import com.ssafy.gitaek.model.Trip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TripMapper {
    // 1. 여행 생성 (Trip 테이블 저장)
    int insertTrip(Trip trip);

    // 2. 참여자 추가 (방장을 첫 번째 멤버로 추가)
    void insertParticipant(@Param("tripId") int tripId, @Param("userId") int userId, @Param("role") String role);

    List<Trip> selectMyTrips(int userId);

    Trip selectTripById(int tripId);
    void deleteTripParticipants(int tripId);
    void deleteTrip(int tripId);

    // 여행 일정에 추가
    void registSchedule(TripScheduleDto tripScheduleDto);
    List<TripScheduleDto> selectSchedulesByTripId(int tripId);

    // 삭제 메서드 (파라미터가 여러 개라 @Param 사용)
    void deleteSchedule(@Param("tripId") int tripId,
                        @Param("tripDay") int tripDay,
                        @Param("poiId") int poiId);

    void updateCurrentEditor(@Param("tripId") int tripId, @Param("userId") Integer userId);

    Trip selectTripByInviteCode(String inviteCode);
    int checkParticipant(@Param("tripId") int tripId, @Param("userId") int userId);

    // [추가] 멤버 나가기
    void deleteParticipant(@Param("tripId") int tripId, @Param("userId") int userId);
}