<script setup>
  import { onMounted, onUnmounted, ref, defineExpose, defineEmits } from 'vue'
  import http from '@/api/http'
  
  const mapContainer = ref(null)
  let mapInstance = null
  
  // ==========================================
  // 📍 상태 변수들
  // ==========================================
  
  // 1. 검색 결과(맛집/숙소 등) 마커 관리
  let searchMarkers = []
  const areMarkersVisible = ref(true) // ★ [신규] 마커 표시 여부 토글 상태
  let activeInfoWindow = null
  let currentPlaces = []
  let debounceTimer = null
  
  // 2. 일정 경로(선, 오버레이) 관리
  let polylines = []          
  let distanceOverlays = []   
  let markerOverlays = []     
  
  // 3. 성능 최적화
  const pathCache = new Map(); 
  let drawRequestId = 0;       
  
  // 4. 카테고리 및 색상
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
  
  // ==========================================
  // 🗺️ 지도 초기화
  // ==========================================
  
  const handleAddPlaceEvent = (event) => {
    const poiId = event.detail
    const place = currentPlaces.find(p => String(p.poiId) === String(poiId))
    if (place) emit('add-to-plan', place)
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
      center: new window.kakao.maps.LatLng(33.450701, 126.570667), 
      level: 9 
    }
    mapInstance = new window.kakao.maps.Map(container, options)
  
    window.kakao.maps.event.addListener(mapInstance, 'dragend', onMapEvent)
    window.kakao.maps.event.addListener(mapInstance, 'zoom_changed', onMapEvent)
    window.kakao.maps.event.addListener(mapInstance, 'click', () => {
      emit('map-clicked')
    })
  
    onMapEvent()
  }
  
  // ==========================================
  // 📏 유틸리티 & API (거리 계산, 길찾기)
  // ==========================================
  const getDistance = (lat1, lng1, lat2, lng2) => {
      const R = 6371; 
      const dLat = (lat2 - lat1) * Math.PI / 180;
      const dLng = (lng2 - lng1) * Math.PI / 180;
      const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                Math.sin(dLng/2) * Math.sin(dLng/2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      return R * c * 1000; 
  }
  
  const fetchCarPath = async (startLat, startLng, endLat, endLng) => {
    const cacheKey = `${startLat},${startLng}-${endLat},${endLng}`;
    if (pathCache.has(cacheKey)) return pathCache.get(cacheKey);
  
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
      pathCache.set(cacheKey, result);
      return result
    } catch (e) {
      return null
    }
  }
  
  // ==========================================
  // 🎨 일정 그리기 로직 (경로 + 마커 + 오버레이)
  // ==========================================
  const drawRoute = async (itinerary) => {
    if (!mapInstance) return
    const currentId = ++drawRequestId;
  
    if (!itinerary || itinerary.length === 0) {
      clearItineraryOverlays();
      return;
    }
  
    const tempPolylines = [];
    const tempDistOverlays = [];
    const tempMarkerOverlays = [];
  
    for (let [dayIndex, day] of itinerary.entries()) {
      if (currentId !== drawRequestId) return;
  
      const items = day.items || day.places
      if (!items || items.length === 0) continue
      const color = routeColors[dayIndex % routeColors.length]
  
      // 1. 번호 마커
      items.forEach((item, itemIndex) => {
         const lat = item.latitude || item.lat || item.placeLat
         const lng = item.longitude || item.lng || item.placeLng
         if (lat && lng) {
           const content = `<div style="width:24px;height:24px;background:${color};color:white;border-radius:50%;text-align:center;line-height:24px;font-weight:bold;font-size:12px;box-shadow:0 2px 4px rgba(0,0,0,0.3);border:2px solid white;cursor:pointer;">${itemIndex + 1}</div>`;
           const overlay = new window.kakao.maps.CustomOverlay({
              position: new window.kakao.maps.LatLng(lat, lng),
              content: content,
              yAnchor: 1,
              zIndex: 20
           });
           tempMarkerOverlays.push(overlay);
         }
      });
  
      if (items.length < 2) continue;
  
      // 2. 경로 및 말풍선
      for (let i = 0; i < items.length - 1; i++) {
        const startNode = items[i]; const endNode = items[i+1];
        const sLat = startNode.latitude || startNode.lat || startNode.placeLat
        const sLng = startNode.longitude || startNode.lng || startNode.placeLng
        const eLat = endNode.latitude || endNode.lat || endNode.placeLat
        const eLng = endNode.longitude || endNode.lng || endNode.placeLng
  
        if (!sLat || !eLat) continue
  
        const distMeters = getDistance(sLat, sLng, eLat, eLng)
        let linePath = null
        let durationSec = 0
        let realDistanceM = 0
        let isWalking = false
  
        if (distMeters > 2000) { 
          const result = await fetchCarPath(sLat, sLng, eLat, eLng)
          if (result) {
            linePath = result.path; durationSec = result.duration; realDistanceM = result.distance
          }
        } else {
          isWalking = true; realDistanceM = distMeters; durationSec = (realDistanceM / 67) * 60
        }
  
        if (!linePath) linePath = [new window.kakao.maps.LatLng(sLat, sLng), new window.kakao.maps.LatLng(eLat, eLng)]
  
        const polyline = new window.kakao.maps.Polyline({
          path: linePath, strokeWeight: isWalking ? 5 : 7, strokeColor: color, strokeOpacity: 0.9,
          strokeStyle: isWalking ? 'shortdot' : 'solid', endArrow: true
        })
        
        addPolylineEvents(polyline, linePath, isWalking);
        tempPolylines.push(polyline);
  
        if (linePath.length > 0) {
          const midPoint = linePath[Math.floor(linePath.length / 2)];
          const minutes = Math.round(durationSec / 60);
          let distText = realDistanceM >= 1000 ? (realDistanceM / 1000).toFixed(1) + 'km' : Math.round(realDistanceM) + 'm';
          const icon = isWalking ? '🚶' : '🚗';
          const badgeContent = `<div style="padding:4px 10px;background:white;border:2px solid ${color};border-radius:20px;font-size:11px;font-weight:bold;color:#333;box-shadow:0 2px 4px rgba(0,0,0,0.2);white-space:nowrap;display:flex;align-items:center;gap:4px;"><span>${icon}</span><span>${minutes}분</span><span style="color:#999;font-size:10px;">(${distText})</span></div>`;
          
          const badgeOverlay = new window.kakao.maps.CustomOverlay({
            position: midPoint, content: badgeContent, yAnchor: 1.5, zIndex: 15
          });
          tempDistOverlays.push(badgeOverlay);
        }
      }
    }
  
    if (currentId !== drawRequestId) return;
    clearItineraryOverlays();
    
    tempPolylines.forEach(p => p.setMap(mapInstance));
    tempDistOverlays.forEach(o => o.setMap(mapInstance));
    tempMarkerOverlays.forEach(m => m.setMap(mapInstance));
    
    polylines = tempPolylines;
    distanceOverlays = tempDistOverlays;
    markerOverlays = tempMarkerOverlays;
  }
  
  const addPolylineEvents = (polyline, linePath, isWalking) => {
    window.kakao.maps.event.addListener(polyline, 'mouseover', () => polyline.setOptions({ strokeWeight: 10, strokeOpacity: 1.0 }));
    window.kakao.maps.event.addListener(polyline, 'mouseout', () => polyline.setOptions({ strokeWeight: isWalking ? 5 : 7, strokeOpacity: 0.9 }));
    window.kakao.maps.event.addListener(polyline, 'click', () => {
        // 집중 모드: 마커 숨기기 (토글 상태도 꺼버림)
        areMarkersVisible.value = false;
        toggleMarkers(false); // 강제로 숨김 처리
  
        const bounds = new window.kakao.maps.LatLngBounds();
        linePath.forEach(point => bounds.extend(point));
        mapInstance.setBounds(bounds, 50);
        console.log("🚀 경로 집중 모드 활성화");
    });
  }
  
  const clearItineraryOverlays = () => {
    polylines.forEach(p => p.setMap(null)); polylines = [];
    distanceOverlays.forEach(o => o.setMap(null)); distanceOverlays = [];
    markerOverlays.forEach(m => m.setMap(null)); markerOverlays = [];
  }
  
  // ==========================================
  // 🔍 검색 결과 마커 관리 (ON/OFF 기능 포함)
  // ==========================================
  
  // ★ [신규] 마커 토글 함수
  const toggleMarkers = (forceState = null) => {
    if (!mapInstance) return;
  
    // forceState가 있으면 그 상태로, 없으면 토글
    if (forceState !== null) {
      areMarkersVisible.value = forceState;
    } else {
      areMarkersVisible.value = !areMarkersVisible.value;
    }
  
    if (areMarkersVisible.value) {
      // 켜기: 현재 데이터를 다시 지도에 그림
      if (searchMarkers.length === 0) {
        fetchMarkersInBounds(); // 데이터가 없으면 새로 가져옴
      } else {
        searchMarkers.forEach(m => m.setMap(mapInstance));
      }
    } else {
      // 끄기: 지도에서만 제거 (데이터는 유지)
      searchMarkers.forEach(m => m.setMap(null));
    }
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
      clearSearchMarkers()
      return
    }
    const bounds = mapInstance.getBounds()
    const sw = bounds.getSouthWest()
    const ne = bounds.getNorthEast()
  
    try {
      const res = await http.get('/attractions/bounds', {
        params: {
          minLat: sw.getLat(), maxLat: ne.getLat(),
          minLng: sw.getLng(), maxLng: ne.getLng(),
          contentTypeId: currentCategory.value
        }
      })
      setSearchMarkers(res.data)
    } catch (e) { console.error(e) }
  }
  
  const clearSearchMarkers = () => {
    if (searchMarkers.length > 0) {
      searchMarkers.forEach(marker => marker.setMap(null))
      searchMarkers = []
    }
  }
  
  const moveCamera = (lat, lng) => {
    if (!mapInstance) return
    const moveLatLon = new window.kakao.maps.LatLng(lat, lng)
    mapInstance.panTo(moveLatLon)
    mapInstance.setLevel(4)
    setTimeout(() => fetchMarkersInBounds(), 500)
  }
  
  const setSearchMarkers = (places) => {
    if (!mapInstance) return
    currentPlaces = places
    
    // 기존 마커 지도에서 제거
    searchMarkers.forEach(marker => marker.setMap(null));
    searchMarkers = []; // 배열 비우기
  
    if (activeInfoWindow) { activeInfoWindow.close(); activeInfoWindow = null }
  
    // 새 마커 생성
    places.forEach(place => {
      const lat = place.latitude || place.lat
      const lng = place.longitude || place.lng
      if (!lat || !lng) return
  
      const marker = new window.kakao.maps.Marker({
        // ★ [핵심] 토글이 켜져 있을 때만 map에 할당, 아니면 null (메모리엔 있지만 안보임)
        map: areMarkersVisible.value ? mapInstance : null,
        position: new window.kakao.maps.LatLng(lat, lng),
        title: place.name
      })
  
      const imageUrl = place.thumbnailUrl || 'https://via.placeholder.com/150x100?text=No+Image';
      const content = `
        <div style="padding:10px; width:240px; background:white; border-radius:8px; box-shadow:0 2px 6px rgba(0,0,0,0.1); display:flex; flex-direction:column; gap:8px;">
          <div style="width:100%; height:120px; border-radius:6px; overflow:hidden; background:#f1f1f1;">
             <img src="${imageUrl}" alt="img" style="width:100%; height:100%; object-fit:cover;" onerror="this.onerror=null; this.src='https://placehold.co/150x100?text=No+Image';"/>
          </div>
          <div>
            <div style="font-weight:bold; font-size:15px; color:#333;">${place.name}</div>
            <div style="font-size:11px; color:#999;">${place.address || '주소 정보 없음'}</div>
          </div>
          <div style="text-align:right;">
             <button onclick="window.dispatchEvent(new CustomEvent('add-place-map', { detail: ${place.poiId} }))"
               style="background:#DE2E5F; color:white; border:none; border-radius:6px; padding:6px 12px; font-size:11px; font-weight:bold; cursor:pointer;">+ 일정에 추가</button>
          </div>
        </div>
      `
      const infowindow = new window.kakao.maps.InfoWindow({ content, removable: true, zIndex: 10 })
      window.kakao.maps.event.addListener(marker, 'click', () => {
        if (activeInfoWindow) activeInfoWindow.close()
        infowindow.open(mapInstance, marker)
        activeInfoWindow = infowindow
        emit('marker-clicked', place)
      })
      searchMarkers.push(marker)
    })
  }
  
  defineExpose({ moveCamera, setMarkers: setSearchMarkers, drawRoute })
  </script>
  
  <template>
    <div class="h-full w-full rounded-3xl overflow-hidden relative shadow-inner bg-gray-100">
      <div ref="mapContainer" class="w-full h-full"></div>
      
      <div class="absolute top-6 left-6 z-20 flex gap-2 flex-wrap max-w-[80%]">
        <button 
          @click="toggleMarkers()"
          class="px-3 py-2 rounded-full text-xs font-bold shadow-md transition-all flex items-center gap-1 border cursor-pointer active:scale-95"
          :class="areMarkersVisible 
            ? 'bg-gray-800 text-white border-gray-800 hover:bg-gray-700' 
            : 'bg-white text-gray-500 border-gray-300 hover:bg-gray-50'"
        >
          <span>{{ areMarkersVisible ? '' : '📍' }}</span>
          <span>{{ areMarkersVisible ? '마커 숨기기' : '마커 보기' }}</span>
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