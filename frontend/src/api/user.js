import http from './http'

// 1. 로그인
export const loginApi = (user) => {
  return http.post('/users/login', user)
}

// 2. 회원가입
export const signupApi = (user) => {
  return http.post('/users', user)
}

// 3. 로그아웃
export const logoutApi = () => {
  return http.post('/users/logout')
}

// 4. 내 정보 조회 (새로고침 시 로그인 유지용)
export const getUserInfoApi = () => {
  return http.get('/users/me')
}