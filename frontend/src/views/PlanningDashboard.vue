<script setup>
import { ref, defineEmits, onMounted, onUnmounted, computed, watch } from "vue";
import {
  MapPin,
  Search,
  Sparkles,
  Edit3,
  Lock,
  X,
  Plus,
  ArrowLeft,
  Calendar,
  Clock,
  Type,
  Copy,
  LogOut,
  Trash2,
} from "lucide-vue-next";
import MapArea from "@/components/MapArea.vue";
import ItineraryList from "@/components/ItineraryList.vue";
import ChatInterface from "@/components/ChatInterface.vue";
import { useTripStore } from "@/stores/tripStore";
import { getAttractionsApi } from "@/api/attraction";
import { useUserStore } from "@/stores/userStore";

const userStore = useUserStore();
const tripStore = useTripStore();

const emit = defineEmits(["back"]);
const mapAreaRef = ref(null);

// 상태 관리
const isEditing = ref(false);
// store의 isLocked 상태를 감지하여 UI 반영
const isLocked = computed(() => tripStore.isLocked);
const currentEditor = computed(() => tripStore.currentEditorName);

const activeTab = ref("itinerary");
const showAiModal = ref(false);
const modalPoiId = ref(0);

// 검색 관련
const searchQuery = ref("");
const searchResults = ref([]);
const isSearching = ref(false);
const allAttractions = ref([]);

// 모달 관련
const showModal = ref(false);
const modalMode = ref("add");
const editTargetId = ref(null);
const modalDayId = ref("1");
const modalTime = ref("14:00");
const modalName = ref("");
const modalAddress = ref("");
const modalLat = ref(0);
const modalLng = ref(0);

// ★ [핵심] 마운트 시 데이터 로드 및 폴링 시작
onMounted(async () => {
  // 1. 관광지 데이터 로드
  await loadRealData();

  // 2. 로그인 정보 확인 후 폴링 시작
  // (새로고침 시 userStore가 아직 로딩 중일 수 있으므로 체크)
  if (userStore.userInfo?.id) {
    tripStore.startPolling(userStore.userInfo.id);
  } else {
    // 정보가 늦게 오면 watch로 감지해서 시작
    const unwatch = watch(
      () => userStore.userInfo,
      (newVal) => {
        if (newVal?.id) {
          tripStore.startPolling(newVal.id);
          unwatch(); // 한 번만 실행하고 감시 종료
        }
      }
    );
  }
});

onUnmounted(async () => {
  // 나가기 전에 내가 수정 중이었다면 권한 반납
  if (isEditing.value) {
    await tripStore.finishEdit();
  }
  tripStore.stopPolling();
});

// 내 권한 상태 동기화 (store -> local)
watch(
  () => tripStore.currentEditorName,
  (val) => {
    isEditing.value = val === "나";
  }
);

const loadRealData = async () => {
  try {
    const response = await getAttractionsApi({ areaCode: 0 });
    allAttractions.value = response.data;
    if (mapAreaRef.value) {
      mapAreaRef.value.setMarkers(allAttractions.value);
    }
  } catch (error) {
    console.error("관광지 데이터 로드 실패", error);
  }
};

const handleSearch = () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = [];
    return;
  }
  isSearching.value = true;
  setTimeout(() => {
    searchResults.value = allAttractions.value.filter(
      (p) =>
        p.name.includes(searchQuery.value) ||
        (p.address && p.address.includes(searchQuery.value))
    );
    isSearching.value = false;
  }, 200);
};

const moveToLocation = (place) => {
  const lat = place.latitude || place.lat;
  const lng = place.longitude || place.lng;
  if (mapAreaRef.value && lat && lng) {
    mapAreaRef.value.moveCamera(lat, lng);
  }
};

// [추가] 모달 열기 (검색 결과에서)
const openAddModalFromSearch = (place) => {
  if (!isEditing.value) {
    alert("편집 권한이 없습니다.\n상단의 [수정 권한 요청]을 먼저 눌러주세요!");
    return;
  }

  modalMode.value = "add";
  modalDayId.value = tripStore.itinerary[0]?.id || "1";
  modalTime.value = "14:00";
  modalName.value = place.name;
  modalAddress.value = place.address || "";
  modalLat.value = place.latitude || 0;
  modalLng.value = place.longitude || 0;
  modalPoiId.value = place.poiId;

  showModal.value = true;
};

// [추가] 모달 열기 (수동)
const openManualAddModal = () => {
  if (!isEditing.value) {
    alert("편집 권한이 없습니다.");
    return;
  }
  modalMode.value = "add";
  modalDayId.value = tripStore.itinerary[0]?.id || "1";
  modalTime.value = "12:00";
  modalName.value = "";
  modalAddress.value = "사용자 지정";
  modalLat.value = 0;
  modalLng.value = 0;
  modalPoiId.value = 0; // POI ID 없음

  showModal.value = true;
};

// [수정] 모달 열기
const openEditModal = (dayId, item) => {
  if (!isEditing.value) {
    alert("편집 권한이 없습니다.");
    return;
  }
  modalMode.value = "edit";
  editTargetId.value = item.id;
  modalDayId.value = dayId;
  modalTime.value = item.time;
  modalName.value = item.name;
  modalAddress.value = item.location;
  showModal.value = true;
};

const handleModalConfirm = () => {
  if (!modalName.value.trim()) return alert("장소 이름을 입력해주세요!");

  if (modalMode.value === "add") {
    const placeData = {
      name: modalName.value,
      address: modalAddress.value,
      lat: modalLat.value,
      lng: modalLng.value,
      poiId: modalPoiId.value,
    };
    tripStore.addPlace(modalDayId.value, placeData, modalTime.value);
  } else {
    tripStore.editItem(
      modalDayId.value,
      editTargetId.value,
      modalTime.value,
      modalName.value
    );
  }
  showModal.value = false;
};

// 권한 요청/반납
const handleRequestEdit = async () => {
  await tripStore.tryRequestEdit();
};
const handleFinishEdit = async () => {
  await tripStore.finishEdit();
};

// [수정] 강력한 클립보드 복사 함수 (어떤 환경에서도 동작 보장)
const copyInviteCode = async () => {
  const code = tripStore.tripInfo.inviteCode;

  // 1. 코드가 없으면 중단
  if (!code) {
    alert("복사할 초대 코드가 없습니다.");
    return;
  }

  try {
    // 2. 최신 방식 시도 (navigator.clipboard) - localhost나 HTTPS에서 작동
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(code);
      alert("초대 코드가 복사되었습니다!");
    } else {
      throw new Error("Clipboard API 사용 불가");
    }
  } catch (err) {
    // 3. [핵심] 최신 방식 실패 시 '구형 방식'으로 강제 복사 (execCommand)
    // 이 방식은 보안 컨텍스트 상관없이 클릭 이벤트 내에서는 거의 100% 동작합니다.
    try {
      const textArea = document.createElement("textarea");
      textArea.value = code;

      // 화면 안 보이게 숨김 처리
      textArea.style.position = "fixed";
      textArea.style.left = "-9999px";
      textArea.style.top = "0";

      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();

      const successful = document.execCommand("copy"); // 옛날 방식 실행
      document.body.removeChild(textArea); // 쓴 텍스트상자 바로 삭제

      if (successful) {
        alert("초대 코드가 복사되었습니다!");
      } else {
        prompt("이 코드를 직접 복사하세요:", code); // 진짜 안되면 창이라도 띄움
      }
    } catch (fallbackErr) {
      console.error("복사 실패:", fallbackErr);
      prompt("이 코드를 직접 복사하세요:", code);
    }
  }
};

const isHost = computed(() => {
  return (
    userStore.userInfo && tripStore.tripInfo.ownerId === userStore.userInfo.id
  );
});

const handleExitOrDelete = async () => {
  if (isHost.value) {
    if (confirm("정말 여행을 삭제하시겠습니까?")) {
      const success = await tripStore.deleteTrip(tripStore.tripInfo.tripId);
      if (success) emit("back");
    }
  } else {
    if (confirm("여행에서 나가시겠습니까?")) {
      const success = await tripStore.leaveTrip();
      if (success) emit("back");
    }
  }
};
</script>

<template>
  <div class="h-screen flex flex-col bg-gray-50 overflow-hidden">
    <header
      class="bg-white px-6 py-4 flex items-center justify-between shadow-sm border-b z-20"
    >
      <div class="flex items-center gap-4">
        <button
          @click="emit('back')"
          class="p-2 rounded-full hover:bg-gray-100 text-gray-500 transition-colors"
        >
          <ArrowLeft class="h-6 w-6" />
        </button>
        <div
          class="w-10 h-10 rounded-2xl bg-[#DE2E5F] flex items-center justify-center shadow-lg shadow-pink-200"
        >
          <MapPin class="h-5 w-5 text-white" />
        </div>
        <div>
          <h1 class="text-xl font-bold text-gray-900">
            {{ tripStore.tripInfo.title }}
          </h1>
          <div class="flex items-center gap-3 text-sm text-gray-500 mt-1">
            <div class="flex items-center gap-1 font-medium">
              <span class="text-[#DE2E5F]">{{
                tripStore.tripInfo.currentParticipants
              }}</span>
              <span>/</span>
              <span>{{ tripStore.tripInfo.maxMembers }}명</span>
            </div>
            <div class="h-3 w-[1px] bg-gray-300"></div>
            <button
              @click="copyInviteCode"
              class="flex items-center gap-1 text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded-md transition-colors text-gray-600"
            >
              <Copy class="w-3 h-3" />
              {{ tripStore.tripInfo.inviteCode || "CODE" }}
            </button>
          </div>
        </div>
      </div>

      <div class="flex items-center gap-3">
        <div v-if="isEditing" class="flex items-center gap-2">
          <div
            class="px-4 py-2 bg-green-50 text-green-700 font-bold rounded-full animate-pulse border border-green-200 flex items-center gap-2"
          >
            <Edit3 class="w-4 h-4" /> 내가 수정 중
          </div>
          <button
            @click="handleFinishEdit"
            class="bg-gray-800 text-white px-4 py-2 rounded-full font-bold hover:bg-gray-700 transition-colors shadow-md"
          >
            완료
          </button>
        </div>

        <div
          v-else-if="isLocked"
          class="flex items-center gap-2 px-4 py-2 bg-red-50 text-red-600 rounded-full font-bold border border-red-100"
        >
          <Lock class="w-4 h-4" />
          <span>{{ currentEditor || "다른 멤버" }}가 수정 중</span>
        </div>

        <button
          v-else
          @click="handleRequestEdit"
          class="flex items-center gap-2 px-4 py-2 border-2 border-gray-200 rounded-full hover:border-[#DE2E5F] hover:text-[#DE2E5F] transition-all font-semibold text-gray-600"
        >
          <Edit3 class="h-4 w-4" />
          수정 권한 요청
        </button>

        <div class="h-8 w-[1px] bg-gray-200 mx-2"></div>

        <button
          @click="handleExitOrDelete"
          class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-full transition-colors"
          :title="isHost ? '여행 삭제' : '여행 나가기'"
        >
          <Trash2 v-if="isHost" class="w-5 h-5" />
          <LogOut v-else class="w-5 h-5" />
        </button>
      </div>
    </header>

    <div class="flex-1 flex overflow-hidden">
      <div class="w-[65%] relative">
        <MapArea ref="mapAreaRef" @add-to-plan="openAddModalFromSearch" />

        <div
          class="absolute top-6 left-6 right-6 z-10 flex flex-col gap-3 pointer-events-none"
        >
          <div class="pointer-events-auto relative max-w-xl">
            <div
              class="absolute inset-0 bg-white/80 backdrop-blur-md rounded-2xl shadow-xl"
            ></div>
            <div class="relative p-3">
              <div
                class="flex items-center gap-2 bg-white/50 rounded-xl px-2 border border-transparent focus-within:border-[#DE2E5F] transition-all"
              >
                <Search class="h-5 w-5 text-gray-400 ml-2" />
                <input
                  v-model="searchQuery"
                  @keydown.enter="handleSearch"
                  type="text"
                  placeholder="관광지 검색..."
                  class="w-full bg-transparent border-none focus:outline-none text-base py-3 placeholder-gray-400"
                />
                <button
                  @click="handleSearch"
                  class="bg-[#DE2E5F] text-white px-4 py-2 rounded-lg text-sm font-bold hover:bg-[#c92552] transition-colors whitespace-nowrap"
                >
                  검색
                </button>
              </div>

              <div
                v-if="searchResults.length > 0"
                class="mt-3 bg-white rounded-xl shadow-lg border border-gray-100 max-h-64 overflow-y-auto animate-in fade-in slide-in-from-top-2"
              >
                <div
                  v-for="place in searchResults"
                  :key="place.poiId"
                  class="p-4 hover:bg-gray-50 flex justify-between items-center border-b last:border-none transition-colors group"
                >
                  <div
                    @click="moveToLocation(place)"
                    class="cursor-pointer flex-1"
                  >
                    <div class="font-bold text-gray-900">{{ place.name }}</div>
                    <div class="text-xs text-gray-500 mt-0.5">
                      {{ place.address }}
                    </div>
                  </div>
                  <div class="flex items-center gap-2">
                    <button
                      @click="moveToLocation(place)"
                      class="p-2 text-gray-400 hover:text-[#DE2E5F] rounded-full hover:bg-pink-50 transition-colors"
                    >
                      <MapPin class="h-4 w-4" />
                    </button>
                    <button
                      v-if="isEditing"
                      @click="openAddModalFromSearch(place)"
                      class="bg-[#DE2E5F] text-white p-2 rounded-lg text-xs font-bold hover:bg-[#c92552] shadow-md flex items-center gap-1 transition-all active:scale-95"
                    >
                      <Plus class="h-4 w-4" />
                      추가
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        class="w-[35%] bg-white shadow-2xl z-10 flex flex-col border-l border-gray-100"
      >
        <div class="flex border-b bg-gray-50">
          <button
            @click="activeTab = 'itinerary'"
            class="flex-1 py-4 text-base font-bold transition-colors border-b-2"
            :class="
              activeTab === 'itinerary'
                ? 'bg-white text-[#DE2E5F] border-[#DE2E5F]'
                : 'text-gray-400 border-transparent hover:text-gray-600'
            "
          >
            일정
          </button>
          <button
            @click="activeTab = 'chat'"
            class="flex-1 py-4 text-base font-bold transition-colors border-b-2"
            :class="
              activeTab === 'chat'
                ? 'bg-white text-[#DE2E5F] border-[#DE2E5F]'
                : 'text-gray-400 border-transparent hover:text-gray-600'
            "
          >
            채팅
          </button>
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

    <div
      v-if="showModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4 animate-in fade-in duration-200"
    >
      <div
        class="bg-white rounded-3xl shadow-2xl w-full max-w-sm overflow-hidden p-6 space-y-6"
      >
        <div class="flex items-center justify-between">
          <h3 class="text-xl font-bold text-gray-900">
            {{ modalMode === "add" ? "일정 추가하기" : "일정 수정하기" }}
          </h3>
          <button
            @click="showModal = false"
            class="text-gray-400 hover:text-gray-600"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <div class="space-y-4">
          <div class="space-y-2">
            <label
              class="text-sm font-bold text-gray-700 flex items-center gap-2"
            >
              <Type class="w-4 h-4 text-[#DE2E5F]" /> 장소 이름
            </label>
            <input
              v-model="modalName"
              type="text"
              placeholder="장소 이름"
              class="w-full p-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all"
            />
            <div class="text-xs text-gray-500 flex items-center gap-1 pl-1">
              <MapPin class="w-3 h-3" /> {{ modalAddress }}
            </div>
          </div>

          <div class="space-y-2">
            <label
              class="text-sm font-bold text-gray-700 flex items-center gap-2"
              ><Calendar class="w-4 h-4 text-[#DE2E5F]" /> 날짜 선택</label
            >
            <select
              v-model="modalDayId"
              :disabled="modalMode === 'edit'"
              class="w-full p-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all disabled:bg-gray-100"
            >
              <option
                v-for="day in tripStore.itinerary"
                :key="day.id"
                :value="day.id"
              >
                {{ day.day }} ({{ day.date }})
              </option>
            </select>
          </div>

          <div class="space-y-2">
            <label
              class="text-sm font-bold text-gray-700 flex items-center gap-2"
              ><Clock class="w-4 h-4 text-[#DE2E5F]" /> 방문 시간</label
            >
            <input
              v-model="modalTime"
              type="time"
              class="w-full p-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all"
            />
          </div>
        </div>

        <div class="flex gap-3 pt-2">
          <button
            @click="showModal = false"
            class="flex-1 py-3.5 rounded-xl font-bold text-gray-600 bg-gray-100 hover:bg-gray-200 transition-colors"
          >
            취소
          </button>
          <button
            @click="handleModalConfirm"
            class="flex-1 py-3.5 rounded-xl font-bold text-white bg-[#DE2E5F] hover:bg-[#c92552] shadow-lg transition-colors"
          >
            {{ modalMode === "add" ? "추가하기" : "수정완료" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
