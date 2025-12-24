<script setup>
import { ref, nextTick, watch, onMounted } from 'vue'
import { Send, User as UserIcon } from 'lucide-vue-next'
import { useTripStore } from '@/stores/tripStore'
import { useUserStore } from '@/stores/userStore' // ★ userStore import

const tripStore = useTripStore()
const userStore = useUserStore() // ★ userStore 사용 선언

const newMessage = ref('')
const messagesContainer = ref(null)

// 스크롤 내리기 함수
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 새 메시지가 오면 스크롤 내리기
watch(() => tripStore.messages.length, () => {
  scrollToBottom()
})

const handleSend = (e) => {
  // 한글 조합 중일 때 전송 방지 (이중 전송 버그 해결)
  if (e && e.isComposing) return
  if (!newMessage.value.trim()) return

  // ★ [핵심 수정] userStore에 있는 내 프로필 이미지(DiceBear URL)를 같이 보냄!
  tripStore.sendChatMessage(
    newMessage.value, 
    userStore.userInfo.id, 
    userStore.userInfo.name,
    userStore.userInfo.profileImg // <--- 여기가 핵심입니다!
  )
  newMessage.value = ''
}

onMounted(async () => {
  if (tripStore.messages.length === 0) {
      await tripStore.fetchMessages()
  }
  scrollToBottom()
})
</script>

<template>
  <div class="flex flex-col h-full bg-white">
    <div class="p-4 border-b bg-white flex justify-between items-center shadow-sm z-10">
      <h3 class="font-bold text-lg text-gray-800">💬 실시간 채팅</h3>
      <span class="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded-full">
        {{ tripStore.tripInfo.currentParticipants }}명 참여 중
      </span>
    </div>

    <div 
      class="flex-1 overflow-y-auto p-4 space-y-4 bg-[#F8F9FA]" 
      ref="messagesContainer"
    >
      <div 
  v-for="msg in tripStore.messages" 
  :key="msg.messageId || Math.random()" 
  class="flex gap-3"
  :class="(msg.userId === userStore.userInfo.id) ? 'flex-row-reverse' : 'flex-row'"
>
  <div 
    v-if="msg.userId !== userStore.userInfo.id"
    class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center flex-shrink-0 overflow-hidden border border-gray-300"
  >
    <img 
  :src="msg.senderProfileImg ? msg.senderProfileImg : `https://api.dicebear.com/7.x/avataaars/svg?seed=${msg.senderName}`"
  @error="(e) => { e.target.src = `https://api.dicebear.com/7.x/avataaars/svg?seed=${msg.senderName}` }"
  class="w-full h-full object-cover"
/>
  </div>

  <div class="flex flex-col max-w-[70%]" :class="(msg.userId === userStore.userInfo.id) ? 'items-end' : 'items-start'">
    <div v-if="msg.userId !== userStore.userInfo.id" class="text-xs text-gray-500 mb-1 ml-1">
      {{ msg.senderName }}
    </div>
    
    <div 
      class="px-4 py-2 rounded-2xl text-sm leading-relaxed shadow-sm break-words"
      :class="(msg.userId === userStore.userInfo.id)
        ? 'bg-[#DE2E5F] text-white rounded-tr-none' 
        : 'bg-white text-gray-700 border border-gray-100 rounded-tl-none'"
    >
      {{ msg.content }}
    </div>
    <span class="text-[10px] text-gray-400 mt-1 px-1">
      {{ msg.sentAt }}
    </span>
  </div>
</div>
    </div>

    <div class="p-4 bg-white border-t">
      <div class="relative flex items-center">
        <input 
          v-model="newMessage" 
          @keydown.enter.prevent="handleSend"
          type="text" 
          placeholder="메시지를 입력하세요..." 
          class="w-full bg-gray-100 text-gray-800 rounded-full py-3 pl-5 pr-12 focus:outline-none focus:ring-2 focus:ring-[#DE2E5F] focus:bg-white transition-all"
        />
        <button 
          @click="handleSend"
          class="absolute right-2 p-2 bg-[#DE2E5F] text-white rounded-full hover:bg-[#c92552] transition-colors shadow-md disabled:opacity-50"
          :disabled="!newMessage.trim()"
        >
          <Send class="w-4 h-4" />
        </button>
      </div>
    </div>
  </div>
</template>