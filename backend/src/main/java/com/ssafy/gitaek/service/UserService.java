package com.ssafy.gitaek.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ssafy.gitaek.mapper.UserMapper;
import com.ssafy.gitaek.model.User;

@Service
public class UserService {

	@Autowired
	private UserMapper userMapper;

	// 회원가입
	public void signup(User user) {
		// 원래는 여기서 비밀번호 암호화(BCrypt) 해야 함
		userMapper.insertUser(user);
	}

	public User login(String email, String password) {
		User user = userMapper.selectUserByEmail(email);
		if (user == null || !user.getPassword().equals(password)) {
			return null;
		}
		return user;
	}

	// [추가] 이메일 중복 체크
	public boolean isEmailExists(String email) {
		int count = userMapper.checkEmailExists(email);
		return count > 0; // 0보다 크면 이미 있다는 뜻

	}

	// [추가] 비밀번호 찾기 (임시 비번 생성 후 반환)
	public String findPassword(String email, String nickname) {
		// 1. 유저 확인
		User user = userMapper.selectUserByEmailAndNick(email, nickname);
		if (user == null) {
			return null; // 일치하는 유저 없음
		}

		// 2. 임시 비밀번호 생성 (간단하게 현재 시간 기준으로 만듦)
		String tempPassword = "TEMP" + System.currentTimeMillis() % 10000;

		// 3. DB에 업데이트 (원래는 암호화 해야 함!)
		user.setPassword(tempPassword);
		userMapper.updatePassword(user);

		return tempPassword; // 사용자에게 보여줄 임시 비번 리턴
	}

	// [추가] 비밀번호 변경 로직
	public boolean changePassword(int userId, String currentPw, String newPw) {
		// 1. DB에서 현재 유저 정보 가져오기
		User user = userMapper.selectUserById(userId);

		// 2. 유저가 없거나, 현재 비밀번호가 틀리면 실패
		if (user == null || !user.getPassword().equals(currentPw)) {
			return false;
		}

		// 3. 새 비밀번호로 변경 및 저장
		user.setPassword(newPw);
		userMapper.updatePassword(user);

		return true; // 성공
	}

}