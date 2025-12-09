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