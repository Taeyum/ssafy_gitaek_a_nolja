package com.ssafy.gitaek.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.gitaek.model.User;

@Mapper
public interface UserMapper {
	int insertUser(User user);

	User selectUserByEmail(String email);

	int checkEmailExists(String email);

	int updateUser(User user);

	User selectUserById(int userId);

	User selectUserByEmailAndNick(@Param("email") String email, @Param("nickname") String nickname);

	int updatePassword(User user);

	// 닉네임 중복 체크
	int checkNicknameExists(String nickname);

	// 회원 탈퇴
	int deleteUser(int userId);
}