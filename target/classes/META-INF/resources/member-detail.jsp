<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원 상세 정보</title>
<link rel="stylesheet" href="${root}/resources/css/member-list.css">
</head>
<body>
	<%@include file="/header.jsp"%>
    <h1>회원 상세 정보</h1>
    <c:if test="${!empty member}">
        <table class="info-table">
            <tr>
                <th>이름</th>
                <td>${member.name}</td>
            </tr>
            <tr>
                <th>이메일</th>
                <td>${member.email}</td>
            </tr>
            <tr>
                <th>비밀번호</th>
                <td>${member.password}</td>
            </tr>
        </table>
    </c:if>
    <c:if test="${empty member}">
        <p>회원 정보가 없습니다.</p>
    </c:if>

    <button class="btn-list" onclick="location.href='${root}/member/list'">목록으로</button>
    <button class="btn-list" onclick="location.href='${root}/member/modify?email=${member.email}'">회원 정보 수정</button>
	
	<form action="${root}/member/remove" method="post">
		<input type="hidden" name="email" value="${member.email}">
		<button>삭제~</button>
	</form>
</body>
</html>