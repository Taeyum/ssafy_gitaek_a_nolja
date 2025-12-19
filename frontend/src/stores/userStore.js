import { ref, computed } from "vue";
import { defineStore } from "pinia";
import {
  loginApi,
  signupApi,
  logoutApi,
  getUserInfoApi,
  checkNicknameApi,
  updateUserApi,
  changePasswordApi,
  deleteUserApi,
} from "@/api/user";

export const useUserStore = defineStore("user", () => {
  const userInfo = ref(null);
  const isLoggedIn = computed(() => !!userInfo.value);

  // ★ [핵심 1] 내 정보 가져오기 (토큰으로 서버에 "나 누구야?" 물어봄)
  const checkLoginStatus = async () => {
    try {
      const token = localStorage.getItem("accessToken");
      if (!token) return; // 토큰 없으면 시도조차 하지 않음

      const response = await getUserInfoApi();
      const rawUser = response.data;

      userInfo.value = {
        id: rawUser.userId,
        email: rawUser.email,
        name: rawUser.nickname,
        role: rawUser.role, // 관리자 확인용
        profileImg: `https://api.dicebear.com/7.x/avataaars/svg?seed=${rawUser.nickname}`,
      };
    } catch (error) {
      console.error("토큰 만료 또는 오류:", error);
      userInfo.value = null;
      localStorage.removeItem("accessToken");
    }
  };

  // ★ [핵심 2] 로그인 (토큰 받아서 저장 -> checkLoginStatus 실행)
  const login = async (email, password, rememberId = false) => {
    try {
      const response = await loginApi({ email, password, rememberId });

      // 백엔드가 주는 건 '토큰 문자열'입니다!
      const token = response.data;

      if (token) {
        localStorage.setItem("accessToken", token); // 1. 토큰 저장
        await checkLoginStatus(); // 2. 내 정보 갱신
        return true;
      }
      return false;
    } catch (error) {
      console.error("로그인 실패:", error);
      alert("이메일 또는 비밀번호를 확인해주세요.");
      return false;
    }
  };

  // 로그아웃
  const logout = async () => {
    try {
      await logoutApi();
    } catch (e) {}
    userInfo.value = null;
    localStorage.removeItem("accessToken"); // 토큰 삭제
  };

  // (나머지 회원가입, 수정 등 기존 기능 유지)
  const signup = async (name, email, password, phoneNumber) => {
    try {
      await signupApi({ email, password, nickname: name, phoneNumber });
      return true;
    } catch (error) {
      alert("회원가입 실패");
      return false;
    }
  };

  const checkNickname = async (nickname) => {
    try {
      return (await checkNicknameApi(nickname)).data;
    } catch {
      return true;
    }
  };

  const updateInfo = async (nickname) => {
    try {
      await updateUserApi(userInfo.value.id, { nickname });
      userInfo.value.name = nickname;
      return true;
    } catch {
      return false;
    }
  };

  const changePassword = async (currentPassword, newPassword) => {
    try {
      await changePasswordApi(userInfo.value.id, {
        currentPassword,
        newPassword,
      });

      // 12/18 추가한 사항
      // ★ 핵심: DB 변경 성공했으니, 가지고 있는 토큰(열쇠) 폐기!
      await logout();

      return true;
    } catch (e) {
      alert(e.response?.data);
      return false;
    }
  };

  const deleteAccount = async (password) => {
    try {
      await deleteUserApi(userInfo.value.id, password);
      // 1218
      // 2. ★ [수정] logout()을 호출하지 마세요! (서버로 요청 보내서 에러남)
      // await logout(); <--- 이거 지우거나 주석 처리
      // 3. 대신 여기서 '수동으로' 클라이언트 청소만 합니다.
      userInfo.value = null;
      localStorage.removeItem("accessToken");
      // 위에 await 주석처리하고, 이거 만듦.
      return true;
    } catch (e) {
      // console.log(e); // F12 콘솔에서 확인 가능
      // // 백엔드 에러 메시지가 있으면 보여주고, 없으면 기본 메시지
      //   const msg = e.response?.data || "탈퇴 처리 중 오류가 발생했습니다.";
      alert(e.response?.data);
      return false;
    }
  };

  return {
    userInfo,
    isLoggedIn,
    login,
    logout,
    signup,
    checkLoginStatus,
    checkNickname,
    updateInfo,
    changePassword,
    deleteAccount,
  };
});
