<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.ssafy.exam.model.dto.Member, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원 목록</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/member-list.css">
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
            <%
                List<Member> memberList = (List<Member>) request.getAttribute("members");

                if (memberList != null) {
                    for (Member member : memberList) {
            %>
            <tr>
                <td><%= member.getName() %></td>
                <td><a href = "member?action=member-detail&email=<%= member.getEmail() %>">
	            	<%= member.getEmail() %>
               	</a>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="2">회원 정보가 없습니다.</td>
            </tr>
            <%
                }
            %>
        </tbody>
    </table>
</body>
</html>