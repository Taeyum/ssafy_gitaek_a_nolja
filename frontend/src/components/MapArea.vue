<script setup>
  import { onMounted, onUnmounted, ref, defineExpose, defineEmits } from 'vue'
  import http from '@/api/http' // ★ API 호출을 위해 추가
  
  const mapContainer = ref(null)
  let mapInstance = null
  let markers = [] 
  let activeInfoWindow = null 
  let currentPlaces = [] 
  
  // 부모에게 보낼 이벤트 정의
  const emit = defineEmits(['marker-clicked', 'add-to-plan'])
  
  // 브라우저(Window)에서 보내는 신호를 감지하는 리스너
  const handleAddPlaceEvent = (event) => {
    const poiId = event.detail
    const place = currentPlaces.find(p => p.poiId === poiId)
    if (place) {
      emit('add-to-plan', place) 
    }
  }
  
  onMounted(() => {
    window.addEventListener('add-place-map', handleAddPlaceEvent)
  
    if (window.kakao && window.kakao.maps) {
      window.kakao.maps.load(() => initMap())
    } else {
      console.error("카카오맵 스크립트 로드 실패")
    }
  })
  
  onUnmounted(() => {
    window.removeEventListener('add-place-map', handleAddPlaceEvent)
  })
  
  const initMap = () => {
    const container = mapContainer.value
    const options = {
      center: new window.kakao.maps.LatLng(37.5665, 126.9780),
      level: 10
    }
    mapInstance = new window.kakao.maps.Map(container, options)
  }
  
  const moveCamera = (lat, lng) => {
    if (!mapInstance) return
    const moveLatLon = new window.kakao.maps.LatLng(lat, lng)
    mapInstance.panTo(moveLatLon)
    mapInstance.setLevel(5)
  }
  
  const setMarkers = (places) => {
    if (!mapInstance) return
    
    currentPlaces = places
  
    // 기존 마커 초기화
    if (markers.length > 0) {
      markers.forEach(marker => marker.setMap(null))
      markers = []
    }
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
      
      // ★ 인포윈도우 HTML 구성
      // 설명 부분(p 태그)에 id를 부여해서 나중에 내용을 바꿔치기 할 수 있게 만듦
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
  
      // ★ 마커 클릭 이벤트 (AI 연동 핵심)
      window.kakao.maps.event.addListener(marker, 'click', async () => {
        // 1. 기존 창 닫고 새 창 열기
        if (activeInfoWindow) activeInfoWindow.close()
        infowindow.open(mapInstance, marker)
        activeInfoWindow = infowindow
        
        emit('marker-clicked', place)
  
        // 2. 설명란 DOM 요소 가져오기
        const descEl = document.getElementById(`desc-${place.poiId}`)
  
        // 3. 데이터가 있으면 바로 보여줌
        if (place.description) {
          if (descEl) descEl.innerHTML = place.description.replace(/\n/g, '<br>')
        } 
        // 4. 데이터가 없으면 AI 로딩 시작
        else {
          if (descEl) descEl.innerHTML = `<span style="color:#DE2E5F; font-weight:bold;">✨ AI가 핫플레이스를 분석 중입니다...</span>`
          
          try {
            // 백엔드(AI) 호출
            const res = await http.get(`/attractions/${place.poiId}/description`)
            const aiText = res.data
  
            // 받아온 데이터 캐싱 (다음 클릭 땐 바로 뜨게)
            place.description = aiText
            
            // 화면 업데이트
            if (descEl) descEl.innerHTML = aiText.replace(/\n/g, '<br>')
  
          } catch (e) {
            console.error(e)
            if (descEl) descEl.innerText = "설명을 불러올 수 없습니다."
          }
        }
      })
  
      markers.push(marker)
    })
  }
  
  defineExpose({
    moveCamera,
    setMarkers
  })
  </script>
  
  <template>
    <div class="h-full w-full rounded-3xl overflow-hidden relative shadow-inner bg-gray-100">
      <div ref="mapContainer" class="w-full h-full"></div>
      <div class="absolute bottom-6 right-6 bg-white/90 backdrop-blur px-4 py-2 rounded-full text-xs font-bold text-gray-600 shadow-md z-10 border border-gray-200">
        Kakao Map API
      </div>
    </div>
  </template>