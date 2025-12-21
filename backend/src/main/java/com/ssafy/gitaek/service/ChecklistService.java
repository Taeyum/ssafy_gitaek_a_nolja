package com.ssafy.gitaek.service;

import com.ssafy.gitaek.dto.ChecklistDto;
import com.ssafy.gitaek.dto.PlanSimpleDto; 
import com.ssafy.gitaek.mapper.ChecklistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChecklistService {
    @Autowired
    private ChecklistMapper checklistMapper;

    // --- 기존 로직들 ---
    public List<ChecklistDto> getMyList(int userId, int planId) {
        return checklistMapper.selectList(userId, planId);
    }

    public void addItem(int userId, int planId, String content) {
        ChecklistDto dto = new ChecklistDto();
        dto.setUserId(userId);
        dto.setPlanId(planId); 
        dto.setContent(content);
        checklistMapper.insertItem(dto);
    }

    public void deleteItem(int checkId, int userId) {
        checklistMapper.deleteItem(checkId, userId);
    }

    public void toggleItem(int checkId, int userId) {
        checklistMapper.toggleItem(checkId, userId);
    }

    public void deleteAll(int userId, int planId) {
        checklistMapper.deleteAllItems(userId, planId);
    }

    public void checkAll(int userId, int planId) {
        checklistMapper.updateAllCheck(userId, planId);
    }
    

    // 메서드
    public List<PlanSimpleDto> getMyPlans(int userId) {
        return checklistMapper.selectMyPlans(userId);
    }
}