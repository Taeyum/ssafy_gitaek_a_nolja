<script setup>
  import { defineEmits, onMounted, onUnmounted } from 'vue'
  import { ArrowLeft, Mail, Calendar, LogOut, ArrowRightCircle, Trash2, Users, Shield } from 'lucide-vue-next'
  import { useUserStore } from '@/stores/userStore'
  import { useTripStore } from '@/stores/tripStore'
  
  const emit = defineEmits(['back', 'go-edit', 'go-plan', 'go-admin'])
  const userStore = useUserStore()
  const tripStore = useTripStore()
  
  let pollingInterval = null 
  
  const handleLogout = () => {
    if (confirm('정말 로그아웃 하시겠습니까?')) {
      userStore.logout()
      emit('back') 
    }
  }
  
  const handleTripClick = (trip) => {
    tripStore.loadTrip(trip)
    emit('go-plan')
  }
  
  // 삭제 OR 나가기 핸들러
  const handleDeleteOrLeave = async (trip) => {
    const isOwner = trip.ownerId === userStore.userInfo.id
  
    if (isOwner) {
      if (confirm(`'${trip.title}' 여행을 정말 삭제하시겠습니까?\n모든 데이터가 사라집니다.`)) {
        const success = await tripStore.deleteTrip(trip.tripId)
        if (success) alert("여행이 삭제되었습니다.")
      }
    } else {
      const success = await tripStore.leaveTrip(trip.tripId)
      if (success) alert("여행에서 나갔습니다.")
    }
  }
  
  // 화면 켜지면 실행
  onMounted(() => {
    tripStore.fetchMyTrips()
  
    // 3초마다 목록 새로고침
    pollingInterval = setInterval(() => {
      tripStore.fetchMyTrips()
    }, 3000)
  })
  
  // 화면 꺼지면 폴링 중단
  onUnmounted(() => {
    if (pollingInterval) {
      clearInterval(pollingInterval)
    }
  })
  </script>
  
  <template>
    <div class="min-h-screen bg-gray-50 font-sans">
      <header class="bg-white border-b sticky top-0 z-50">
          <div class="container mx-auto px-6 py-4 flex items-center gap-4">
              <button @click="emit('back')" class="p-2 rounded-full hover:bg-gray-100 text-gray-500 transition-colors">
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
                      <img :src="userStore.userInfo?.profileImg || 'https://placehold.co/150x150'" class="w-32 h-32 rounded-full border-4 border-white bg-white shadow-md object-cover" alt="Profile"/>
                      
                      <div class="flex items-center gap-2">
                        <button 
                          v-if="userStore.userInfo?.role === 'ADMIN'"
                          @click="emit('go-admin')" 
                          class="px-4 py-2 bg-gray-900 text-white border border-gray-900 rounded-xl text-sm font-bold hover:bg-gray-700 transition-colors flex items-center gap-1 shadow-md"
                        >
                          <Shield class="w-4 h-4" /> 관리자
                        </button>
  
                        <button @click="emit('go-edit')" class="px-4 py-2 border border-gray-200 rounded-xl text-sm font-bold hover:bg-gray-50 transition-colors">
                          프로필 수정
                        </button>
                      </div>
                  </div>
  
                  <h2 class="text-3xl font-bold text-gray-900 mb-1 flex items-center gap-2">
                    {{ userStore.userInfo?.nickname || userStore.userInfo?.name }}
                    
                    <span v-if="userStore.userInfo?.role === 'ADMIN'" class="bg-gray-900 text-white text-[10px] px-2 py-1 rounded-full flex items-center gap-1 align-middle shadow-sm">
                        <Shield class="w-3 h-3" /> ADMIN
                    </span>
                  </h2>
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
              @click.stop="handleDeleteOrLeave(trip)"
              class="absolute top-4 right-4 p-2 text-gray-300 hover:text-red-500 transition-colors z-10 hover:bg-red-50 rounded-full"
              :title="trip.ownerId === userStore.userInfo.id ? '여행 삭제' : '여행 나가기'"
            >
              <Trash2 v-if="trip.ownerId === userStore.userInfo.id" class="w-5 h-5" />
              <LogOut v-else class="w-5 h-5" />
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