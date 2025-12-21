<script setup>
import { ref, onMounted, computed, defineEmits } from "vue";
import {
  Trash2,
  Plus,
  ArrowLeft,
  Check,
  CheckCircle2,
  StickyNote,
  Plane,
  FolderOpen,
} from "lucide-vue-next";
import {
  getChecklist,
  addCheckItem,
  deleteCheckItem,
  toggleCheckItem,
  deleteAllCheckItems,
  checkAllCheckItems,
  getMyPlans,
} from "@/api/checklist";
import { useUserStore } from "@/stores/userStore";

const emit = defineEmits(["back"]);
const userStore = useUserStore();

// --- 상태 관리 ---
const mode = ref("select"); // 'select'(목록 고르기) 또는 'detail'(내용 보기)
const currentPlanId = ref(0);
const currentPlanTitle = ref("");

const myPlans = ref([]); // 여행 목록 데이터
const items = ref([]); // 체크리스트 아이템 데이터
const newItem = ref("");

// 1. 초기 로딩: 여행 목록 가져오기
const loadPlans = async () => {
  try {
    const res = await getMyPlans();
    myPlans.value = res.data;
  } catch (e) {
    console.error("여행 목록 로드 실패", e);
  }
};

// 2. 폴더 클릭 시: 상세 화면으로 진입
const openChecklist = async (planId, title) => {
  currentPlanId.value = planId;
  currentPlanTitle.value = title;
  mode.value = "detail"; // 화면 전환

  newItem.value = ""; // ★ 추가: 입력창 초기화
  items.value = []; // (선택) 로딩 중에 이전 리스트 잔상 없애기

  await loadItems(); // 해당 리스트 불러오기
};

// 3. 뒤로가기 로직 (상세 -> 목록 -> 메인)
const handleBack = () => {
  if (mode.value === "detail") {
    mode.value = "select"; // 목록 화면으로 복귀
    items.value = []; // 데이터 비우기
    newItem.value = ""; // 입력창 초기화
  } else {
    emit("back"); // 아예 메인으로 나가기
  }
};

// --- CRUD 로직 (planId 사용) ---
const loadItems = async () => {
  try {
    const res = await getChecklist(currentPlanId.value);
    items.value = res.data;
  } catch (e) {
    console.error(e);
  }
};

const handleAdd = async () => {
  if (!newItem.value.trim()) return;
  // ★ 현재 보고 있는 planId로 저장
  await addCheckItem(newItem.value, currentPlanId.value);
  newItem.value = "";
  await loadItems();
};

const handleDelete = async (id) => {
  await deleteCheckItem(id);
  await loadItems();
};

const handleToggle = async (id) => {
  // UI 반응성 향상 (Optimistic Update)
  const target = items.value.find((i) => i.checkId === id);
  if (target) target.isChecked = !target.isChecked;
  try {
    await toggleCheckItem(id);
  } catch {
    await loadItems();
  }
};

const handleCheckAll = async () => {
  if (confirm("모두 완료 처리할까요?")) {
    await checkAllCheckItems(currentPlanId.value);
    await loadItems();
  }
};

const handleDeleteAll = async () => {
  if (confirm("전체 삭제하시겠습니까?")) {
    await deleteAllCheckItems(currentPlanId.value);
    await loadItems();
  }
};

const progress = computed(() => {
  if (items.value.length === 0) return 0;
  const checkedCount = items.value.filter((i) => i.isChecked).length;
  return Math.round((checkedCount / items.value.length) * 100);
});

// 컴포넌트 켜지면 여행 목록부터 로딩
onMounted(() => {
  loadPlans();
});
</script>

<template>
  <div
    class="min-h-screen bg-[#F2F2F7] p-6 flex justify-center items-start pt-12 md:pt-20 font-sans"
  >
    <div class="w-full max-w-lg space-y-6">
      <div class="flex flex-col gap-1 px-2">
        <button
          @click="handleBack"
          class="self-start flex items-center gap-1 text-[#DE2E5F] hover:opacity-70 transition-opacity font-bold mb-4 text-base"
        >
          <ArrowLeft class="w-5 h-5" />
          {{ mode === "detail" ? "보관함으로" : "메인으로" }}
        </button>

        <h1
          class="text-3xl md:text-4xl font-extrabold text-gray-900 tracking-tight"
        >
          {{
            mode === "select"
              ? "체크리스트 보관함"
              : currentPlanId === 0
              ? "나의 체크리스트"
              : currentPlanTitle
          }}
        </h1>
        <p v-if="mode === 'select'" class="text-gray-500 font-medium ml-1">
          여행을 위한 필수 체크리스트
        </p>
      </div>

      <Transition name="fade" mode="out-in">
        <div v-if="mode === 'select'" class="space-y-6">
          <div
            @click="openChecklist(0, '나의 체크리스트')"
            class="bg-white p-6 rounded-[24px] shadow-sm hover:shadow-lg transition-all cursor-pointer group border-2 border-transparent hover:border-[#DE2E5F]/10 active:scale-[0.98]"
          >
            <div class="flex items-center gap-4">
              <div
                class="w-12 h-12 rounded-2xl bg-gray-100 flex items-center justify-center group-hover:bg-[#DE2E5F] transition-colors duration-300"
              >
                <StickyNote
                  class="w-6 h-6 text-gray-500 group-hover:text-white transition-colors"
                />
              </div>
              <div>
                <h3 class="text-xl font-bold text-gray-900">일반 체크리스트</h3>
                <p class="text-gray-400 text-sm font-medium">종합 체크리스트</p>
              </div>
            </div>
          </div>

          <div class="border-t border-gray-200/60 my-4"></div>

          <div>
            <h2
              class="text-sm font-bold text-gray-400 mb-3 px-2 flex items-center gap-1"
            >
              <Plane class="w-4 h-4" />
              내 여행 계획별 체크리스트
            </h2>

            <div v-if="myPlans.length > 0" class="grid gap-3">
              <div
                v-for="plan in myPlans"
                :key="plan.planId"
                @click="openChecklist(plan.planId, plan.title)"
                class="bg-white p-5 rounded-[20px] shadow-sm hover:shadow-md transition-all cursor-pointer flex items-center justify-between group active:scale-[0.98]"
              >
                <div class="flex items-center gap-3">
                  <div
                    class="w-10 h-10 rounded-full bg-pink-50 flex items-center justify-center"
                  >
                    <FolderOpen class="w-5 h-5 text-[#DE2E5F]" />
                  </div>
                  <span class="font-bold text-gray-800 text-lg">{{
                    plan.title
                  }}</span>
                </div>
                <ArrowLeft
                  class="w-5 h-5 text-gray-300 rotate-180 group-hover:text-[#DE2E5F] transition-colors"
                />
              </div>
            </div>

            <div
              v-else
              class="text-center py-10 text-gray-400 bg-white/50 rounded-3xl border border-dashed border-gray-300"
            >
              <p class="text-sm font-bold">아직 생성된 여행이 없어요.</p>
              <p class="text-xs mt-1">여행 그룹을 만들면 폴더가 생깁니다!</p>
            </div>
          </div>
        </div>

        <div v-else class="space-y-6">
          <div
            class="bg-white rounded-[24px] p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-white/50"
          >
            <div class="flex justify-between items-end mb-3">
              <div>
                <span class="text-4xl font-black text-gray-900 tracking-tighter"
                  >{{ progress }}%</span
                >
                <span class="text-sm text-gray-400 font-bold ml-2">준비됨</span>
              </div>
              <div
                class="text-xs font-bold bg-pink-50 text-[#DE2E5F] px-3 py-1.5 rounded-full border border-pink-100"
              >
                {{ items.filter((i) => i.isChecked).length }} /
                {{ items.length }}
              </div>
            </div>
            <div class="w-full bg-gray-100 rounded-full h-3 overflow-hidden">
              <div
                class="h-full bg-gradient-to-r from-[#DE2E5F] to-[#ff5d8d] transition-all duration-700 ease-out rounded-full shadow-[0_0_10px_#DE2E5F66]"
                :style="{ width: `${progress}%` }"
              ></div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <button
              @click="handleCheckAll"
              class="bg-white p-4 rounded-2xl shadow-sm hover:shadow-md transition-all flex items-center justify-center gap-2 group active:scale-95"
            >
              <CheckCircle2
                class="w-5 h-5 text-gray-400 group-hover:text-[#DE2E5F]"
              />
              <span class="font-bold text-gray-600 group-hover:text-[#DE2E5F]"
                >모두 완료</span
              >
            </button>
            <button
              @click="handleDeleteAll"
              class="bg-white p-4 rounded-2xl shadow-sm hover:shadow-md transition-all flex items-center justify-center gap-2 group active:scale-95"
            >
              <Trash2 class="w-5 h-5 text-gray-400 group-hover:text-red-500" />
              <span class="font-bold text-gray-600 group-hover:text-red-500"
                >전체 삭제</span
              >
            </button>
          </div>

          <div class="relative group">
            <input
              v-model="newItem"
              @keyup.enter="handleAdd"
              type="text"
              placeholder="준비물 추가..."
              class="w-full bg-white pl-5 pr-14 py-4 rounded-2xl text-gray-900 font-bold shadow-sm focus:outline-none focus:ring-2 focus:ring-[#DE2E5F]/20"
            />
            <button
              @click="handleAdd"
              class="absolute right-2 top-2 bottom-2 aspect-square bg-[#DE2E5F] text-white rounded-xl flex items-center justify-center hover:bg-[#c92552] shadow-md active:scale-90 transition-transform"
            >
              <Plus class="w-5 h-5 stroke-[3]" />
            </button>
          </div>

          <TransitionGroup name="list" tag="div" class="space-y-3 pb-20">
            <div
              v-for="item in items"
              :key="item.checkId"
              class="group bg-white p-4 rounded-2xl shadow-sm flex items-center gap-4 cursor-pointer active:scale-[0.99] transition-all border border-transparent hover:border-pink-100"
              @click="handleToggle(item.checkId)"
            >
              <div
                class="w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all duration-300 flex-shrink-0"
                :class="
                  item.isChecked
                    ? 'bg-[#DE2E5F] border-[#DE2E5F]'
                    : 'border-gray-300'
                "
              >
                <Check
                  v-if="item.isChecked"
                  class="w-3.5 h-3.5 text-white stroke-[4]"
                />
              </div>
              <span
                class="flex-1 text-lg font-bold truncate transition-all"
                :class="{ 'text-gray-300 line-through': item.isChecked }"
              >
                {{ item.content }}
              </span>
              <button
                @click.stop="handleDelete(item.checkId)"
                class="text-gray-300 hover:text-red-500 md:opacity-0 md:group-hover:opacity-100 transition-opacity p-2"
              >
                <Trash2 class="w-5 h-5" />
              </button>
            </div>
          </TransitionGroup>

          <div
            v-if="items.length === 0"
            class="text-center py-10 text-gray-400"
          >
            <p class="font-bold text-sm">리스트가 비어있습니다.</p>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
