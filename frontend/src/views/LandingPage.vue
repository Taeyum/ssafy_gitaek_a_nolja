<script setup>
import { ref, defineEmits } from 'vue'
import { MapPin, Users, MessageSquare, Sparkles, X, Calendar, User, Heart, Type, LogOut } from 'lucide-vue-next'
import { useTripStore } from '@/stores/tripStore'
// ★ [추가됨] 유저 스토어 및 인증 모달
import { useUserStore } from '@/stores/userStore'
import AuthModal from '@/components/AuthModal.vue'

const emit = defineEmits(['start', 'my-page'])
const tripStore = useTripStore()
const userStore = useUserStore() // ★ 유저 스토어 사용

const showCreateModal = ref(false)

// ★ [추가됨] 로그인 모달 상태
const showAuthModal = ref(false)
const authMode = ref('login') // 'login' or 'signup'

// 로그인/회원가입 모달 열기 함수
const openAuthModal = (mode) => {
  authMode.value = mode
  showAuthModal.value = true
}

// 입력 폼 데이터 (기존 유지)
const tripTitle = ref("")   
const duration = ref(3)
const members = ref(4)
const tripStyle = ref('friend')

const handleCreateGroup = () => {
  // ★ 로그인 안 했으면 로그인부터 하라고 시킴
  if (!userStore.isLoggedIn) {
    alert("로그인이 필요한 서비스입니다!")
    openAuthModal('login')
    return
  }

  const title = tripTitle.value.trim() ? tripTitle.value : '나의 즐거운 여행'

  tripStore.createNewTrip({
    title: title,       
    duration: duration.value,
    members: members.value, 
    style: tripStyle.value
  })

  showCreateModal.value = false
  emit('start')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-sans">
    <header class="border-b bg-white/80 backdrop-blur-md sticky top-0 z-50 shadow-sm">
      <div class="container mx-auto px-6 py-4 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-10 h-10 rounded-2xl bg-[#DE2E5F] flex items-center justify-center shadow-lg shadow-pink-200">
            <MapPin class="h-5 w-5 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-gray-900">Gitaek Anolja</h1>
        </div>
        
        <nav class="flex items-center gap-3">
          <template v-if="!userStore.isLoggedIn">
            <button 
              @click="openAuthModal('login')"
              class="px-4 py-2 text-gray-500 hover:bg-gray-100 rounded-full transition-colors font-medium"
            >
              로그인
            </button>
            <button 
              @click="openAuthModal('signup')"
              class="px-4 py-2 bg-[#DE2E5F] text-white rounded-full hover:bg-[#c92552] transition-colors font-bold shadow-md"
            >
              회원가입
            </button>
          </template>
          
          <template v-else>
  <div 
    @click="emit('my-page')" 
    class="flex items-center gap-3 cursor-pointer hover:opacity-80 transition-opacity"
  >
    <span class="font-bold text-gray-700">
      👋 {{ userStore.userInfo.name }}님
    </span>
    <img 
      :src="userStore.userInfo.profileImg" 
      class="w-9 h-9 rounded-full border border-gray-200 bg-white"
      alt="Profile"
    />
    <button 
      @click.stop="userStore.logout"
      class="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-full transition-colors"
      title="로그아웃"
    >
      <LogOut class="w-5 h-5" />
    </button>
  </div>
</template>
        </nav>
      </div>
    </header>

    <section class="container mx-auto px-6 py-24 text-center">
      <div class="max-w-3xl mx-auto space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
        <h2 class="text-5xl md:text-6xl font-bold text-gray-900 leading-tight">
          친구들과 함께
          <br />
          <span class="text-[#DE2E5F]">실시간으로</span> 여행 계획하기
        </h2>
        <p class="text-xl text-gray-500 leading-relaxed">
          기택 아놀자에서 국내 여행을 함께 계획하고, 실시간으로 소통하며,<br class="hidden md:block" />
          AI 추천으로 완벽한 일정을 만들어보세요.
        </p>
        <div class="flex items-center justify-center gap-4 pt-4">
          <button 
            @click="showCreateModal = true"
            class="text-lg px-8 py-4 rounded-full bg-[#DE2E5F] text-white hover:bg-[#c92552] shadow-xl hover:shadow-2xl hover:-translate-y-1 transition-all flex items-center font-bold"
          >
            <Users class="mr-2 h-5 w-5" />
            그룹 만들기
          </button>
          
          <button class="text-lg px-8 py-4 rounded-full border-2 border-gray-200 bg-white hover:border-[#DE2E5F] hover:text-[#DE2E5F] transition-all font-bold">
            참여 코드로 입장
          </button>
        </div>
      </div>
    </section>

    <section class="container mx-auto px-6 py-16">
      <div class="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
        <div class="bg-white p-8 rounded-3xl shadow-xl hover:shadow-2xl transition-all hover:-translate-y-1 duration-300">
          <div class="h-16 w-16 rounded-2xl bg-[#DE2E5F]/10 flex items-center justify-center mb-6">
            <MapPin class="h-8 w-8 text-[#DE2E5F]" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-gray-900">지도 기반 계획</h3>
          <p class="text-gray-500 leading-relaxed">
            지도에서 여행지를 검색하고 일정에 추가하여 직관적으로 루트를 계획하세요.
          </p>
        </div>
        <div class="bg-white p-8 rounded-3xl shadow-xl hover:shadow-2xl transition-all hover:-translate-y-1 duration-300 delay-100">
          <div class="h-16 w-16 rounded-2xl bg-orange-100 flex items-center justify-center mb-6">
            <MessageSquare class="h-8 w-8 text-orange-500" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-gray-900">실시간 협업</h3>
          <p class="text-gray-500 leading-relaxed">
            친구들과 실시간으로 채팅하며 의견을 나누고 함께 일정을 수정하세요.
          </p>
        </div>
        <div class="bg-white p-8 rounded-3xl shadow-xl hover:shadow-2xl transition-all hover:-translate-y-1 duration-300 delay-200">
          <div class="h-16 w-16 rounded-2xl bg-purple-100 flex items-center justify-center mb-6">
            <Sparkles class="h-8 w-8 text-purple-600" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-gray-900">AI 추천</h3>
          <p class="text-gray-500 leading-relaxed">
            AI가 여행 스타일에 맞는 최적의 여행지와 루트를 추천해드립니다.
          </p>
        </div>
      </div>
    </section>

    <section class="container mx-auto px-6 py-24">
      <div class="max-w-3xl mx-auto text-center bg-gradient-to-br from-[#DE2E5F] to-[#b01e45] text-white rounded-3xl p-12 shadow-2xl">
        <h2 class="text-4xl font-bold mb-4">지금 바로 여행 계획을 시작하세요</h2>
        <p class="text-lg mb-8 opacity-90">
          무료로 그룹을 만들고 친구들을 초대해보세요
        </p>
        <button 
          @click="showCreateModal = true"
          class="text-lg px-8 py-6 rounded-full bg-white text-[#DE2E5F] font-bold shadow-lg hover:shadow-xl hover:bg-gray-50 transition-all"
        >
          무료로 시작하기
        </button>
      </div>
    </section>

    <footer class="border-t bg-white">
      <div class="container mx-auto px-6 py-8 text-center text-sm text-gray-400">
        <p>&copy; 2025 Gitaek Anolja. All rights reserved.</p>
      </div>
    </footer>

    <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden p-8 space-y-6">
        <div class="flex items-center justify-between">
          <h3 class="text-2xl font-bold text-gray-900">여행 정보 입력</h3>
          <button @click="showCreateModal = false" class="text-gray-400 hover:text-gray-600">
            <X class="w-6 h-6" />
          </button>
        </div>

        <div class="space-y-6">
          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <Type class="w-4 h-4 text-[#DE2E5F]" /> 여행 제목
            </label>
            <input 
              v-model="tripTitle"
              type="text" 
              placeholder="예: 우정 파괴 제주 여행"
              class="w-full p-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all"
            />
          </div>

          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <Calendar class="w-4 h-4 text-[#DE2E5F]" /> 여행 기간 (일)
            </label>
            <div class="flex items-center gap-4">
              <input 
                v-model="duration" 
                type="range" min="1" max="10" 
                class="flex-1 accent-[#DE2E5F] cursor-pointer"
              />
              <span class="text-lg font-bold text-[#DE2E5F] w-12 text-right">{{ duration }}일</span>
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

    <AuthModal 
      :is-open="showAuthModal" 
      :initial-mode="authMode" 
      @close="showAuthModal = false"
    />
  </div>
</template>