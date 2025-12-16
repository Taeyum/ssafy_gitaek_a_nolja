<script setup>
import { ref, nextTick, watch, onMounted } from 'vue'
import { Send, User as UserIcon } from 'lucide-vue-next'
import { useTripStore } from '@/stores/tripStore'
import { useUserStore } from '@/stores/userStore'

const tripStore = useTripStore()
const userStore = useUserStore()

const newMessage = ref('')
const messagesContainer = ref(null)

// 스크롤을 맨 아래로 내리는 함수
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 메시지가 오면 스크롤 내리기
watch(() => tripStore.messages, () => {
  scrollToBottom()
}, { deep: true })

const handleSend = async () => {
  if (!newMessage.value.trim()) return
  
  await tripStore.sendMessage(newMessage.value)
  newMessage.value = '' // 입력창 비우기
}

onMounted(() => {
  scrollToBottom()
})
</script>

<template>
  <div class="flex flex-col h-full bg-white">
    <div class="p-4 border-b bg-white flex justify-between items-center shadow-sm z-10">
      <h3 class="font-bold text-lg text-gray-800">💬 실시간 채팅</h3>
      <span class="text-xs bg-gray-100 text-gray-500 px-2 py-1 rounded-full">
        참여자 {{ tripStore.tripInfo.currentParticipants || 0 }}명
      </span>
    </div>

    <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50 scroll-smooth">
      <div v-if="tripStore.messages.length === 0" class="text-center text-gray-400 text-sm py-10">
        아직 대화가 없습니다.<br>첫 메시지를 남겨보세요! 👋
      </div>

      <div 
        v-for="msg in tripStore.messages" 
        :key="msg.messageId" 
        class="flex gap-3"
        :class="msg.mine ? 'flex-row-reverse' : ''"
      >
        <div v-if="!msg.mine" class="flex-shrink-0 w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center overflow-hidden border border-gray-300">
           <UserIcon class="w-5 h-5 text-gray-500" />
        </div>

        <div class="flex flex-col max-w-[70%]" :class="msg.mine ? 'items-end' : 'items-start'">
          <div v-if="!msg.mine" class="text-xs text-gray-500 mb-1 ml-1">
            {{ msg.senderName }}
          </div>
          
          <div 
            class="px-4 py-2 rounded-2xl text-sm leading-relaxed shadow-sm break-words"
            :class="msg.mine 
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
          class="w-full bg-gray-100 text-gray-800 rounded-full py-3 pl-5 pr-12 focus:outline-none focus:ring-2 focus:ring-[#DE2E5F] focus:bg-white transition-all placeholder-gray-400"
        />
        <button 
          @click="handleSend"
          class="absolute right-2 p-2 bg-[#DE2E5F] text-white rounded-full hover:bg-[#c92552] transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="!newMessage.trim()"
        >
          <Send class="w-4 h-4" />
        </button>
      </div>
    </div>
  </div>
</template>