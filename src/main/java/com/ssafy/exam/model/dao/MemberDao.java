package com.ssafy.exam.model.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ssafy.exam.model.dto.Member;

public interface MemberDao {

    int registMember(Member member);

    Member existMemberIdPassword(@Param("name") String name, @Param("password") String password);

    List<Member> selectAllMembers();

    Member getMember(String email);

    int modifyMember(Member member);

    int removeMember(String email);

    String findPassword(@Param("email") String email, @Param("name") String name);
}
