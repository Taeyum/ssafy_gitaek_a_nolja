<script setup>
import { onMounted, ref, defineExpose } from 'vue'

const mapContainer = ref(null)
// 지도 객체를 담을 변수 (함수들끼리 공유하기 위해 밖으로 뺌)
let mapInstance = null

onMounted(() => {
  // 카카오 스크립트가 로드되었는지 확인
  if (window.kakao && window.kakao.maps) {
    initMap()
  } else {
    // 스크립트가 아직 안 불러와졌으면 0.1초 뒤에 다시 시도 (안전장치)
    const script = document.createElement('script')
    script.onload = () => kakao.maps.load(initMap)
    script.src = '//dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=9d4d9f67c91e42b726115bd0e1bbcec8'
    document.head.appendChild(script)
  }
})

const initMap = () => {
  const container = mapContainer.value
  const options = {
    center: new window.kakao.maps.LatLng(33.450701, 126.570667), // 초기값 (제주)
    level: 3
  }
  mapInstance = new window.kakao.maps.Map(container, options)
}

// ▼▼▼ 부모 컴포넌트가 호출할 함수 (카메라 이동) ▼▼▼
const moveCamera = (lat, lng) => {
  if (!mapInstance) return
  
  // 카카오맵의 '부드러운 이동(panTo)' 기능 사용
  const moveLatLon = new window.kakao.maps.LatLng(lat, lng)
  mapInstance.panTo(moveLatLon)
  
  // 이동한 곳에 마커 하나 찍어주기 (센스!)
  new window.kakao.maps.Marker({
    position: moveLatLon,
    map: mapInstance
  })
}

// 밖에서 이 함수를 쓸 수 있게 노출
defineExpose({
  moveCamera
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