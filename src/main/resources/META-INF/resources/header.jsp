<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<style>
a {
	text-decoration: none;
}
</style>

<div>
	<span> <a href="${root}/">메인</a></span> |
	
	<c:if test="${empty sessionScope.loginUser}">
		<span> <a href="${root}/member/login">로그인</a></span> |
		<span> <a href="${root}/member/regist">회원가입</a></span> |
	</c:if>
	
	<c:if test="${not empty sessionScope.loginUser}">
		<span> <a href="${root}/member/list">목록 조회</a></span> |
		<span> <a href="${root}/map">관광지 조회</a></span> |
		<span> <a href="${root}/plan">나만의 여행 계획</a></span> |
		<span>안녕하세요 ${sessionScope.loginUser.name} 님</span> |
		<span> <a href="${root}/member/logout">로그아웃</a></span> |
	</c:if>

</div>
<hr />
<script type="text/javascript">
	const alertMsg = '${param.alertMsg}';
	if (alertMsg) {
		alert(alertMsg);
	}
</script>
