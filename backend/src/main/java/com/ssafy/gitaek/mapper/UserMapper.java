package com.ssafy.gitaek.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.gitaek.model.User;

@Mapper
public interface UserMapper {
	// 회원가입
	int insertUser(User user);

	// 로그인용 (이메일로 내 정보 찾아오기)
	User selectUserByEmail(String email);

	// 이메일 중복 체크
	int checkEmailExists(String email);

	// [추가] 내 정보 수정용 (닉네임 변경 등)
	int updateUser(User user);

	// [추가] ID로 유저 정보 가져오기 (비번 변경 시 본인 확인용)
	User selectUserById(int userId);

	User selectUserByEmailAndNick(@Param("email") String email, @Param("nickname") String nickname);

	int updatePassword(User user);
}