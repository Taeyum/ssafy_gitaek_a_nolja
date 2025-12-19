import http from './http'

// 1. 회원 목록 조회 (페이징 + 검색)
export const getUserListApi = (page, type, keyword) => {
  return http.get('/admin/users', {
    params: {
      page: page || 1,
      type: type || '',
      keyword: keyword || ''
    }
  })
}

// 2. 권한 변경 (이 부분이 빠져있었을 겁니다)
export const changeUserRoleApi = (userId, role) => {
  return http.put(`/admin/users/${userId}/role`, { role })
}

// 3. 비밀번호 초기화
export const resetUserPasswordApi = (userId) => {
  return http.put(`/admin/users/${userId}/reset-pw`)
}

// 4. 관광지 데이터 수집
export const loadTourDataApi = () => {
  return http.get('/admin/load-data')
}