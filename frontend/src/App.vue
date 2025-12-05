<script setup>
import { ref } from 'vue'
import LandingPage from '@/views/LandingPage.vue'
import PlanningDashboard from '@/views/PlanningDashboard.vue'
import MyPage from '@/views/MyPage.vue'

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