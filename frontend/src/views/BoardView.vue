<script setup>
import { ref, onMounted, onUnmounted, defineEmits, computed, nextTick, watch } from "vue";
import {
  getBoardList,
  getBoardDetail,
  writeBoard,
  updateBoard,
  deleteBoard,
  toggleLike,
} from "@/api/board";

import { 
  getReviewList, 
  getReviewDetail, 
  toggleReviewLike 
} from "@/api/review";

import { useUserStore } from "@/stores/userStore";
import {
  Edit,
  Trash2,
  ArrowLeft,
  MessageSquare,
  PenTool,
  Map,
  Star,
  Menu,
  ChevronRight,
  ChevronLeft,
  Trophy,
  Camera,
  Eye,
  Heart,    
  X,        
  MapPin,   
  Edit2,
  Search,
  User
} from "lucide-vue-next";

import christmasImg from "@/assets/christmas.jpg";
import likeImg from "@/assets/like.jpg";
import presentImg from "@/assets/present.jpg";

import { Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const emit = defineEmits(["back", "go-review", "go-mypage", "go-liked-reviews"]);
const userStore = useUserStore();

// --- 상태 관리 ---
const mode = ref("list");
const list = ref([]); // 전체 게시판 글
const reviews = ref([]); 

//1224
const currentPost = ref({});
// const currentCategory = ref("all");

const form = ref({ title: "", content: "" });

// 모달 상태
const showReviewModal = ref(false);
const selectedReview = ref(null);

// 게시판 필터 상태
const boardSearchKeyword = ref(""); 
const boardSortBy = ref("latest"); 
const showMyBoardsOnly = ref(false);

// 게시판 페이지네이션 상태
const boardCurrentPage = ref(1);
const boardItemsPerPage = 10;

// ★ 실시간 접속자 관련 상태 & 함수
// 더미 데이터 (테스트용)
const onlineUsers = ref([
  { nickname: "여행가고파", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" },
  { nickname: "떠나자", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka" },
  { nickname: "구름따라", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Bob" },
  { nickname: "바람따라", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Caleb" },
  { nickname: "별보러가자", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Dia" },
  { nickname: "낭만캠퍼", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Edward" },
  { nickname: "휴식필요", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Fiona" },
  { nickname: "주말랭이", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=George" },
  { nickname: "산타는형", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Hanna" },
  { nickname: "바다소녀", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Ivan" },
  { nickname: "캠핑러", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Jack" },
  { nickname: "등산왕", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Kyle" },
  { nickname: "서핑매니아", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Liam" },
  { nickname: "뚜벅이", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Mina" },
  { nickname: "사진작가", profileImg: "https://api.dicebear.com/7.x/avataaars/svg?seed=Noah" },
]);

const userLimit = ref(10); // 10명씩 보여주기
let stompClient = null;

// 화면에 실제로 보여줄 명단
const visibleOnlineUsers = computed(() => {
  return onlineUsers.value.slice(0, userLimit.value);
});

// 사이드바 스크롤 감지 함수
const handleUserScroll = (e) => {
  const { scrollTop, clientHeight, scrollHeight } = e.target;
  // 스크롤이 바닥에 닿으면
  if (scrollTop + clientHeight >= scrollHeight - 10) {
    if (userLimit.value < onlineUsers.value.length) {
      userLimit.value += 10; 
    }
  }
};

const connectSocket = () => {
  if (!userStore.isLoggedIn) return;

  const socket = new SockJS("http://localhost:8080/ws-stomp");
  stompClient = Stomp.over(socket);
  stompClient.debug = () => {}; 

  stompClient.connect({}, () => {
    stompClient.subscribe("/sub/board/online", (message) => {
      const users = JSON.parse(message.body);
      const uniqueUsers = users.filter((v, i, a) => a.findIndex(t => (t.nickname === v.nickname)) === i);
      onlineUsers.value = uniqueUsers;
    });

    const myInfo = {
      nickname: userStore.userInfo.name,
      profileImg: userStore.userInfo.profileImg
    };
    stompClient.send("/pub/board/join", {}, JSON.stringify(myInfo));
  });
};

const disconnectSocket = () => {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
  }
};

// --- Computed: 찐후기 랭킹 ---
const topRankedList = computed(() => {
  return [...reviews.value] 
    .sort((a, b) => {
        if (b.likeCount !== a.likeCount) return b.likeCount - a.likeCount;
        return b.hit - a.hit;
    }) 
    .slice(0, 8); 
});

// --- Computed: 게시판 필터링 및 정렬 ---
const filteredBoardList = computed(() => {
  let result = [...list.value];

  if (showMyBoardsOnly.value && userStore.isLoggedIn) {
      const myName = userStore.userInfo.name || userStore.userInfo.nickname;
      result = result.filter(item => item.writerName === myName); 
  }

  if (boardSearchKeyword.value.trim()) {
      const keyword = boardSearchKeyword.value.trim().toLowerCase();
      result = result.filter(item => item.title.toLowerCase().includes(keyword));
  }

  result.sort((a, b) => {
      if (boardSortBy.value === 'latest') {
          return b.boardId - a.boardId;
      } else if (boardSortBy.value === 'hit') {
          if (b.hit !== a.hit) return b.hit - a.hit;
          return b.boardId - a.boardId;
      } else if (boardSortBy.value === 'like') {
          if (b.likeCount !== a.likeCount) return b.likeCount - a.likeCount;
          return b.boardId - a.boardId;
      }
      return 0;
  });

  return result;
});

// Computed: 게시판 페이지네이션
const boardTotalPages = computed(() => {
  return Math.ceil(filteredBoardList.value.length / boardItemsPerPage);
});

const paginatedBoardList = computed(() => {
  const start = (boardCurrentPage.value - 1) * boardItemsPerPage;
  const end = start + boardItemsPerPage;
  return filteredBoardList.value.slice(start, end);
});

// 필터 변경 시 1페이지로 리셋
watch([boardSearchKeyword, boardSortBy, showMyBoardsOnly], () => {
  boardCurrentPage.value = 1;
});

// 페이지 변경 함수
const setBoardPage = (page) => {
  if (page >= 1 && page <= boardTotalPages.value) {
    boardCurrentPage.value = page;
  }
};

// --- 초기 데이터 로드 ---
const loadData = async () => {
  try {
    const boardRes = await getBoardList();
    list.value = boardRes.data;
    const reviewRes = await getReviewList();
    reviews.value = reviewRes.data;
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  loadData();
  startInterval();
  connectSocket(); 
});

onUnmounted(() => {
  clearInterval(slideInterval);
  disconnectSocket(); 
});

// --- 캐러셀 로직 ---
const currentSlide = ref(0);
const isTransitioning = ref(true);
const isAnimating = ref(false);
let slideInterval = null;

const slides = [
  { 
    id: 1, 
    badge: "EVENT", 
    title: "이번 겨울, 어디로 떠나세요?", 
    desc: "여행 후기 남기고 경품 받아가세요!", 
    btnText: "후기 작성하러 가기", 
    action: () => emit('go-review'), 
    image: presentImg
  },
  { id: 2, badge: "MERRY CHRISTMAS", title: "따뜻한 크리스마스 파티", desc: "소중한 친구들과 함께 나누는 행복한 시간", image: christmasImg, gradient: "from-gray-800 to-gray-900" },
  { 
    id: 3, 
    badge: "MY LIKE", 
    title: "내가 찜한 여행 계획 모아보기", 
    desc: "마음 속에 담아 두었던 여행 계획들을 바로 확인하세요.", 
    image: likeImg,           
    btnText: "찜 목록 보러가기", 
    action: () => emit('go-liked-reviews'), 
    gradient: "from-purple-800 to-indigo-900" 
  }
];

const extendedSlides = computed(() => [...slides, slides[0]]);

const nextSlide = () => {
  if (isAnimating.value) return;
  isAnimating.value = true;
  isTransitioning.value = true;
  currentSlide.value++;
  if (currentSlide.value === slides.length) {
    setTimeout(() => {
      isTransitioning.value = false;
      currentSlide.value = 0;
      nextTick(() => { isAnimating.value = false; });
    }, 500); 
  } else { setTimeout(() => { isAnimating.value = false; }, 500); }
};

const prevSlide = () => {
  if (isAnimating.value) return;
  isAnimating.value = true;
  if (currentSlide.value === 0) {
    isTransitioning.value = false;
    currentSlide.value = slides.length;
    setTimeout(() => {
      isTransitioning.value = true;
      currentSlide.value = slides.length - 1;
      setTimeout(() => { isAnimating.value = false; }, 500);
    }, 50);
  } else {
    isTransitioning.value = true;
    currentSlide.value--;
    setTimeout(() => { isAnimating.value = false; }, 500);
  }
};

const setSlide = (index) => {
  if (isAnimating.value) return;
  currentSlide.value = index;
  resetInterval();
};

const startInterval = () => { slideInterval = setInterval(nextSlide, 5000); };
const resetInterval = () => { clearInterval(slideInterval); startInterval(); };

// --- 기능 함수들 ---

const fetchList = async () => {
  try {
    const resBoard = await getBoardList();
    list.value = resBoard.data;
    const resReview = await getReviewList();
    reviews.value = resReview.data;
  } catch (e) {
    console.error(e);
  }
};

const toggleMyBoards = () => {
    if (!userStore.isLoggedIn) return alert("로그인이 필요합니다.");
    showMyBoardsOnly.value = !showMyBoardsOnly.value;
};

const openPopularReview = async (reviewId) => {
  try {
    const res = await getReviewDetail(reviewId);
    selectedReview.value = res.data;
    showReviewModal.value = true;
    const target = reviews.value.find(r => r.reviewId === reviewId);
    if(target) target.hit = res.data.hit;
  } catch (e) {
    alert("내용을 불러오지 못했습니다.");
  }
};

const handleReviewLike = async (review) => {
  if (!userStore.isLoggedIn) return alert("로그인이 필요합니다.");
  try {
    const res = await toggleReviewLike(review.reviewId);
    const isLiked = res.data; 
    if (review.isLiked !== isLiked) {
        review.likeCount += isLiked ? 1 : -1;
        review.isLiked = isLiked;
    }
    const target = reviews.value.find(r => r.reviewId === review.reviewId);
    if(target) {
        target.isLiked = review.isLiked;
        target.likeCount = review.likeCount;
    }
  } catch (e) {
    console.error(e);
  }
};

const handleBoardListLike = async (item) => {
  if (!userStore.isLoggedIn) return alert("로그인이 필요합니다.");
  try {
     const { data } = await toggleLike(item.boardId);
     item.isLiked = data;
     item.likeCount += data ? 1 : -1;
  } catch (e) {
     console.error(e);
  }
};

const handleBoardLike = async () => {
  if (!userStore.isLoggedIn) return alert("로그인 필요");
  try {
     const { data } = await toggleLike(currentPost.value.boardId);
     currentPost.value.isLiked = data;
     currentPost.value.likeCount += data ? 1 : -1;
     const target = list.value.find(i => i.boardId === currentPost.value.boardId);
     if (target) {
        target.isLiked = data;
        target.likeCount = currentPost.value.likeCount;
     }
  } catch (e) {
     console.error(e);
  }
};

const goDetail = async (id) => {
  try {
    const res = await getBoardDetail(id);
    currentPost.value = res.data;
    const myName = userStore.userInfo.name || userStore.userInfo.nickname;
    
    if (res.data.isMine) currentPost.value.isMine = true;
    else if (myName && res.data.writerName === myName) currentPost.value.isMine = true;
    else currentPost.value.isMine = false;

    const targetItem = list.value.find((item) => item.boardId === id);
    if (targetItem) targetItem.hit = res.data.hit;

    mode.value = "detail";
  } catch (e) {
    console.error(e);
    alert("글을 불러오지 못했습니다.");
  }
};

const goList = () => {
  mode.value = "list";
  fetchList();
};

const goWrite = () => {
  if (!userStore.isLoggedIn) return alert("로그인이 필요합니다.");
  form.value = { title: "", content: "" };
  mode.value = "write";
};

const goEdit = () => {
  form.value = {
    title: currentPost.value.title,
    content: currentPost.value.content,
  };
  mode.value = "edit";
};

const save = async () => {
  if (!form.value.title.trim() || !form.value.content.trim())
    return alert("제목과 내용을 입력하세요.");
  try {
    if (mode.value === "write") {
      await writeBoard(form.value);
      alert("작성이 완료되었습니다.");
    } else {
      await updateBoard(currentPost.value.boardId, form.value);
      alert("수정이 완료되었습니다.");
    }
    goList();
  } catch (e) {
    alert("저장 실패");
  }
};

const remove = async () => {
  if (!confirm("정말 삭제하시겠습니까? 복구할 수 없습니다.")) return;
  try {
    await deleteBoard(currentPost.value.boardId);
    alert("삭제되었습니다.");
    goList();
  } catch (e) {
    alert("삭제 실패");
  }
};
</script>

<template>
  <div class="max-w-7xl mx-auto p-6 min-h-screen bg-white flex gap-8 relative">
    
    <aside class="w-64 shrink-0 hidden lg:block sticky top-6 h-fit">
      <div class="bg-gray-50 rounded-3xl p-6 border border-gray-100">
        <h3 class="text-xl font-black text-gray-800 mb-6 flex items-center gap-2">
          <Menu class="w-6 h-6 text-[#DE2E5F]" />
          커뮤니티
        </h3>
        
        <nav class="space-y-2">
          <button 
            @click="currentCategory = 'all'; goList()" 
            class="w-full text-left px-4 py-3 rounded-xl font-bold transition-all flex items-center gap-3" 
            :class="currentCategory === 'all' ? 'bg-white text-[#DE2E5F] shadow-sm ring-1 ring-gray-200' : 'text-gray-500 hover:bg-gray-100'"
          >
            <MessageSquare class="w-5 h-5" /> 전체글 보기
          </button>

          <button 
            @click="currentCategory = 'review'; $emit('go-review')" 
            class="w-full text-left px-4 py-3 rounded-xl font-bold transition-all flex items-center gap-3"
            :class="currentCategory === 'review' ? 'bg-white text-[#DE2E5F] shadow-sm ring-1 ring-gray-200' : 'text-gray-500 hover:bg-gray-100'"
          >
            <Star class="w-5 h-5" /> 여행 찐후기
          </button>
          
          <button 
            @click="currentCategory = 'like'; $emit('go-liked-reviews')" 
            class="w-full text-left px-4 py-3 rounded-xl font-bold transition-all flex items-center gap-3"
            :class="currentCategory === 'like' ? 'bg-white text-[#DE2E5F] shadow-sm ring-1 ring-gray-200' : 'text-gray-500 hover:bg-gray-100'"
          >
            <Heart class="w-5 h-5" /> 좋아요
          </button>
        </nav>
        <div class="mt-8 pt-8 border-t border-gray-200">
           <p class="text-xs text-gray-400 mb-4 font-bold">내 활동</p>
           <div v-if="userStore.isLoggedIn" @click="$emit('go-mypage')" class="flex items-center gap-3 mb-2 cursor-pointer p-2 -ml-2 rounded-xl hover:bg-gray-100 transition-all group">
             <img :src="userStore.userInfo.profileImg" class="w-10 h-10 rounded-full bg-white shadow-sm border object-cover" />
             <div class="flex-1 min-w-0">
               <p class="text-sm font-bold text-gray-800 truncate">{{ userStore.userInfo.name }}님</p>
               <p class="text-xs text-gray-500 truncate">{{ userStore.userInfo.email }}</p>
             </div>
             <ChevronRight class="w-4 h-4 text-gray-300 group-hover:text-gray-500 transition-colors" />
           </div>
           <div v-else class="text-sm text-gray-400">로그인이 필요합니다.</div>
        </div>

        <div class="mt-8 pt-8 border-t border-gray-200">
           <div class="flex items-center gap-2 mb-4">
             <div class="relative flex h-3 w-3">
               <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
               <span class="relative inline-flex rounded-full h-3 w-3 bg-green-500"></span>
             </div>
             <p class="text-xs text-gray-400 font-bold">투게더 ({{ onlineUsers.length }})</p>
           </div>

           <div 
             v-if="onlineUsers.length > 0" 
             @scroll="handleUserScroll"
             class="space-y-3 h-[500px] overflow-y-auto custom-scrollbar pr-2"
           >
              <div 
                v-for="(user, idx) in visibleOnlineUsers" 
                :key="idx" 
                class="flex items-center gap-3 p-2 bg-white rounded-xl shadow-sm border border-gray-100"
              >
                <div class="relative">
                  <img :src="user.profileImg" class="w-8 h-8 rounded-full bg-gray-100 object-cover border" />
                  <div class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 border-2 border-white rounded-full"></div>
                </div>
                <span class="text-sm font-bold text-gray-700 truncate max-w-[100px]">{{ user.nickname }}</span>
              </div>
           </div>
           
           <div v-else class="text-xs text-center text-gray-400 py-4 bg-white rounded-xl border border-dashed">
              접속자가 없습니다.
           </div>
        </div>

      </div>
    </aside>

    <main class="flex-1 min-w-0">
      
      <div v-if="mode === 'list'" class="space-y-8 animate-fade-in">
        
        <button @click="emit('back')" class="flex items-center gap-2 text-gray-400 hover:text-[#DE2E5F] font-bold transition-colors"><ArrowLeft class="w-5 h-5" /> 메인으로 돌아가기</button>

        <div class="relative overflow-hidden rounded-3xl shadow-lg h-64 group select-none bg-gray-100">
           <div class="flex h-full ease-in-out" :class="isTransitioning ? 'transition-transform duration-500' : 'duration-0'" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
             <div v-for="(slide, index) in extendedSlides" :key="index" class="w-full shrink-0 h-full relative">
                <div class="absolute inset-0">
                   <template v-if="slide.image">
                      <img :src="slide.image" class="w-full h-full object-cover" />
                      <div class="absolute inset-0 bg-black/40"></div>
                   </template>
                   <div v-else class="w-full h-full bg-gradient-to-r" :class="slide.gradient"></div>
                </div>
                <div class="relative z-10 h-full flex flex-col justify-center p-8 text-white">
                   <div class="max-w-lg">
                      <span class="bg-white/20 px-3 py-1 rounded-full text-xs font-bold backdrop-blur-sm mb-3 inline-block">{{ slide.badge }}</span>
                      <h2 class="text-3xl font-black mb-2 leading-tight drop-shadow-md">{{ slide.title }}</h2>
                      <p class="opacity-95 text-lg drop-shadow-sm" :class="slide.btnText ? 'mb-6' : 'mb-0'">{{ slide.desc }}</p>
                      
                     <button 
  v-if="slide.btnText" 
  @click="slide.action" 
  class="font-black text-sm px-8 py-3 rounded-full transition-all duration-300 transform" 
  :class="slide.id === 1 
    ? 'bg-black/30 text-white shadow-none drop-shadow-lg hover:bg-black/50' 
    : 'bg-white shadow-xl hover:bg-gray-50 ' + (slide.image ? 'text-gray-800' : (slide.gradient.includes('FF6B6B') ? 'text-[#DE2E5F]' : 'text-emerald-600'))"
>
  {{ slide.btnText }}
</button>

                   </div>
                </div>
             </div>
           </div>
           <button @click="prevSlide" class="absolute left-4 top-1/2 -translate-y-1/2 bg-white/20 hover:bg-white/40 p-2 rounded-full backdrop-blur-md text-white opacity-0 group-hover:opacity-100 transition-opacity z-20"><ChevronLeft class="w-6 h-6" /></button>
           <button @click="nextSlide" class="absolute right-4 top-1/2 -translate-y-1/2 bg-white/20 hover:bg-white/40 p-2 rounded-full backdrop-blur-md text-white opacity-0 group-hover:opacity-100 transition-opacity z-20"><ChevronRight class="w-6 h-6" /></button>
           <div class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2 z-20">
             <button v-for="(slide, index) in slides" :key="index" @click="setSlide(index)" class="w-2.5 h-2.5 rounded-full transition-all duration-300" :class="(currentSlide % slides.length) === index ? 'bg-white w-6' : 'bg-white/50 hover:bg-white/80'"></button>
           </div>
        </div>

        <div>
           <h3 class="text-xl font-black text-gray-800 mb-4 flex items-center gap-2"><Trophy class="w-5 h-5 text-yellow-500" /> 실시간 인기 찐후기 TOP 8</h3>
           <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
             <div v-for="(item, index) in topRankedList" :key="item.reviewId" @click="openPopularReview(item.reviewId)" class="group relative aspect-[3/4] rounded-2xl overflow-hidden cursor-pointer shadow-md hover:shadow-xl transition-all">
                 <img :src="item.thumbnail || `https://source.unsplash.com/random/400x500/?travel&sig=${item.reviewId}`" class="absolute inset-0 w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                 <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
                 <div class="absolute top-0 left-0 bg-[#DE2E5F] text-white w-10 h-10 flex items-center justify-center font-black text-xl rounded-br-2xl shadow-lg z-10">{{ index + 1 }}</div>
                 <div class="absolute bottom-0 left-0 p-4 w-full">
                    <h4 class="text-white font-bold text-lg leading-tight mb-1 line-clamp-2 group-hover:text-pink-200 transition-colors">{{ item.title }}</h4>
                    <div class="flex items-center justify-between text-white/80 text-xs font-medium">
                       <span>{{ item.writerName }}</span>
                       <div class="flex items-center gap-2">
                           <span class="flex items-center gap-0.5"><Heart class="w-3 h-3" :class="{'fill-current text-pink-300': item.isLiked}" /> {{ item.likeCount }}</span>
                           <span class="flex items-center gap-0.5"><Eye class="w-3 h-3"/> {{ item.hit }}</span>
                       </div>
                    </div>
                 </div>
             </div>
             <div v-if="topRankedList.length === 0" class="col-span-4 text-center py-10 text-gray-400 bg-gray-50 rounded-2xl border border-dashed">아직 등록된 찐후기가 없어요.</div>
           </div>
        </div>

        <div>
           <div class="flex justify-between items-end mb-4">
             <h3 class="text-xl font-black text-gray-800 flex items-center gap-2"><MessageSquare class="w-5 h-5 text-gray-400" /> 최신 글</h3>
             <button @click="goWrite" class="bg-gray-900 text-white px-4 py-2 rounded-lg font-bold text-sm shadow hover:bg-gray-800 transition-all flex items-center gap-2"><PenTool class="w-3 h-3" /> 글쓰기</button>
           </div>

           <div class="mb-4 space-y-3">
               <div class="relative">
                   <input v-model="boardSearchKeyword" type="text" placeholder="제목으로 검색..." class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 bg-white focus:outline-none focus:border-[#DE2E5F] text-sm" />
                   <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
               </div>
               <div class="flex gap-2 overflow-x-auto pb-1 scrollbar-hide items-center">
                   <button @click="toggleMyBoards" class="flex items-center gap-1 px-3 py-1.5 rounded-full text-xs font-bold transition-all border mr-1" :class="showMyBoardsOnly ? 'bg-gray-800 text-white border-gray-800' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'"><User class="w-3 h-3" /> 내 글만</button>
                   <button @click="boardSortBy = 'latest'" class="px-3 py-1.5 rounded-full text-xs font-bold transition-all border" :class="boardSortBy === 'latest' ? 'bg-[#DE2E5F] text-white border-[#DE2E5F]' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'">최신순</button>
                   <button @click="boardSortBy = 'hit'" class="px-3 py-1.5 rounded-full text-xs font-bold transition-all border" :class="boardSortBy === 'hit' ? 'bg-[#DE2E5F] text-white border-[#DE2E5F]' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'">조회순</button>
                   <button @click="boardSortBy = 'like'" class="px-3 py-1.5 rounded-full text-xs font-bold transition-all border" :class="boardSortBy === 'like' ? 'bg-[#DE2E5F] text-white border-[#DE2E5F]' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'">인기순</button>
               </div>
           </div>

           <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mb-6">
             <table class="w-full text-left">
              <thead class="bg-gray-50 text-gray-600 text-sm uppercase">
                <tr>
                  <th class="px-6 py-4 w-16 text-center">No</th>
                  <th class="px-6 py-4">제목</th>
                  <th class="px-6 py-4 w-32">작성자</th>
                  <th class="px-6 py-4 w-24 text-center">조회수</th>
                  <th class="px-6 py-4 w-24 text-center">좋아요</th>
                  <th class="px-6 py-4 w-32 text-center">날짜</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100">
                <tr v-for="(item, index) in paginatedBoardList" :key="item.boardId" @click="goDetail(item.boardId)" class="hover:bg-pink-50 cursor-pointer transition-colors group">
                  <td class="px-6 py-4 text-center text-gray-400 font-mono">
                    {{ filteredBoardList.length - ((boardCurrentPage - 1) * boardItemsPerPage) - index }}
                  </td>
                  <td class="px-6 py-4"><div class="flex items-center justify-between"><span class="font-bold text-gray-800 group-hover:text-[#DE2E5F] transition-colors">{{ item.title }}</span></div></td>
                  <td class="px-6 py-4 flex items-center gap-2"><div class="w-6 h-6 rounded-full bg-gray-100 overflow-hidden border"><img :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${item.writerName}`" /></div><span class="text-sm text-gray-600">{{ item.writerName }}</span></td>
                  <td class="px-6 py-4 text-center text-gray-500 text-sm">{{ item.hit }}</td>
                  <td class="px-6 py-4 text-center"><button @click.stop="handleBoardListLike(item)" class="inline-flex items-center justify-center text-sm font-bold transition-colors p-1.5 rounded-full hover:bg-pink-50 w-10 h-10 mx-auto" :class="item.isLiked ? 'text-[#DE2E5F] bg-pink-50' : 'text-gray-400 hover:text-[#DE2E5F]'">{{ item.likeCount }}</button></td>
                  <td class="px-6 py-4 text-center text-gray-400 text-xs">{{ item.createdAt }}</td>
                </tr>
                <tr v-if="paginatedBoardList.length === 0"><td colspan="6" class="text-center py-16 text-gray-400"><div class="flex flex-col items-center gap-2"><MessageSquare class="w-10 h-10 opacity-20" /><span>게시글이 없습니다.</span></div></td></tr>
              </tbody>
             </table>
           </div>

           <div v-if="filteredBoardList.length > 0 && boardTotalPages > 1" class="flex justify-center items-center gap-2 pb-10">
               <button 
                   @click="setBoardPage(boardCurrentPage - 1)" 
                   :disabled="boardCurrentPage === 1"
                   class="p-2 rounded-xl border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
               >
                   <ChevronLeft class="w-5 h-5 text-gray-600" />
               </button>

               <button 
                   v-for="page in boardTotalPages" 
                   :key="page"
                   @click="setBoardPage(page)"
                   class="w-10 h-10 rounded-xl text-sm font-bold transition-all border"
                   :class="boardCurrentPage === page ? 'bg-[#DE2E5F] border-[#DE2E5F] text-white shadow-md' : 'bg-white border-transparent text-gray-500 hover:bg-gray-50 hover:text-gray-900'"
               >
                   {{ page }}
               </button>

               <button 
                   @click="setBoardPage(boardCurrentPage + 1)" 
                   :disabled="boardCurrentPage === boardTotalPages"
                   class="p-2 rounded-xl border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
               >
                   <ChevronRight class="w-5 h-5 text-gray-600" />
               </button>
           </div>
        </div>

      </div>

      <div v-else class="animate-fade-in">
         <button @click="goList" class="mb-6 flex items-center gap-2 text-gray-500 hover:text-[#DE2E5F] font-bold transition-colors"><ArrowLeft class="w-5 h-5" /> 전체 목록으로 돌아가기</button>

         <div v-if="mode === 'detail'" class="bg-white p-8 rounded-3xl shadow-lg border border-gray-100 min-h-[500px]">
            <div class="border-b pb-6 mb-8">
              <div class="flex justify-between items-start mb-4"><h2 class="text-3xl font-black text-gray-900 leading-tight flex-1">{{ currentPost.title }}</h2></div>
              <div class="flex justify-between items-center text-sm text-gray-500">
                <div class="flex items-center gap-3"><img :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${currentPost.writerName}`" class="w-8 h-8 rounded-full bg-gray-100" /><span class="font-bold text-gray-700">{{ currentPost.writerName }}</span><span class="text-gray-300">|</span><span>{{ currentPost.createdAt }}</span></div>
                <div class="bg-gray-100 px-3 py-1 rounded-full text-xs font-bold">조회수 {{ currentPost.hit }}</div>
              </div>
            </div>
            <div class="min-h-[200px] text-gray-800 leading-8 whitespace-pre-wrap text-lg">{{ currentPost.content }}</div>
            <div class="flex justify-between items-center mt-10 pt-6 border-t">
              <button @click="handleBoardLike" class="flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold transition-all active:scale-95" :class="currentPost.isLiked ? 'bg-pink-100 text-[#DE2E5F]' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"><Heart class="w-5 h-5" :class="{ 'fill-current': currentPost.isLiked }" /> 좋아요 {{ currentPost.likeCount }}</button>
              <div v-if="currentPost.isMine" class="flex gap-2">
                <button @click="goEdit" class="flex items-center gap-2 px-5 py-2.5 bg-gray-100 rounded-xl font-bold text-gray-600 hover:bg-gray-200 transition-colors"><Edit class="w-4 h-4" /> 수정</button>
                <button @click="remove" class="flex items-center gap-2 px-5 py-2.5 bg-[#DE2E5F] text-white rounded-xl font-bold hover:bg-[#c92552] transition-colors shadow-md"><Trash2 class="w-4 h-4" /> 삭제</button>
              </div>
            </div>
         </div>

         <div v-else class="bg-white p-8 rounded-3xl shadow-lg border border-gray-100 space-y-6">
            <h2 class="text-2xl font-black text-gray-800 flex items-center gap-2 pb-4 border-b">{{ mode === "write" ? "✏️ 새 글 작성" : "📝 글 수정" }}</h2>
            <div><label class="block text-sm font-bold text-gray-700 mb-2">제목</label><input v-model="form.title" type="text" class="w-full p-4 bg-gray-50 rounded-xl border border-transparent focus:bg-white focus:border-[#DE2E5F] focus:outline-none transition-all font-bold text-lg placeholder-gray-400" placeholder="제목을 입력하세요" /></div>
            <div><label class="block text-sm font-bold text-gray-700 mb-2">내용</label><textarea v-model="form.content" rows="15" class="w-full p-4 bg-gray-50 rounded-xl border border-transparent focus:bg-white focus:border-[#DE2E5F] focus:outline-none transition-all resize-none text-gray-700 placeholder-gray-400 leading-relaxed" placeholder="여행 이야기를 자유롭게 나누세요"></textarea></div>
            <div class="flex gap-3 pt-4 border-t">
              <button @click="goList" class="flex-1 py-4 bg-gray-100 rounded-xl font-bold text-gray-600 hover:bg-gray-200 transition-colors">취소</button>
              <button @click="save" class="flex-1 py-4 bg-[#DE2E5F] text-white rounded-xl font-bold hover:bg-[#c92552] shadow-md hover:shadow-lg transition-all transform active:scale-95">저장하기</button>
            </div>
         </div>
      </div>

    </main>

    <div v-if="showReviewModal && selectedReview" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showReviewModal = false"></div>
        <div class="bg-white w-full max-w-2xl rounded-3xl shadow-2xl relative overflow-hidden z-10 flex flex-col max-h-[90vh] animate-fade-in">
            <div class="relative h-64 sm:h-80 bg-black shrink-0">
                <img :src="selectedReview.thumbnail || 'https://via.placeholder.com/800x400'" class="w-full h-full object-cover opacity-90" />
                <button @click="showReviewModal = false" class="absolute top-4 right-4 bg-black/50 p-2 rounded-full text-white hover:bg-black/70 transition-colors"><X class="w-5 h-5" /></button>
                <div v-if="selectedReview.tripTitle" class="absolute top-4 left-4 bg-black/50 backdrop-blur-sm text-white text-xs font-bold px-3 py-1.5 rounded-full flex items-center gap-1"><MapPin class="w-3 h-3" /> {{ selectedReview.tripTitle }}</div>
                <div class="absolute bottom-0 left-0 right-0 p-6 bg-gradient-to-t from-black/80 to-transparent">
                    <h2 class="text-2xl font-bold text-white mb-1 drop-shadow-md">{{ selectedReview.title }}</h2>
                    <div class="flex items-center gap-2 text-white/80 text-sm"><span>{{ selectedReview.writerName }}</span><span>•</span><span>{{ selectedReview.createdAt }}</span></div>
                </div>
            </div>
            <div class="p-6 overflow-y-auto"><p class="text-gray-700 leading-relaxed whitespace-pre-wrap text-lg">{{ selectedReview.content }}</p></div>
            <div class="p-4 border-t bg-gray-50 flex items-center justify-between shrink-0">
                <button @click="handleReviewLike(selectedReview)" class="flex items-center gap-2 px-4 py-2 rounded-full transition-colors font-bold" :class="selectedReview.isLiked ? 'bg-pink-100 text-[#DE2E5F]' : 'bg-gray-200 text-gray-500 hover:bg-gray-300'"><Heart class="w-5 h-5" :class="{'fill-current': selectedReview.isLiked}" /> {{ selectedReview.likeCount }}</button>
                <div class="flex items-center gap-1 text-xs text-gray-400"><Eye class="w-4 h-4"/> 조회수 {{ selectedReview.hit }}</div>
            </div>
        </div>
    </div>

  </div>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* ★ 스크롤바 디자인 (깔끔하고 얇은 회색 바) */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px; /* 얇게 */
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent; /* 배경 투명 */
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: transparent; /* 연한 회색 */
    border-radius: 3px;        /* 둥글게 */
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background-color: #9ca3af; /* 호버 시 약간 진해짐 */
}
</style>