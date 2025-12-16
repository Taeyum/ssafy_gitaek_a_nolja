import http from './http'

// 여행 생성 API 호출
export const createTripApi = (tripData) => {
  // tripData = { title, duration, maxMembers, style, startDate }
  return http.post('/trips', tripData)
}

export const getMyTripsApi = () => {
  return http.get('/trips/my')
}

// 여행 삭제 API
export const deleteTripApi = (tripId) => {
  return http.delete(`/trips/${tripId}`)
}

// 일정 추가 API
export const addScheduleApi = (scheduleData) => {
  return http.post('/trips/schedule', scheduleData)
}
// 조회 API
export const getSchedulesApi = (tripId) => {
  return http.get(`/trips/${tripId}/schedules`)
}

export const deleteScheduleApi = (tripId, tripDay, poiId) => {
  // DELETE 요청에 파라미터를 보낼 때는 params 옵션 사용
  return http.delete(`/trips/${tripId}/schedules`, {
    params: { tripDay, poiId }
  })
}

// 권한 요청
export const requestEditApi = (tripId) => http.post(`/trips/${tripId}/edit/request`)
// 권한 해제
export const releaseEditApi = (tripId) => http.post(`/trips/${tripId}/edit/release`)
// 여행 상태 조회 (누가 에디터인지 확인용 - 기존 getTripApi 재활용 가능하거나 새로 생성)
export const getTripStatusApi = (tripId) => http.get(`/trips/${tripId}`)

export const joinTripApi = (inviteCode) => {
  // 백엔드 Controller가 @RequestBody Map<String, String>을 받으므로 객체 형태로 보냅니다.
  return http.post('/trips/join', { inviteCode })
}

// [추가] 여행 나가기
export const leaveTripApi = (tripId) => http.post(`/trips/${tripId}/leave`)

// [추가] 채팅 관련 API
export const getMessagesApi = (tripId) => http.get(`/chat/${tripId}`)
export const sendMessageApi = (tripId, content) => http.post(`/chat/${tripId}`, { content })