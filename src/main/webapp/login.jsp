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
	String saveId = "";
	String check = "";
	Cookie[] cookies = request.getCookies();
	if(cookies != null){
		for(Cookie c : cookies) {
				if(c.getName().equals("id")) {
					saveId = c.getValue();
					check = "checked";
					break;
				}
		}
	}
%>
<h1>로그인 페이지</h1>
	<form action = "/trip/member" method = "post">
		<input type = "hidden" name = "action" value = "login-member" />
		<label>이름 <input type="text" name ="name" value = "<%=saveId%>"/> </label> <br>
		<label>비밀번호 <input type="password" name ="password"/> </label> <br>
		
		<input type="checkbox" name="remember-me" value="on" <%=check%>/>
        <label for="remember-me">id 기억하기</label>
		<button>전송</button>
	</form>
	<button><a href="/trip/member-findpw.jsp">비밀번호 찾기</a></button>
	
</body>
</html>