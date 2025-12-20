<script setup>
  import { onMounted, onUnmounted, ref, defineExpose, defineEmits } from 'vue'
  import http from '@/api/http'
  
  const mapContainer = ref(null)
  let mapInstance = null
  let markers = []
  let activeInfoWindow = null
  let currentPlaces = []
  let debounceTimer = null
  
  // 동선(Polyline)들을 저장할 배열
  let polylines = [] 
  
  // ItineraryList와 동일한 색상 순서
  const routeColors = [
    '#DE2E5F', '#3B82F6', '#10B981', '#F59E0B', '#8B5CF6', 
    '#EF4444', '#06B6D4', '#D946EF', '#84CC16', '#6366F1'
  ]
  
  const currentCategory = ref(12)
  const categories = [
    { id: 12, name: '관광지', icon: '🏞️' },
    { id: 39, name: '맛집', icon: '🍚' },
    { id: 32, name: '숙소', icon: '🏨' },
    { id: 14, name: '문화', icon: '🎨' }
  ]
  
  const emit = defineEmits(['marker-clicked', 'add-to-plan', 'map-clicked'])
  
  // ★ [수정 1] ID 비교 시 문자열로 변환하여 안전하게 비교
  const handleAddPlaceEvent = (event) => {
    const poiId = event.detail
    const place = currentPlaces.find(p => String(p.poiId) === String(poiId))
    if (place) {
      emit('add-to-plan', place)
    }
  }
  
  onMounted(() => {
    window.addEventListener('add-place-map', handleAddPlaceEvent)
  
    if (window.kakao && window.kakao.maps) {
      window.kakao.maps.load(() => initMap())
    }
  })
  
  onUnmounted(() => {
    window.removeEventListener('add-place-map', handleAddPlaceEvent)
  })
  
  const initMap = () => {
    const container = mapContainer.value
    const options = {
      center: new window.kakao.maps.LatLng(37.5665, 126.9780),
      level: 3 
    }
    mapInstance = new window.kakao.maps.Map(container, options)
  
    window.kakao.maps.event.addListener(mapInstance, 'dragend', onMapEvent)
    window.kakao.maps.event.addListener(mapInstance, 'zoom_changed', onMapEvent)
    window.kakao.maps.event.addListener(mapInstance, 'click', () => {
      emit('map-clicked')
    })
  
    onMapEvent()
  }
  
  const changeCategory = (id) => {
    currentCategory.value = id
    fetchMarkersInBounds()
  }
  
  const onMapEvent = () => {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      fetchMarkersInBounds()
    }, 500)
  }
  
  const fetchMarkersInBounds = async () => {
    if (!mapInstance) return
  
    const level = mapInstance.getLevel()
    if (level > 9) { 
      clearMarkers()
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
      setMarkers(res.data)
    } catch (e) {
      console.error("영역 검색 실패", e)
    }
  }
  
  const clearMarkers = () => {
    if (markers.length > 0) {
      markers.forEach(marker => marker.setMap(null))
      markers = []
    }
  }
  
  const moveCamera = (lat, lng) => {
    if (!mapInstance) return
    const moveLatLon = new window.kakao.maps.LatLng(lat, lng)
    mapInstance.panTo(moveLatLon)
    mapInstance.setLevel(4)
    setTimeout(() => {
      fetchMarkersInBounds()
    }, 500)
  }
  
  const setMarkers = (places) => {
    if (!mapInstance) return
    
    currentPlaces = places
    clearMarkers()
    
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
  
      const imageUrl = place.thumbnailUrl || 'https://via.placeholder.com/150x100?text=No+Image';
      
      const content = `
        <div style="padding:10px; width:240px; background:white; border-radius:8px; box-shadow:0 2px 6px rgba(0,0,0,0.1); display:flex; flex-direction:column; gap:8px;">
          <div style="width:100%; height:120px; border-radius:6px; overflow:hidden; background:#f1f1f1;">
             <img src="${imageUrl}" alt="img" style="width:100%; height:100%; object-fit:cover;" onerror="this.onerror=null; this.src='https://placehold.co/150x100?text=No+Image';"/>
          </div>
          <div>
            <div style="font-weight:bold; font-size:15px; color:#333; margin-bottom:4px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
              ${place.name}
            </div>
            <p id="desc-${place.poiId}" style="font-size:12px; color:#666; margin-bottom:6px; line-height:1.4; min-height:20px; word-break: keep-all;">
               ${place.description ? place.description : '✨ AI 분석 대기중...'}
            </p>
            <div style="font-size:11px; color:#999;">${place.address || '주소 정보 없음'}</div>
          </div>
          <div style="text-align:right;">
             <button 
               onclick="window.dispatchEvent(new CustomEvent('add-place-map', { detail: ${place.poiId} }))"
               style="background:#DE2E5F; color:white; border:none; border-radius:6px; padding:6px 12px; font-size:11px; font-weight:bold; cursor:pointer;"
             >
               + 일정에 추가
             </button>
          </div>
        </div>
      `
  
      const infowindow = new window.kakao.maps.InfoWindow({
        content: content,
        removable: true,
        zIndex: 10
      })
  
      window.kakao.maps.event.addListener(marker, 'click', async () => {
        if (activeInfoWindow) activeInfoWindow.close()
        infowindow.open(mapInstance, marker)
        activeInfoWindow = infowindow
        
        emit('marker-clicked', place)
  
        const descEl = document.getElementById(`desc-${place.poiId}`)
        if (place.description) {
          if (descEl) descEl.innerHTML = place.description.replace(/\n/g, '<br>')
        } else {
          if (descEl) descEl.innerHTML = `<span style="color:#DE2E5F; font-weight:bold;">✨ AI가 핫플레이스를 분석 중입니다...</span>`
          try {
            const res = await http.get(`/attractions/${place.poiId}/description`)
            const aiText = res.data
            place.description = aiText
            if (descEl) descEl.innerHTML = aiText.replace(/\n/g, '<br>')
          } catch (e) {
            if (descEl) descEl.innerText = "설명을 불러올 수 없습니다."
          }
        }
      })
  
      markers.push(marker)
    })
  }
  
  // ★ [수정 2] 날짜별 동선 그리기 (변수명 & 좌표 처리 강화)
  const drawRoute = (itinerary) => {
    if (!mapInstance) return
  
    // 기존 선 지우기
    if (polylines.length > 0) {
      polylines.forEach(line => line.setMap(null))
      polylines = []
    }
  
    if (!itinerary || itinerary.length === 0) return
  
    itinerary.forEach((day, index) => {
      // 1. ItineraryList에서 사용하는 'items' 변수명을 우선 체크
      const items = day.items || day.places
      if (!items || items.length < 2) return
  
      const linePath = items.map(p => {
        // 2. DB(DTO)에서 오는 placeLat/Lng 와 일반 lat/lng 모두 체크
        const lat = p.placeLat || p.latitude || p.lat
        const lng = p.placeLng || p.longitude || p.lng
        
        // 좌표가 없으면 null 반환 (filter로 걸러냄)
        if (!lat || !lng) return null
        
        return new window.kakao.maps.LatLng(lat, lng)
      }).filter(p => p !== null) // 유효하지 않은 좌표 제거
  
      // 점이 2개 이상이어야 선을 그을 수 있음
      if (linePath.length < 2) return
  
      const color = routeColors[index % routeColors.length]
  
      const polyline = new window.kakao.maps.Polyline({
        path: linePath,
        strokeWeight: 6,
        strokeColor: color, // 날짜별 고유 색상
        strokeOpacity: 0.8,
        strokeStyle: 'solid'
      })
  
      polyline.setMap(mapInstance)
      polylines.push(polyline)
    })
  }
  
  defineExpose({ moveCamera, setMarkers, drawRoute })
  </script>
  
  <template>
    <div class="h-full w-full rounded-3xl overflow-hidden relative shadow-inner bg-gray-100">
      <div ref="mapContainer" class="w-full h-full"></div>
      <div class="absolute top-6 left-6 z-20 flex gap-2">
        <button 
          v-for="cat in categories" 
          :key="cat.id"
          @click="changeCategory(cat.id)"
          class="px-3 py-2 rounded-full text-xs font-bold shadow-md transition-all flex items-center gap-1 border cursor-pointer active:scale-95"
          :class="currentCategory === cat.id 
            ? 'bg-[#DE2E5F] text-white border-[#DE2E5F] ring-2 ring-pink-200' 
            : 'bg-white text-gray-700 border-gray-200 hover:bg-gray-50'"
        >
          <span>{{ cat.icon }}</span>
          <span>{{ cat.name }}</span>
        </button>
      </div>
      <div class="absolute bottom-6 right-6 bg-white/90 backdrop-blur px-4 py-2 rounded-full text-xs font-bold text-gray-600 shadow-md z-10 border border-gray-200">
        Kakao Map API
      </div>
    </div>
  </template>