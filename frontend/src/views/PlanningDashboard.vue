<script setup>
import { ref, defineEmits, onMounted, onUnmounted } from 'vue'
import { MapPin, Search, Sparkles, Edit3, Lock, X, Plus, ArrowLeft, CheckCircle, Calendar, Clock, Type , Copy} from 'lucide-vue-next'
import MapArea from '@/components/MapArea.vue'
import ItineraryList from '@/components/ItineraryList.vue'
import ChatInterface from '@/components/ChatInterface.vue'
import { useTripStore } from '@/stores/tripStore'
import { getAttractionsApi } from '@/api/attraction'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
const tripStore = useTripStore()

const emit = defineEmits(['back'])
const mapAreaRef = ref(null)

// 상태 관리
const isEditing = ref(false)
const activeTab = ref('itinerary') 
const showAiModal = ref(false)
const modalPoiId = ref(0) // ★ 추가

// 검색 및 데이터 관련
const searchQuery = ref("")
const searchResults = ref([])
const isSearching = ref(false)
const allAttractions = ref([]) // ★ 전체 관광지 데이터 (DB에서 온 것)


// 모달 관련 상태
const showModal = ref(false)
const modalMode = ref('add')
const editTargetId = ref(null)
const modalDayId = ref("1")
const modalTime = ref("14:00")
const modalName = ref("")
const modalAddress = ref("")
const modalLat = ref(0)
const modalLng = ref(0)

// ★ 화면 켜지자마자 실행 & 폴링 시작
onMounted(async () => {
  await loadRealData()
  // 내 ID를 넘겨줘서 내가 에디터인지 남이 에디터인지 판별 (폴링 시작)
  if (userStore.userInfo && userStore.userInfo.id) {
      tripStore.startPolling(userStore.userInfo.id)
  }
})

// ★ 화면 꺼지면 폴링 종료 (필수! 성능 저하 방지)
onUnmounted(() => {
  tripStore.stopPolling()
})

// ★ 백엔드에서 데이터 가져와서 지도에 뿌리기
const loadRealData = async () => {
  try {
    // areaCode: 0 (전체 조회) or 1 (서울) 등으로 변경 가능
    const response = await getAttractionsApi({ areaCode: 0 }) 
    
    // 1. 데이터 저장
    allAttractions.value = response.data
    console.log(`관광지 ${allAttractions.value.length}개 로드 완료!`)

    // 2. 지도에 마커 찍기 (MapArea의 함수 호출)
    if (mapAreaRef.value) {
      mapAreaRef.value.setMarkers(allAttractions.value)
    }
  } catch (error) {
    console.error("관광지 불러오기 실패:", error)
  }
}

// ★ 검색 기능 (DB 데이터인 allAttractions에서 필터링)
const handleSearch = () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  isSearching.value = true
  
  // 1600개 정도는 프론트에서 필터링해도 충분히 빠릅니다.
  setTimeout(() => {
    searchResults.value = allAttractions.value.filter(p => 
      p.name.includes(searchQuery.value) || (p.address && p.address.includes(searchQuery.value))
    )
    isSearching.value = false
  }, 200)
}

// 지도 이동 (DB 컬럼명 -> MapArea 함수 호출)
const moveToLocation = (place) => {
  const lat = place.latitude || place.lat
  const lng = place.longitude || place.lng
  
  if(mapAreaRef.value && lat && lng) {
    mapAreaRef.value.moveCamera(lat, lng)
  }
}

// 검색 결과에서 [추가] 클릭 시
const openAddModalFromSearch = (place) => {
  // ★ [수정] 수정 권한 체크 (isEditing 변수는 이제 로컬이 아니라 store 상태에 따라감)
  if (!isEditing.value) {
    alert("편집 모드에서만 추가할 수 있습니다.\n상단의 [수정 권한 요청] 버튼을 먼저 눌러주세요!");
    return;
  }

  modalMode.value = 'add'
  // tripStore에 일정이 하나도 없으면 '1'로 fallback
  modalDayId.value = tripStore.itinerary[0]?.id || "1"
  modalTime.value = "14:00"
    
  // 데이터 채우기
  modalName.value = place.name
  modalAddress.value = place.address || "주소 미상"
  modalLat.value = place.latitude || 0
  modalLng.value = place.longitude || 0
  modalPoiId.value = place.poiId

  showModal.value = true
}

// [직접 추가하기] 버튼
const openManualAddModal = () => {
  // ★ [수정] 권한 체크
  if (!isEditing.value) {
    alert("편집 모드가 아닙니다.");
    return;
  }

  modalMode.value = 'add'
  modalDayId.value = tripStore.itinerary[0]?.id || "1"
  modalTime.value = "12:00"
  
  modalName.value = ""
  modalAddress.value = "사용자 지정 장소"
  modalLat.value = 0
  modalLng.value = 0
  
  showModal.value = true
}

// 리스트에서 [수정] 버튼
const openEditModal = (dayId, item) => {
  modalMode.value = 'edit'
  editTargetId.value = item.id
  
  modalDayId.value = dayId
  modalTime.value = item.time
  modalName.value = item.name
  modalAddress.value = item.location
  
  showModal.value = true
}

// 모달 확인 (저장/수정)
const handleModalConfirm = () => {
  if (!modalName.value.trim()) {
    alert("장소 이름을 입력해주세요!")
    return
  }

  if (modalMode.value === 'add') {
    const placeData = {
      name: modalName.value,
      address: modalAddress.value,
      lat: modalLat.value,
      lng: modalLng.value,
      poiId: modalPoiId.value
    }
    tripStore.addPlace(modalDayId.value, placeData, modalTime.value)
  } else {
    tripStore.editItem(modalDayId.value, editTargetId.value, modalTime.value, modalName.value)
  }

  showModal.value = false
  // 검색어 초기화 (선택사항)
  // searchQuery.value = ""
  // searchResults.value = []
  activeTab.value = 'itinerary'
}

// ★ [수정] 권한 요청 버튼 클릭 시
const handleRequestEdit = async () => {
    const success = await tripStore.tryRequestEdit()
    if (success) {
        isEditing.value = true
    }
}

// ★ [수정] 권한 반납(완료) 버튼 클릭 시
const handleFinishEdit = async () => {
    await tripStore.finishEdit()
    isEditing.value = false
}

const copyInviteCode = () => {
  if (tripStore.tripInfo.inviteCode) {
    navigator.clipboard.writeText(tripStore.tripInfo.inviteCode)
    alert(`초대 코드 [${tripStore.tripInfo.inviteCode}] 복사 완료!`)
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
            <button @click="copyInviteCode" class="flex items-center gap-1 text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded-md transition-colors text-gray-600">
              <Copy class="w-3 h-3" />
              코드: {{ tripStore.tripInfo.inviteCode || 'CODE' }}
            </button>
          </div>
        </div>
      </div>

      <div class="flex items-center gap-3">
         <div v-if="isEditing" class="flex items-center gap-2">
            <div class="px-4 py-2 bg-green-50 text-green-700 font-bold rounded-full animate-pulse border border-green-200">
                ✏️ 수정 중...
            </div>
            <button @click="handleFinishEdit" class="bg-gray-800 text-white px-4 py-2 rounded-full font-bold hover:bg-gray-700 transition-colors shadow-md">
                완료
            </button>
         </div>

         <div v-else-if="tripStore.isLocked" class="flex items-center gap-2 px-4 py-2 bg-red-50 text-red-600 rounded-full font-bold border border-red-100">
            <Lock class="w-4 h-4" />
            <span>{{ tripStore.currentEditorName || '누군가' }}가 수정 중입니다</span>
         </div>

         <button v-else @click="handleRequestEdit" class="flex items-center gap-2 px-4 py-2 border-2 border-gray-200 rounded-full hover:border-[#DE2E5F] hover:text-[#DE2E5F] transition-all font-semibold text-gray-600">
            <Edit3 class="h-4 w-4" />
            수정 권한 요청
         </button>
      </div>
    </header>

    <div class="flex-1 flex overflow-hidden">
        <div class="w-[65%] relative">
             <MapArea ref="mapAreaRef"
             @add-to-plan="openAddModalFromSearch" />
             
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
                             <div v-for="place in searchResults" :key="place.poiId" class="p-4 hover:bg-gray-50 flex justify-between items-center border-b last:border-none transition-colors group">
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