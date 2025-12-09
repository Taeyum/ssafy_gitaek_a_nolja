<script setup>
import { ref, defineEmits } from 'vue'
import { MapPin, Search, Sparkles, Edit3, Lock, X, Plus, ArrowLeft, CheckCircle, Calendar, Clock, Type , Copy} from 'lucide-vue-next'
import MapArea from '@/components/MapArea.vue'
import ItineraryList from '@/components/ItineraryList.vue'
import ChatInterface from '@/components/ChatInterface.vue'
import { useTripStore } from '@/stores/tripStore'

const emit = defineEmits(['back'])
const mapAreaRef = ref(null)

// 상태 관리
const isEditing = ref(false)
const currentEditor = ref(null)
const activeTab = ref('itinerary') 
const showAiModal = ref(false)

// 검색 관련
const searchQuery = ref("")
const searchResults = ref([])
const isSearching = ref(false)

// ▼▼▼ [업그레이드] 일정 모달 상태 (추가/수정 통합) ▼▼▼
const showModal = ref(false)
const modalMode = ref('add') // 'add' 또는 'edit'
const editTargetId = ref(null) // 수정할 아이템 ID

// 모달 입력값들
const modalDayId = ref("1")
const modalTime = ref("14:00")
const modalName = ref("") // 장소 이름 (직접 입력 가능)
const modalAddress = ref("") // 주소
const modalLat = ref(0)
const modalLng = ref(0)

const mockPlaces = [
  { id: 1, name: "성산일출봉", address: "제주 서귀포시 성산읍", lat: 33.4582, lng: 126.9427 },
  { id: 2, name: "함덕해수욕장", address: "제주 제주시 조천읍", lat: 33.5434, lng: 126.6697 },
  { id: 3, name: "제주국제공항", address: "제주 제주시 공항로", lat: 33.5104, lng: 126.4913 },
  { id: 4, name: "협재해수욕장", address: "제주 제주시 한림읍", lat: 33.3938, lng: 126.2396 },
  { id: 5, name: "한라산 백록담", address: "제주 서귀포시 토평동", lat: 33.3617, lng: 126.5292 },
]

const tripStore = useTripStore()

// 1. 검색 결과에서 [추가] 클릭 시
const openAddModalFromSearch = (place) => {
  modalMode.value = 'add'
  modalDayId.value = tripStore.itinerary[0].id
  modalTime.value = "14:00"
  
  // 검색된 정보 채우기
  modalName.value = place.name
  modalAddress.value = place.address
  modalLat.value = place.lat
  modalLng.value = place.lng
  
  showModal.value = true
}

// 2. 하단 [직접 추가하기] 버튼 클릭 시
const openManualAddModal = () => {
  modalMode.value = 'add'
  modalDayId.value = tripStore.itinerary[0].id
  modalTime.value = "12:00"
  
  // 빈 정보로 시작
  modalName.value = ""
  modalAddress.value = "사용자 지정 장소"
  modalLat.value = 0
  modalLng.value = 0
  
  showModal.value = true
}

// 3. 리스트에서 [수정] 아이콘 클릭 시
const openEditModal = (dayId, item) => {
  modalMode.value = 'edit'
  editTargetId.value = item.id
  
  // 기존 정보 채우기
  modalDayId.value = dayId
  modalTime.value = item.time
  modalName.value = item.name
  modalAddress.value = item.location
  
  showModal.value = true
}

// 4. 모달 [확인] 클릭 (저장 로직 분기)
const handleModalConfirm = () => {
  if (!modalName.value.trim()) {
    alert("장소 이름을 입력해주세요!")
    return
  }

  if (modalMode.value === 'add') {
    // 추가 로직
    const placeData = {
      name: modalName.value,
      address: modalAddress.value,
      lat: modalLat.value,
      lng: modalLng.value
    }
    tripStore.addPlace(modalDayId.value, placeData, modalTime.value)
  } else {
    // 수정 로직
    tripStore.editItem(modalDayId.value, editTargetId.value, modalTime.value, modalName.value)
  }

  // 정리 및 닫기
  showModal.value = false
  searchResults.value = []
  searchQuery.value = ""
  activeTab.value = 'itinerary'
}

// ... (기존 검색/지도 핸들러들 유지) ...
const handleSearch = () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  isSearching.value = true
  setTimeout(() => {
    searchResults.value = mockPlaces.filter(p => p.name.includes(searchQuery.value))
    isSearching.value = false
  }, 300)
}

const moveToLocation = (place) => {
  mapAreaRef.value?.moveCamera(place.lat, place.lng)
  searchQuery.value = place.name 
}

const handleRequestEdit = () => {
  isEditing.value = true
  currentEditor.value = "나"
}

const handleFinishEdit = () => {
  isEditing.value = false
  currentEditor.value = null
}

const copyInviteCode = () => {
  if (tripStore.tripInfo.inviteCode) {
    navigator.clipboard.writeText(tripStore.tripInfo.inviteCode)
    alert(`초대 코드 [${tripStore.tripInfo.inviteCode}]가 복사되었습니다!\n친구에게 공유하세요.`)
  } else {
    alert("초대 코드가 없습니다.")
  }
}

</script>

<template>
  <div class="h-screen flex flex-col bg-gray-50 overflow-hidden">
    
    <header class="bg-white px-6 py-4 flex items-center justify-between shadow-sm border-b z-20">
      <div class="flex items-center gap-4">
        <button @click="emit('back')" class="p-2 rounded-full hover:bg-gray-100 text-gray-500 transition-colors">
          <ArrowLeft class="h-6 w-6" />
        </button>
        <div class="w-10 h-10 rounded-2xl bg-[#DE2E5F] flex items-center justify-center shadow-lg shadow-pink-200">
          <MapPin class="h-5 w-5 text-white" />
        </div>
        <div>
          <h1 class="text-xl font-bold text-gray-900">{{ tripStore.tripInfo.title }}</h1>
          
          <div class="flex items-center gap-3 text-sm text-gray-500 mt-1">
            <div class="flex items-center gap-1 font-medium">
              <span class="text-[#DE2E5F]">{{ tripStore.tripInfo.currentParticipants || 1 }}</span>
              <span>/</span>
              <span>{{ tripStore.tripInfo.maxMembers }}명 참여 중</span>
            </div>

            <div class="h-3 w-[1px] bg-gray-300"></div>

            <button 
              @click="copyInviteCode"
              class="flex items-center gap-1 text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded-md transition-colors text-gray-600"
            >
              <Copy class="w-3 h-3" />
              코드: {{ tripStore.tripInfo.inviteCode || 'CODE' }}
            </button>
          </div>
        </div>
      </div>

      <div class="flex items-center gap-3">
         <div v-if="isEditing" class="flex items-center gap-2">
          <div class="flex items-center gap-2 px-4 py-2 bg-green-50 border border-green-200 rounded-full animate-pulse">
            <Edit3 class="h-4 w-4 text-green-600" />
            <span class="text-sm font-bold text-green-700">수정 중</span>
          </div>
          <button @click="handleFinishEdit" class="flex items-center gap-1 px-4 py-2 bg-gray-800 text-white rounded-full hover:bg-gray-700 transition-colors text-sm font-bold shadow-md">
            <CheckCircle class="h-4 w-4" />
            완료
          </button>
        </div>
        <div v-else-if="currentEditor" class="flex items-center gap-2 px-4 py-2 bg-gray-100 rounded-full">
          <Lock class="h-4 w-4 text-gray-500" />
          <span class="text-sm text-gray-500">{{ currentEditor }}님이 수정 중...</span>
        </div>
        <button v-else @click="handleRequestEdit" class="flex items-center gap-2 px-4 py-2 border-2 border-gray-200 rounded-full hover:border-[#DE2E5F] hover:text-[#DE2E5F] transition-all font-semibold text-gray-600">
          <Edit3 class="h-4 w-4" />
          수정 권한 요청
        </button>
      </div>
    </header>

    <div class="flex-1 flex overflow-hidden">
        <div class="w-[65%] relative">
             <MapArea ref="mapAreaRef" />
             <div class="absolute top-6 left-6 right-6 z-10 flex flex-col gap-3 pointer-events-none">
                <div class="pointer-events-auto relative max-w-xl">
                    <div class="absolute inset-0 bg-white/80 backdrop-blur-md rounded-2xl shadow-xl"></div>
                    <div class="relative p-3">
                         <div class="flex items-center gap-2 bg-white/50 rounded-xl px-2 border border-transparent focus-within:border-[#DE2E5F] transition-all">
                             <Search class="h-5 w-5 text-gray-400 ml-2" />
                            <input v-model="searchQuery" @keydown.enter="handleSearch" type="text" placeholder="여행지 검색" class="w-full bg-transparent border-none focus:outline-none text-base py-3 placeholder-gray-400" />
                            <button @click="handleSearch" class="bg-[#DE2E5F] text-white px-4 py-2 rounded-lg text-sm font-bold hover:bg-[#c92552] transition-colors whitespace-nowrap">검색</button>
                        </div>
                         <div v-if="searchResults.length > 0" class="mt-3 bg-white rounded-xl shadow-lg border border-gray-100 max-h-64 overflow-y-auto animate-in fade-in slide-in-from-top-2">
                             <div v-for="place in searchResults" :key="place.id" class="p-4 hover:bg-gray-50 flex justify-between items-center border-b last:border-none transition-colors group">
                                  <div @click="moveToLocation(place)" class="cursor-pointer flex-1">
                                    <div class="font-bold text-gray-900">{{ place.name }}</div>
                                    <div class="text-xs text-gray-500 mt-0.5">{{ place.address }}</div>
                                  </div>
                                  <div class="flex items-center gap-2">
                                    <button @click="moveToLocation(place)" class="p-2 text-gray-400 hover:text-[#DE2E5F] rounded-full hover:bg-pink-50 transition-colors">
                                      <MapPin class="h-4 w-4" />
                                    </button>
                                    <button v-if="isEditing" @click="openAddModalFromSearch(place)" class="bg-[#DE2E5F] text-white p-2 rounded-lg text-xs font-bold hover:bg-[#c92552] shadow-md flex items-center gap-1 transition-all active:scale-95">
                                      <Plus class="h-4 w-4" />
                                      추가
                                    </button>
                                  </div>
                                </div>
                             </div>
                    </div>
                 </div>
                 <button @click="showAiModal = true" class="pointer-events-auto w-48 bg-white/90 backdrop-blur-md text-[#DE2E5F] hover:bg-white shadow-xl rounded-2xl h-12 flex items-center justify-center gap-2 text-base font-bold border-2 border-white transition-all">
                    <Sparkles class="h-5 w-5" />
                    AI 추천 받기
                 </button>
             </div>
        </div>
        
        <div class="w-[35%] bg-white shadow-2xl z-10 flex flex-col border-l border-gray-100">
             <div class="flex border-b bg-gray-50">
                <button @click="activeTab = 'itinerary'" class="flex-1 py-4 text-base font-bold transition-colors border-b-2" :class="activeTab === 'itinerary' ? 'bg-white text-[#DE2E5F] border-[#DE2E5F]' : 'text-gray-400 border-transparent hover:text-gray-600'">일정</button>
                <button @click="activeTab = 'chat'" class="flex-1 py-4 text-base font-bold transition-colors border-b-2" :class="activeTab === 'chat' ? 'bg-white text-[#DE2E5F] border-[#DE2E5F]' : 'text-gray-400 border-transparent hover:text-gray-600'">채팅</button>
            </div>
            <div class="flex-1 overflow-hidden relative">
                <ItineraryList 
                    v-if="activeTab === 'itinerary'" 
                    :isEditing="isEditing" 
                    @edit-item="openEditModal" 
                    @open-manual-add="openManualAddModal"
                    class="absolute inset-0" 
                />
                <ChatInterface v-else class="absolute inset-0" />
            </div>
        </div>
    </div>
    
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4 animate-in fade-in duration-200">
        <div class="bg-white rounded-3xl shadow-2xl w-full max-w-sm overflow-hidden p-6 space-y-6">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-bold text-gray-900">
                {{ modalMode === 'add' ? '일정 추가하기' : '일정 수정하기' }}
              </h3>
              <button @click="showModal = false" class="text-gray-400 hover:text-gray-600">
                <X class="w-6 h-6" />
              </button>
            </div>
            
            <div class="space-y-4">
                 <div class="space-y-2">
                     <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
                      <Type class="w-4 h-4 text-[#DE2E5F]" /> 장소 이름
                    </label>
                    <input v-model="modalName" type="text" placeholder="장소 이름" class="w-full p-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all" />
                    <div class="text-xs text-gray-500 flex items-center gap-1 pl-1"><MapPin class="w-3 h-3" /> {{ modalAddress }}</div>
                 </div>
                 
                 <div class="space-y-2">
                    <label class="text-sm font-bold text-gray-700 flex items-center gap-2"><Calendar class="w-4 h-4 text-[#DE2E5F]" /> 날짜 선택</label>
                    <select v-model="modalDayId" :disabled="modalMode === 'edit'" class="w-full p-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all disabled:bg-gray-100">
                      <option v-for="day in tripStore.itinerary" :key="day.id" :value="day.id">{{ day.day }} ({{ day.date }})</option>
                    </select>
                 </div>
                 
                 <div class="space-y-2">
                    <label class="text-sm font-bold text-gray-700 flex items-center gap-2"><Clock class="w-4 h-4 text-[#DE2E5F]" /> 방문 시간</label>
                    <input v-model="modalTime" type="time" class="w-full p-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all" />
                 </div>
            </div>
            
             <div class="flex gap-3 pt-2">
              <button @click="showModal = false" class="flex-1 py-3.5 rounded-xl font-bold text-gray-600 bg-gray-100 hover:bg-gray-200 transition-colors">취소</button>
              <button @click="handleModalConfirm" class="flex-1 py-3.5 rounded-xl font-bold text-white bg-[#DE2E5F] hover:bg-[#c92552] shadow-lg transition-colors">{{ modalMode === 'add' ? '추가하기' : '수정완료' }}</button>
            </div>
         </div>
    </div>
  </div>
</template>