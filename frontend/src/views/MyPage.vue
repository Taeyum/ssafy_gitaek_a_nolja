<script setup>
import { defineEmits, onMounted } from 'vue'
import { ArrowLeft, User, Mail, MapPin, Calendar, LogOut, ArrowRightCircle, Trash2, Users } from 'lucide-vue-next'
import { useUserStore } from '@/stores/userStore'
import { useTripStore } from '@/stores/tripStore'

const emit = defineEmits(['back', 'go-edit', 'go-plan'])
const userStore = useUserStore()
const tripStore = useTripStore()

const handleLogout = () => {
  if (confirm('정말 로그아웃 하시겠습니까?')) {
    userStore.logout()
    emit('back') 
  }
}

const handleTripClick = (trip) => {
  // 1. 스토어에 해당 여행 정보 장전
  tripStore.loadTrip(trip)
  // 2. App.vue에게 화면 전환 요청
  emit('go-plan')
}

// ★ [추가됨] 여행 삭제 핸들러
const handleDeleteTrip = async (tripId) => {
  if (confirm("정말 이 여행 계획을 삭제하시겠습니까?")) {
    const success = await tripStore.deleteTrip(tripId)
    if (success) {
      alert("여행이 삭제되었습니다.")
    }
  }
}

// 화면 켜지면 여행 목록 불러오기
onMounted(() => {
  tripStore.fetchMyTrips()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-sans">
    <header class="bg-white border-b sticky top-0 z-50">
        <div class="container mx-auto px-6 py-4 flex items-center gap-4">
            <button @click="emit('back')" class="p-2 rounded-full hover:bg-gray-100 text-gray-500 transition-colors"><ArrowLeft class="h-6 w-6" /></button>
            <h1 class="text-xl font-bold text-gray-900">마이 페이지</h1>
        </div>
    </header>

    <div class="container mx-auto px-6 py-12 max-w-4xl">
        <div class="bg-white rounded-3xl shadow-xl overflow-hidden mb-8">
            <div class="h-32 bg-gradient-to-r from-[#DE2E5F] to-[#b01e45]"></div>
            <div class="px-8 pb-8">
                <div class="relative flex justify-between items-end -mt-12 mb-6">
                    <img :src="userStore.userInfo?.profileImg" class="w-32 h-32 rounded-full border-4 border-white bg-white shadow-md object-cover" alt="Profile"/>
                    <button @click="emit('go-edit')" class="px-4 py-2 border border-gray-200 rounded-xl text-sm font-bold hover:bg-gray-50 transition-colors">프로필 수정</button>
                </div>
                <h2 class="text-3xl font-bold text-gray-900 mb-1">{{ userStore.userInfo?.name }}</h2>
                <div class="flex items-center gap-2 text-gray-500 mb-6"><Mail class="w-4 h-4" />{{ userStore.userInfo?.email }}</div>
            </div>
        </div>

      <h3 class="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
        <Calendar class="w-5 h-5 text-[#DE2E5F]" />
        나의 여행 기록 ({{ tripStore.myTrips.length }})
      </h3>
      
      <div v-if="tripStore.myTrips.length > 0" class="grid md:grid-cols-2 gap-6">
        <div 
          v-for="trip in tripStore.myTrips" 
          :key="trip.tripId"
          class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md hover:border-[#DE2E5F] transition-all group relative cursor-pointer"
          @click="handleTripClick(trip)"
        >
          <button 
            @click.stop="handleDeleteTrip(trip.tripId)"
            class="absolute top-4 right-4 p-2 text-gray-300 hover:text-red-500 transition-colors z-10 hover:bg-red-50 rounded-full"
            title="여행 삭제"
          >
            <Trash2 class="w-5 h-5" />
          </button>

          <div class="flex justify-between items-start mb-4 pr-8">
            <div>
              <h4 class="font-bold text-lg text-gray-900 group-hover:text-[#DE2E5F] transition-colors">
                {{ trip.title }}
              </h4>
              <p class="text-sm text-gray-500 mt-1">
                {{ trip.startDate }} ~ {{ trip.endDate }}
              </p>
            </div>
          </div>
          
          <div class="flex items-center justify-between mt-6">
             <div class="flex items-center gap-2">
                <span class="bg-gray-100 text-gray-600 text-xs font-bold px-2 py-1 rounded-full uppercase">
                    {{ trip.style || 'TRIP' }}
                </span>
                <span class="bg-pink-50 text-[#DE2E5F] text-xs font-bold px-2 py-1 rounded-full flex items-center gap-1">
                    <Users class="w-3 h-3" />
                    {{ trip.currentParticipants }} / {{ trip.maxParticipants }}
                </span>
             </div>
             <ArrowRightCircle class="w-5 h-5 text-gray-300 group-hover:text-[#DE2E5F] transition-colors" />
          </div>
        </div>
      </div>

      <div v-else class="text-center py-12 bg-white rounded-3xl border border-dashed border-gray-200">
        <p class="text-gray-400 mb-4">아직 계획된 여행이 없습니다.</p>
        <button @click="emit('back')" class="text-[#DE2E5F] font-bold hover:underline">
            여행 계획하러 가기
        </button>
      </div>

      <div class="mt-12 text-center">
        <button @click="handleLogout" class="inline-flex items-center gap-2 text-gray-400 hover:text-red-500 transition-colors font-medium text-sm">
          <LogOut class="w-4 h-4" /> 로그아웃
        </button>
      </div>
    </div>
  </div>
</template>