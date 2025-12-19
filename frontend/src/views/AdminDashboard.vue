<script setup>
    import { ref, onMounted, defineEmits } from 'vue'
    import { 
      ArrowLeft, Shield, Users, Database, Search, 
      RotateCcw, RefreshCw, CheckCircle 
    } from 'lucide-vue-next'
    import { getUserListApi, changeUserRoleApi, resetUserPasswordApi, loadTourDataApi } from '@/api/admin'
    
    const emit = defineEmits(['back'])
    
    // --- 상태 관리 ---
    const userList = ref([])
    const totalPages = ref(1)
    const currentPage = ref(1)
    const totalCount = ref(0) // 전체 회원 수
    
    // 검색 필터
    const searchType = ref('nickname') // 'nickname' or 'email'
    const searchKeyword = ref('')
    
    // 로딩 상태
    const isLoadingData = ref(false)
    
    // --- 1. 회원 목록 불러오기 ---
    const fetchUsers = async (page = 1) => {
      try {
        const response = await getUserListApi(page, searchType.value, searchKeyword.value)
        // 백엔드 리턴: { users: [...], totalPage: 5, totalCount: 100 }
        const data = response.data
        
        userList.value = data.users || []
        totalPages.value = data.totalPage || 1
        // totalCount는 없을 수도 있으니 체크 (UserMapper.xml 확인 필요)
        totalCount.value = userList.value.length 
        currentPage.value = page
      } catch (error) {
        console.error("회원 목록 로딩 실패:", error)
        if(error.response?.status === 403) {
          alert("관리자 권한이 없습니다.")
          emit('back')
        }
      }
    }
    
    // --- 2. 권한 변경 ---
    const handleChangeRole = async (userId, event) => {
      const newRole = event.target.value
      if (!confirm(`해당 회원의 권한을 [${newRole}]로 변경하시겠습니까?`)) {
        fetchUsers(currentPage.value) // 취소 시 UI 원상복구
        return
      }
    
      try {
        await changeUserRoleApi(userId, newRole)
        alert("권한이 변경되었습니다.")
        fetchUsers(currentPage.value)
      } catch (error) {
        alert("권한 변경 실패: " + (error.response?.data || "오류 발생"))
      }
    }
    
    // --- 3. 비밀번호 초기화 ---
    const handleResetPw = async (userId, nickname) => {
      if (!confirm(`[${nickname}] 님의 비밀번호를 '1234'로 초기화하시겠습니까?`)) return
    
      try {
        await resetUserPasswordApi(userId)
        alert("비밀번호가 '1234'로 초기화되었습니다.")
      } catch (error) {
        alert("초기화 실패")
      }
    }
    
    // --- 4. 데이터 수집 ---
    const handleLoadData = async () => {
      if (!confirm("공공데이터포털에서 관광지 데이터를 수집하시겠습니까?\n(약 1~2분 소요될 수 있습니다.)")) return
    
      isLoadingData.value = true
      try {
        const response = await loadTourDataApi()
        alert(response.data || "데이터 수집 완료!")
      } catch (error) {
        alert("데이터 수집 중 오류가 발생했습니다.")
      } finally {
        isLoadingData.value = false
      }
    }
    
    const handleSearch = () => {
      currentPage.value = 1
      fetchUsers(1)
    }
    
    onMounted(() => {
      fetchUsers(1)
    })
    </script>
    
    <template>
      <div class="min-h-screen bg-gray-50 flex flex-col">
        <header class="bg-white px-6 py-4 flex items-center gap-4 shadow-sm border-b sticky top-0 z-10">
          <button @click="emit('back')" class="p-2 rounded-full hover:bg-gray-100 text-gray-500 transition-colors">
            <ArrowLeft class="h-6 w-6" />
          </button>
          <div class="w-10 h-10 rounded-2xl bg-gray-900 flex items-center justify-center shadow-lg">
            <Shield class="h-5 w-5 text-white" />
          </div>
          <div>
            <h1 class="text-xl font-bold text-gray-900">관리자 대시보드</h1>
            <p class="text-xs text-gray-500">시스템 데이터 및 회원 권한 관리</p>
          </div>
        </header>
    
        <main class="flex-1 p-6 max-w-6xl w-full mx-auto space-y-6">
          
          <section class="bg-white rounded-3xl p-6 shadow-sm border border-gray-100">
            <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2 mb-4">
              <Database class="w-5 h-5 text-[#DE2E5F]" />
              데이터 동기화
            </h2>
            
            <div class="flex items-center justify-between bg-gray-50 p-5 rounded-2xl border border-gray-100">
              <div>
                <div class="font-bold text-gray-800 mb-1 flex items-center gap-2">
                  전국 관광지 데이터 수집
                  <span class="text-xs bg-blue-100 text-blue-600 px-2 py-0.5 rounded-full">TourAPI 4.0</span>
                </div>
                <p class="text-sm text-gray-500">공공데이터포털 API를 호출하여 최신 관광지 정보를 DB에 저장합니다.</p>
              </div>
              <button 
                @click="handleLoadData" 
                :disabled="isLoadingData"
                class="flex items-center gap-2 px-5 py-2.5 bg-white border border-gray-200 shadow-sm rounded-xl font-bold text-gray-700 hover:text-[#DE2E5F] hover:border-[#DE2E5F] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': isLoadingData }" />
                {{ isLoadingData ? '수집 중...' : '데이터 수집 시작' }}
              </button>
            </div>
          </section>
    
          <section class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
            <div class="p-6 border-b border-gray-100 flex flex-col md:flex-row md:items-center justify-between gap-4">
              <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">
                <Users class="w-5 h-5 text-[#DE2E5F]" />
                회원 목록 조회
              </h2>
    
              <div class="flex items-center gap-2">
                <select v-model="searchType" class="bg-gray-50 border border-gray-200 text-gray-700 text-sm rounded-xl focus:ring-[#DE2E5F] focus:border-[#DE2E5F] p-2.5 outline-none">
                  <option value="nickname">닉네임</option>
                  <option value="email">이메일</option>
                </select>
                <div class="relative">
                  <input 
                    v-model="searchKeyword" 
                    @keyup.enter="handleSearch"
                    type="text" 
                    class="bg-gray-50 border border-gray-200 text-gray-900 text-sm rounded-xl focus:ring-[#DE2E5F] focus:border-[#DE2E5F] w-48 pl-3 p-2.5 outline-none" 
                    placeholder="검색어 입력"
                  >
                </div>
                <button @click="handleSearch" class="bg-[#DE2E5F] text-white px-4 py-2.5 rounded-xl text-sm font-bold hover:bg-[#c92552] transition-colors">
                  <Search class="w-4 h-4" />
                </button>
              </div>
            </div>
    
            <div class="overflow-x-auto">
              <table class="w-full text-sm text-left text-gray-500">
                <thead class="text-xs text-gray-700 uppercase bg-gray-50 border-b">
                  <tr>
                    <th class="px-6 py-3">ID</th>
                    <th class="px-6 py-3">사용자</th>
                    <th class="px-6 py-3">이메일</th>
                    <th class="px-6 py-3">가입일</th>
                    <th class="px-6 py-3">권한</th>
                    <th class="px-6 py-3 text-center">비번 초기화</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="userList.length === 0">
                    <td colspan="6" class="px-6 py-10 text-center text-gray-400">데이터가 없습니다.</td>
                  </tr>
                  <tr v-for="user in userList" :key="user.userId" class="bg-white border-b hover:bg-gray-50 transition-colors">
                    <td class="px-6 py-4 font-medium">#{{ user.userId }}</td>
                    <td class="px-6 py-4 flex items-center gap-2">
                      <div class="w-8 h-8 rounded-full bg-gray-200 overflow-hidden">
                        <img :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.nickname}`" alt="img" />
                      </div>
                      {{ user.nickname }}
                    </td>
                    <td class="px-6 py-4">{{ user.email }}</td>
                    <td class="px-6 py-4">{{ user.createdAt ? user.createdAt.substring(0,10) : '-' }}</td>
                    <td class="px-6 py-4">
                      <select 
                        :value="user.role" 
                        @change="handleChangeRole(user.userId, $event)"
                        class="bg-white border border-gray-200 text-xs font-bold py-1 px-2 rounded-lg focus:border-[#DE2E5F] outline-none cursor-pointer"
                        :class="user.role === 'ADMIN' ? 'text-[#DE2E5F] border-pink-200 bg-pink-50' : 'text-gray-600'"
                      >
                        <option value="USER">USER</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                    </td>
                    <td class="px-6 py-4 text-center">
                      <button 
                        @click="handleResetPw(user.userId, user.nickname)"
                        class="text-gray-400 hover:text-orange-500 hover:bg-orange-50 p-2 rounded-lg transition-colors"
                        title="1234로 초기화"
                      >
                        <RotateCcw class="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
    
            <div class="p-4 border-t border-gray-100 flex justify-center gap-2">
              <button 
                v-for="p in totalPages" 
                :key="p"
                @click="fetchUsers(p)"
                class="w-8 h-8 flex items-center justify-center rounded-lg text-sm font-bold transition-colors"
                :class="currentPage === p ? 'bg-[#DE2E5F] text-white' : 'text-gray-500 hover:bg-gray-100'"
              >
                {{ p }}
              </button>
            </div>
          </section>
        </main>
      </div>
    </template>