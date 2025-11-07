package com.ssafy.exam.model.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.ssafy.exam.model.dao.MemberDao;
import com.ssafy.exam.model.dto.Member;
import com.ssafy.exam.util.DBUtil;

// 요청 처리 담당
// 나중에 컨트롤러가 많아져서 이 Service가 많은 컨트롤러에서 동시적으로 접근하면 꼬이기 때문에 싱글톤으로!
public class MemberService {

	private static MemberService instance = new MemberService();

	public static MemberService getInstance() {
		return instance;
	}

	private MemberService() {
	}

	MemberDao dao = MemberDao.getInstance();

	// 회원 가입 요청 처리
	public int registMember(Member member) throws SQLException {
		Connection conn = DBUtil.getConnection();
		try {
			return dao.registMember(conn, member);
		}finally {
			DBUtil.close(conn);
		}
	}

	// 로그인 처리
	public Member loginMember(String name, String password) throws SQLException {
		Connection conn = DBUtil.getConnection();
		try {
			return dao.existMemberIdPassword(conn, name, password);
		} finally {
			DBUtil.close(conn);
		}
	}

	// 회원 반환 처리
	public List<Member> allMember() throws SQLException {
		Connection conn = DBUtil.getConnection();
		try {
			return dao.selectAllMembers(conn);
		} finally {
			DBUtil.close(conn);
		}
	}

	// 회원 정보 조회
	public Member getMember(String email) throws SQLException {
		Connection conn = DBUtil.getConnection();
		try {
			return dao.getMember(conn, email);
		} finally {
			DBUtil.close(conn);
		}
	}

	// 회원 정보 수정
	public int memberModify(String name, String email, String password) throws SQLException {
		Connection conn = DBUtil.getConnection();
		try {
			return dao.modifyMember(conn, name, email, password);
		} finally {
			DBUtil.close(conn);
		}
	}

	// 회원 삭제
	public int memberRemove(String email) throws SQLException {
		Connection conn = DBUtil.getConnection();
		try {
			return dao.removeMember(conn, email);
		} finally {
			DBUtil.close(conn);
		}
	}
	//비밀번호 찾기
	public String findPassword(String email, String name) throws SQLException {
		Connection conn = DBUtil.getConnection();
		try {
			return dao.findPassword(conn, email, name);
		} finally {
			DBUtil.close(conn);
		}
	}
}
