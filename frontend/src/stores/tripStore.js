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
  getTripStatusApi    
} from '@/api/trip'

export const useTripStore = defineStore('trip', () => {
  // 1. 상태 (State)
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
  
  // 동시성 제어용 상태
  const currentEditorName = ref(null) 
  const isLocked = ref(false)         
  let pollingInterval = null          

  // 2. 액션 (Actions)

  // 내 여행 목록 불러오기
  const fetchMyTrips = async () => {
    try {
      const response = await getMyTripsApi()
      myTrips.value = response.data
    } catch (error) {
      console.error("여행 목록 불러오기 실패:", error)
    }
  }

  // 여행 불러오기 (DB 일정 연동)
  const loadTrip = async (trip) => { 
    tripInfo.value = {
      tripId: trip.tripId,
      title: trip.title,
      startDate: trip.startDate,
      endDate: trip.endDate,
      style: trip.style,
      maxMembers: trip.maxParticipants,
      currentParticipants: trip.currentParticipants || 1,
      inviteCode: 'LOADED'
    }

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

    try {
      const res = await getSchedulesApi(trip.tripId)
      const dbSchedules = res.data
      
      dbSchedules.forEach(item => {
        const dayIndex = item.tripDay - 1
        if (newItinerary[dayIndex]) {
          newItinerary[dayIndex].items.push({
            id: Date.now() + Math.random().toString(),
            time: "12:00", 
            name: item.placeName,
            location: item.placeAddress,
            lat: item.placeLat,
            lng: item.placeLng,
            poiId: item.poiId
          })
        }
      })
      console.log("일정 로드 완료:", dbSchedules.length + "개")
    } catch (error) {
      console.error("일정 불러오기 실패:", error)
    }
    
    itinerary.value = newItinerary
  }

  // 여행 생성
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
      
      tripInfo.value = {
        tripId: savedTrip.tripId,
        title: savedTrip.title,
        duration: info.duration,
        startDate: savedTrip.startDate,
        endDate: savedTrip.endDate,
        maxMembers: savedTrip.maxParticipants,
        style: savedTrip.style || info.style,
        currentMembers: [{ id: 'me', name: '나(방장)', avatar: 'Me', color: 'bg-gray-200' }],
        inviteCode: 'Generating...'
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
      console.error("여행 생성 실패:", error)
      alert("여행을 생성하는 중 문제가 발생했습니다.")
      return false
    }
  }

  // ★ [수정됨] 장소 추가 (장소 중복 + 시간 중복 체크)
  const addPlace = async (dayId, place, time) => {
    const targetDay = itinerary.value.find(d => d.id === dayId)
    if (!targetDay) return

    const inputTime = time ? time : "12:00"

    // 1. 장소 중복 체크
    const isPoiDuplicate = targetDay.items.some(item => item.poiId === place.poiId);
    if (isPoiDuplicate) {
        alert(`[${place.name}] 이미 해당 날짜에 추가된 장소입니다!`);
        return; 
    }

    // 2. ★ [추가] 시간 중복 체크
    const isTimeDuplicate = targetDay.items.some(item => item.time === inputTime);
    if (isTimeDuplicate) {
        alert(`선택하신 시간(${inputTime})에는 이미 일정이 있습니다.\n다른 시간을 선택하거나 기존 일정을 변경해주세요.`);
        return; 
    }

    // 3. 임시 ID 생성
    const tempId = Date.now().toString()
    
    // 4. 화면(UI)에 먼저 추가
    const newItem = {
      id: tempId,
      time: inputTime,
      name: place.name,
      location: place.address || "주소 정보 없음",
      lat: place.lat || 0,
      lng: place.lng || 0,
      poiId: place.poiId 
    }
    
    targetDay.items.push(newItem)
    targetDay.items.sort((a, b) => a.time.localeCompare(b.time))

    // 5. 실제 DB 저장 시도
    try {
      const dayNumber = parseInt(dayId.replace(/[^0-9]/g, "")) || 1; 

      const scheduleData = {
        tripId: tripInfo.value.tripId, 
        poiId: place.poiId,            
        tripDay: dayNumber,           
        visitOrder: targetDay.items.length, 
        memo: ""                       
      }

      await addScheduleApi(scheduleData)
      console.log("DB 저장 성공!")

    } catch (error) {
      console.error("일정 DB 저장 실패:", error)
      alert("서버 저장에 실패했습니다. 잠시 후 다시 시도해주세요.")

      // 실패 시 롤백
      const rollbackDay = itinerary.value.find(d => d.id === dayId)
      if (rollbackDay) {
        rollbackDay.items = rollbackDay.items.filter(item => item.id !== tempId)
      }
    }
  }

  // 아이템 수정
  const editItem = (dayId, itemId, newTime, newName) => {
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

  // 아이템 삭제
  const removePlace = async (dayId, itemId) => {
    const targetDay = itinerary.value.find(d => d.id === dayId)
    if (!targetDay) return

    const itemToRemove = targetDay.items.find(item => item.id === itemId)
    if (!itemToRemove) return

    try {
      const dayNumber = parseInt(dayId.replace(/[^0-9]/g, "")) || 1;
      
      await deleteScheduleApi(
        tripInfo.value.tripId, 
        dayNumber,             
        itemToRemove.poiId     
      )

      targetDay.items = targetDay.items.filter(item => item.id !== itemId)
      console.log("일정 삭제 완료")

    } catch (error) {
      console.error("삭제 실패:", error)
      alert("일정 삭제에 실패했습니다.")
    }
  }

  // 여행 삭제
  const deleteTrip = async (tripId) => {
    try {
      await deleteTripApi(tripId)
      await fetchMyTrips() 
      return true
    } catch (error) {
      alert(error.response?.data || "삭제 실패")
      return false
    }
  }

  // --- 동시성 제어 (폴링) ---
  const startPolling = (userId) => {
    if (pollingInterval) return 

    pollingInterval = setInterval(async () => {
      try {
        const res = await getTripStatusApi(tripInfo.value.tripId)
        const trip = res.data
        
        if (trip.currentEditorId) {
            if (trip.currentEditorId !== userId) {
                currentEditorName.value = "다른 사용자" 
                isLocked.value = true
            } else {
                currentEditorName.value = "나"
                isLocked.value = false
            }
        } else {
            currentEditorName.value = null
            isLocked.value = false
        }
      } catch (e) {
        // console.error("폴링 실패", e) // 로그 너무 많이 뜨면 주석 처리
      }
    }, 3000) 
  }

  const stopPolling = () => {
    if (pollingInterval) {
      clearInterval(pollingInterval)
      pollingInterval = null
    }
  }

  const tryRequestEdit = async () => {
    if (!tripInfo.value.tripId || tripInfo.value.tripId === 0) {
      alert("여행 정보를 불러오지 못했습니다. 목록에서 다시 선택해주세요.")
      return false
    }

    try {
      await requestEditApi(tripInfo.value.tripId)
      currentEditorName.value = "나"
      isLocked.value = false
      return true
    } catch (error) {
      const status = error.response?.status
      if (status === 409) {
        alert("다른 사용자가 이미 수정 중입니다!")
      } else if (status === 401) {
        alert("로그인이 필요합니다.")
      } else if (status === 404) {
        alert("여행 정보를 찾을 수 없습니다.")
      } else {
        alert("알 수 없는 오류가 발생했습니다.")
      }
      return false
    }
  }

  const finishEdit = async () => {
    try {
      await releaseEditApi(tripInfo.value.tripId)
      currentEditorName.value = null
      return true
    } catch (e) {
      console.error(e)
    }
  }

  return { 
    tripInfo, itinerary, myTrips, 
    currentEditorName, isLocked, // state
    fetchMyTrips, loadTrip, createNewTrip, addPlace, editItem, removePlace, deleteTrip, // basic actions
    startPolling, stopPolling, tryRequestEdit, finishEdit // polling actions
  }
})