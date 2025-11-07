package com.ssafy.exam.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.ssafy.exam.model.dto.Member;
import com.ssafy.exam.model.service.MemberService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet implementation class MusicController
 */
//
@WebServlet("/member")
public class MemberController extends HttpServlet implements ControllerHelper {
	private static final long serialVersionUID = 1L;

	private MemberService service = MemberService.getInstance();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = getActionParameter(request, response);
		System.out.println(request.getParameter("action"));
		switch (action) {
		case "login-form" -> forward(request, response, "/login.jsp");
		case "regist-member" -> forward(request, response, "/regist-member.jsp");
		case "header-form" -> forward(request, response, "/header.jsp");
		case "index" -> forward(request, response, "/index.jsp");
		case "member-list" -> getAllMember(request, response);
		case "member-detail" -> getMemberDetail(request, response);
		case "member-modify" -> memberDetailModify(request, response);
		case "logout-form" -> logoutMember(request, response);
		// 비밀번호 찾기
		case "find-password" -> findPassword(request, response);
		// 관광지 페이지 이동
		case "map" -> forward(request,response,"/map.jsp");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = getActionParameter(request, response);
		System.out.println("현재 액션 : " + action);
		switch (action) {
		// 회원 가입 처리
		case "regist-member" -> registMember(request, response);
		// 로그인 처리
		case "login-member" -> loginMember(request, response);
		// 회원 수정
		case "member-modify" -> memberModify(request, response);
		// 회원 탈퇴
		case "remove-member" -> memberRemove(request, response);

		}
	}
	// dsd
	
	// 회원 가입 처리하는 메소드
	private void registMember(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// 1. 회원 데이터 파라미터에서 가져오기
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		Member member = new Member(name, email, password);
		System.out.println(name + " " + email + " " + password);
		// 2. 요청 처리 시키기 : service
		try {
			service.registMember(member);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 3. 결과 페이지 이동
		// 회원가입이 완료되었으니 로그인 페이지로 이동
//		forward(request, response, "/login.jsp");
		// 응답이 나갔다가 다시 들어오는 요청이기 때문에 context root 가 있어야 함
		redirect(request, response, "/member?action=login-form");
	}

	// -------------------------------------------- 로그인, 로그 아웃
	// ------------------------------------

	// 로그인 처리하는 메소드
	private void loginMember(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// 1. 회원 데이터 파라미터에서 가져오기
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		String rememberMe = request.getParameter("remember-me");

		// 체크박스 on
		if (rememberMe != null) {
			Cookie idCookie = new Cookie("id", name);
			idCookie.setMaxAge(60 * 60);
			idCookie.setPath("/");
			response.addCookie(idCookie);
		} else {
			// 체크 박스 off
			Cookie idCookie = new Cookie("id", "");
			idCookie.setMaxAge(0);
			idCookie.setPath("/");
			response.addCookie(idCookie);
		}

		try {
			Member m = service.loginMember(name, password);
			System.out.println(m);
			if (m == null) {
				forward(request, response, "/login.jsp");
			} else {
				HttpSession session = request.getSession();
				session.setAttribute("loginUser", m);
				redirect(request, response, "/member?action=index");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 로그 아웃하는 메소드
	private void logoutMember(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		HttpSession session = request.getSession();
		session.invalidate();
		System.out.println("세션 파괴 완료");
		redirect(request, response, "/member?action=index");
	}

	// ----------------------------------------- 회원 관리
	// --------------------------------------

	// 전체 회원을 반환하는 메소드
	private void getAllMember(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			List<Member> memberList = service.allMember();
			request.setAttribute("members", memberList);
			forward(request, response, "/member-list.jsp");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 회원 상세 목록
	private void getMemberDetail(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String email = request.getParameter("email");
		try {
			Member member = service.getMember(email);
			request.setAttribute("member", member);
			forward(request, response, "/member-detail.jsp");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 회원 정보 수정 페이지
	private void memberDetailModify(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String email = request.getParameter("email");
		System.out.println(email);
		try {
			Member member = service.getMember(email);
			request.setAttribute("member", member);
			forward(request, response, "/member-modify.jsp");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 회원 수정 post
	private void memberModify(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		try {
			int result = service.memberModify(name, email, password);
			if (result == 1) {
				System.out.println("변경 완료");
			} else {
				System.out.println("변경 실패");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		redirect(request, response, "/member?action=member-list");
	}

	// 회원 제거
	private void memberRemove(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String email = request.getParameter("email");

		try {
			int result = service.memberRemove(email);
			if (result == 1) {
				System.out.println("삭제 완료");
			} else {
				System.out.println("삭제 실패! 존재하지 않는 이메일");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		redirect(request, response, "/member?action=member-list");
	}
	private void findPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email");
		String name = request.getParameter("name");
		try {
			String password = service.findPassword(email, name);
			request.setAttribute("password", password);
			forward(request, response, "/member-findpw.jsp");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
