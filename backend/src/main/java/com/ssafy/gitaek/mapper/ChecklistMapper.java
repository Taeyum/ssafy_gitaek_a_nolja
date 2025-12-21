package com.ssafy.gitaek.mapper;

import com.ssafy.gitaek.dto.ChecklistDto;
import com.ssafy.gitaek.dto.PlanSimpleDto; 
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChecklistMapper {
    List<ChecklistDto> selectList(@Param("userId") int userId, @Param("planId") int planId);
    void insertItem(ChecklistDto dto);
    
    void deleteItem(@Param("checkId") int checkId, @Param("userId") int userId);
    void toggleItem(@Param("checkId") int checkId, @Param("userId") int userId);

    void deleteAllItems(@Param("userId") int userId, @Param("planId") int planId);
    void updateAllCheck(@Param("userId") int userId, @Param("planId") int planId);

    // 내 여행 목록 가져오기
    List<PlanSimpleDto> selectMyPlans(int userId);
}