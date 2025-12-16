<script setup>
import { ref, defineEmits } from 'vue'
import { MapPin, Users, Sparkles, X, Calendar, User, Heart, Type, ArrowRight, Github, Instagram, Twitter } from 'lucide-vue-next'
import { useTripStore } from '@/stores/tripStore'
import { useUserStore } from '@/stores/userStore'
import AuthModal from '@/components/AuthModal.vue'

const emit = defineEmits(['start', 'my-page'])
const tripStore = useTripStore()
const userStore = useUserStore()

const showCreateModal = ref(false)
const showAuthModal = ref(false)
const authMode = ref('login')

const inviteCodeInput = ref("") // ★ 초대 코드 입력값 저장용

// 입력 폼 데이터
const tripTitle = ref("")   
const duration = ref(3)
const members = ref(4)
const tripStyle = ref('friend')
const startDate = ref(new Date().toISOString().substring(0, 10))

const openAuthModal = (mode) => {
  authMode.value = mode
  showAuthModal.value = true
}

const handleCreateGroup = async () => {
  if (!userStore.isLoggedIn) {
    alert("로그인이 필요한 서비스입니다!")
    openAuthModal('login')
    return
  }

  const title = tripTitle.value.trim() ? tripTitle.value : '나의 즐거운 여행'

  const success = await tripStore.createNewTrip({
    title: title,       
    duration: duration.value,
    members: members.value, 
    style: tripStyle.value,
    startDate: startDate.value 
  })

  if (success) {
    showCreateModal.value = false
    emit('start')
  }
}

// ★ [추가] 초대 코드 입장 핸들러
const handleJoinByCode = async () => {
  if (!userStore.isLoggedIn) {
    alert("로그인이 필요합니다.")
    openAuthModal('login')
    return
  }
  if (!inviteCodeInput.value.trim()) {
    alert("초대 코드를 입력해주세요.")
    return
  }

  const joinedTrip = await tripStore.joinTrip(inviteCodeInput.value)
  
  if (joinedTrip) {
    alert(`[${joinedTrip.title}] 여행에 참가했습니다!`)
    emit('my-page') // 마이페이지로 이동 (또는 바로 대시보드로 이동 가능)
  }
}
</script>

<template>
  <div class="min-h-screen bg-white font-sans overflow-x-hidden selection:bg-[#DE2E5F] selection:text-white">
    
    <header class="fixed w-full top-0 z-50 bg-white/70 backdrop-blur-lg border-b border-gray-100 transition-all">
      <div class="container mx-auto px-6 h-16 flex items-center justify-between">
        <div class="flex items-center gap-2 cursor-pointer" @click="$router.go(0)"> <div class="w-8 h-8 rounded-xl bg-gradient-to-tr from-[#DE2E5F] to-[#ff5d8d] flex items-center justify-center shadow-lg shadow-pink-200">
            <MapPin class="h-4 w-4 text-white" />
          </div>
          <h1 class="text-xl font-extrabold tracking-tight text-gray-900">Gitaek <span class="text-[#DE2E5F]">Anolja</span></h1>
        </div>
        
        <nav class="flex items-center gap-4">
          <template v-if="!userStore.isLoggedIn">
            <button @click="openAuthModal('login')" class="text-sm font-semibold text-gray-500 hover:text-[#DE2E5F] transition-colors">로그인</button>
            <button @click="openAuthModal('signup')" class="px-5 py-2 bg-[#DE2E5F] text-white text-sm font-bold rounded-full hover:bg-[#c92552] shadow-md hover:shadow-lg transition-all transform hover:-translate-y-0.5">
              회원가입
            </button>
          </template>
          <template v-else>
            <div @click="emit('my-page')" class="flex items-center gap-2 cursor-pointer p-1 pr-3 rounded-full hover:bg-gray-100 transition-all border border-transparent hover:border-gray-200">
              <img :src="userStore.userInfo.profileImg || 'https://via.placeholder.com/150'" class="w-8 h-8 rounded-full bg-gray-100 object-cover" alt="Profile" />
              <span class="font-bold text-sm text-gray-700">{{ userStore.userInfo.nickname || userStore.userInfo.name }}님</span>
            </div>
          </template>
        </nav>
      </div>
    </header>

    <section class="relative pt-32 pb-20 lg:pt-48 lg:pb-32 overflow-hidden">
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-full h-full -z-10">
        <div class="absolute top-0 left-1/4 w-72 h-72 bg-pink-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob"></div>
        <div class="absolute top-0 right-1/4 w-72 h-72 bg-purple-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-2000"></div>
        <div class="absolute -bottom-8 left-1/3 w-72 h-72 bg-yellow-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-4000"></div>
      </div>

      <div class="absolute top-1/4 left-[10%] animate-float-slow hidden lg:block">
        <div class="bg-white p-3 rounded-2xl shadow-xl rotate-[-6deg]">
          <span class="text-4xl">✈️</span>
        </div>
      </div>
      <div class="absolute bottom-1/4 right-[10%] animate-float hidden lg:block">
        <div class="bg-white p-3 rounded-2xl shadow-xl rotate-[12deg]">
          <span class="text-4xl">🏝️</span>
        </div>
      </div>
      <div class="absolute top-1/3 right-[15%] animate-float-fast hidden lg:block">
        <div class="bg-white p-3 rounded-2xl shadow-xl rotate-[6deg]">
          <span class="text-4xl">🍜</span>
        </div>
      </div>

      <div class="container mx-auto px-6 text-center relative z-10 flex flex-col items-center">
        <div class="inline-block mb-4 px-4 py-1.5 rounded-full bg-pink-50 border border-pink-100 text-[#DE2E5F] font-bold text-sm shadow-sm animate-fade-in-up">
          🚀 친구들과 함께 떠나는 국내 여행
        </div>
        <h2 class="text-5xl lg:text-7xl font-black text-gray-900 leading-tight mb-6 animate-fade-in-up delay-100">
          여행 계획, 더 이상<br />
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-[#DE2E5F] to-[#FF6B6B]">혼자 고민하지 마세요</span>
        </h2>
        <p class="text-xl text-gray-500 mb-10 max-w-2xl mx-auto leading-relaxed animate-fade-in-up delay-200">
          실시간으로 친구들과 지도를 보며 동선을 짜고,<br class="hidden md:block" />
          AI가 추천해주는 최적의 경로로 완벽한 여행을 만들어보세요.
        </p>
        
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4 animate-fade-in-up delay-300 w-full max-w-lg">
          <button 
            @click="showCreateModal = true"
            class="w-full sm:w-auto px-8 py-4 rounded-full bg-[#DE2E5F] text-white text-lg font-bold shadow-lg shadow-pink-200 hover:bg-[#c92552] hover:shadow-xl hover:-translate-y-1 transition-all flex items-center justify-center gap-2 whitespace-nowrap"
          >
            <Users class="w-5 h-5" />
            그룹 만들고 시작하기
          </button>
          
          <div class="relative w-full sm:w-auto flex-1 group">
             <input 
                v-model="inviteCodeInput" 
                type="text" 
                placeholder="참여 코드 입력" 
                class="w-full pl-6 pr-14 py-4 rounded-full border-2 border-gray-100 bg-white/80 backdrop-blur text-gray-700 font-bold focus:border-[#DE2E5F] focus:outline-none transition-all shadow-sm group-hover:shadow-md uppercase"
                @keyup.enter="handleJoinByCode"
             />
             <button 
                @click="handleJoinByCode"
                class="absolute right-2 top-2 bottom-2 w-10 h-10 bg-gray-900 rounded-full flex items-center justify-center text-white hover:bg-[#DE2E5F] transition-colors shadow-md"
             >
                <ArrowRight class="w-5 h-5" />
             </button>
          </div>
        </div>

      </div>
    </section>

    <section class="border-y border-gray-100 bg-gray-50/50">
      <div class="container mx-auto px-6 py-12">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
          <div>
            <div class="text-3xl font-black text-gray-900 mb-1">10k+</div>
            <div class="text-sm text-gray-500 font-medium">누적 여행 그룹</div>
          </div>
          <div>
            <div class="text-3xl font-black text-gray-900 mb-1">50k+</div>
            <div class="text-sm text-gray-500 font-medium">생성된 일정</div>
          </div>
          <div>
            <div class="text-3xl font-black text-gray-900 mb-1">98%</div>
            <div class="text-sm text-gray-500 font-medium">사용자 만족도</div>
          </div>
          <div>
            <div class="text-3xl font-black text-gray-900 mb-1">Free</div>
            <div class="text-sm text-gray-500 font-medium">평생 무료 이용</div>
          </div>
        </div>
      </div>
    </section>

    <section class="container mx-auto px-6 py-24">
      <div class="text-center mb-16">
        <h3 class="text-3xl font-bold text-gray-900 mb-4">왜 기택 아놀자 인가요?</h3>
        <p class="text-gray-500">복잡한 여행 계획, 이제 쉽고 재미있게 해결하세요.</p>
      </div>

      <div class="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
        <div class="group bg-white p-8 rounded-[2rem] border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-2 transition-all duration-300">
          <div class="w-14 h-14 rounded-2xl bg-pink-50 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
            <MapPin class="h-7 w-7 text-[#DE2E5F]" />
          </div>
          <h3 class="text-xl font-bold mb-3 text-gray-900">지도 기반 직관적 계획</h3>
          <p class="text-gray-500 leading-relaxed">
            더 이상 엑셀과 지도를 번갈아 보지 마세요.<br>
            지도 위에서 바로 찍고 드래그하면 끝!
          </p>
        </div>

        <div class="group bg-white p-8 rounded-[2rem] border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-2 transition-all duration-300">
          <div class="w-14 h-14 rounded-2xl bg-orange-50 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
            <Users class="h-7 w-7 text-orange-500" />
          </div>
          <h3 class="text-xl font-bold mb-3 text-gray-900">실시간 동시 편집</h3>
          <p class="text-gray-500 leading-relaxed">
            초대 링크 하나로 친구들 소환!<br>
            같은 화면을 보며 실시간으로 함께 짜요.
          </p>
        </div>

        <div class="group bg-white p-8 rounded-[2rem] border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-2 transition-all duration-300">
          <div class="w-14 h-14 rounded-2xl bg-purple-50 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
            <Sparkles class="h-7 w-7 text-purple-600" />
          </div>
          <h3 class="text-xl font-bold mb-3 text-gray-900">AI 스마트 추천</h3>
          <p class="text-gray-500 leading-relaxed">
            어디 갈지 막막할 땐 AI에게 물어보세요.<br>
            취향에 딱 맞는 코스를 제안해 드립니다.
          </p>
        </div>
      </div>
    </section>

    <section class="container mx-auto px-6 py-20">
      <div class="relative overflow-hidden rounded-[3rem] bg-gray-900 text-white p-12 lg:p-20 text-center">
        <div class="absolute top-0 right-0 w-64 h-64 bg-[#DE2E5F] rounded-full mix-blend-overlay filter blur-3xl opacity-20"></div>
        <div class="absolute bottom-0 left-0 w-64 h-64 bg-purple-500 rounded-full mix-blend-overlay filter blur-3xl opacity-20"></div>

        <div class="relative z-10 max-w-2xl mx-auto">
          <h2 class="text-3xl md:text-5xl font-bold mb-6">여행 준비, <br/>지금 바로 시작해보세요.</h2>
          <p class="text-gray-400 mb-10 text-lg">별도의 설치 없이 웹에서 바로 시작할 수 있습니다.<br>친구들을 초대하고 즐거운 계획을 세워보세요.</p>
          <button 
            @click="showCreateModal = true"
            class="px-10 py-4 rounded-full bg-white text-gray-900 text-lg font-bold hover:bg-gray-100 transition-colors shadow-lg"
          >
            무료로 시작하기
          </button>
        </div>
      </div>
    </section>

    <footer class="bg-white border-t border-gray-100 pt-16 pb-8">
      <div class="container mx-auto px-6">
        <div class="flex flex-col md:flex-row justify-between items-start mb-12 gap-8">
          <div>
            <div class="flex items-center gap-2 mb-4">
              <div class="w-8 h-8 rounded-lg bg-[#DE2E5F] flex items-center justify-center">
                <MapPin class="h-4 w-4 text-white" />
              </div>
              <span class="text-xl font-bold text-gray-900">Gitaek Anolja</span>
            </div>
            <p class="text-gray-500 text-sm leading-relaxed max-w-xs">
              친구들과 함께하는 가장 쉬운 여행 계획 플랫폼.<br>
              지금 바로 당신만의 여행 지도를 그려보세요.
            </p>
          </div>
          
          <div class="flex gap-12">
            <div>
              <h4 class="font-bold text-gray-900 mb-4">Service</h4>
              <ul class="space-y-2 text-sm text-gray-500">
                <li><a href="#" class="hover:text-[#DE2E5F]">기능 소개</a></li>
                <li><a href="#" class="hover:text-[#DE2E5F]">요금제</a></li>
                <li><a href="#" class="hover:text-[#DE2E5F]">자주 묻는 질문</a></li>
              </ul>
            </div>
            <div>
              <h4 class="font-bold text-gray-900 mb-4">Contact</h4>
              <ul class="space-y-2 text-sm text-gray-500">
                <li><a href="#" class="hover:text-[#DE2E5F]">문의하기</a></li>
                <li><a href="#" class="hover:text-[#DE2E5F]">제휴 문의</a></li>
              </ul>
            </div>
          </div>
        </div>
        
        <div class="border-t border-gray-100 pt-8 flex flex-col md:flex-row justify-between items-center gap-4">
          <p class="text-sm text-gray-400">&copy; 2025 Gitaek Anolja. All rights reserved.</p>
          <div class="flex gap-4">
            <Github class="w-5 h-5 text-gray-400 hover:text-gray-900 cursor-pointer" />
            <Twitter class="w-5 h-5 text-gray-400 hover:text-blue-400 cursor-pointer" />
            <Instagram class="w-5 h-5 text-gray-400 hover:text-pink-500 cursor-pointer" />
          </div>
        </div>
      </div>
    </footer>

    <div v-if="showCreateModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/40 backdrop-blur-md transition-opacity" @click="showCreateModal = false"></div>
      
      <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden p-8 space-y-6 transform transition-all scale-100">
        <div class="flex items-center justify-between">
          <h3 class="text-2xl font-bold text-gray-900">여행 정보 입력</h3>
          <button @click="showCreateModal = false" class="text-gray-400 hover:text-gray-600 bg-gray-100 p-2 rounded-full transition-colors">
            <X class="w-5 h-5" />
          </button>
        </div>

        <div class="space-y-6">
          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <Type class="w-4 h-4 text-[#DE2E5F]" /> 여행 제목
            </label>
            <input v-model="tripTitle" type="text" placeholder="예: 우정 파괴 제주 여행" class="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all" />
          </div>

          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <Calendar class="w-4 h-4 text-[#DE2E5F]" /> 시작 날짜
            </label>
            <input v-model="startDate" type="date" class="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all" />
          </div>

          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <span class="text-base">🕒</span> 여행 기간 ({{ duration }}일)
            </label>
            <div class="flex items-center gap-4">
              <input v-model="duration" type="range" min="1" max="10" class="flex-1 accent-[#DE2E5F] cursor-pointer" />
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <User class="w-4 h-4 text-[#DE2E5F]" /> 여행 인원
            </label>
            <div class="flex items-center gap-3">
              <button @click="members > 1 && members--" class="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-xl font-bold hover:bg-gray-200">-</button>
              <span class="flex-1 text-center text-lg font-bold">{{ members }}명</span>
              <button @click="members < 20 && members++" class="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-xl font-bold hover:bg-gray-200">+</button>
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <Heart class="w-4 h-4 text-[#DE2E5F]" /> 여행 컨셉
            </label>
            <div class="grid grid-cols-2 gap-3">
              <button @click="tripStyle = 'friend'" class="p-3 rounded-xl border-2 text-sm font-bold transition-all" :class="tripStyle === 'friend' ? 'border-[#DE2E5F] bg-[#DE2E5F]/5 text-[#DE2E5F]' : 'border-gray-100 text-gray-500 hover:border-gray-300'">👯 친구끼리</button>
              <button @click="tripStyle = 'family'" class="p-3 rounded-xl border-2 text-sm font-bold transition-all" :class="tripStyle === 'family' ? 'border-[#DE2E5F] bg-[#DE2E5F]/5 text-[#DE2E5F]' : 'border-gray-100 text-gray-500 hover:border-gray-300'">👨‍👩‍👧‍👦 가족여행</button>
              <button @click="tripStyle = 'couple'" class="p-3 rounded-xl border-2 text-sm font-bold transition-all" :class="tripStyle === 'couple' ? 'border-[#DE2E5F] bg-[#DE2E5F]/5 text-[#DE2E5F]' : 'border-gray-100 text-gray-500 hover:border-gray-300'">💑 커플여행</button>
              <button @click="tripStyle = 'solo'" class="p-3 rounded-xl border-2 text-sm font-bold transition-all" :class="tripStyle === 'solo' ? 'border-[#DE2E5F] bg-[#DE2E5F]/5 text-[#DE2E5F]' : 'border-gray-100 text-gray-500 hover:border-gray-300'">🎒 혼자여행</button>
            </div>
          </div>
        </div>

        <button 
          @click="handleCreateGroup"
          class="w-full py-4 rounded-xl font-bold text-white bg-[#DE2E5F] hover:bg-[#c92552] shadow-lg hover:shadow-xl transition-all text-lg flex items-center justify-center gap-2"
        >
          여행 계획 시작하기
          <Sparkles class="w-5 h-5" />
        </button>
      </div>
    </div>
    
    <AuthModal :is-open="showAuthModal" :initial-mode="authMode" @close="showAuthModal = false" />
  </div>
</template>

<style scoped>
/* 둥둥 떠다니는 애니메이션 정의 */
@keyframes float {
  0% { transform: translateY(0px) rotate(12deg); }
  50% { transform: translateY(-20px) rotate(12deg); }
  100% { transform: translateY(0px) rotate(12deg); }
}
@keyframes float-slow {
  0% { transform: translateY(0px) rotate(-6deg); }
  50% { transform: translateY(-15px) rotate(-6deg); }
  100% { transform: translateY(0px) rotate(-6deg); }
}
@keyframes float-fast {
  0% { transform: translateY(0px) rotate(6deg); }
  50% { transform: translateY(-10px) rotate(6deg); }
  100% { transform: translateY(0px) rotate(6deg); }
}
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

.animate-float { animation: float 6s ease-in-out infinite; }
.animate-float-slow { animation: float-slow 8s ease-in-out infinite; }
.animate-float-fast { animation: float-fast 4s ease-in-out infinite; }
.animate-blob { animation: blob 7s infinite; }
.animation-delay-2000 { animation-delay: 2s; }
.animation-delay-4000 { animation-delay: 4s; }
</style>