import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  // 1. 상태 (State)
  const userInfo = ref(null) // 로그인 안 하면 null, 하면 객체 { name: '...', id: '...' }
  const isLoggedIn = computed(() => !!userInfo.value) // 로그인 여부 (true/false)

  // 2. 액션 (Action)
  
  // 로그인 (가짜 로직)
  const login = (email, password) => {
    // 나중엔 여기서 axios.post('/api/login', ...) 해서 백엔드랑 통신함
    // 지금은 무조건 성공하는 척함
    userInfo.value = {
      id: 1,
      email: email,
      name: '기택', // 닉네임 예시
      profileImg: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix' // 랜덤 아바타
    }
    return true
  }

  // 회원가입 (가짜 로직)
  const signup = (name, email, password) => {
    // 백엔드 저장 로직 들어갈 자리
    userInfo.value = {
      id: 2,
      email: email,
      name: name,
      profileImg: `https://api.dicebear.com/7.x/avataaars/svg?seed=${name}`
    }
    return true
  }

  // 로그아웃
  const logout = () => {
    userInfo.value = null
  }

  return { userInfo, isLoggedIn, login, signup, logout }
})