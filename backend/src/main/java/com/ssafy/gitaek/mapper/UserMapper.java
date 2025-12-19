package com.ssafy.gitaek.mapper;

import java.util.List;
import java.util.Map;

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
	
	// 관리자용 매서드 추가
	int countUserList(Map<String, Object> params);
	
    List<User> selectUserList(Map<String, Object> params);
    
    int updateUserRole(@Param("userId") int userId, @Param("role") String role);
    
    int deleteUser(int userId);
}