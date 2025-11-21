<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원 정보 수정</title>
<link rel="stylesheet" href="${root}/resources/css/member-list.css">
</head>
<body>
    <%@include file="/header.jsp"%>
    <h1>회원 정보 수정</h1>
	<form action="${root}/member/modify" method="post">
        <table class="info-table">
            <tr>
                <th>이름</th>
                <td><input type="text" name="name" value="${member.name}"/></td>
            </tr>
            <tr>
                <th>이메일</th>
                <td><input type="text" name="email" value="${member.email}" readonly/></td>
            </tr>
            <tr>
                <th>비밀번호</th>
                <td><input type="password" name="password" value="${member.password}"/></td>
            </tr>
        </table>
        <button>수정</button>
    </form>
</body>
</html>