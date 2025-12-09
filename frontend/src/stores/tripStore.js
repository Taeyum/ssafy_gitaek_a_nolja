import { ref } from 'vue'
import { defineStore } from 'pinia'
import { createTripApi, getMyTripsApi, deleteTripApi } from '@/api/trip'

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

  // ★ [수정됨] 여행 불러오기 (날짜 계산 로직 적용)
  const loadTrip = (trip) => {
    // 1. 기본 정보 세팅
    tripInfo.value = {
      tripId: trip.tripId,
      title: trip.title,
      startDate: trip.startDate,
      endDate: trip.endDate,
      style: trip.style,
      maxMembers: trip.maxParticipants,
      // ★ [추가] 백엔드에서 가져온 인원 수 저장!
      currentParticipants: trip.currentParticipants || 1,
      currentMembers: [], // 나중에 멤버 조회 API 필요
      inviteCode: 'LOADED'
    }

    // 2. 날짜 차이 계산 및 일정표 초기화
    const start = new Date(trip.startDate)
    const end = new Date(trip.endDate)

    // 시간 차이(밀리초) 계산
    const diffTime = Math.abs(end - start)
    // 일(Day) 단위로 변환
    const dayCount = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1

    const newItinerary = []
    
    for (let i = 0; i < dayCount; i++) {
        // 각 일차의 실제 날짜 계산 (시작일 + i일)
        const currentDate = new Date(start)
        currentDate.setDate(start.getDate() + i)
        
        // 날짜를 "YYYY-MM-DD" 문자열로 변환
        const dateString = currentDate.toISOString().split('T')[0]

        newItinerary.push({ 
            id: (i + 1).toString(), 
            day: `${i + 1}일차`,    
            date: dateString,       
            items: []               // 나중엔 여기에 DB 데이터를 조회해서 넣어야 함
        })
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

      // 생성 시에는 duration 기준으로 일정표 생성
      const newItinerary = []
      for (let i = 1; i <= info.duration; i++) {
        newItinerary.push({
          id: i.toString(),
          day: `${i}일차`,
          date: `Day ${i}`, // 생성 직후에는 날짜 계산 생략 (또는 위 loadTrip 로직 복사 가능)
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

  // 장소 추가
  const addPlace = (dayId, place, time) => {
    const targetDay = itinerary.value.find(d => d.id === dayId)
    if (targetDay) {
      targetDay.items.push({
        id: Date.now().toString(),
        time: time ? time : "12:00",
        name: place.name,
        location: place.address || "주소 정보 없음",
        lat: place.lat || 0,
        lng: place.lng || 0
      })
      targetDay.items.sort((a, b) => a.time.localeCompare(b.time))
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
  const removePlace = (dayId, itemId) => {
    const targetDay = itinerary.value.find(d => d.id === dayId)
    if (targetDay) {
      targetDay.items = targetDay.items.filter(item => item.id !== itemId)
    }
  }

  // ★ [추가] 여행 삭제 액션
  const deleteTrip = async (tripId) => {
    try {
      await deleteTripApi(tripId)
      // 삭제 성공하면 목록 다시 불러오기
      await fetchMyTrips() 
      return true
    } catch (error) {
      // 서버에서 보낸 에러 메시지 표시 (권한 없음 등)
      alert(error.response?.data || "삭제 실패")
      return false
    }
  }

  return { 
    tripInfo, itinerary, myTrips, 
    fetchMyTrips, loadTrip, createNewTrip, 
    addPlace, editItem, removePlace ,
    deleteTrip
  }
})