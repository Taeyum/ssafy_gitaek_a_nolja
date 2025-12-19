<script setup>
  import { ref, onMounted } from 'vue'
  import LandingPage from '@/views/LandingPage.vue'
  import PlanningDashboard from '@/views/PlanningDashboard.vue'
  import MyPage from '@/views/MyPage.vue'
  import ProfileEdit from '@/views/ProfileEdit.vue'
  import AdminDashboard from '@/views/AdminDashboard.vue' // ★ 관리자 페이지 import 확인!
  
  import { useUserStore } from '@/stores/userStore'
  
  const userStore = useUserStore()
  
  onMounted(() => {
    userStore.checkLoginStatus()
  })
  
  // 현재 보여줄 화면 상태 (landing, planning, mypage, profile-edit, admin)
  const currentView = ref('landing')
  </script>
  
  <template>
    <Transition name="fade" mode="out-in">
      <LandingPage 
        v-if="currentView === 'landing'" 
        @start="currentView = 'planning'" 
        @my-page="currentView = 'mypage'" 
      />
      
      <PlanningDashboard 
        v-else-if="currentView === 'planning'" 
        @back="currentView = 'landing'"
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