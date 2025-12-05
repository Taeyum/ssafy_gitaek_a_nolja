package com.ssafy.gitaek.service;

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

}