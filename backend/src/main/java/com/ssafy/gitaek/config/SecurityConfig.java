package com.ssafy.gitaek.config;

import com.ssafy.gitaek.jwt.JwtAuthenticationFilter;
import com.ssafy.gitaek.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	// 1. 비밀번호 암호화 기계 (이게 있어야 로그인 비교 가능)
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 2. 시큐리티 필터 설정 (여기가 문지기 설정)
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// CSRF 비활성화 (JWT 쓸 땐 필요 없음)
				.csrf(AbstractHttpConfigurer::disable)
				// CORS 설정 (프론트랑 통신하려면 필수)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				// 세션 안 씀 (STATELESS)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// ★ 주소별 권한 설정 (여기가 핵심!)
				.authorizeHttpRequests(auth -> auth
						// 1. 누구나 들어갈 수 있는 곳 (로그인, 회원가입, 관광지 조회)
						.requestMatchers("/", "/index.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/users", "/api/users/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/attractions/**", "/api/trips/**").permitAll() 
						// http://localhost:8080/api/admin/load-data  허용																					
						.requestMatchers("/api/admin/load-data").permitAll()
						.requestMatchers("/ws-stomp/**").permitAll()
						.requestMatchers("/api/users/check-email", "/api/users/password-recovery").permitAll()
						// 2. 관리자만 가능한 곳
						.requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
						// 3. 나머지는 다 로그인해야 함
						.anyRequest().authenticated())
				// JWT 필터 끼워넣기
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// CORS 설정 (프론트엔드 5173 포트 허용)
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// 프론트엔드 주소 (Vite 기본 포트)
		config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "https://gitaek.vercel.app"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}