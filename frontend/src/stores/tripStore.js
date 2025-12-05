import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useTripStore = defineStore('trip', () => {
  // 1. 상태 (State)
  const tripInfo = ref({
    title: '',      
    duration: 2,    
    maxMembers: 4,     // ★ [변경] 최대 인원 (목표)
    currentMembers: [], // ★ [추가] 실제 들어온 사람 리스트
    inviteCode: '',    // ★ [추가] 초대 코드
    style: 'friend'
  })

  const itinerary = ref([])

  // 2. 액션 (Action)
  const createNewTrip = (info) => {
    // 랜덤 초대 코드 생성 (가짜)
    const randomCode = Math.random().toString(36).substring(2, 8).toUpperCase()

    tripInfo.value = { 
      title: info.title,
      duration: info.duration,
      maxMembers: info.members, // 입력받은 숫자는 '최대 인원'이 됨
      style: info.style,
      inviteCode: randomCode,
      // 방장은 무조건 포함
      currentMembers: [
        { id: 'me', name: '나(방장)', avatar: 'Me', color: 'bg-gray-200' }
      ]
    }

    // 일정표 생성 로직 (기존 유지)
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
  }

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

  const removePlace = (dayId, itemId) => {
    const targetDay = itinerary.value.find(d => d.id === dayId)
    if (targetDay) {
      targetDay.items = targetDay.items.filter(item => item.id !== itemId)
    }
  }

  return { tripInfo, itinerary, createNewTrip, addPlace, editItem, removePlace }
})