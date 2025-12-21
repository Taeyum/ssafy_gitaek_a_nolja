<script setup>
import { defineProps } from 'vue'
import { Bell, UserPlus, Edit3 } from 'lucide-vue-next'

const props = defineProps({
  notifications: {
    type: Array,
    default: () => []
  }
})
</script>

<template>
  <div class="fixed top-20 right-5 z-[9999] flex flex-col gap-3 pointer-events-none">
    <TransitionGroup name="toast">
      <div 
        v-for="noti in notifications" 
        :key="noti.id"
        class="pointer-events-auto bg-white/90 backdrop-blur-sm border-l-4 shadow-lg rounded-r-lg p-4 min-w-[300px] flex items-center gap-3"
        :class="{
          'border-[#DE2E5F]': noti.type === 'EDIT',
          'border-blue-500': noti.type === 'ENTRY',
          'border-green-500': noti.type === 'INFO'
        }"
      >
        <div class="p-2 rounded-full bg-gray-100">
          <Edit3 v-if="noti.type === 'EDIT'" class="w-5 h-5 text-[#DE2E5F]" />
          <UserPlus v-else-if="noti.type === 'ENTRY'" class="w-5 h-5 text-blue-500" />
          <Bell v-else class="w-5 h-5 text-green-500" />
        </div>
        
        <div class="flex-1">
          <h4 class="font-bold text-sm text-gray-800" v-if="noti.senderName">
            {{ noti.senderName }}
          </h4>
          <p class="text-sm text-gray-600">{{ noti.message }}</p>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.4s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>