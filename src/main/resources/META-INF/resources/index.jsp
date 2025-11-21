<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>메인 페이지</title>
</head>
<body>
		<%@include file="/header.jsp"%>
		<h1>메인 페이지 입니다.</h1>
		
		<a href = "${root}/member/regist">회원가입 페이지</a>
		<a href = "${root}/member/list">회원 목록 조회</a>

</body>
</html>
