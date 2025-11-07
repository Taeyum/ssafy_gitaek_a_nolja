package com.ssafy.exam.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ssafy.exam.model.dto.Member;
import com.ssafy.exam.util.DBUtil;

public class MemberDao  {

	private static MemberDao instance = new MemberDao();

	public static MemberDao getInstance() {
		return instance;
	}

	private MemberDao() {
	}
	// 테이블체크
	public void tableCheck() {
	    try (Connection con = DBUtil.getConnection()) {
	        // 테이블 존재 여부 확인
	        boolean exists = false;

	        try (PreparedStatement pstmt = con.prepareStatement(
	                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?")) {
	            pstmt.setString(1, con.getCatalog()); // 현재 DB 이름
	            pstmt.setString(2, "Member");

	            try (ResultSet rs = pstmt.executeQuery()) {
	                if (rs.next()) {
	                    exists = rs.getInt(1) > 0;
	                }
	            }
	        }

	        if (exists) {
	            System.out.println("테이블 존재합니다");
	            return;
	        }

	        // 테이블이 없으면 생성
	        String createSql = "CREATE TABLE Member ("
	                         + "mno INT PRIMARY KEY AUTO_INCREMENT,"
	                         + "name VARCHAR(100) NOT NULL,"
	                         + "email VARCHAR(100) NOT NULL UNIQUE,"
	                         + "password VARCHAR(255) NOT NULL,"
	                         + "role VARCHAR(50)"
	                         + ")";
	        try (PreparedStatement pstmt = con.prepareStatement(createSql)) {
	            pstmt.executeUpdate();
	            System.out.println("테이블 생성 완료");
	        }

	        // 기본 회원 데이터 삽입
	        String insertSql = "INSERT INTO Member (name, email, password, role) VALUES (?, ?, ?, ?)";
	        try (PreparedStatement pstmt = con.prepareStatement(insertSql)) {
	            for (int i = 0; i < 10; i++) {
	                pstmt.setString(1, "테스트" + (10 - i));
	                pstmt.setString(2, "test" + i + "@ssafy.com");
	                pstmt.setString(3, "1234");
	                pstmt.setString(4, "USER");
	                pstmt.addBatch();
	            }

	            // 관리자 계정 추가
	            pstmt.setString(1, "관리자");
	            pstmt.setString(2, "admin@ssafy.com");
	            pstmt.setString(3, "1234");
	            pstmt.setString(4, "ADMIN");
	            pstmt.addBatch();

	            pstmt.executeBatch();
	            System.out.println("데이터 삽입 완료");
	        }

	    } catch (Exception e) {
	        System.err.println("오류 발생");
	        e.printStackTrace();
	    }
	}


	// 회원 저장
	public int registMember(Connection conn, Member member) {
	    String sql = "INSERT INTO Member (name, email, password, role) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, member.getName());
	        pstmt.setString(2, member.getEmail());
	        pstmt.setString(3, member.getPassword());
	        pstmt.setString(4, "USER"); // 기본 역할
	        return pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return 0;
	    }
	}

	// 회원이 존재하는가?
	public Member existMemberIdPassword(Connection conn, String name, String password) {
	    String sql = "SELECT * FROM Member WHERE name = ? AND password = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, name);
	        pstmt.setString(2, password);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                Member member = new Member();
	                member.setMno(rs.getInt("mno"));
	                member.setName(rs.getString("name"));
	                member.setEmail(rs.getString("email"));
	                member.setPassword(rs.getString("password"));
	                member.setRole(rs.getString("role"));
	                return member;
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	// 멤버 목록 반환
	public List<Member> selectAllMembers(Connection conn) {
		String sql = "SELECT * FROM Member";
		List<Member> members = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				Member member = new Member();
				member.setMno(rs.getInt("mno"));
				member.setName(rs.getString("name"));
				member.setEmail(rs.getString("email"));
				member.setPassword(rs.getString("password"));
				member.setRole(rs.getString("role"));
				members.add(member);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return members;
	}

	public Member getMember(Connection conn, String email) {
		String sql = "SELECT * FROM Member WHERE email = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, email);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Member member = new Member();
					member.setMno(rs.getInt("mno"));
					member.setName(rs.getString("name"));
					member.setEmail(rs.getString("email"));
					member.setPassword(rs.getString("password"));
					member.setRole(rs.getString("role"));
					return member;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 멤버 수정
	public int modifyMember(Connection conn, String name, String email, String password) {
		String sql = "UPDATE Member SET name = ?, password = ? WHERE email = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.setString(2, password);
			pstmt.setString(3, email);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	// 멤버 삭제
	public int removeMember(Connection conn, String email) {
		String sql = "DELETE FROM Member WHERE email = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, email);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	//비밀번호 찾기
	public String findPassword(Connection conn, String email, String name) {
		String sql = "SELECT password FROM Member WHERE email = ? AND name = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, email);
			pstmt.setString(2, name);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("password");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void save() {
		// TODO Auto-generated method stub
		
	}

}
