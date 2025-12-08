import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { loginApi, signupApi, logoutApi, getUserInfoApi, checkNicknameApi, updateUserApi, changePasswordApi, deleteUserApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 1. 상태
  const userInfo = ref(null)
  const isLoggedIn = computed(() => !!userInfo.value)

  // 2. 액션

  // ★ [핵심] 로그인 로직 수정
  const login = async (email, password, rememberId = false) => {
    try {
      const response = await loginApi({ email, password, rememberId })
      
      // 백엔드에서 온 데이터(rawUser)를 프론트엔드용(userInfo)으로 변환
      const rawUser = response.data
      
      userInfo.value = {
        id: rawUser.userId,        // 백엔드: userId -> 프론트: id
        email: rawUser.email,
        // ★ 여기가 문제였음! 백엔드는 nickname, 프론트는 name
        name: rawUser.nickname,    
        // ★ 이미지가 없으니 랜덤 아바타 생성 (닉네임 기반)
        profileImg: `https://api.dicebear.com/7.x/avataaars/svg?seed=${rawUser.nickname}` 
      }
      
      return true // 성공했다고 확실하게 보고
    } catch (error) {
      console.error("로그인 실패:", error)
      alert("이메일 또는 비밀번호를 확인해주세요.")
      return false // 실패 보고
    }

    
  }

  // 회원가입
  const signup = async (name, email, password) => {
    try {
      await signupApi({ 
        email, 
        password, 
        nickname: name 
      })
      return true
    } catch (error) {
      console.error("회원가입 실패:", error)
      alert("회원가입 중 오류가 발생했습니다.")
      return false
    }
  }

  // 로그아웃
  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error("로그아웃 요청 실패 (세션 만료 등):", error)
    } finally {
      // 서버 응답과 상관없이 프론트에서는 무조건 로그아웃 처리
      userInfo.value = null
    }
  }

  // 새로고침 시 로그인 유지
  const checkLoginStatus = async () => {
    try {
      const response = await getUserInfoApi()
      const rawUser = response.data
      
      // 여기도 데이터 변환 로직 적용
      userInfo.value = {
        id: rawUser.userId,
        email: rawUser.email,
        name: rawUser.nickname, // ★ nickname -> name 연결
        profileImg: `https://api.dicebear.com/7.x/avataaars/svg?seed=${rawUser.nickname}`
      }
    } catch (error) {
      userInfo.value = null
    }
  }

  // 닉네임 중복 확인
  const checkNickname = async (nickname) => {
    try {
      const res = await checkNicknameApi(nickname)
      return res.data // true(중복) or false(사용가능)
    } catch (err) {
      console.error(err)
      return true // 에러나면 일단 중복으로 처리
    }
  }

  // 정보 수정
  const updateInfo = async (nickname) => {
    if (!userInfo.value) return false
    try {
      await updateUserApi(userInfo.value.id, { nickname })
      // 스토어 정보도 갱신
      userInfo.value.name = nickname // 프론트 변수명(name) <-> 백엔드(nickname) 주의
      return true
    } catch (err) {
      alert("정보 수정 실패")
      return false
    }
  }

  // 비밀번호 변경
  const changePassword = async (currentPassword, newPassword) => {
    if (!userInfo.value) return false
    try {
      await changePasswordApi(userInfo.value.id, { currentPassword, newPassword })
      return true
    } catch (err) {
      // 백엔드에서 400 에러(비번 틀림)를 보내면 여기서 잡힘
      alert(err.response?.data || "비밀번호 변경 실패")
      return false
    }
  }

  // 회원 탈퇴
  const deleteAccount = async (password) => {
    if (!userInfo.value) return false
    try {
      await deleteUserApi(userInfo.value.id, password)
      userInfo.value = null // 로그아웃 처리
      return true
    } catch (err) {
      alert(err.response?.data || "탈퇴 실패")
      return false
    }
  }

  return { 
    userInfo, isLoggedIn, login, signup, logout, checkLoginStatus, 
    checkNickname, updateInfo, changePassword, deleteAccount // ★ 반환 추가
  }
})