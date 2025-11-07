package com.ssafy.exam.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List; 

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/member")
public class LoggingFilter extends HttpFilter implements Filter {
    // 음악 관련 액션 목록 (주요 음악 관리 기능)
    private static final List<String> PROTECTED_MUSIC_ACTIONS = List.of(
        "music-list", "music-detail", "music-modify", "music-regist", "music-remove", "musicregist"
    );

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response; // Response 객체도 형변환

        
        System.out.println("요청 파라미터 분석");
        request.getParameterMap().forEach((k, v) -> {
            System.out.printf("name: %s, value: %s\n", k, Arrays.toString(v));
        });
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        
       
        String action = httpRequest.getParameter("action");
        
        
        if (PROTECTED_MUSIC_ACTIONS.contains(action)) {
            
            HttpSession session = httpRequest.getSession(false); 
            boolean isLoggedIn = (session != null && session.getAttribute("loginUser") != null);

            if (!isLoggedIn) {
                System.out.println("인증 실패: 로그인이 필요한 음악 페이지 접근 시도 -> 로그인 폼으로 리다이렉트");
                String contextPath = httpRequest.getContextPath();
                httpResponse.sendRedirect(contextPath + "/member?action=login-form");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}