<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>비밀번호 찾기</title>
</head>
<body>
    <%@ include file="/header.jsp" %>

    <h1>비밀번호찾기</h1>
    <p>이메일과 이름을 입력해주세요</p>

    <form action="/trip/member" method="get">
        <input type="hidden" name="action" value="find-password" />
        <label>이메일 <input type="email" name="email"></label>
        <label>이름 <input type="text" name="name"></label><br>
        <button type="submit">비밀번호 확인하기</button>
    </form>

    <!-- 컨트롤러에서 넘겨줬을 때만 출력 -->
    <c:if test="${not empty password}">
        <p>회원님의 비밀번호는 <strong>${password}</strong> 입니다.</p>
    </c:if>
</body>
</html>
