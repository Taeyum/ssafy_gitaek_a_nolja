import axios from 'axios'

// Axios 인스턴스 생성
const http = axios.create({
  baseURL: '/api', // Vite 프록시를 타도록 설정
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
  withCredentials: true, // ★ 핵심: 세션 ID(쿠키)를 주고받기 위해 필수!
})

export default http