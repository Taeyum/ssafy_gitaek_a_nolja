<script setup>
import { onMounted, ref, defineExpose, defineEmits } from 'vue'

const mapContainer = ref(null)
let mapInstance = null
let markers = [] 
let activeInfoWindow = null 

const emit = defineEmits(['marker-clicked'])

onMounted(() => {
  if (window.kakao && window.kakao.maps) {
    window.kakao.maps.load(() => {
      initMap()
    })
  } else {
    console.error("카카오맵 스크립트가 로드되지 않았습니다.")
  }
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

    // ★ [수정됨] 인포윈도우 내용 (이미지 추가)
    // 이미지가 없으면 기본 이미지(No Image)를 보여주도록 처리
    const imageUrl = place.thumbnailUrl || 'https://via.placeholder.com/150x100?text=No+Image';
    
    const content = `
      <div style="padding:10px; width:220px; background:white; border-radius:8px; box-shadow:0 2px 6px rgba(0,0,0,0.1); display:flex; flex-direction:column; gap:8px;">
        <div style="width:100%; height:120px; border-radius:6px; overflow:hidden; background:#f1f1f1;">
           <img src="${imageUrl}" alt="img" style="width:100%; height:100%; object-fit:cover;" onerror="this.src='https://via.placeholder.com/150x100?text=Error'"/>
        </div>
        <div>
          <div style="font-weight:bold; font-size:14px; color:#333; margin-bottom:4px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${place.name}</div>
          <div style="font-size:11px; color:#888;">${place.address || '주소 정보 없음'}</div>
        </div>
        <div style="text-align:right;">
           <button style="background:#DE2E5F; color:white; border:none; border-radius:4px; padding:4px 8px; font-size:11px; cursor:pointer;" onclick="window.dispatchEvent(new CustomEvent('add-place', { detail: ${place.poiId} }))">
             + 담기
           </button>
        </div>
      </div>
    `

    const infowindow = new window.kakao.maps.InfoWindow({
      content: content,
      removable: true,
      zIndex: 10 // 다른 마커보다 위에 뜨도록
    })

    window.kakao.maps.event.addListener(marker, 'click', () => {
      if (activeInfoWindow) {
        activeInfoWindow.close()
      }
      infowindow.open(mapInstance, marker)
      activeInfoWindow = infowindow
      
      // (선택) 부모에게 알림
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