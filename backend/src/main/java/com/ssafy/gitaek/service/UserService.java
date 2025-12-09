package com.ssafy.gitaek.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssafy.gitaek.mapper.UserMapper;
import com.ssafy.gitaek.model.User;

@Service
public class UserService {

	@Autowired
	private UserMapper userMapper;

	public void signup(User user) {
		userMapper.insertUser(user);
	}

	public User login(String email, String password) {
		User user = userMapper.selectUserByEmail(email);
		if (user == null || !user.getPassword().equals(password)) {
			return null;
		}
		return user;
	}

	public boolean isEmailExists(String email) {
		int count = userMapper.checkEmailExists(email);
		return count > 0;

	}

	public String findPassword(String email, String nickname) {
		User user = userMapper.selectUserByEmailAndNick(email, nickname);
		if (user == null) {
			return null;
		}

		String tempPassword = "TEMP" + System.currentTimeMillis() % 10000;

		user.setPassword(tempPassword);
		userMapper.updatePassword(user);

		return tempPassword;
	}

	public boolean changePassword(int userId, String currentPw, String newPw) {
		User user = userMapper.selectUserById(userId);

		if (user == null || !user.getPassword().equals(currentPw)) {
			return false;
		}

		user.setPassword(newPw);
		userMapper.updatePassword(user);

		return true;
	}
	
// 관리자 기능 추가
    
    // 1. 회원 목록 조회 (페이징 포함)
    public Map<String, Object> getUserList(int page, String type, String keyword) {
        int limit = 10; // 한 페이지당 10명
        int offset = (page - 1) * limit;

        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("keyword", keyword);
        params.put("offset", offset);
        params.put("limit", limit);

        List<User> users = userMapper.selectUserList(params);
        int totalCount = userMapper.countUserList(params);
        int totalPage = (int) Math.ceil((double) totalCount / limit);

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("totalPage", totalPage);
        
        return result;
    }

    // 2. 권한 변경 (정지/해제/관리자승격)
    public void changeUserRole(int userId, String role) {
        userMapper.updateUserRole(userId, role);
    }

    // 3. 비밀번호 초기화 (1234로 강제 변경)
    public void resetUserPassword(int userId) {
        User user = new User();
        user.setUserId(userId);
        user.setPassword("1234"); // 초기화 비번
        userMapper.updatePassword(user);
    }

}