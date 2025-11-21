<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원 목록</title>
<link rel="stylesheet" href="${root}/resources/css/member-list.css">
</head>

<body>
	<%@include file="/header.jsp"%>
    <h1>회원 목록😊😂😎</h1>
    <table class="info-table">
        <thead>
            <tr>
                <th>이름</th>
                <th>이메일</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty members}">
                    <c:forEach var="member" items="${members}">
                        <tr>
                            <td>${member.name}</td>
                            <td><a href="${root}/member/detail?email=${member.email}">
                                ${member.email}
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td colspan="2">회원 정보가 없습니다.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</body>
</html>