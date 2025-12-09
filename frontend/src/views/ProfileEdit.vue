<script setup>
import { ref, onMounted, computed, defineEmits } from 'vue'
import { ArrowLeft, User, Mail, Lock, AlertCircle, Save } from 'lucide-vue-next'
import { useUserStore } from '@/stores/userStore'

const emit = defineEmits(['back'])
const userStore = useUserStore()

// --- 1. 기본 정보 수정 ---
const formNickname = ref('')
const isNicknameChecked = ref(true) 
const originalNickname = ref('')    

const handleNicknameInput = () => {
  if (formNickname.value !== originalNickname.value) {
    isNicknameChecked.value = false
  } else {
    isNicknameChecked.value = true 
  }
}

const handleCheckNickname = async () => {
  if (!formNickname.value.trim()) return alert("닉네임을 입력하세요.")
  
  const isDuplicate = await userStore.checkNickname(formNickname.value)
  if (isDuplicate) {
    alert("이미 사용 중인 닉네임입니다.")
    isNicknameChecked.value = false
  } else {
    alert("사용 가능한 닉네임입니다.")
    isNicknameChecked.value = true
  }
}

const handleUpdateInfo = async () => {
  if (!isNicknameChecked.value) return alert("닉네임 중복 확인을 해주세요.")
  
  const success = await userStore.updateInfo(formNickname.value)
  if (success) {
    alert("회원 정보가 수정되었습니다.")
    originalNickname.value = formNickname.value 
  }
}

// --- 2. 비밀번호 변경 ---
const currentPw = ref('')
const newPw = ref('')
const confirmPw = ref('')

const isPwMatch = computed(() => {
  return newPw.value && confirmPw.value && newPw.value === confirmPw.value
})

const handleChangePw = async () => {
  if (!currentPw.value || !newPw.value) return alert("빈칸을 입력해주세요.")
  if (!isPwMatch.value) return alert("새 비밀번호가 일치하지 않습니다.")
  
  const success = await userStore.changePassword(currentPw.value, newPw.value)
  if (success) {
    alert("비밀번호가 변경되었습니다. 다시 로그인해주세요.")
    userStore.logout()
    // 비밀번호 바뀌면 메인(로그인 화면)으로 튕기게 하기 위해 App.vue가 처리하도록 함
    location.reload() 
  }
}

// --- 3. 회원 탈퇴 ---
const deletePw = ref('') 

const handleDeleteAccount = async () => {
  if (!deletePw.value) return alert("비밀번호를 입력해주세요.")
  
  if (confirm("정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
    const success = await userStore.deleteAccount(deletePw.value)
    if (success) {
      alert("탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.")
      location.reload()
    }
  }
}

onMounted(() => {
  if (userStore.userInfo) {
    formNickname.value = userStore.userInfo.name 
    originalNickname.value = userStore.userInfo.name
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-sans pb-12">
    <header class="bg-white border-b sticky top-0 z-50">
      <div class="container mx-auto px-6 py-4 flex items-center gap-4">
        <button 
          @click="emit('back')" 
          class="p-2 rounded-full hover:bg-gray-100 text-gray-500 transition-colors"
        >
          <ArrowLeft class="h-6 w-6" />
        </button>
        <h1 class="text-xl font-bold text-gray-900">회원정보 수정</h1>
      </div>
    </header>

    <div class="container mx-auto px-6 py-8 max-w-2xl space-y-8">
      
      <section class="bg-white rounded-3xl shadow-sm border border-gray-100 p-8">
        <h2 class="text-xl font-bold text-gray-900 mb-6 flex items-center gap-2">
          <User class="w-5 h-5 text-[#DE2E5F]" /> 기본 정보 수정
        </h2>
        
        <div class="space-y-6">
          <div>
            <label class="block text-sm font-bold text-gray-700 mb-2">이메일</label>
            <div class="flex items-center gap-2 px-4 py-3 bg-gray-100 rounded-xl text-gray-500">
              <Mail class="w-4 h-4" />
              {{ userStore.userInfo?.email }}
            </div>
          </div>

          <div>
            <label class="block text-sm font-bold text-gray-700 mb-2">닉네임</label>
            <div class="flex gap-2">
              <input 
                v-model="formNickname"
                @input="handleNicknameInput"
                type="text" 
                class="flex-1 px-4 py-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F] focus:ring-1 focus:ring-[#DE2E5F] transition-all"
              />
              <button 
                @click="handleCheckNickname"
                :disabled="isNicknameChecked"
                class="px-4 py-2 rounded-xl text-sm font-bold transition-colors border-2"
                :class="isNicknameChecked 
                  ? 'bg-green-50 text-green-600 border-green-200 cursor-default' 
                  : 'bg-white text-[#DE2E5F] border-[#DE2E5F] hover:bg-pink-50'"
              >
                {{ isNicknameChecked ? '확인완료' : '중복확인' }}
              </button>
            </div>
          </div>

          <button 
            @click="handleUpdateInfo"
            :disabled="!isNicknameChecked || formNickname === originalNickname"
            class="w-full py-3.5 rounded-xl font-bold text-white bg-[#DE2E5F] hover:bg-[#c92552] transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <Save class="w-4 h-4" />
            수정사항 저장
          </button>
        </div>
      </section>

      <section class="bg-white rounded-3xl shadow-sm border border-gray-100 p-8">
        <h2 class="text-xl font-bold text-gray-900 mb-6 flex items-center gap-2">
          <Lock class="w-5 h-5 text-[#DE2E5F]" /> 비밀번호 변경
        </h2>

        <div class="space-y-4">
          <input v-model="currentPw" type="password" placeholder="현재 비밀번호" class="w-full px-4 py-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F]" />
          <input v-model="newPw" type="password" placeholder="새 비밀번호" class="w-full px-4 py-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F]" />
          <input v-model="confirmPw" type="password" placeholder="새 비밀번호 확인" class="w-full px-4 py-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-[#DE2E5F]" />
          
          <p v-if="newPw && confirmPw" class="text-xs font-bold" :class="isPwMatch ? 'text-green-600' : 'text-red-500'">
            {{ isPwMatch ? '비밀번호가 일치합니다.' : '비밀번호가 일치하지 않습니다.' }}
          </p>

          <button 
            @click="handleChangePw"
            class="w-full py-3.5 rounded-xl font-bold border-2 border-gray-200 text-gray-600 hover:border-[#DE2E5F] hover:text-[#DE2E5F] transition-colors mt-2"
          >
            비밀번호 변경
          </button>
        </div>
      </section>

      <section class="bg-red-50 rounded-3xl border border-red-100 p-8">
        <h2 class="text-lg font-bold text-red-600 mb-2 flex items-center gap-2">
          <AlertCircle class="w-5 h-5" /> 회원 탈퇴
        </h2>
        <p class="text-sm text-red-400 mb-6">
          안전을 위해 비밀번호를 입력해주세요. 탈퇴 시 모든 정보가 삭제됩니다.
        </p>

        <div class="flex gap-2">
          <input 
            v-model="deletePw" 
            type="password" 
            placeholder="비밀번호 입력" 
            class="flex-1 px-4 py-3 bg-white border border-red-200 rounded-xl focus:outline-none focus:border-red-500"
          />
          <button 
            @click="handleDeleteAccount"
            class="px-6 py-3 bg-red-500 text-white font-bold rounded-xl hover:bg-red-600 transition-colors whitespace-nowrap"
          >
            탈퇴
          </button>
        </div>
      </section>

    </div>
  </div>
</template>