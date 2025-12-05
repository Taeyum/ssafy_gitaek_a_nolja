<script setup>
import { ref } from 'vue'
import { Send } from 'lucide-vue-next'

const message = ref("")
const sampleMessages = ref([
  { id: "1", user: "김지은", message: "성산일출봉 일정 좋은 것 같아요!", time: "오전 10:23" },
  { id: "2", user: "나", message: "저도 동의합니다. 한라산은 어때요?", time: "오전 10:25" },
  { id: "3", user: "박민수", message: "한라산 좋죠! 날씨 확인해봤는데 괜찮을 것 같습니다", time: "오전 10:27" },
])

const handleSend = () => {
  if (!message.value.trim()) return
  
  sampleMessages.value.push({
    id: Date.now().toString(),
    user: "나",
    message: message.value,
    time: new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
  })
  message.value = ""
}
</script>

<template>
  <div class="flex-1 flex flex-col h-full bg-white">
    <div class="flex-1 overflow-y-auto p-6 space-y-4">
      <div v-for="msg in sampleMessages" :key="msg.id" class="space-y-1">
        <div class="flex items-baseline gap-2">
          <span class="text-sm font-semibold text-gray-900">{{ msg.user }}</span>
          <span class="text-xs text-gray-400">{{ msg.time }}</span>
        </div>
        <div 
          class="px-4 py-2.5 inline-block max-w-[85%] rounded-2xl text-sm leading-relaxed"
          :class="msg.user === '나' 
            ? 'bg-[#DE2E5F] text-white rounded-tr-none' 
            : 'bg-gray-100 text-gray-800 rounded-tl-none'"
        >
          {{ msg.message }}
        </div>
      </div>
    </div>

    <div class="border-t p-4 bg-white">
      <div class="flex items-center gap-2 bg-gray-50 p-2 rounded-full border border-gray-200 focus-within:border-[#DE2E5F] focus-within:ring-1 focus-within:ring-[#DE2E5F] transition-all">
        <input
          type="text"
          placeholder="메시지를 입력하세요..."
          v-model="message"
          @keydown.enter="handleSend"
          class="flex-1 bg-transparent border-none focus:outline-none px-3 text-sm"
        />
        <button 
          @click="handleSend" 
          :disabled="!message.trim()"
          class="p-2 rounded-full bg-[#DE2E5F] text-white disabled:opacity-50 disabled:cursor-not-allowed hover:bg-[#c92552] transition-colors"
        >
          <Send class="h-4 w-4" />
        </button>
      </div>
    </div>
  </div>
</template>