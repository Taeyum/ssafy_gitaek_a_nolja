<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원가입</title>
</head>
<body>
	<%@include file="/header.jsp"%>
	
	<form action = "${root}/member/regist" method = "post">
		<label>이름 <input type="text" name ="name"/> </label> <br>
		<label>이메일 <input type="email" name ="email"/> </label> <br>
		<label>비밀번호 <input type="password" name ="password"/> </label> <br>
		<button>전송</button>
	</form>
</body>
</html>