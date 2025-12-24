<script setup>
import { ref, onMounted, defineEmits, watch, computed } from "vue";
import { 
  ArrowLeft, Heart, MessageCircle, Camera, 
  MapPin, Plus, X, MoreHorizontal, Trash2, Edit2, Map, Search, Eye, User,
  ChevronLeft, ChevronRight // ★ 페이지 이동 아이콘 추가
} from "lucide-vue-next"; 
import { useUserStore } from "@/stores/userStore";
import { 
  getReviewList, getReviewDetail, writeReview, 
  updateReview, deleteReview, toggleReviewLike 
} from "@/api/review";
import { getMyTripsApi, getSchedulesApi } from "@/api/trip";

import defaultImg from "@/assets/default.jpg";

const emit = defineEmits(["back"]);
const userStore = useUserStore();

// --- 상태 관리 ---
const reviews = ref([]);
const myTrips = ref([]); 
const showModal = ref(false); 
const showDetailModal = ref(false); 
const isEditing = ref(false); 
const isLoading = ref(false);

// 필터 및 정렬 상태
const searchKeyword = ref(""); 
const sortBy = ref("latest");  
const showMyReviewsOnly = ref(false);

// ★ [수정] 페이지네이션 상태
const currentPage = ref(1);
const itemsPerPage = 8;

// 폼 데이터
const form = ref({
  reviewId: 0,
  title: "",
  content: "",
  thumbnail: "", 
  tripId: 0, 
});

const currentReview = ref(null);
const tripRouteSummary = ref("");

// --- Computed: 필터링 및 정렬된 전체 목록 ---
const filteredReviews = computed(() => {
  let result = [...reviews.value];

  // 1. 내 글만 보기
  if (showMyReviewsOnly.value && userStore.isLoggedIn) {
    const myId = userStore.userInfo.id;
    result = result.filter(review => review.userId === myId);
  }

  // 2. 검색어 필터링
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase();
    result = result.filter(review => 
      review.title.toLowerCase().includes(keyword)
    );
  }

  // 3. 정렬 로직
  result.sort((a, b) => {
    if (sortBy.value === "latest") {
      return b.reviewId - a.reviewId; 
    } else if (sortBy.value === "hit") {
      if (b.hit !== a.hit) return b.hit - a.hit; 
      return b.reviewId - a.reviewId;
    } else if (sortBy.value === "like") {
      if (b.likeCount !== a.likeCount) return b.likeCount - a.likeCount; 
      return b.reviewId - a.reviewId;
    }
    return 0;
  });

  return result;
});

// ★ [추가] 총 페이지 수 계산
const totalPages = computed(() => {
  return Math.ceil(filteredReviews.value.length / itemsPerPage);
});

// ★ [수정] 현재 페이지에 해당하는 데이터만 자르기
const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredReviews.value.slice(start, end);
});

// ★ [추가] 필터 조건이 바뀌면 1페이지로 리셋
watch([searchKeyword, sortBy, showMyReviewsOnly], () => {
  currentPage.value = 1;
});

// ★ [추가] 페이지 변경 함수
const setPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    window.scrollTo({ top: 0, behavior: 'smooth' }); // 페이지 넘기면 맨 위로 스크롤
  }
};

// --- API 로딩 ---
const loadReviews = async () => {
  isLoading.value = true;
  try {
    const res = await getReviewList();
    reviews.value = res.data;
  } catch (e) {
    console.error(e);
  } finally {
    isLoading.value = false;
  }
};

const fetchMyTrips = async () => {
  try {
    if (myTrips.value.length > 0) return;
    const { data } = await getMyTripsApi();
    myTrips.value = data;
  } catch (e) {
    console.error("여행 목록 로딩 실패", e);
  }
};

// --- Watch: 여행 선택 시 데이터 자동 채우기 ---
watch(() => form.value.tripId, async (newTripId) => {
  if (!newTripId || newTripId === 0) {
    tripRouteSummary.value = "";
    form.value.thumbnail = defaultImg;
    return;
  }

  try {
    const { data: schedules } = await getSchedulesApi(newTripId);
    
    if (!schedules || schedules.length === 0) {
        form.value.thumbnail = defaultImg;
        return;
    }

    const firstPlaceWithImage = schedules.find(s => s.thumbnailUrl);
    if (firstPlaceWithImage) {
      form.value.thumbnail = firstPlaceWithImage.thumbnailUrl;
    } else {
      form.value.thumbnail = defaultImg;
    }

    let summary = "";
    let contentRoute = ""; 
    const days = {};
    schedules.forEach(s => {
        if (!days[s.tripDay]) days[s.tripDay] = [];
        days[s.tripDay].push(s.placeName);
    });

    Object.keys(days).sort().forEach(day => {
        const routeLine = `[${day}일차] ` + days[day].join(" -> ");
        summary += routeLine + "\n";
        contentRoute += routeLine + "\n";
    });

    tripRouteSummary.value = summary;

    if (!isEditing.value && !form.value.content) {
        form.value.content = `여행 경로:\n${contentRoute}\n후기:\n`;
    }

  } catch (e) {
    console.error("일정 불러오기 실패", e);
    form.value.thumbnail = defaultImg;
  }
});

onMounted(() => {
  loadReviews();
});

// --- Modal & CRUD Functions ---

const toggleMyReviews = () => {
    if (!userStore.isLoggedIn) {
        alert("로그인이 필요한 기능입니다.");
        return;
    }
    showMyReviewsOnly.value = !showMyReviewsOnly.value;
};

const openWriteModal = async () => {
  if (!userStore.isLoggedIn) return alert("로그인이 필요합니다.");
  isEditing.value = false;
  form.value = { 
      reviewId: 0,
      title: "", 
      content: "", 
      thumbnail: defaultImg, 
      tripId: 0 
  };
  tripRouteSummary.value = ""; 
  showModal.value = true;
  await fetchMyTrips(); 
};

const openEditModal = async (review) => {
  isEditing.value = true;
  form.value = { 
    reviewId: review.reviewId,
    title: review.title,
    content: review.content,
    thumbnail: review.thumbnail || defaultImg,
    tripId: review.tripId || 0
  };
  tripRouteSummary.value = ""; 
  showDetailModal.value = false; 
  showModal.value = true;
  await fetchMyTrips(); 
};

const handleSave = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    return alert("제목과 내용을 입력해주세요.");
  }
  if (!form.value.thumbnail) form.value.thumbnail = defaultImg;

  try {
    if (isEditing.value) {
      await updateReview(form.value.reviewId, form.value);
      alert("수정되었습니다.");
    } else {
      await writeReview(form.value);
      alert("작성되었습니다.");
    }
    showModal.value = false;
    loadReviews(); 
  } catch (e) {
    console.error(e);
    alert("저장 실패");
  }
};

const handleDelete = async (reviewId) => {
  if (!confirm("정말 삭제하시겠습니까?")) return;
  try {
    await deleteReview(reviewId);
    alert("삭제되었습니다.");
    showDetailModal.value = false; 
    loadReviews(); 
  } catch (e) {
    console.error(e);
    alert("삭제 실패");
  }
};

const openDetail = async (id) => {
  try {
    const res = await getReviewDetail(id);
    currentReview.value = res.data;
    
    if (userStore.userInfo && userStore.userInfo.id === currentReview.value.userId) {
        currentReview.value.isMine = true;
    }
    showDetailModal.value = true;
    
    const target = reviews.value.find(r => r.reviewId === id);
    if (target) target.hit = res.data.hit;
  } catch (e) {
    alert("불러오기 실패");
  }
};

const handleLike = async (review) => {
  if (!userStore.isLoggedIn) return alert("로그인이 필요합니다.");
  try {
    const res = await toggleReviewLike(review.reviewId);
    const isLiked = res.data; 
    
    if (review.isLiked !== isLiked) {
        review.isLiked = isLiked;
        review.likeCount += isLiked ? 1 : -1;
    }

    const targetInList = reviews.value.find(r => r.reviewId === review.reviewId);
    if (targetInList && targetInList !== review) {
        targetInList.isLiked = isLiked;
        targetInList.likeCount = review.likeCount;
    }

    if (currentReview.value && currentReview.value.reviewId === review.reviewId && currentReview.value !== review) {
        currentReview.value.isLiked = isLiked;
        currentReview.value.likeCount = review.likeCount;
    }
  } catch (e) {
    console.error(e);
  }
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <header class="bg-white/80 backdrop-blur-md sticky top-0 z-10 border-b border-gray-100">
      <div class="max-w-4xl mx-auto px-4 h-16 flex items-center justify-between">
        <button @click="emit('back')" class="p-2 -ml-2 hover:bg-gray-100 rounded-full transition-colors">
          <ArrowLeft class="w-6 h-6 text-gray-700" />
        </button>
        <h1 class="text-lg font-bold text-gray-800 flex items-center gap-2">
            <Camera class="w-5 h-5 text-[#DE2E5F]" />
            여행 찐후기
        </h1>
        <button @click="openWriteModal" class="p-2 bg-[#DE2E5F] text-white rounded-full shadow-lg hover:bg-[#c92552] transition-transform hover:scale-105 active:scale-95">
          <Plus class="w-5 h-5" />
        </button>
      </div>
    </header>

    <main class="flex-1 max-w-4xl mx-auto w-full p-4">
        
        <div class="mb-6 space-y-4">
            <div class="relative">
                <input 
                    v-model="searchKeyword"
                    type="text" 
                    placeholder="제목으로 후기 검색..." 
                    class="w-full pl-11 pr-4 py-3 rounded-2xl border-none bg-white shadow-sm focus:ring-2 focus:ring-[#DE2E5F] text-sm transition-all"
                />
                <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            </div>

            <div class="flex gap-2 overflow-x-auto pb-1 scrollbar-hide items-center">
                <button 
                    @click="toggleMyReviews"
                    class="flex items-center gap-1 px-4 py-2 rounded-full text-xs font-bold transition-all border mr-2"
                    :class="showMyReviewsOnly ? 'bg-gray-800 text-white border-gray-800 shadow-md' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'"
                >
                    <User class="w-3 h-3" /> 내 글만 보기
                </button>

                <button @click="sortBy = 'latest'" class="px-4 py-2 rounded-full text-xs font-bold transition-all border" :class="sortBy === 'latest' ? 'bg-[#DE2E5F] text-white border-[#DE2E5F] shadow-sm' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'">최신순</button>
                <button @click="sortBy = 'hit'" class="px-4 py-2 rounded-full text-xs font-bold transition-all border" :class="sortBy === 'hit' ? 'bg-[#DE2E5F] text-white border-[#DE2E5F] shadow-sm' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'">조회 많은순</button>
                <button @click="sortBy = 'like'" class="px-4 py-2 rounded-full text-xs font-bold transition-all border" :class="sortBy === 'like' ? 'bg-[#DE2E5F] text-white border-[#DE2E5F] shadow-sm' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'">인기순 (좋아요)</button>
            </div>
        </div>

        <div v-if="isLoading" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div v-for="i in 4" :key="i" class="bg-white rounded-2xl h-64 animate-pulse"></div>
        </div>

        <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div 
                v-for="review in paginatedReviews" 
                :key="review.reviewId"
                class="bg-white rounded-3xl overflow-hidden shadow-sm border border-gray-100 hover:shadow-md transition-shadow cursor-pointer group"
                @click="openDetail(review.reviewId)"
            >
                <div class="aspect-[4/3] bg-gray-100 relative overflow-hidden">
                    <img 
                        :src="review.thumbnail || defaultImg" 
                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                    
                    <div class="absolute inset-0 bg-gradient-to-b from-black/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none"></div>
                    <div class="absolute top-4 left-4 right-4 z-10 opacity-0 group-hover:opacity-100 transition-opacity duration-300 transform group-hover:translate-y-0 translate-y-[-10px]">
                        <h3 class="text-white font-bold text-lg leading-tight line-clamp-2 drop-shadow-md">
                            {{ review.title }}
                        </h3>
                    </div>

                    <div v-if="review.tripTitle" class="absolute bottom-3 left-3 bg-black/50 backdrop-blur-sm text-white text-[10px] font-bold px-2 py-1 rounded-full flex items-center gap-1 z-10">
                        <MapPin class="w-3 h-3" /> {{ review.tripTitle }}
                    </div>
                </div>

                <div class="p-4">
                    <div class="flex items-center justify-between mb-3">
                        <div class="flex items-center gap-2">
                            <div class="w-6 h-6 rounded-full bg-gradient-to-tr from-yellow-400 to-[#DE2E5F] p-[1px]">
                                <div class="w-full h-full rounded-full bg-white flex items-center justify-center text-[10px]">
                                    {{ review.writerName?.charAt(0) }}
                                </div>
                            </div>
                            <span class="text-sm font-bold text-gray-700">{{ review.writerName }}</span>
                        </div>
                        <span class="text-xs text-gray-400">{{ review.createdAt }}</span>
                    </div>
                    
                    <p class="text-sm text-gray-600 line-clamp-2 mb-3 h-10">{{ review.content }}</p>
                    
                    <div class="flex items-center justify-between pt-2 border-t border-gray-50">
                        <button 
                            @click.stop="handleLike(review)" 
                            class="flex items-center gap-1 text-sm font-medium transition-colors" 
                            :class="review.isLiked ? 'text-[#DE2E5F]' : 'text-gray-400 hover:text-[#DE2E5F]'"
                        >
                            <Heart class="w-4 h-4" :class="{'fill-current': review.isLiked}" />
                            {{ review.likeCount }}
                        </button>
                        <div class="flex items-center gap-1 text-xs text-gray-400">👀 {{ review.hit }}</div>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="!isLoading && totalPages > 1" class="flex justify-center items-center gap-2 mt-10 pb-10">
            <button 
                @click="setPage(currentPage - 1)" 
                :disabled="currentPage === 1"
                class="p-2 rounded-xl border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
                <ChevronLeft class="w-5 h-5 text-gray-600" />
            </button>

            <button 
                v-for="page in totalPages" 
                :key="page"
                @click="setPage(page)"
                class="w-10 h-10 rounded-xl text-sm font-bold transition-all border"
                :class="currentPage === page ? 'bg-[#DE2E5F] border-[#DE2E5F] text-white shadow-md' : 'bg-white border-transparent text-gray-500 hover:bg-gray-50 hover:text-gray-900'"
            >
                {{ page }}
            </button>

            <button 
                @click="setPage(currentPage + 1)" 
                :disabled="currentPage === totalPages"
                class="p-2 rounded-xl border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
                <ChevronRight class="w-5 h-5 text-gray-600" />
            </button>
        </div>

        <div v-if="!isLoading && filteredReviews.length === 0" class="text-center py-20">
            <div class="bg-gray-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4">
                <Search class="w-10 h-10 text-gray-400" />
            </div>
            <p class="text-gray-500 font-bold">
                {{ showMyReviewsOnly ? '작성한 후기가 없습니다.' : '검색 결과가 없습니다.' }}
            </p>
            <p class="text-gray-400 text-sm mt-1">
                {{ showMyReviewsOnly ? '첫 후기를 작성해보세요!' : '다른 검색어로 찾아보시거나 첫 후기를 작성해보세요!' }}
            </p>
        </div>
    </main>

    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="showModal = false"></div>
        <div class="bg-white w-full max-w-lg rounded-3xl shadow-2xl relative overflow-hidden z-10 flex flex-col max-h-[90vh]">
            <div class="p-4 border-b flex justify-between items-center">
                <h3 class="font-bold text-lg">{{ isEditing ? '후기 수정' : '새 후기 작성' }}</h3>
                <button @click="showModal = false"><X class="w-6 h-6 text-gray-400" /></button>
            </div>
            
            <div class="p-6 overflow-y-auto space-y-5">
                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-1">여행 선택</label>
                    <div class="relative">
                        <select 
                            v-model="form.tripId" 
                            class="w-full p-3 bg-gray-50 rounded-xl border-none focus:ring-2 focus:ring-[#DE2E5F] appearance-none text-gray-700 font-bold"
                        >
                            <option :value="0">여행 선택 안 함 (직접 작성)</option>
                            <option v-for="trip in myTrips" :key="trip.tripId" :value="trip.tripId">
                                {{ trip.title }} ({{ trip.startDate }})
                            </option>
                        </select>
                        <MapPin class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                    </div>
                </div>

                <div v-if="form.tripId !== 0" class="bg-pink-50 rounded-xl p-4 border border-pink-100">
                    <div class="flex items-center gap-2 text-[#DE2E5F] font-bold text-sm mb-2">
                        <Map class="w-4 h-4" />
                        <span>여행 경로가 자동으로 연결되었습니다!</span>
                    </div>
                    <div class="text-xs text-gray-600 bg-white p-3 rounded-lg border border-pink-100 whitespace-pre-wrap max-h-24 overflow-y-auto">
                        {{ tripRouteSummary || '경로 정보를 불러오는 중...' }}
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-2">대표 사진</label>
                    <div class="w-full h-48 bg-gray-100 rounded-xl overflow-hidden relative border">
                         <img :src="form.thumbnail || defaultImg" class="w-full h-full object-cover" />
                         <div class="absolute bottom-2 right-2 bg-black/50 text-white text-[10px] px-2 py-1 rounded-full">
                            {{ form.tripId !== 0 ? '여행지 사진' : '기본 이미지' }}
                         </div>
                    </div>
                    <p class="text-xs text-gray-400 mt-2">
                        * 여행을 선택하면 해당 여행지의 사진이 자동으로 설정됩니다.<br>
                        * 여행을 선택하지 않거나 사진이 없으면 기본 이미지가 적용됩니다.
                    </p>
                </div>

                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-1">제목</label>
                    <input v-model="form.title" type="text" placeholder="어떤 여행이었나요?" class="w-full p-3 bg-gray-50 rounded-xl border-none focus:ring-2 focus:ring-[#DE2E5F]" />
                </div>

                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-1">내용</label>
                    <textarea v-model="form.content" rows="6" placeholder="솔직한 후기를 들려주세요." class="w-full p-3 bg-gray-50 rounded-xl border-none focus:ring-2 focus:ring-[#DE2E5F] resize-none"></textarea>
                </div>
            </div>

            <div class="p-4 border-t bg-gray-50">
                <button @click="handleSave" class="w-full py-3 bg-[#DE2E5F] text-white font-bold rounded-xl hover:bg-[#c92552] transition-colors">
                    {{ isEditing ? '수정 완료' : '등록하기' }}
                </button>
            </div>
        </div>
    </div>

    <div v-if="showDetailModal && currentReview" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showDetailModal = false"></div>
        <div class="bg-white w-full max-w-2xl rounded-3xl shadow-2xl relative overflow-hidden z-10 flex flex-col max-h-[90vh]">
            
            <div class="relative h-64 sm:h-80 bg-black">
                <img :src="currentReview.thumbnail || defaultImg" class="w-full h-full object-cover opacity-90" />
                <button @click="showDetailModal = false" class="absolute top-4 right-4 bg-black/50 p-2 rounded-full text-white hover:bg-black/70">
                    <X class="w-5 h-5" />
                </button>
                <div v-if="currentReview.tripTitle" class="absolute top-4 left-4 bg-black/50 backdrop-blur-sm text-white text-xs font-bold px-3 py-1.5 rounded-full flex items-center gap-1">
                    <MapPin class="w-3 h-3" /> {{ currentReview.tripTitle }}
                </div>
                <div class="absolute bottom-0 left-0 right-0 p-6 bg-gradient-to-t from-black/80 to-transparent">
                    <h2 class="text-2xl font-bold text-white mb-1">{{ currentReview.title }}</h2>
                    <div class="flex items-center gap-2 text-white/80 text-sm">
                        <span>{{ currentReview.writerName }}</span>
                        <span>•</span>
                        <span>{{ currentReview.createdAt }}</span>
                    </div>
                </div>
            </div>

            <div class="p-6 overflow-y-auto">
                <p class="text-gray-700 leading-relaxed whitespace-pre-wrap text-lg">{{ currentReview.content }}</p>
            </div>

            <div class="p-4 border-t bg-gray-50 flex items-center justify-between">
                <button 
                    @click="handleLike(currentReview)"
                    class="flex items-center gap-2 px-4 py-2 rounded-full transition-colors font-bold"
                    :class="currentReview.isLiked ? 'bg-pink-100 text-[#DE2E5F]' : 'bg-gray-200 text-gray-500'"
                >
                    <Heart class="w-5 h-5" :class="{'fill-current': currentReview.isLiked}" />
                    {{ currentReview.likeCount }}
                </button>

                <div v-if="currentReview.isMine" class="flex gap-2">
                    <button @click="openEditModal(currentReview)" class="flex items-center gap-1 px-4 py-2 bg-gray-200 rounded-xl text-sm font-bold text-gray-700 hover:bg-gray-300">
                        <Edit2 class="w-4 h-4" /> 수정
                    </button>
                    <button @click="handleDelete(currentReview.reviewId)" class="flex items-center gap-1 px-4 py-2 bg-red-100 rounded-xl text-sm font-bold text-red-600 hover:bg-red-200">
                        <Trash2 class="w-4 h-4" /> 삭제
                    </button>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}
.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>