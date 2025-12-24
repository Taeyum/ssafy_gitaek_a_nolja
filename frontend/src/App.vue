<script setup>
import { ref, onMounted } from "vue";
import LandingPage from "@/views/LandingPage.vue";
import PlanningDashboard from "@/views/PlanningDashboard.vue";
import MyPage from "@/views/MyPage.vue";
import ProfileEdit from "@/views/ProfileEdit.vue";
import AdminDashboard from "@/views/AdminDashboard.vue"; // ★ 관리자 페이지 import 확인!
// [추가] 체크리스트 컴포넌트 가져오기
import ChecklistView from "@/views/ChecklistView.vue";
import BoardView from "@/views/BoardView.vue";
// ★ [수정 1] 찐후기 페이지(ReviewView)를 가져오는 코드가 빠져있어서 추가했습니다!
import ReviewView from "@/views/ReviewView.vue";
import LikedReviewView from "@/views/LikedReviewView.vue"; // ★ [추가]


import { useUserStore } from "@/stores/userStore";

const userStore = useUserStore();

onMounted(() => {
  userStore.checkLoginStatus();

// 현재 보여줄 화면 상태 (landing, planning, mypage, profile-edit, admin)
const currentView = ref("landing");

const previousView = ref("landing"); // 이전 화면 기억용

const goToReview = (from) => {
  previousView.value = from; // 어디서 왔는지 저장 ('landing' or 'board')
  currentView.value = "review"; // 화면 전환
};

// ★ [추가 1] 현재 선택된 여행 ID (0이면 일반 체크리스트, 숫자가 있으면 여행용)
const currentPlanId = ref(0);

  // 중복 로딩 방지
  if (!document.getElementById("kakao-map-script")) {
    const script = document.createElement("script");
    script.id = "kakao-map-script";
    
    // ★ [핵심 수정] autoload=false를 다시 넣어야 합니다!
    // libraries=... 앞에 &autoload=false 추가
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=${import.meta.env.VITE_KAKAO_MAP_KEY}&libraries=services,clusterer,drawing`;
    
    script.onload = () => console.log("✅ 카카오 스크립트 로드 완료 (대기 모드)");
    document.head.appendChild(script);
    }
  });
  const currentView = ref("landing");

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
  } else {
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
      @back="currentView = 'landing'"
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
