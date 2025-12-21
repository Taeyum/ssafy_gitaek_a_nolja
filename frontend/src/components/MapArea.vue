<script setup>
import { onMounted, onUnmounted, ref, defineExpose, defineEmits } from 'vue'
import http from '@/api/http'

// ==========================================
// 📍 상태 및 변수 정의
// ==========================================
const mapContainer = ref(null)
let mapInstance = null

// 마커 및 오버레이 관리
let searchMarkers = []      
let currentPlaces = []      
let activeInfoWindow = null 

// 경로(Polyline) 및 오버레이 관리
let polylines = []
let distanceOverlays = []
let markerOverlays = []

// 성능 및 상태 관리
let debounceTimer = null
const pathCache = new Map() 
let drawRequestId = 0

// UI 상태
const areMarkersVisible = ref(true) 
const currentCategory = ref(12)     

// 상수 데이터
const routeColors = [
  '#DE2E5F', '#3B82F6', '#10B981', '#F59E0B', '#8B5CF6',
  '#EF4444', '#06B6D4', '#D946EF', '#84CC16', '#6366F1'
]

const categories = [
  { id: 12, name: '관광지', icon: '🏞️' },
  { id: 39, name: '맛집', icon: '🍚' },
  { id: 32, name: '숙소', icon: '🏨' },
  { id: 14, name: '문화', icon: '🎨' }
]

const emit = defineEmits(['marker-clicked', 'add-to-plan', 'map-clicked'])

// ==========================================
// 🗺️ 지도 초기화 및 이벤트
// ==========================================
onMounted(() => {
  window.addEventListener('add-place-map', handleAddPlaceEvent)
  
  if (window.kakao && window.kakao.maps) {
    window.kakao.maps.load(() => initMap())
  }
})

onUnmounted(() => {
  window.removeEventListener('add-place-map', handleAddPlaceEvent)
})

const handleAddPlaceEvent = (event) => {
  const poiId = event.detail
  const place = currentPlaces.find(p => String(p.poiId) === String(poiId))
  if (place) emit('add-to-plan', place)
}

const initMap = () => {
  const container = mapContainer.value
  const options = {
    center: new window.kakao.maps.LatLng(33.450701, 126.570667), // 제주도 기본 좌표
    level: 9
  }
  mapInstance = new window.kakao.maps.Map(container, options)

  window.kakao.maps.event.addListener(mapInstance, 'dragend', onMapIdle)
  window.kakao.maps.event.addListener(mapInstance, 'zoom_changed', onMapIdle)
  window.kakao.maps.event.addListener(mapInstance, 'click', () => {
    if (activeInfoWindow) {
      activeInfoWindow.close()
      activeInfoWindow = null
    }
    emit('map-clicked')
  })

  onMapIdle()
}

// ==========================================
// 🔍 장소 마커 로직 (요청하신 AI 스타일 복구)
// ==========================================

const onMapIdle = () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    fetchMarkersInBounds()
  }, 500)
}

const fetchMarkersInBounds = async () => {
  if (!mapInstance || !areMarkersVisible.value) return 

  const level = mapInstance.getLevel()
  if (level > 10) {
    clearSearchMarkers()
    return
  }

  const bounds = mapInstance.getBounds()
  const sw = bounds.getSouthWest()
  const ne = bounds.getNorthEast()

  try {
    const res = await http.get('/attractions/bounds', {
      params: {
        minLat: sw.getLat(),
        maxLat: ne.getLat(),
        minLng: sw.getLng(),
        maxLng: ne.getLng(),
        contentTypeId: currentCategory.value
      }
    })
    renderMarkers(res.data)
  } catch (e) {
    console.error('마커 로딩 실패:', e)
  }
}

// ★ [디자인 개선] 깔끔하고 감각적인 인포윈도우 스타일
const renderMarkers = (places) => {
  clearSearchMarkers()
  currentPlaces = places

  if (activeInfoWindow) {
    activeInfoWindow.close()
    activeInfoWindow = null
  }

  places.forEach(place => {
    const lat = place.latitude || place.lat
    const lng = place.longitude || place.lng
    if (!lat || !lng) return

    const position = new window.kakao.maps.LatLng(lat, lng)
    const marker = new window.kakao.maps.Marker({
      map: mapInstance,
      position: position,
      title: place.name
    })

    const imageUrl = place.thumbnailUrl || 'https://via.placeholder.com/280x160?text=No+Image';

    // ============================================================
    // 🎨 [Design] 깔끔한 모던 카드 스타일 (Inline CSS)
    // ============================================================
    const content = `
      <div style="
          width: 280px; 
          background: #fff; 
          border-radius: 12px; 
          box-shadow: 0 8px 20px rgba(0,0,0,0.15); 
          overflow: hidden; 
          font-family: 'Pretendard', sans-serif;
          margin-bottom: 2px; /* 그림자 잘림 방지 */
      ">
        <div style="width: 100%; height: 160px; position: relative; background-color: #f3f4f6;">
           <img src="${imageUrl}" alt="img" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.src='https://via.placeholder.com/280x160?text=Trip';"/>
           
           <div style="
               position: absolute; bottom: 0; left: 0; right: 0;
               padding: 40px 16px 12px 16px;
               background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
               color: white;
           ">
              <div style="font-weight: 700; font-size: 18px; line-height: 1.2;">${place.name}</div>
              <div style="font-size: 12px; opacity: 0.9; margin-top: 4px; font-weight: 300;">📍 ${place.address || '주소 정보 없음'}</div>
           </div>
        </div>

        <div style="padding: 16px;">
           
           <div style="
               background-color: #F8F9FA; 
               border: 1px solid #E9ECEF;
               border-radius: 8px; 
               padding: 12px; 
               margin-bottom: 16px;
           ">
              <div style="
                  font-size: 11px; 
                  font-weight: 700; 
                  color: #DE2E5F; 
                  margin-bottom: 6px; 
                  display: flex; align-items: center; gap: 4px;
                  text-transform: uppercase; letter-spacing: 0.5px;
              ">
                  <span style="font-size: 14px;">✨</span> AI SUMMARY
              </div>
              
              <p id="desc-${place.poiId}" style="
                  font-size: 13px; 
                  color: #495057; 
                  line-height: 1.5; 
                  margin: 0; 
                  word-break: keep-all;
                  min-height: 20px;
              ">
                 ${place.description ? place.description : '<span style="color:#aaa;">AI가 장소를 분석하고 있어요...</span>'}
              </p>
           </div>

           <button
              onclick="window.dispatchEvent(new CustomEvent('add-place-map', { detail: ${place.poiId} }))"
              style="
                 width: 100%;
                 background-color: #DE2E5F; 
                 color: white; 
                 border: none; 
                 border-radius: 8px; 
                 padding: 12px; 
                 font-size: 14px; 
                 font-weight: 700; 
                 cursor: pointer;
                 transition: background 0.2s;
              "
              onmouseover="this.style.backgroundColor='#be123c'"
              onmouseout="this.style.backgroundColor='#DE2E5F'"
           >
             + 일정에 담기
           </button>
        </div>
      </div>
    `

    // 인포윈도우 생성 (removable: true -> X 버튼 생성됨)
    const infowindow = new window.kakao.maps.InfoWindow({
      content: content,
      removable: true,
      zIndex: 10
    })

    // 마커 클릭 이벤트
    window.kakao.maps.event.addListener(marker, 'click', async () => {
      if (activeInfoWindow) activeInfoWindow.close()
      
      // 클릭 시 지도가 살짝 위로 이동하여 팝업이 잘 보이게 함
      const moveLatLon = new window.kakao.maps.LatLng(lat + 0.003, lng) 
      mapInstance.panTo(moveLatLon)

      infowindow.open(mapInstance, marker)
      activeInfoWindow = infowindow
      
      emit('marker-clicked', place)

      // 텍스트 교체 로직 (기존과 동일)
      setTimeout(async () => {
          const descEl = document.getElementById(`desc-${place.poiId}`)
          if (!descEl) return;

          if (place.description) {
            descEl.innerHTML = place.description.replace(/\n/g, '<br>')
          } else {
            try {
              const res = await http.get(`/attractions/${place.poiId}/description`)
              const aiText = res.data
              place.description = aiText
              
              // 부드럽게 텍스트 변경
              descEl.style.opacity = 0;
              descEl.innerHTML = aiText.replace(/\n/g, '<br>')
              
              // 페이드인 효과
              let op = 0.1;
              let timer = setInterval(function () {
                  if (op >= 1){ clearInterval(timer); }
                  descEl.style.opacity = op;
                  op += op * 0.1;
              }, 10);

            } catch (e) {
              console.error(e)
              descEl.innerText = "설명을 불러올 수 없습니다."
            }
          }
      }, 100);
    })

    searchMarkers.push(marker)
  })
}

const clearSearchMarkers = () => {
  searchMarkers.forEach(marker => marker.setMap(null))
  searchMarkers = []
}

// 카테고리 변경
const changeCategory = (id) => {
  currentCategory.value = id
  areMarkersVisible.value = true 
  fetchMarkersInBounds()
}

// 마커 토글
const toggleMarkers = () => {
  areMarkersVisible.value = !areMarkersVisible.value
  
  if (areMarkersVisible.value) {
    if (searchMarkers.length === 0) fetchMarkersInBounds()
    else searchMarkers.forEach(m => m.setMap(mapInstance))
  } else {
    searchMarkers.forEach(m => m.setMap(null))
    if (activeInfoWindow) {
      activeInfoWindow.close()
      activeInfoWindow = null
    }
  }
}

// ==========================================
// 🎨 일정 경로(Polyline) 그리기 로직 (기존 유지)
// ==========================================
const drawRoute = async (itinerary) => {
  if (!mapInstance) return
  const currentId = ++drawRequestId

  if (!itinerary || itinerary.length === 0) {
    clearItineraryOverlays()
    return
  }

  const tempPolylines = []
  const tempDistOverlays = []
  const tempMarkerOverlays = []

  for (let [dayIndex, day] of itinerary.entries()) {
    if (currentId !== drawRequestId) return 

    const items = day.items || day.places
    if (!items || items.length === 0) continue
    const color = routeColors[dayIndex % routeColors.length]

    // 순서 번호 마커
    items.forEach((item, itemIndex) => {
       const lat = item.latitude || item.lat || item.placeLat
       const lng = item.longitude || item.lng || item.placeLng
       if (lat && lng) {
         const content = `<div style="width:24px;height:24px;background:${color};color:white;border-radius:50%;text-align:center;line-height:24px;font-weight:bold;font-size:12px;box-shadow:0 2px 4px rgba(0,0,0,0.3);border:2px solid white;">${itemIndex + 1}</div>`
         const overlay = new window.kakao.maps.CustomOverlay({
           position: new window.kakao.maps.LatLng(lat, lng),
           content: content,
           yAnchor: 1,
           zIndex: 20
         })
         tempMarkerOverlays.push(overlay)
       }
    })

    if (items.length < 2) continue

    // 경로 연결
    for (let i = 0; i < items.length - 1; i++) {
      const start = items[i]
      const end = items[i+1]
      const sLat = start.latitude || start.lat || start.placeLat
      const sLng = start.longitude || start.lng || start.placeLng
      const eLat = end.latitude || end.lat || end.placeLat
      const eLng = end.longitude || end.lng || end.placeLng

      if (!sLat || !eLat) continue

      const distMeters = getDistance(sLat, sLng, eLat, eLng)
      let linePath = null
      let durationSec = 0
      let realDistanceM = 0
      let isWalking = false

      if (distMeters > 2000) { 
        const result = await fetchCarPath(sLat, sLng, eLat, eLng)
        if (result) {
          linePath = result.path
          durationSec = result.duration
          realDistanceM = result.distance
        }
      } else {
        isWalking = true
        realDistanceM = distMeters
        durationSec = (realDistanceM / 67) * 60 
      }

      if (!linePath) {
        linePath = [new window.kakao.maps.LatLng(sLat, sLng), new window.kakao.maps.LatLng(eLat, eLng)]
      }

      const polyline = new window.kakao.maps.Polyline({
        path: linePath,
        strokeWeight: isWalking ? 5 : 7,
        strokeColor: color,
        strokeOpacity: 0.9,
        strokeStyle: isWalking ? 'shortdot' : 'solid',
        endArrow: true
      })
      
      addPolylineEvents(polyline, linePath, isWalking)
      tempPolylines.push(polyline)

      if (linePath.length > 0) {
        const midPoint = linePath[Math.floor(linePath.length / 2)]
        const minutes = Math.round(durationSec / 60)
        let distText = realDistanceM >= 1000 ? (realDistanceM / 1000).toFixed(1) + 'km' : Math.round(realDistanceM) + 'm'
        const icon = isWalking ? '🚶' : '🚗'
        const badgeContent = `<div style="padding:4px 10px;background:white;border:2px solid ${color};border-radius:20px;font-size:11px;font-weight:bold;color:#333;box-shadow:0 2px 4px rgba(0,0,0,0.2);white-space:nowrap;display:flex;align-items:center;gap:4px;"><span>${icon}</span><span>${minutes}분</span><span style="color:#999;font-size:10px;">(${distText})</span></div>`
        
        const badgeOverlay = new window.kakao.maps.CustomOverlay({
          position: midPoint, content: badgeContent, yAnchor: 1.5, zIndex: 15
        })
        tempDistOverlays.push(badgeOverlay)
      }
    }
  }

  if (currentId !== drawRequestId) return

  clearItineraryOverlays()
  
  tempPolylines.forEach(p => p.setMap(mapInstance))
  tempDistOverlays.forEach(o => o.setMap(mapInstance))
  tempMarkerOverlays.forEach(m => m.setMap(mapInstance))
  
  polylines = tempPolylines
  distanceOverlays = tempDistOverlays
  markerOverlays = tempMarkerOverlays
}

const addPolylineEvents = (polyline, linePath, isWalking) => {
  window.kakao.maps.event.addListener(polyline, 'mouseover', () => {
    polyline.setOptions({ strokeWeight: 10, strokeOpacity: 1.0, zIndex: 100 })
  })
  window.kakao.maps.event.addListener(polyline, 'mouseout', () => {
    polyline.setOptions({ strokeWeight: isWalking ? 5 : 7, strokeOpacity: 0.9, zIndex: 0 })
  })
  window.kakao.maps.event.addListener(polyline, 'click', () => {
    const bounds = new window.kakao.maps.LatLngBounds()
    linePath.forEach(point => bounds.extend(point))
    mapInstance.setBounds(bounds, 100) 
  })
}

const clearItineraryOverlays = () => {
  polylines.forEach(p => p.setMap(null)); polylines = []
  distanceOverlays.forEach(o => o.setMap(null)); distanceOverlays = []
  markerOverlays.forEach(m => m.setMap(null)); markerOverlays = []
}

// ==========================================
// 📏 유틸리티
// ==========================================
const getDistance = (lat1, lng1, lat2, lng2) => {
    const R = 6371
    const dLat = (lat2 - lat1) * Math.PI / 180
    const dLng = (lng2 - lng1) * Math.PI / 180
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLng/2) * Math.sin(dLng/2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    return R * c * 1000
}

const fetchCarPath = async (startLat, startLng, endLat, endLng) => {
  const cacheKey = `${startLat},${startLng}-${endLat},${endLng}`
  if (pathCache.has(cacheKey)) return pathCache.get(cacheKey)

  try {
    const start = `${startLng},${startLat}`
    const end = `${endLng},${endLat}`
    const res = await http.get('/attractions/path', { params: { start, end } })
    const routes = res.data.routes[0]
    
    let path = []
    routes.sections.forEach(section => {
      section.roads.forEach(road => {
        const vertexes = road.vertexes 
        for (let i = 0; i < vertexes.length; i += 2) {
          path.push(new window.kakao.maps.LatLng(vertexes[i+1], vertexes[i]))
        }
      })
    })

    const result = {
      path: path,
      duration: routes.summary.duration,
      distance: routes.summary.distance
    }
    pathCache.set(cacheKey, result)
    return result
  } catch (e) {
    return null
  }
}

const moveCamera = (lat, lng) => {
  if (!mapInstance) return
  const moveLatLon = new window.kakao.maps.LatLng(lat, lng)
  mapInstance.panTo(moveLatLon)
}

defineExpose({ moveCamera, setMarkers: renderMarkers, drawRoute })
</script>

<template>
  <div class="h-full w-full rounded-3xl overflow-hidden relative shadow-inner bg-gray-100">
    <div ref="mapContainer" class="w-full h-full"></div>
    
    <div class="absolute top-6 left-6 z-20 flex gap-2 flex-wrap max-w-[90%]">
      <button 
        @click="toggleMarkers"
        class="px-3 py-2 rounded-full text-xs font-bold shadow-md transition-all flex items-center gap-1 border cursor-pointer active:scale-95"
        :class="areMarkersVisible 
          ? 'bg-gray-800 text-white border-gray-800 hover:bg-gray-700' 
          : 'bg-white text-gray-500 border-gray-300 hover:bg-gray-50'"
      >
        <span>{{ areMarkersVisible ? '📍' : '✖️' }}</span>
        <span>{{ areMarkersVisible ? '마커 ON' : '마커 OFF' }}</span>
      </button>

      <button v-for="cat in categories" :key="cat.id" @click="changeCategory(cat.id)"
        class="px-3 py-2 rounded-full text-xs font-bold shadow-md transition-all flex items-center gap-1 border cursor-pointer active:scale-95"
        :class="currentCategory === cat.id 
          ? 'bg-[#DE2E5F] text-white border-[#DE2E5F]' 
          : 'bg-white text-gray-700 border-gray-200 hover:bg-gray-50'"
      >
        <span>{{ cat.icon }}</span><span>{{ cat.name }}</span>
      </button>
    </div>
    
    <div class="absolute bottom-6 right-6 bg-white/90 backdrop-blur px-4 py-2 rounded-full text-xs font-bold text-gray-600 shadow-md z-10 border border-gray-200">
      Kakao Mobility & Maps
    </div>
  </div>
</template>