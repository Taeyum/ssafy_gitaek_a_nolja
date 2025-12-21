<script setup>
import { ref, onMounted } from "vue";
import LandingPage from "@/views/LandingPage.vue";
import PlanningDashboard from "@/views/PlanningDashboard.vue";
import MyPage from "@/views/MyPage.vue";
import ProfileEdit from "@/views/ProfileEdit.vue";
import AdminDashboard from "@/views/AdminDashboard.vue"; // ★ 관리자 페이지 import 확인!
// [추가] 체크리스트 컴포넌트 가져오기
import ChecklistView from "@/views/ChecklistView.vue";

import { useUserStore } from "@/stores/userStore";

const userStore = useUserStore();

onMounted(() => {
  userStore.checkLoginStatus();
});

// 현재 보여줄 화면 상태 (landing, planning, mypage, profile-edit, admin)
const currentView = ref("landing");

// ★ [추가 1] 현재 선택된 여행 ID (0이면 일반 체크리스트, 숫자가 있으면 여행용)
const currentPlanId = ref(0);

// ★ [추가 2] 체크리스트로 이동하는 함수 (ID 설정 + 화면 전환)
const goChecklist = (planId = 0) => {
  currentPlanId.value = planId;
  currentView.value = "checklist";
};

// ★ [추가 3] 체크리스트에서 뒤로가기 눌렀을 때 (스마트 네비게이션)
const handleChecklistBack = () => {
  // 일반 리스트였으면 -> 메인(landing)으로
  if (currentPlanId.value === 0) {
    currentView.value = "landing";
  }
  // 여행용 리스트였으면 -> 여행 계획 목록(planning)으로
  else {
    currentView.value = "planning";
  }
};
</script>

<template>
  <Transition name="fade" mode="out-in">
    <LandingPage
      v-if="currentView === 'landing'"
      @start="currentView = 'planning'"
      @my-page="currentView = 'mypage'"
      @go-check="currentView = 'checklist'"
    />

    <PlanningDashboard
      v-else-if="currentView === 'planning'"
      @back="currentView = 'landing'"
      @go-check-plan="goChecklist"
    />

    <MyPage
      v-else-if="currentView === 'mypage'"
      @back="currentView = 'landing'"
      @go-edit="currentView = 'profile-edit'"
      @go-plan="currentView = 'planning'"
      @go-admin="currentView = 'admin'"
    />

    <ProfileEdit
      v-else-if="currentView === 'profile-edit'"
      @back="currentView = 'mypage'"
    />

    <AdminDashboard
      v-else-if="currentView === 'admin'"
      @back="currentView = 'mypage'"
    />

    <ChecklistView
      v-else-if="currentView === 'checklist'"
      @back="currentView = 'landing'"
    />
  </Transition>
</template>

<style>
/* 화면 전환 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
