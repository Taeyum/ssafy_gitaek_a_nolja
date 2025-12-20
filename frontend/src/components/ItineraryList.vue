<script setup>
  import { defineProps, defineEmits } from 'vue' 
  import { MapPin, Clock, Trash2, GripVertical, Plus, Edit2 } from 'lucide-vue-next' 
  import draggable from 'vuedraggable'
  import { useTripStore } from '@/stores/tripStore'
  
  const props = defineProps({
    isEditing: {
      type: Boolean,
      default: false
    }
  })
  
  // ★ 'focus-place' 이벤트 추가 (부모에게 좌표 전달용)
  const emit = defineEmits(['edit-item', 'open-manual-add', 'focus-place'])
  const store = useTripStore()
  
  const dayColors = [
    '#DE2E5F', '#3B82F6', '#10B981', '#F59E0B', '#8B5CF6', 
    '#EF4444', '#06B6D4', '#D946EF', '#84CC16', '#6366F1'
  ]
  
  const dragOptions = {
    animation: 200,
    group: "itinerary-items",
    disabled: false,
    ghostClass: "ghost-card"
  }
  
  // ★ [신규 함수] 리스트 클릭 시 지도 이동 이벤트 발생
  const moveToMap = (item) => {
    // 1. 다양한 이름의 좌표 속성 대응 (DB, API, 수동 추가 등)
    const lat = item.latitude || item.lat || item.placeLat;
    const lng = item.longitude || item.lng || item.placeLng;
  
    // 2. 좌표가 유효하면 부모(PlanningDashboard)에게 신호 전송
    if (lat && lng) {
      emit('focus-place', { lat, lng });
    } else {
      console.warn("이 장소에는 좌표 정보가 없습니다:", item.name);
    }
  }
  </script>
  
  <template>
    <div class="p-6 space-y-8 h-full overflow-y-auto bg-white scrollbar-hide">
      <div v-for="(day, index) in store.itinerary" :key="day.id" class="space-y-4">
        
        <div class="flex items-center gap-3 sticky top-0 bg-white/95 backdrop-blur z-10 py-2 border-b border-gray-100">
          <div 
            class="w-1.5 h-8 rounded-full shadow-sm"
            :style="{ backgroundColor: dayColors[index % dayColors.length] }"
          ></div>
  
          <div>
             <div class="flex items-center gap-2">
               <span 
                 class="text-xl font-black"
                 :style="{ color: dayColors[index % dayColors.length] }"
               >
                 Day {{ day.day }}
               </span>
               <span class="text-sm text-gray-500 font-medium">{{ day.date }}</span>
             </div>
          </div>
        </div>
  
        <div class="relative pl-4">
          <div class="absolute left-3 top-0 bottom-0 w-0.5 bg-gray-100"></div>
  
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
              <div class="relative group pl-4"> 
                <div 
                  class="absolute left-0.5 top-6 w-3 h-3 rounded-full border-2 border-white shadow-md z-10"
                  :style="{ backgroundColor: dayColors[index % dayColors.length] }"
                ></div>
  
                <div 
                  @click="moveToMap(item)"
                  class="bg-white border border-gray-200 rounded-2xl p-4 shadow-sm transition-all relative overflow-hidden select-none"
                  :class="{ 
                    'cursor-move border-l-4': isEditing,          /* 편집 모드: 드래그 커서 */
                    'cursor-pointer hover:shadow-md hover:bg-gray-50': !isEditing, /* 일반 모드: 포인터 커서 & 호버 효과 */
                    'border-l-4': isEditing 
                  }"
                  :style="{ borderLeftColor: isEditing ? dayColors[index % dayColors.length] : '' }"
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
                        <span class="line-clamp-1">{{ item.location || item.address }}</span>
                      </div>
                    </div>
  
                    <div v-if="isEditing" class="flex flex-col gap-1">
                      <button 
                        @click.stop="emit('edit-item', day.id, item)"
                        class="p-1.5 text-gray-300 hover:text-blue-500 hover:bg-blue-50 rounded-full transition-colors"
                        title="수정"
                      >
                        <Edit2 class="h-4 w-4" />
                      </button>
                      <button 
                        @click.stop="store.removePlace(day.id, item.id)"
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
  
  /* 스크롤바 숨기기 (선택 사항) */
  .scrollbar-hide::-webkit-scrollbar {
      display: none;
  }
  .scrollbar-hide {
      -ms-overflow-style: none;
      scrollbar-width: none;
  }
  </style>