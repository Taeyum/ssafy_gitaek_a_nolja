<script setup>
import { ref, defineProps, defineEmits, watch} from 'vue'
import { X, Mail, Lock, User, ArrowRight } from 'lucide-vue-next'
import { useUserStore } from '@/stores/userStore'

const props = defineProps({
  isOpen: Boolean,
  initialMode: { type: String, default: 'login' } // 'login' or 'signup'
})

const emit = defineEmits(['close'])
const userStore = useUserStore()

// 현재 모드 ('login' <-> 'signup')
const mode = ref(props.initialMode)

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    mode.value = props.initialMode
  }
})

// 입력값
const name = ref('')
const email = ref('')
const password = ref('')

const handleSubmit = async () => { // ★ async 추가
  if (mode.value === 'login') {
    // 로그인 시도
    const success = await userStore.login(email.value, password.value) // ★ await 추가
    
    if (success) {
      alert(`${userStore.userInfo.name}님 환영합니다!`)
      emit('close') // 성공했을 때만 닫기
      // 입력창 초기화
      name.value = ''
      email.value = ''
      password.value = ''
    }
  } else {
    // 회원가입 시도
    if (!name.value) return alert('이름을 입력해주세요.')
    
    const success = await userStore.signup(name.value, email.value, password.value) // ★ await 추가
    
    if (success) {
      alert('회원가입이 완료되었습니다! 로그인해주세요.')
      mode.value = 'login' // 회원가입 성공하면 로그인 창으로 전환
    }
  }
}

// 모드 전환 (로그인 <-> 회원가입)
const toggleMode = () => {
  mode.value = mode.value === 'login' ? 'signup' : 'login'
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/50 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>

    <div class="relative w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden transform transition-all scale-100">
      
      <button @click="$emit('close')" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 z-10">
        <X class="w-6 h-6" />
      </button>

      <div class="p-8">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-gray-900 mb-2">
            {{ mode === 'login' ? '다시 만나서 반가워요!' : '여행을 시작해볼까요?' }}
          </h2>
          <p class="text-gray-500 text-sm">
            {{ mode === 'login' ? '계속하려면 로그인을 진행해주세요.' : '기택 아놀자와 함께 최고의 여행을 계획하세요.' }}
          </p>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div v-if="mode === 'signup'" class="space-y-1">
            <label class="text-sm font-bold text-gray-700 ml-1">이름</label>
            <div class="relative">
              <User class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input v-model="name" type="text" placeholder="홍길동" class="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all" />
            </div>
          </div>

          <div class="space-y-1">
            <label class="text-sm font-bold text-gray-700 ml-1">이메일</label>
            <div class="relative">
              <Mail class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input v-model="email" type="email" placeholder="example@email.com" class="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all" required />
            </div>
          </div>

          <div class="space-y-1">
            <label class="text-sm font-bold text-gray-700 ml-1">비밀번호</label>
            <div class="relative">
              <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input v-model="password" type="password" placeholder="••••••••" class="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all" required />
            </div>
          </div>

          <button type="submit" class="w-full py-4 mt-6 rounded-xl font-bold text-white bg-[#DE2E5F] hover:bg-[#c92552] shadow-lg hover:shadow-xl transition-all flex items-center justify-center gap-2 group">
            {{ mode === 'login' ? '로그인하기' : '회원가입 완료' }}
            <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
          </button>
        </form>

        <div class="mt-6 text-center text-sm text-gray-500">
          {{ mode === 'login' ? '아직 회원이 아니신가요?' : '이미 계정이 있으신가요?' }}
          <button @click="toggleMode" class="font-bold text-[#DE2E5F] hover:underline ml-1">
            {{ mode === 'login' ? '회원가입' : '로그인' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>