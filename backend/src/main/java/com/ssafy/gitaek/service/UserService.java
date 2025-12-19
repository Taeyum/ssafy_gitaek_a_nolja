package com.ssafy.gitaek.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // ★ 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.gitaek.mapper.TripMapper;
import com.ssafy.gitaek.mapper.UserMapper;
import com.ssafy.gitaek.model.User;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TripMapper tripMapper; // ★ TripMapper 주입 (여행 삭제용)


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
    
    // 회원 탈퇴
    @Transactional
    public boolean deleteUser(int userId, String password) {
        User user = userMapper.selectUserById(userId);

        // 1. 유저가 없거나 비밀번호가 틀리면 실패
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return false;
        }
        
        // 2. ★ [핵심 추가] 이 사람이 '방장'인 여행들 먼저 삭제!
        // (이걸 안 하면 FK 제약조건 때문에 에러남)
        tripMapper.deleteTripsByOwnerId(userId);


        // 2. 삭제 진행
        userMapper.deleteUser(userId);
        return true;
    }
    // 1. 닉네임 중복 체크
    public boolean checkNickname(String nickname) {
        // 이메일&닉네임으로 조회하는 기존 로직 활용하거나, 카운트 쿼리 사용
        // 여기서는 간단하게 selectUserByEmailAndNick 등을 응용하거나 새로 만듭니다.
        // Mapper에 'checkNicknameExists'가 있다면 그걸 쓰고, 없다면 아래처럼:
        return userMapper.checkNicknameExists(nickname) > 0;
    }

    // 2. 닉네임 변경
    public void updateNickname(int userId, String newNickname) {
        User user = new User();
        user.setUserId(userId);
        user.setNickname(newNickname);
        // UserMapper.xml에 updateUser가 nickname을 업데이트하도록 되어 있어야 함
        userMapper.updateUser(user);
    }
}