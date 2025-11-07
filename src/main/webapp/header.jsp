<%@page import="com.ssafy.exam.model.dto.Member"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.servletContext.contextPath }" />
<style>
a {
	text-decoration: none;
}
</style>
<div>
	<span> <a href="/trip/member?action=index">메인</a></span> |
	<span> <a href="/trip/member?action=login-form">로그인</a></span> |
	<span> <a href="/trip/member?action=member-list">목록 조회</a></span> |
	<span> <a href="/trip/member?action=map">관광지 조회</a></span> |
	<span> <a href="/trip/plan?action=plan-form">나만의 여행 계획</a></span> |
<%-- 	<span>안녕하세요${sessionScope.loginUser.name} 님 </span> | <span> <a
		href="/trip/member?action=logout-form">로그아웃</a>
	</span> |
--%>
	<%
    Member loginUser = (Member) session.getAttribute("loginUser");
    if (loginUser != null) {
	%>
    <span>안녕하세요 <%= loginUser.getName() %> 님</span> |
	<%
    }
	%>
    <span><a href="/trip/member?action=logout-form">로그아웃</a></span> |

</div>
<hr />
<script type="text/javascript">
	const alertMsg = `${param.alertMsg}` || `${alertMsg}`;
	if (alertMsg) {
		alert(alertMsg);
	}
</script>
