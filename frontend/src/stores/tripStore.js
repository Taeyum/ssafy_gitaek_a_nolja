import { ref } from 'vue'
import { defineStore } from 'pinia'
import { 
  createTripApi, 
  getMyTripsApi, 
  deleteTripApi, 
  addScheduleApi, 
  getSchedulesApi, 
  deleteScheduleApi,
  requestEditApi,     
  releaseEditApi,     
  getTripStatusApi,
  joinTripApi,
  leaveTripApi,
  getMessagesApi,
  sendMessageApi
} from '@/api/trip'

export const useTripStore = defineStore('trip', () => {
  // --- 상태 (State) ---
  const tripInfo = ref({
    tripId: 0,
    title: '',
    duration: 2,
    startDate: '',
    endDate: '',
    maxMembers: 4,
    currentMembers: [],
    inviteCode: '',
    style: 'friend'
  })

  const itinerary = ref([])
  const myTrips = ref([])
  const messages = ref([])
  
  // 동시성 제어용
  const currentEditorName = ref(null) 
  const isLocked = ref(false)         
  let pollingInterval = null          

  // --- 액션 (Actions) ---

  const fetchMyTrips = async () => {
    try {
      const response = await getMyTripsApi()
      myTrips.value = response.data
    } catch (error) {
      console.error("여행 목록 로드 실패", error)
    }
  }

  const loadTrip = async (trip) => { 
    tripInfo.value = {
      tripId: trip.tripId,
      title: trip.title,
      startDate: trip.startDate,
      endDate: trip.endDate,
      style: trip.style,
      maxMembers: trip.maxParticipants,
      currentParticipants: trip.currentParticipants || 1,
      inviteCode: trip.inviteCode || 'CODE_ERR' ,
      ownerId: trip.ownerId
    }

    // 날짜 틀 생성
    const start = new Date(trip.startDate)
    const end = new Date(trip.endDate)
    const diffTime = Math.abs(end - start)
    const dayCount = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1

    const newItinerary = []
    for (let i = 0; i < dayCount; i++) {
        const currentDate = new Date(start)
        currentDate.setDate(start.getDate() + i)
        newItinerary.push({ 
            id: (i + 1).toString(), 
            day: `${i + 1}일차`,    
            date: currentDate.toISOString().split('T')[0],       
            items: [] 
        })
    }

    // 일정 데이터 매핑
    try {
      const res = await getSchedulesApi(trip.tripId)
      const dbSchedules = res.data
      
      dbSchedules.forEach(item => {
        const dayIndex = item.tripDay - 1
        if (newItinerary[dayIndex]) {
          newItinerary[dayIndex].items.push({
            id: Date.now() + Math.random().toString(),
            time: item.scheduleTime,
            name: item.placeName,
            location: item.placeAddress,
            lat: item.placeLat,
            lng: item.placeLng,
            poiId: item.poiId
          })
        }
      })
    } catch (error) {
      console.error("일정 데이터 로드 실패", error)
    }
    
    itinerary.value = newItinerary
  }

  const createNewTrip = async (info) => {
    try {
      const response = await createTripApi({
        title: info.title,
        duration: info.duration,
        maxMembers: info.members,
        style: info.style,
        startDate: info.startDate
      })
      const savedTrip = response.data
      
      // 생성 직후 바로 로드
      tripInfo.value = {
        tripId: savedTrip.tripId,
        title: savedTrip.title,
        startDate: savedTrip.startDate,
        endDate: savedTrip.endDate,
        inviteCode: savedTrip.inviteCode,
        ownerId: savedTrip.ownerId,
        maxMembers: savedTrip.maxParticipants, 
        currentParticipants: 1, 
        style: savedTrip.style
      }
      const newItinerary = []
      for (let i = 1; i <= info.duration; i++) {
        newItinerary.push({
          id: i.toString(),
          day: `${i}일차`,
          date: `Day ${i}`,
          items: []
        })
      }
      itinerary.value = newItinerary
      return true
    } catch (error) {
      alert("여행 생성 실패")
      return false
    }
  }

  const addPlace = async (dayId, place, time) => {
    const targetDay = itinerary.value.find(d => d.id === dayId)
    if (!targetDay) return

    const inputTime = time ? time : "12:00"

    // 중복 체크
    if (targetDay.items.some(item => item.poiId === place.poiId)) {
        alert(`[${place.name}] 이미 추가된 장소입니다!`);
        return; 
    }
    if (targetDay.items.some(item => item.time === inputTime)) {
        alert(`선택하신 시간(${inputTime})에 이미 일정이 있습니다.`);
        return; 
    }

    const tempId = Date.now().toString()
    const newItem = {
      id: tempId,
      time: inputTime,
      name: place.name,
      location: place.address || "",
      lat: place.lat || 0,
      lng: place.lng || 0,
      poiId: place.poiId 
    }
    
    targetDay.items.push(newItem)
    targetDay.items.sort((a, b) => a.time.localeCompare(b.time))

    try {
      const dayNumber = parseInt(dayId.replace(/[^0-9]/g, "")) || 1; 
      await addScheduleApi({
        tripId: tripInfo.value.tripId, 
        poiId: place.poiId,            
        tripDay: dayNumber,           
        visitOrder: targetDay.items.length, 
        memo: "",
        scheduleTime: inputTime                       
      })
    } catch (error) {
      alert("저장 실패. 다시 시도해주세요.")
      targetDay.items = targetDay.items.filter(item => item.id !== tempId) // 롤백
    }
  }

  const editItem = (dayId, itemId, newTime, newName) => {
    // (기존 코드 유지 - 단순 화면 업데이트용이라면)
    const targetDay = itinerary.value.find(d => d.id === dayId)
    if (targetDay) {
      const item = targetDay.items.find(i => i.id === itemId)
      if (item) {
        item.time = newTime
        item.name = newName
        targetDay.items.sort((a, b) => a.time.localeCompare(b.time))
      }
    }
  }

  const removePlace = async (dayId, itemId) => {
    const targetDay = itinerary.value.find(d => d.id === dayId)
    if (!targetDay) return
    const itemToRemove = targetDay.items.find(item => item.id === itemId)
    if (!itemToRemove) return

    try {
      const dayNumber = parseInt(dayId.replace(/[^0-9]/g, "")) || 1;
      await deleteScheduleApi(tripInfo.value.tripId, dayNumber, itemToRemove.poiId)
      targetDay.items = targetDay.items.filter(item => item.id !== itemId)
    } catch (error) {
      alert("삭제 실패")
    }
  }

  const deleteTrip = async (tripId) => {
    try {
      await deleteTripApi(tripId)
      await fetchMyTrips() 
      return true
    } catch (error) { return false }
  }

  const joinTrip = async (code) => {
    try {
      const response = await joinTripApi(code)
      return response.data
    } catch (error) {
      alert(error.response?.data || "입장 실패")
      return null
    }
  }

  // ★ [추가] DB에서 최신 일정 가져와서 덮어쓰기 (새로고침 없이 반영)
  const refreshItinerary = async () => {
    if (!tripInfo.value.tripId) return

    try {
      const res = await getSchedulesApi(tripInfo.value.tripId)
      const dbSchedules = res.data

      // 1. 기존 일정 초기화 (날짜 틀은 유지하고 아이템만 비움)
      itinerary.value.forEach(day => {
        day.items = []
      })

      // 2. DB 데이터 다시 채워넣기
      dbSchedules.forEach(item => {
        const dayIndex = item.tripDay - 1
        if (itinerary.value[dayIndex]) {
          itinerary.value[dayIndex].items.push({
            // 프론트에서 관리할 ID (DB ID가 없으면 임시로 만듦)
            id: item.scheduleId ? String(item.scheduleId) : String(Date.now() + Math.random()),
            time: item.scheduleTime,
            name: item.placeName,
            location: item.placeAddress,
            lat: item.placeLat,
            lng: item.placeLng,
            poiId: item.poiId
          })
        }
      })
    } catch (error) {
      console.error("일정 동기화 실패", error)
    }
  }

  // ★ [수정됨] 폴링 로직 (Lock 확인 + 데이터 동기화)
  const checkStatus = async (userId) => {
    try {
      // 1. 여행 상태(Lock, 인원수) 확인
      const res = await getTripStatusApi(tripInfo.value.tripId)
      const trip = res.data
      
      // 인원수 갱신
      if (trip.currentParticipants) {
          tripInfo.value.currentParticipants = trip.currentParticipants
      }

      await fetchMessages()
      
      // 2. Lock 상태 처리
      if (trip.currentEditorId) {
          if (trip.currentEditorId !== userId) {
              currentEditorName.value = "다른 사용자" 
              isLocked.value = true
              
              // ★ 중요: 남이 수정 중이거나, 아무도 수정 안 할때는
              // 최신 데이터를 계속 받아와야 함!
              await refreshItinerary()
              
          } else {
              currentEditorName.value = "나"
              isLocked.value = false
              // (내가 수정 중일 때는 불러오지 않음 -> 내 화면이 깜빡거리거나 입력 중인게 날아갈 수 있어서)
          }
      } else {
          // 아무도 수정 안 하는 상태 -> 최신 데이터 동기화
          currentEditorName.value = null
          isLocked.value = false
          await refreshItinerary()
      }

    } catch (e) { 
      // console.error(e) 
    }
  }

  const startPolling = (userId) => {
    if (pollingInterval) return 
    
    // 1. 시작하자마자 1번 즉시 실행 (딜레이 없음)
    checkStatus(userId)

    // 2. 1초마다 계속 확인 (3초 -> 1초로 단축)
    pollingInterval = setInterval(() => {
        checkStatus(userId)
    }, 1000) 
  }

  const stopPolling = () => {
    if (pollingInterval) {
      clearInterval(pollingInterval)
      pollingInterval = null
    }
  }

  const tryRequestEdit = async () => {
    if (!tripInfo.value.tripId) return false
    try {
      await requestEditApi(tripInfo.value.tripId)
      // 성공하자마자 상태 업데이트
      currentEditorName.value = "나"
      isLocked.value = false
      return true
    } catch (error) {
      if (error.response?.status === 409) alert("다른 사용자가 수정 중입니다!")
      else alert("오류가 발생했습니다.")
      return false
    }
  }

  const finishEdit = async () => {
    try {
      await releaseEditApi(tripInfo.value.tripId)
      currentEditorName.value = null
      return true
    } catch (e) { console.error(e) }
  }

  // [수정] 여행 나가기 (ID를 직접 받을 수도 있게 변경)
  const leaveTrip = async (targetTripId = null) => {
    // 1. 파라미터로 받은 ID가 있으면 그거 쓰고, 없으면 현재 로드된 여행 ID 사용
    const idToDelete = targetTripId || tripInfo.value.tripId
    
    if (!idToDelete) {
        alert("나갈 여행이 선택되지 않았습니다.")
        return false
    }

    if (!confirm("정말 이 여행 계획에서 나가시겠습니까?")) return false

    try {
      await leaveTripApi(idToDelete)
      await fetchMyTrips() // 목록 새로고침
      return true
    } catch (error) {
      alert(error.response?.data || "나가기 실패")
      return false
    }
  }

  // 채팅 목록 불러오기
  const fetchMessages = async () => {
    if (!tripInfo.value.tripId) return
    try {
      const res = await getMessagesApi(tripInfo.value.tripId)
      messages.value = res.data
    } catch (e) {
      // console.error(e)
    }
  }

  // 메시지 전송
  const sendMessage = async (content) => {
    if (!content.trim()) return
    try {
      await sendMessageApi(tripInfo.value.tripId, content)
      await fetchMessages() // 전송 후 바로 갱신
    } catch (e) {
      alert("메시지 전송 실패")
    }
  }

  return { 
    tripInfo, itinerary, myTrips, 
    currentEditorName, isLocked, 
    fetchMyTrips, loadTrip, createNewTrip, addPlace, editItem, removePlace, deleteTrip, joinTrip,
    startPolling, stopPolling, tryRequestEdit, finishEdit ,
    leaveTrip, messages, fetchMessages, sendMessage
  }
})