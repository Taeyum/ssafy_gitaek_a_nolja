<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%@include file="/header.jsp"%>
	<% 
    	String root = request.getContextPath();
	%>
	<form action = "<%=root%>/member" method = "post">
		<input type = "hidden" name = "action" value = "regist-member" />
		<label>이름 <input type="text" name ="name"/> </label> <br>
		<label>이메일 <input type="email" name ="email"/> </label> <br>
		<label>비밀번호 <input type="password" name ="password"/> </label> <br>
		<button>전송</button>
	</form>
</body>
</html>