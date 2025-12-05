<script setup>
import { defineEmits } from 'vue'
import { ArrowLeft, User, Mail, MapPin, Calendar, LogOut } from 'lucide-vue-next'
import { useUserStore } from '@/stores/userStore'

const emit = defineEmits(['back'])
const userStore = useUserStore()

const handleLogout = () => {
  if (confirm('정말 로그아웃 하시겠습니까?')) {
    userStore.logout()
    emit('back') // 로그아웃 후 메인으로 이동
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-sans">
    <header class="bg-white border-b sticky top-0 z-50">
      <div class="container mx-auto px-6 py-4 flex items-center gap-4">
        <button 
          @click="emit('back')" 
          class="p-2 rounded-full hover:bg-gray-100 text-gray-500 transition-colors"
        >
          <ArrowLeft class="h-6 w-6" />
        </button>
        <h1 class="text-xl font-bold text-gray-900">마이 페이지</h1>
      </div>
    </header>

    <div class="container mx-auto px-6 py-12 max-w-4xl">
      <div class="bg-white rounded-3xl shadow-xl overflow-hidden mb-8">
        <div class="h-32 bg-gradient-to-r from-[#DE2E5F] to-[#b01e45]"></div>
        <div class="px-8 pb-8">
          <div class="relative flex justify-between items-end -mt-12 mb-6">
            <img 
              :src="userStore.userInfo?.profileImg" 
              class="w-32 h-32 rounded-full border-4 border-white bg-white shadow-md object-cover"
              alt="Profile"
            />
            <button 
              class="px-4 py-2 border border-gray-200 rounded-xl text-sm font-bold hover:bg-gray-50 transition-colors"
            >
              프로필 수정
            </button>
          </div>
          
          <h2 class="text-3xl font-bold text-gray-900 mb-1">
            {{ userStore.userInfo?.name }}
          </h2>
          <div class="flex items-center gap-2 text-gray-500 mb-6">
            <Mail class="w-4 h-4" />
            {{ userStore.userInfo?.email }}
          </div>

          <div class="flex gap-4 border-t pt-6">
            <div class="text-center px-6">
              <div class="text-2xl font-bold text-[#DE2E5F]">3</div>
              <div class="text-xs text-gray-500 font-medium">다녀온 여행</div>
            </div>
            <div class="text-center px-6 border-l">
              <div class="text-2xl font-bold text-gray-900">12</div>
              <div class="text-xs text-gray-500 font-medium">작성한 리뷰</div>
            </div>
          </div>
        </div>
      </div>

      <h3 class="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
        <Calendar class="w-5 h-5 text-[#DE2E5F]" />
        최근 여행 기록
      </h3>
      
      <div class="grid md:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h4 class="font-bold text-lg text-gray-900">우정 파괴 제주 여행</h4>
              <p class="text-sm text-gray-500">2024.02.15 - 2024.02.18</p>
            </div>
            <span class="bg-green-100 text-green-700 text-xs font-bold px-2 py-1 rounded-full">완료됨</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <MapPin class="w-4 h-4" />
            제주도 서귀포시 외 5곳
          </div>
        </div>

        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h4 class="font-bold text-lg text-gray-900">부산 먹방 투어</h4>
              <p class="text-sm text-gray-500">2023.12.24 - 2023.12.25</p>
            </div>
            <span class="bg-green-100 text-green-700 text-xs font-bold px-2 py-1 rounded-full">완료됨</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <MapPin class="w-4 h-4" />
            부산 해운대구 외 8곳
          </div>
        </div>
      </div>

      <div class="mt-12 text-center">
        <button 
          @click="handleLogout"
          class="inline-flex items-center gap-2 text-gray-400 hover:text-red-500 transition-colors font-medium text-sm"
        >
          <LogOut class="w-4 h-4" />
          로그아웃
        </button>
      </div>
    </div>
  </div>
</template>