<script setup>
  import { ref, onMounted } from "vue";
  import LandingPage from "@/views/LandingPage.vue";
  import PlanningDashboard from "@/views/PlanningDashboard.vue";
  import MyPage from "@/views/MyPage.vue";
  import ProfileEdit from "@/views/ProfileEdit.vue";
  import AdminDashboard from "@/views/AdminDashboard.vue"; 
  import ChecklistView from "@/views/ChecklistView.vue";
  
  import { useUserStore } from "@/stores/userStore";
  
  const userStore = useUserStore();
  
  onMounted(() => {
  userStore.checkLoginStatus();

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
  const currentPlanId = ref(0);
  
  const goChecklist = (planId = 0) => {
    currentPlanId.value = planId;
    currentView.value = "checklist";
  };
  
  // 체크리스트에서 뒤로가기 (스마트 네비게이션)
  const handleChecklistBack = () => {
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
        @go-check="goChecklist" 
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
        @back="handleChecklistBack"
      />
    </Transition>
  </template>
  
  <style>
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.3s ease;
  }
  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }
  </style>