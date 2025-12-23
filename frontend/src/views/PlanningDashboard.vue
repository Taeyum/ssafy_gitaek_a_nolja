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
import http from "@/api/http";

import ToastNotification from "@/components/ToastNotification.vue";

const userStore = useUserStore();
const tripStore = useTripStore();

const emit = defineEmits(["back"]);
const mapAreaRef = ref(null);

// 상태 관리
const isEditing = ref(false);
const isLocked = computed(() => tripStore.isLocked);
const currentEditor = computed(() => tripStore.currentEditorName);

const activeTab = ref("itinerary");
const modalPoiId = ref(0);

// 안 읽은 채팅 개수 관리
const unreadChatCount = ref(0);

// 현재 보고 있는 날짜 (null이면 전체)
const currentViewDayId = ref(null);

// 탭 전환 함수
const switchTab = (tabName) => {
  activeTab.value = tabName;
  if (tabName === "chat") {
    unreadChatCount.value = 0;
  }
};

// 메시지 알림
watch(
  () => tripStore.messages.length,
  (newLen, oldLen) => {
    if (activeTab.value !== "chat" && oldLen !== undefined && newLen > oldLen) {
      unreadChatCount.value += newLen - oldLen;
    }
  }
);

// ★ [핵심] 날짜별 경로 필터링 핸들러
const handleToggleDayRoute = (dayId) => {
  if (!mapAreaRef.value) return;

  // 1. 현재 보고 있는 날짜 상태 업데이트
  currentViewDayId.value = dayId;

  if (dayId) {
    // 특정 날짜만 그리기
    const targetDay = tripStore.itinerary.filter((d) => d.id === dayId);
    mapAreaRef.value.drawRoute(targetDay);

    if (targetDay[0]?.items?.length > 0) {
      const first = targetDay[0].items[0];
      const lat = first.lat || first.latitude || first.placeLat;
      const lng = first.lng || first.longitude || first.placeLng;
      if (lat && lng) mapAreaRef.value.moveCamera(lat, lng);
    }
  } else {
    // 전체 그리기
    mapAreaRef.value.drawRoute(tripStore.itinerary);
  }
};

// ★ [신규] 지도에서 계산된 이동 시간을 받아서 리스트에 반영하는 함수
const handleUpdateTravelTimes = (times) => {
  // times: [{ dayId, itemId, travelTime }, ...]
  times.forEach((info) => {
    const day = tripStore.itinerary.find((d) => d.id === info.dayId);
    if (day) {
      const item = day.items.find(
        (i) => i.id === info.itemId || i.scheduleId === info.itemId
      );
      if (item) {
        // 화면(스토어 상태)에 즉시 반영 -> ItineraryList가 반응함
        item.travelTime = info.travelTime;
      }
    }
  });
};

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

/* =========================================
     🤖 AI 여행 코스 추천 로직
     ========================================= */
const showAiModal = ref(false);
const aiForm = ref({
  destination: "",
  totalDays: 0,
  style: "",
});
const isAiLoading = ref(false);

const calculateDuration = (start, end) => {
  if (!start || !end) return 1;
  const s = new Date(start);
  const e = new Date(end);
  const diffTime = Math.abs(e - s);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
  return diffDays;
};

const openAiModal = () => {
  if (!isEditing.value) {
    alert("편집 권한이 없습니다.\n먼저 [수정 권한 요청]을 해주세요!");
    return;
  }
  const trip = tripStore.tripInfo;
  if (!trip) return alert("여행 정보가 없습니다.");

  const totalDays = calculateDuration(trip.startDate, trip.endDate);
  aiForm.value = {
    destination: "",
    totalDays: totalDays,
    style: "",
  };
  showAiModal.value = true;
};

const fetchAiPlan = async () => {
  if (!aiForm.value.destination) return alert("여행지를 입력해주세요");
  if (!aiForm.value.style) return alert("여행 스타일을 입력해주세요");

  isAiLoading.value = true;

  // ★ try 시작
  try {
    const res = await http.post("/attractions/ai-plan", aiForm.value);
    const aiPlans =
      typeof res.data === "string" ? JSON.parse(res.data) : res.data;

    console.log("🤖 AI 원본 응답:", aiPlans);

    const targetRegion = aiForm.value.destination.substring(0, 2);
    const filteredAiPlans = aiPlans.filter((plan) => {
      if (!plan.address || !plan.address.includes(targetRegion)) return false;
      return true;
    });

    const maxDay = aiForm.value.totalDays;
    let addedCount = 0;

    // 표준 시간 슬롯 정의
    const timeSlots = [
      "10:00",
      "11:30",
      "12:00",
      "13:00",
      "13:30",
      "14:00",
      "15:00",
      "16:00",
      "17:00",
      "18:00",
      "19:00",
      "20:00",
    ];

    // 중복 방지용 Set (이름, ID 이중 체크)
    const existingIds = new Set();
    const existingNames = new Set();

    tripStore.itinerary.forEach((day) => {
      day.items.forEach((item) => {
        existingNames.add(item.name.replace(/\s/g, ""));
        existingIds.add(item.poiId);
      });
    });

    for (const plan of filteredAiPlans) {
      if (plan.day > 0 && plan.day <= maxDay) {
        const targetDay = tripStore.itinerary.find(
          (d) => parseInt(d.day) === plan.day
        );

        if (targetDay) {
          // 1. 이미 등록된 POI ID인가?
          if (existingIds.has(plan.poiId)) continue;

          // 2. 이름이 비슷한가?
          const planNameClean = plan.title.replace(/\s/g, "");
          if (existingNames.has(planNameClean)) {
            continue;
          }

          // 빈 시간 슬롯 찾기
          let finalTime = null;

          if (plan.time && !isTimeOccupied(targetDay.items, plan.time)) {
            finalTime = plan.time;
          } else {
            for (const slot of timeSlots) {
              if (!isTimeOccupied(targetDay.items, slot)) {
                finalTime = slot;
                break;
              }
            }
          }

          if (!finalTime) {
            console.warn(`Day ${plan.day} 꽉 참: ${plan.title} 추가 실패`);
            continue;
          }

          if (plan.poiId && plan.poiId > 0) {
            await tripStore.addPlace(
              targetDay.id,
              {
                name: plan.title,
                address: plan.address,
                poiId: plan.poiId,
                lat: plan.lat,
                lng: plan.lng,
                memo: plan.memo,
                // AI가 준 시간 정보 저장
                duration: plan.duration,
                travelTime: plan.travel_time,
              },
              finalTime
            );

            existingIds.add(plan.poiId);
            existingNames.add(planNameClean);
            addedCount++;
          }
        }
      }
    }

    if (addedCount === 0) {
      alert(`추가할 장소가 없거나, 일정이 꽉 차서 추가하지 못했습니다.`);
    } else {
      alert(`성공! ${addedCount}개의 장소를 빈 시간에 맞춰 추가했습니다.`);
      showAiModal.value = false;
    }
  } catch (e) {
    // ★ catch 블록
    console.error("AI 에러 상세:", e);
    alert("일정 저장 중 오류가 발생했습니다.");
  } finally {
    // ★ finally 블록
    isAiLoading.value = false;
  }
};

// 보조 함수
const isTimeOccupied = (items, timeStr) => {
  const target = timeStr.substring(0, 5);
  return items.some((item) => item.time.substring(0, 5) === target);
};

/* =========================================
     기본 로직
     ========================================= */

onMounted(async () => {
  await loadRealData();

  if (userStore.userInfo?.id) {
    tripStore.startPolling(userStore.userInfo.id);
  } else {
    const unwatch = watch(
      () => userStore.userInfo,
      (newVal) => {
        if (newVal?.id) {
          tripStore.startPolling(newVal.id);
          unwatch();
        }
      }
    );
  }

  // 페이지 로딩 시 기존 일정이 있다면 즉시 지도에 그리기
  if (tripStore.itinerary && tripStore.itinerary.length > 0) {
    setTimeout(() => {
      if (mapAreaRef.value) {
        console.log("🚀 초기 전체 경로 그리기 실행");
        mapAreaRef.value.drawRoute(tripStore.itinerary);
      }
    }, 500);
  }
});

// 일정 데이터가 변경되면 지도 갱신
watch(
  () => tripStore.itinerary,
  (newItinerary) => {
    if (mapAreaRef.value && newItinerary.length > 0) {
      // 특정 날짜를 보고 있었다면, 그 날짜만 다시 그리기
      if (currentViewDayId.value) {
        const targetDay = newItinerary.filter(
          (d) => d.id === currentViewDayId.value
        );
        if (targetDay.length > 0) {
          mapAreaRef.value.drawRoute(targetDay);
        } else {
          currentViewDayId.value = null;
          mapAreaRef.value.drawRoute(newItinerary);
        }
      } else {
        // 전체 그리기
        mapAreaRef.value.drawRoute(newItinerary);
      }
    }
  },
  { deep: true }
);

watch(
  () => tripStore.tripInfo.tripId,
  (newId) => {
    if (newId && newId > 0) {
      console.log(`🚀 여행 ID(${newId}) 확인됨 -> 소켓 연결 시도`);
      tripStore.connectTripSocket(newId);
    }
  },
  { immediate: true }
);

onUnmounted(async () => {
  if (isEditing.value) {
    await tripStore.finishEdit();
  }
  tripStore.stopPolling();
  tripStore.disconnectTripSocket();
});

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
    console.log(
      "검색용 데이터 로드 완료 (지도 표시 안 함):",
      allAttractions.value.length + "개"
    );
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
    searchResults.value = allAttractions.value.filter((p) =>
      p.name.includes(searchQuery.value)
    );
    isSearching.value = false;
  }, 200);
};

const handleMapClick = () => {
  if (searchResults.value.length > 0) {
    searchResults.value = [];
  }
};

const moveToLocation = (place) => {
  const lat = place.latitude || place.lat;
  const lng = place.longitude || place.lng;
  if (mapAreaRef.value && lat && lng) {
    mapAreaRef.value.moveCamera(lat, lng);
    searchResults.value = [];
  }
};

const handleFocusPlace = ({ lat, lng }) => {
  if (mapAreaRef.value) {
    mapAreaRef.value.moveCamera(lat, lng);
  }
};

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

  if (mapAreaRef.value) {
    mapAreaRef.value.moveCamera(modalLat.value, modalLng.value);
  }
};

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
  modalPoiId.value = 0;
  showModal.value = true;
};

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

const handleRequestEdit = async () => {
  await tripStore.tryRequestEdit();
};
const handleFinishEdit = async () => {
  await tripStore.finishEdit();
};

const copyInviteCode = async () => {
  const code = tripStore.tripInfo.inviteCode;
  if (!code) {
    alert("복사할 초대 코드가 없습니다.");
    return;
  }
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(code);
      alert("초대 코드가 복사되었습니다!");
    } else {
      throw new Error("Clipboard API 사용 불가");
    }
  } catch (err) {
    try {
      const textArea = document.createElement("textarea");
      textArea.value = code;
      textArea.style.position = "fixed";
      textArea.style.left = "-9999px";
      textArea.style.top = "0";
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      const successful = document.execCommand("copy");
      document.body.removeChild(textArea);
      if (successful) {
        alert("초대 코드가 복사되었습니다!");
      } else {
        prompt("이 코드를 직접 복사하세요:", code);
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
  <div class="h-screen flex flex-col bg-gray-50 overflow-hidden relative">
    <ToastNotification :notifications="tripStore.notifications" />

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
        <MapArea
          ref="mapAreaRef"
          @add-to-plan="openAddModalFromSearch"
          @map-clicked="handleMapClick"
          @update-travel-times="handleUpdateTravelTimes"
        />

        <div
          class="absolute top-6 left-6 right-6 z-10 flex flex-col gap-3 pointer-events-none"
        >
          <div class="pointer-events-auto flex justify-end gap-2 max-w-xl">
            <button
              @click="openAiModal"
              class="bg-white/90 backdrop-blur text-gray-800 px-4 py-2 rounded-xl shadow-lg font-bold hover:bg-white flex items-center gap-2 border border-purple-100 transition-all active:scale-95"
            >
              <Sparkles class="w-4 h-4 text-purple-600" />
              <span
                class="bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent"
                >AI 코스 추천</span
              >
            </button>
          </div>

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
                  @keydown.enter.prevent="handleSearch"
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
            @click="switchTab('itinerary')"
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
            @click="switchTab('chat')"
            class="flex-1 py-4 text-base font-bold transition-colors border-b-2 relative"
            :class="
              activeTab === 'chat'
                ? 'bg-white text-[#DE2E5F] border-[#DE2E5F]'
                : 'text-gray-400 border-transparent hover:text-gray-600'
            "
          >
            채팅
            <span
              v-if="unreadChatCount > 0"
              class="absolute top-3 right-8 bg-red-500 text-white text-[10px] px-1.5 py-0.5 rounded-full animate-bounce shadow-sm"
            >
              {{ unreadChatCount > 99 ? "99+" : unreadChatCount }}
            </span>
          </button>
        </div>
        <div class="flex-1 overflow-hidden relative">
          <ItineraryList
            v-if="activeTab === 'itinerary'"
            :isEditing="isEditing"
            @edit-item="openEditModal"
            @open-manual-add="openManualAddModal"
            @focus-place="handleFocusPlace"
            @toggle-day="handleToggleDayRoute"
            class="absolute inset-0"
          />
          <ChatInterface v-else class="absolute inset-0" />
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div
        v-if="showModal"
        class="fixed inset-0 z-[99999] flex items-center justify-center bg-black/40 backdrop-blur-sm p-4 animate-in fade-in duration-200"
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
    </Teleport>

    <Teleport to="body">
      <div
        v-if="showAiModal"
        class="fixed inset-0 z-[99999] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-in fade-in"
      >
        <div
          class="bg-white rounded-3xl shadow-2xl w-full max-w-sm overflow-hidden p-8 relative"
        >
          <h3
            class="text-xl font-bold mb-6 text-gray-800 flex items-center gap-2"
          >
            <Sparkles class="w-6 h-6 text-[#DE2E5F]" /> AI 맞춤 코스 추천
          </h3>

          <div class="space-y-5">
            <div>
              <label class="block text-sm font-bold text-gray-800 mb-1">
                어디로 가시나요? <span class="text-[#DE2E5F]">*</span>
              </label>

              <input
                v-model="aiForm.destination"
                list="korea-cities"
                class="w-full border-2 border-gray-200 p-3 rounded-xl bg-white focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all font-medium text-gray-900 placeholder-gray-400"
                :placeholder="
                  tripStore.tripInfo?.title ||
                  '도시 이름을 입력하세요 (예: 강릉)'
                "
              />

              <datalist id="korea-cities">
                <option value="서울"></option>
                <option value="부산"></option>
                <option value="제주"></option>
                <option value="강릉"></option>
                <option value="경주"></option>
                <option value="여수"></option>
                <option value="전주"></option>
                <option value="속초"></option>
                <option value="인천"></option>
                <option value="대구"></option>
                <option value="대전"></option>
                <option value="광주"></option>
                <option value="가평"></option>
                <option value="춘천"></option>
                <option value="포천"></option>
              </datalist>

              <p class="text-xs text-gray-500 mt-1 pl-1">
                * 화살표를 누르거나 도시 이름을 입력하세요.
              </p>
            </div>

            <div>
              <label class="block text-sm font-bold text-gray-500 mb-1"
                >여행 기간</label
              >
              <div
                class="w-full border p-3 rounded-xl bg-gray-100 text-gray-500 flex justify-between items-center cursor-not-allowed"
              >
                <span class="text-sm"
                  >{{ tripStore.tripInfo?.startDate }} ~
                  {{ tripStore.tripInfo?.endDate }}</span
                >
                <span class="font-bold text-[#DE2E5F]"
                  >총 {{ aiForm.totalDays }}일</span
                >
              </div>
            </div>

            <div>
              <label class="block text-sm font-bold text-gray-800 mb-1"
                >어떤 여행을 원하세요?</label
              >
              <input
                v-model="aiForm.style"
                class="w-full border-2 border-[#DE2E5F] p-3 rounded-xl bg-white focus:outline-none focus:ring-4 focus:ring-pink-100 transition-all placeholder-gray-300"
                placeholder="예: 힐링, 맛집 투어, 역사 탐방"
                @keydown.enter.prevent="fetchAiPlan"
              />
            </div>
          </div>

          <div class="mt-8 flex gap-3">
            <button
              @click="showAiModal = false"
              class="flex-1 py-3.5 bg-gray-100 rounded-xl font-bold text-gray-600 hover:bg-gray-200 transition-colors"
            >
              취소
            </button>
            <button
              @click="fetchAiPlan"
              :disabled="isAiLoading || !aiForm.style || !aiForm.destination"
              class="flex-1 py-3.5 bg-[#DE2E5F] text-white rounded-xl font-bold flex justify-center items-center gap-2 hover:bg-[#c92552] transition-colors disabled:bg-gray-300 shadow-lg shadow-pink-200"
            >
              <span v-if="isAiLoading" class="animate-spin text-white">⏳</span>
              {{ isAiLoading ? "생성 중..." : "코스 만들기" }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>