<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>로그인</title>
</head>
<body>
	<%@include file="/header.jsp"%>

	<h1>로그인 페이지</h1>
	<form action = "${root}/member/login" method = "post">
		<label>이름 <input type="text" name ="name" value = "${cookie.id.value}"/> </label> <br>
		<label>비밀번호 <input type="password" name ="password"/> </label> <br>
		
		<input type="checkbox" name="rememberMe" value="on" ${not empty cookie.id.value ? 'checked' : ''}/>
        <label for="rememberMe">id 기억하기</label>
		<button>전송</button>
	</form>
	<a href="${root}/member/find-password"><button>비밀번호 찾기</button></a>
	
</body>
</html>