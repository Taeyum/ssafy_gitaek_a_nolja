<script setup>
import { ref, onMounted } from 'vue'
import LandingPage from '@/views/LandingPage.vue'
import PlanningDashboard from '@/views/PlanningDashboard.vue'
import MyPage from '@/views/MyPage.vue'
import ProfileEdit from '@/views/ProfileEdit.vue' // ★ 추가됨
import { useUserStore } from '@/stores/userStore' // 추가


const userStore = useUserStore()

// 앱이 시작될 때 로그인 상태 확인
onMounted(() => {
  userStore.checkLoginStatus()
})
// 현재 화면 상태 ('landing' 또는 'planning')
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
    />

    <ProfileEdit
      v-else-if="currentView === 'profile-edit'"
      @back="currentView = 'mypage'"
    />
  </Transition>
</template>

<style>
/* 부드러운 화면 전환 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>