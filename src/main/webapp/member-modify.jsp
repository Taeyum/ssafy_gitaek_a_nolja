<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/member-list.css">
</head>
<body>
<h1>회원 상세 정보</h1>
	<form action = "/trip/member" method = "post">
		<input type = "hidden" name = "action" value = "member-modify" />
        <table class="info-table">
            <tr>
                <th>이름</th>
                <td><input type="text" name="name" value = "${member.name}"/></td>
            </tr>
            <tr>
                <th>이메일</th>
                <td><input type="text" name="email" value = "${member.email}"/></td>
            </tr>
            <tr>
                <th>비밀번호</th>
                <td><input type="text" name="password" value = "${member.password}"/></td>
            </tr>
        </table>
    <button>수정</button>
    </form>
	
</body>
</html>