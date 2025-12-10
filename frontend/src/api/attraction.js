// src/api/attraction.js
import http from './http'

// 관광지 목록 조회
// param 예시: { areaCode: 1, contentTypeId: 12 }
export const getAttractionsApi = (param) => {
  return http.get('/attractions', { params: param })
}