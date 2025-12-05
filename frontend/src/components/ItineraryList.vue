<script setup>
import { defineProps, defineEmits } from 'vue' // defineEmits 추가
import { MapPin, Clock, Trash2, GripVertical, Plus, Edit2 } from 'lucide-vue-next' // Edit2 아이콘 추가
import draggable from 'vuedraggable'
import { useTripStore } from '@/stores/tripStore'

const props = defineProps({
  isEditing: {
    type: Boolean,
    default: false
  }
})

// 부모(Dashboard)에게 신호를 보낼 이벤트 정의
const emit = defineEmits(['edit-item', 'open-manual-add'])

const store = useTripStore()

const dragOptions = {
  animation: 200,
  group: "itinerary-items",
  disabled: false,
  ghostClass: "ghost-card"
}
</script>

<template>
  <div class="p-6 space-y-8 h-full overflow-y-auto bg-white">
    <div v-for="day in store.itinerary" :key="day.id" class="space-y-4">
      <div class="flex items-center gap-3 sticky top-0 bg-white/95 backdrop-blur z-10 py-2 border-b border-gray-100">
        <div class="px-4 py-1.5 bg-[#DE2E5F]/10 rounded-full">
          <span class="text-sm font-bold text-[#DE2E5F]">{{ day.day }}</span>
        </div>
        <span class="text-sm text-gray-500">{{ day.date }}</span>
      </div>

      <div class="relative pl-8">
        <div class="absolute left-3 top-0 bottom-0 w-0.5 bg-gray-200"></div>

        <draggable 
          v-model="day.items" 
          group="itinerary-items" 
          item-key="id"
          :animation="200"
          :disabled="!isEditing"
          ghost-class="ghost-card"
          class="space-y-4 min-h-[10px]"
        >
          <template #item="{ element: item }">
            <div class="relative group">
              <div class="absolute -left-5 top-6 w-3 h-3 rounded-full bg-[#DE2E5F] border-2 border-white shadow-md z-10"></div>

              <div 
                class="bg-white border border-gray-200 rounded-2xl p-4 shadow-sm hover:shadow-md transition-all relative overflow-hidden select-none"
                :class="{ 
                  'cursor-move border-[#DE2E5F]/30 hover:border-[#DE2E5F]': isEditing,
                  'cursor-default': !isEditing 
                }"
              >
                <div class="flex items-start gap-3 relative z-10">
                  <GripVertical v-if="isEditing" class="h-5 w-5 text-gray-300 mt-1 flex-shrink-0" />
                  
                  <div class="flex-1 space-y-2">
                    <div class="flex items-center gap-2">
                      <div class="px-2.5 py-0.5 bg-gray-100 rounded-md inline-flex items-center gap-1.5">
                        <Clock class="h-3 w-3 text-gray-500" />
                        <span class="text-xs font-bold text-gray-600">{{ item.time }}</span>
                      </div>
                    </div>
                    <h4 class="font-bold text-base text-gray-900">{{ item.name }}</h4>
                    <div class="flex items-center gap-1.5 text-xs text-gray-500">
                      <MapPin class="h-3.5 w-3.5 text-gray-400" />
                      <span>{{ item.location }}</span>
                    </div>
                  </div>

                  <div v-if="isEditing" class="flex flex-col gap-1">
                    <button 
                      @click="emit('edit-item', day.id, item)"
                      class="p-1.5 text-gray-300 hover:text-blue-500 hover:bg-blue-50 rounded-full transition-colors"
                      title="수정"
                    >
                      <Edit2 class="h-4 w-4" />
                    </button>
                    <button 
                      @click="store.removePlace(day.id, item.id)"
                      class="p-1.5 text-gray-300 hover:text-red-500 hover:bg-red-50 rounded-full transition-colors"
                      title="삭제"
                    >
                      <Trash2 class="h-4 w-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </div>

    <button
      v-if="isEditing"
      @click="emit('open-manual-add')"
      class="w-full rounded-2xl border-2 border-dashed border-gray-300 h-14 flex items-center justify-center gap-2 text-gray-400 font-semibold hover:bg-[#DE2E5F]/5 hover:border-[#DE2E5F]/50 hover:text-[#DE2E5F] transition-all"
    >
      <Plus class="w-5 h-5" />
      장소 직접 추가하기
    </button>
  </div>
</template>

<style scoped>
.ghost-card {
  opacity: 0.5;
  background: #fdf2f8;
  border: 2px dashed #DE2E5F;
}
</style>