<script setup>
import { onMounted, onUnmounted, ref, defineExpose, defineEmits } from 'vue'

const mapContainer = ref(null)
let mapInstance = null
let markers = [] 
let activeInfoWindow = null 
let currentPlaces = [] // ★ 현재 지도에 뿌려진 데이터들을 기억할 변수

// 부모에게 보낼 이벤트 정의 ('add-to-plan' 추가됨)
const emit = defineEmits(['marker-clicked', 'add-to-plan'])

// ★ [핵심 1] 브라우저(Window)에서 보내는 신호를 감지하는 리스너 등록
const handleAddPlaceEvent = (event) => {
  const poiId = event.detail
  // ID로 해당 장소 객체를 찾음
  const place = currentPlaces.find(p => p.poiId === poiId)
  if (place) {
    emit('add-to-plan', place) // 부모에게 "이거 추가해!" 라고 전달
  }
}

onMounted(() => {
  // 이벤트 리스너 부착
  window.addEventListener('add-place-map', handleAddPlaceEvent)

  if (window.kakao && window.kakao.maps) {
    window.kakao.maps.load(() => initMap())
  } else {
    console.error("카카오맵 스크립트 로드 실패")
  }
})

// 컴포넌트 꺼질 때 리스너 제거 (메모리 누수 방지)
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
  
  // ★ 데이터를 백업해둠 (나중에 ID로 찾기 위해)
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
    
    // ★ [핵심 2] onclick 부분 수정
    // 버튼 클릭 시 'add-place-map'이라는 이름으로 이벤트를 날립니다.
    const content = `
      <div style="padding:10px; width:220px; background:white; border-radius:8px; box-shadow:0 2px 6px rgba(0,0,0,0.1); display:flex; flex-direction:column; gap:8px;">
        <div style="width:100%; height:120px; border-radius:6px; overflow:hidden; background:#f1f1f1;">
           <img src="${imageUrl}" alt="img" style="width:100%; height:100%; object-fit:cover;" onerror="this.onerror=null; this.src='https://placehold.co/150x100?text=No+Image';"/>
        </div>
        <div>
          <div style="font-weight:bold; font-size:14px; color:#333; margin-bottom:4px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${place.name}</div>
          <div style="font-size:11px; color:#888;">${place.address || '주소 정보 없음'}</div>
        </div>
        <div style="text-align:right;">
           <button 
             onclick="window.dispatchEvent(new CustomEvent('add-place-map', { detail: ${place.poiId} }))"
             style="background:#DE2E5F; color:white; border:none; border-radius:4px; padding:6px 12px; font-size:11px; font-weight:bold; cursor:pointer;"
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

    window.kakao.maps.event.addListener(marker, 'click', () => {
      if (activeInfoWindow) activeInfoWindow.close()
      infowindow.open(mapInstance, marker)
      activeInfoWindow = infowindow
      
      emit('marker-clicked', place)
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