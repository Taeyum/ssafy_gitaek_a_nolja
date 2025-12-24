<script setup>
import { ref, onMounted } from "vue";
import LandingPage from "@/views/LandingPage.vue";
import PlanningDashboard from "@/views/PlanningDashboard.vue";
import MyPage from "@/views/MyPage.vue";
import ProfileEdit from "@/views/ProfileEdit.vue";
import AdminDashboard from "@/views/AdminDashboard.vue";
import ChecklistView from "@/views/ChecklistView.vue";
import BoardView from "@/views/BoardView.vue";
import ReviewView from "@/views/ReviewView.vue";
import LikedReviewView from "@/views/LikedReviewView.vue";

import { useUserStore } from "@/stores/userStore";

const userStore = useUserStore();

// 현재 보여줄 화면 상태
const currentView = ref("landing");
const previousView = ref("landing"); // 이전 화면 기억용 (뒤로가기 시 사용)

// ★ [수정] 카카오맵 로딩 로직을 onMounted 안으로 깔끔하게 정리했습니다.
onMounted(() => {
  userStore.checkLoginStatus();

  // 카카오맵 스크립트 중복 로딩 방지
  if (!document.getElementById("kakao-map-script")) {
    const script = document.createElement("script");
    script.id = "kakao-map-script";
    // autoload=false 필수!
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=${import.meta.env.VITE_KAKAO_MAP_KEY}&libraries=services,clusterer,drawing`;
    
    script.onload = () => console.log("✅ 카카오 스크립트 로드 완료 (대기 모드)");
    document.head.appendChild(script);
  }
});

// 찐후기 페이지로 이동 (어디서 왔는지 저장)
const goToReview = (from) => {
  previousView.value = from; // 'landing' 또는 'board' 저장
  currentView.value = "review";
};

// 현재 선택된 여행 ID (0이면 일반, 숫자면 여행용)
const currentPlanId = ref(0);

// 체크리스트로 이동
const goChecklist = (planId = 0) => {
  currentPlanId.value = planId;
  currentView.value = "checklist";
};

// 체크리스트에서 뒤로가기 (스마트 네비게이션)
const handleChecklistBack = () => {
  if (currentPlanId.value === 0) {
    currentView.value = "landing"; // 일반이면 메인으로
  } else {
    currentView.value = "planning"; // 여행용이면 계획 목록으로
  }
};
</script>

<template>
  <Transition name="fade" mode="out-in">
    <LandingPage
      v-if="currentView === 'landing'"
      @start="currentView = 'planning'"
      @my-page="currentView = 'mypage'"
      @go-check="goChecklist(0)" 
      @go-board="currentView = 'board'"
      @go-review="goToReview('landing')"
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
      :plan-id="currentPlanId"
      @back="handleChecklistBack"
    />

    <BoardView
      v-else-if="currentView === 'board'"
      @back="currentView = 'landing'"
      @go-review="goToReview('board')"
      @go-mypage="currentView = 'mypage'"
      @go-liked-reviews="currentView = 'liked-reviews'"
    />

    <ReviewView
      v-else-if="currentView === 'review'"
      @back="currentView = previousView"
    />

    <LikedReviewView
      v-else-if="currentView === 'liked-reviews'"
      @back="currentView = 'board'"
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