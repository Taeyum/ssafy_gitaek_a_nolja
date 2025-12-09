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

// 5. 닉네임 중복 체크
export const checkNicknameApi = (nickname) => {
  return http.get(`/users/check/${nickname}`)
}

// 6. 회원 정보 수정
export const updateUserApi = (userId, userInfo) => {
  return http.put(`/users/${userId}`, userInfo)
}

// 7. 비밀번호 변경
export const changePasswordApi = (userId, passwords) => {
  return http.put('/users/password', passwords)
}

// 8. 회원 탈퇴
export const deleteUserApi = (userId, password) => {
  return http.delete(`/users/${userId}`, { data: { password } })
}