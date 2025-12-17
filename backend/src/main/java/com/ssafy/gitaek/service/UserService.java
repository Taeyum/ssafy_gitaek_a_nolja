package com.ssafy.gitaek.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // ★ 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.gitaek.mapper.UserMapper;
import com.ssafy.gitaek.model.User;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // ★ 암호화 도구 주입

    // 회원가입 (암호화 적용)
    @Transactional
    public void signup(User user) {
        // 비밀번호 암호화해서 저장
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        // DTO에서 Role을 설정해서 넘겨주지만, 혹시 모르니 안전장치
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        
        userMapper.insertUser(user);
    }

    // 로그인 (컨트롤러에서 처리하지만 검증용으로 유지)
    public User login(String email, String password) {
        User user = userMapper.selectUserByEmail(email);
        // ★ matches()로 암호화된 비번 비교
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public boolean isEmailExists(String email) {
        return userMapper.checkEmailExists(email) > 0;
    }

    public String findPassword(String email, String nickname) {
        User user = userMapper.selectUserByEmailAndNick(email, nickname);
        if (user == null) return null;

        String tempPassword = "TEMP" + System.currentTimeMillis() % 10000;

        // ★ 임시 비번도 암호화 저장
        user.setPassword(passwordEncoder.encode(tempPassword));
        userMapper.updatePassword(user);

        return tempPassword; // 사용자에겐 평문 전달
    }

    public boolean changePassword(int userId, String currentPw, String newPw) {
        User user = userMapper.selectUserById(userId);

        // ★ 현재 비번 확인 (암호화 비교)
        if (user == null || !passwordEncoder.matches(currentPw, user.getPassword())) {
            return false;
        }

        // ★ 새 비번 암호화
        user.setPassword(passwordEncoder.encode(newPw));
        userMapper.updatePassword(user);

        return true;
    }

    // --- 관리자 기능 ---
    public Map<String, Object> getUserList(int page, String type, String keyword) {
        int limit = 10;
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

    public void changeUserRole(int userId, String role) {
        userMapper.updateUserRole(userId, role);
    }

    public void resetUserPassword(int userId) {
        User user = new User();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode("1234")); // ★ 초기화 비번도 암호화
        userMapper.updatePassword(user);
    }
}