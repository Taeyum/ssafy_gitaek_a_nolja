<script setup>
import { ref, onMounted, defineEmits } from "vue";
import { ArrowLeft, Heart, Eye } from "lucide-vue-next";
import { getLikedReviewList, getReviewDetail, toggleReviewLike } from "@/api/review";

const emit = defineEmits(["back"]);

// 상태 관리
const reviews = ref([]);
const isLoading = ref(false);
const showDetailModal = ref(false);
const selectedReview = ref({});

// 좋아요한 목록 불러오기
const fetchLikedReviews = async () => {
  isLoading.value = true;
  try {
    const res = await getLikedReviewList();
    reviews.value = res.data;
  } catch (e) {
    console.error("좋아요 목록 로드 실패", e);
  } finally {
    isLoading.value = false;
  }
};

// 상세 보기 모달 열기
const openDetail = async (review) => {
  try {
    // 상세 정보(내용 등) 가져오기
    const res = await getReviewDetail(review.reviewId);
    selectedReview.value = res.data;
    showDetailModal.value = true;
  } catch (e) {
    alert("상세 정보를 불러오지 못했습니다.");
  }
};

// 모달 닫기
const closeDetail = () => {
  showDetailModal.value = false;
  selectedReview.value = {};
  // 닫을 때 목록 갱신 (좋아요 취소했을 수도 있으므로)
  fetchLikedReviews();
};

// 좋아요 토글 (상세 모달 내부용)
const handleLike = async (review) => {
  try {
    const res = await toggleReviewLike(review.reviewId);
    review.isLiked = res.data; // true or false
    review.likeCount += review.isLiked ? 1 : -1;
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  fetchLikedReviews();
});
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <header class="bg-white border-b sticky top-0 z-10 px-6 py-4 flex items-center gap-4">
      <button 
        @click="emit('back')" 
        class="p-2 hover:bg-gray-100 rounded-full transition-colors"
      >
        <ArrowLeft class="w-6 h-6 text-gray-700" />
      </button>
      <div>
        <h1 class="text-xl font-bold text-gray-800">내가 좋아요한 찐후기</h1>
        <p class="text-xs text-gray-400">관심 있는 여행 계획들을 모아봤어요</p>
      </div>
    </header>

    <main class="flex-1 p-6 max-w-7xl mx-auto w-full">
      <div v-if="isLoading" class="text-center py-20 text-gray-400">
        로딩 중...
      </div>

      <div v-else-if="reviews.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div 
          v-for="review in reviews" 
          :key="review.reviewId"
          @click="openDetail(review)"
          class="bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-all cursor-pointer group border border-gray-100"
        >
          <div class="h-48 overflow-hidden bg-gray-200 relative">
            <img 
              v-if="review.thumbnail" 
              :src="review.thumbnail" 
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" 
            />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
              NO IMAGE
            </div>
            <div v-if="review.tripTitle" class="absolute bottom-2 left-2 bg-black/60 text-white text-[10px] px-2 py-1 rounded-full backdrop-blur-sm">
              ✈️ {{ review.tripTitle }}
            </div>
          </div>

          <div class="p-4">
            <h3 class="font-bold text-gray-800 mb-1 line-clamp-1">{{ review.title }}</h3>
            <div class="flex items-center justify-between text-xs text-gray-500 mt-3">
              <span>{{ review.writerName }}</span>
              <div class="flex items-center gap-2">
                <span class="flex items-center gap-0.5 text-[#DE2E5F]">
                  <Heart class="w-3.5 h-3.5 fill-current" /> {{ review.likeCount }}
                </span>
                <span class="flex items-center gap-0.5">
                  <Eye class="w-3.5 h-3.5" /> {{ review.hit }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-20 text-gray-400">
        <Heart class="w-12 h-12 mx-auto mb-3 text-gray-300" />
        <p>아직 좋아요한 찐후기가 없습니다.</p>
      </div>
    </main>

    <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm" @click.self="closeDetail">
        <div class="bg-white rounded-3xl w-full max-w-2xl max-h-[90vh] overflow-hidden shadow-2xl flex flex-col relative animate-blob">
            <div class="h-64 bg-gray-200 relative shrink-0">
                <img v-if="selectedReview.thumbnail" :src="selectedReview.thumbnail" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center bg-gray-100 text-gray-400">이미지 없음</div>
                <button @click="closeDetail" class="absolute top-4 right-4 bg-black/30 text-white p-2 rounded-full hover:bg-black/50 transition-colors backdrop-blur-sm">
                   <ArrowLeft class="w-5 h-5" />
                </button>
                <div class="absolute bottom-0 left-0 right-0 p-6 bg-gradient-to-t from-black/80 to-transparent">
                    <h2 class="text-2xl font-bold text-white mb-1">{{ selectedReview.title }}</h2>
                    <div class="flex items-center gap-2 text-white/80 text-sm">
                        <span>{{ selectedReview.writerName }}</span><span>•</span><span>{{ selectedReview.createdAt }}</span>
                    </div>
                </div>
            </div>
            <div class="p-6 overflow-y-auto">
                <p class="text-gray-700 leading-relaxed whitespace-pre-wrap text-lg">{{ selectedReview.content }}</p>
            </div>
            <div class="p-4 border-t bg-gray-50 flex items-center justify-between shrink-0">
                <button @click="handleLike(selectedReview)" class="flex items-center gap-2 px-4 py-2 rounded-full transition-colors font-bold" 
                   :class="selectedReview.isLiked ? 'bg-pink-100 text-[#DE2E5F]' : 'bg-gray-200 text-gray-500'">
                   <Heart class="w-5 h-5" :class="{'fill-current': selectedReview.isLiked}" /> {{ selectedReview.likeCount }}
                </button>
                <div class="flex items-center gap-1 text-xs text-gray-400"><Eye class="w-4 h-4"/> 조회수 {{ selectedReview.hit }}</div>
            </div>
        </div>
    </div>
  </div>
</template>