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
  
  const emit = defineEmits(['marker-clicked', 'add-to-plan', 'map-clicked', 'update-travel-times'])
  
  // ==========================================
  // 🗺️ 지도 초기화 및 이벤트
  // ==========================================
  onMounted(() => {
    // 1. 카카오 객체가 들어왔는지 0.1초마다 감시
    const interval = setInterval(() => {
      // window.kakao가 있고, 그 안에 maps가 있다면?
      if (window.kakao && window.kakao.maps) {
        clearInterval(interval);
        console.log("📦 카카오 객체 감지됨! 포장 뜯기 시작(load)...");
  
        // 2. ★ autoload=false일 때는 반드시 이 load 함수 안에서 지도를 그려야 합니다.
        window.kakao.maps.load(() => {
          console.log("🎉 포장 뜯기 완료! 지도 그리기 시작");
          initMap(); // 이제 LatLng를 써도 에러가 안 납니다.
        });
      }
    }, 100);
  });
  
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
        activeInfoWindow.setMap(null)
        activeInfoWindow = null
      }
      emit('map-clicked')
    })
  
    onMapIdle()
  }
  
  // ==========================================
  // 🔍 장소 마커 로직
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
  
  const renderMarkers = (places) => {
    clearSearchMarkers()
    currentPlaces = places
  
    if (activeInfoWindow) {
      activeInfoWindow.setMap(null)
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
        title: place.name,
        zIndex: 0
      })
  
      const imageUrl = place.thumbnailUrl || 'https://placehold.co/280x160?text=Trip';
  
      const content = document.createElement('div');
      content.style.cssText = 'position: absolute; left: -140px; bottom: 50px; width: 280px;';
  
      content.innerHTML = `
        <div style="
            background: #fff; 
            border-radius: 12px; 
            box-shadow: 0 10px 25px rgba(0,0,0,0.25); 
            overflow: hidden; 
            font-family: 'Pretendard', sans-serif;
            display: flex; flex-direction: column;
        ">
          <div style="width: 100%; height: 150px; position: relative; background-color: #eee;">
             <img src="${imageUrl}" style="width: 100%; height: 100%; object-fit: cover;" />
             
             <button class="close-btn" style="
                position: absolute; top: 10px; right: 10px;
                background: rgba(0,0,0,0.6); color: white;
                border: none; border-radius: 50%; width: 26px; height: 26px;
                cursor: pointer; display: flex; align-items: center; justify-content: center;
                font-size: 14px; padding: 0; z-index: 10;
             ">✕</button>
  
             <div style="
                 position: absolute; bottom: 0; left: 0; right: 0;
                 padding: 30px 16px 12px 16px;
                 background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
                 color: white;
             ">
                <div style="
                    font-weight: 700; font-size: 18px; line-height: 1.3; 
                    word-break: keep-all; 
                    text-shadow: 0 1px 3px rgba(0,0,0,0.5);
                ">${place.name}</div>
                <div style="
                    font-size: 12px; opacity: 0.95; margin-top: 4px; font-weight: 300;
                    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
                ">📍 ${place.address || '주소 정보 없음'}</div>
             </div>
          </div>
  
          <div style="padding: 16px;">
             <div class="scroll-box" style="
                 background-color: #F8F9FA; border: 1px solid #E9ECEF;
                 border-radius: 8px; padding: 12px; margin-bottom: 12px;
                 max-height: 100px; overflow-y: auto; 
                 overscroll-behavior: contain;
             ">
                <div style="font-size: 11px; font-weight: 700; color: #DE2E5F; margin-bottom: 6px;">
                    ✨ AI SUMMARY
                </div>
                <p class="ai-desc" style="
                    font-size: 13px; color: #495057; line-height: 1.5; margin: 0;
                    word-break: keep-all; white-space: normal;
                ">
                   ${place.description ? place.description : '<span style="color:#aaa;">AI 분석 중...</span>'}
                </p>
             </div>
  
             <button class="add-btn" style="
                 width: 100%; background-color: #DE2E5F; color: white; 
                 border: none; border-radius: 8px; padding: 12px; 
                 font-size: 14px; font-weight: 700; cursor: pointer;
                 box-shadow: 0 4px 6px rgba(222, 46, 95, 0.2);
             ">+ 일정에 담기</button>
          </div>
          
          <div style="
              position: absolute; bottom: -8px; left: 50%; transform: translateX(-50%);
              width: 0; height: 0; 
              border-left: 8px solid transparent;
              border-right: 8px solid transparent;
              border-top: 8px solid #fff;
              filter: drop-shadow(0 4px 2px rgba(0,0,0,0.1));
          "></div>
        </div>
      `;
  
      const overlay = new window.kakao.maps.CustomOverlay({
        content: content,
        map: null,
        position: position,
        clickable: true,
        zIndex: 1000
      });
  
      content.addEventListener('mouseenter', () => mapInstance.setZoomable(false));
      content.addEventListener('mouseleave', () => mapInstance.setZoomable(true));
      content.addEventListener('touchmove', (e) => e.stopPropagation());
  
      const closeBtn = content.querySelector('.close-btn');
      closeBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          overlay.setMap(null);
          activeInfoWindow = null;
          mapInstance.setZoomable(true);
      });
  
      const addBtn = content.querySelector('.add-btn');
      addBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          emit('add-to-plan', place);
      });
  
      window.kakao.maps.event.addListener(marker, 'click', async () => {
        if (activeInfoWindow) activeInfoWindow.setMap(null);
        
        const projection = mapInstance.getProjection();
        const markerPoint = projection.pointFromCoords(position);
        const offsetY = 250;
        const newCenterPoint = new window.kakao.maps.Point(markerPoint.x, markerPoint.y - offsetY);
        const newCenterLatLon = projection.coordsFromPoint(newCenterPoint);
        
        mapInstance.panTo(newCenterLatLon);
  
        overlay.setMap(mapInstance);
        activeInfoWindow = overlay;
        
        emit('marker-clicked', place);
  
        if (!place.description) {
           try {
              const descEl = content.querySelector('.ai-desc');
              
              // 1. 서버 요청
              const res = await http.get(`/attractions/${place.poiId}/description`);
              console.log("📦 AI 응답 데이터 확인:", res.data); // 콘솔에서 구조 확인용

              // 2. 데이터 정제 (무조건 문자열로 변환)
              let finalDesc = "";

              if (typeof res.data === 'string') {
                  // 그냥 문자열이면 바로 씀
                  finalDesc = res.data;
              } else if (typeof res.data === 'object' && res.data !== null) {
                  // 객체라면 'description' 키를 꺼냄 (없으면 'message' 등 확인)
                  if (res.data.description) {
                      finalDesc = res.data.description;
                  } else {
                      // 만약 description 키가 없다면 강제로 문자열로 풀어서 보여줌 (디버깅용)
                      finalDesc = JSON.stringify(res.data); 
                  }
              } else {
                  finalDesc = "설명 데이터가 없습니다.";
              }

              // 3. 화면 적용 (이제 finalDesc는 무조건 문자열이라 에러 안 남)
              place.description = finalDesc;
              
              descEl.style.opacity = 0;
              // replace는 문자열에만 쓸 수 있음 (안전장치 추가)
              descEl.innerHTML = String(finalDesc).replace(/\n/g, '<br>');
              
              let op = 0.1;
              let timer = setInterval(() => {
                  if (op >= 1) clearInterval(timer);
                  descEl.style.opacity = op;
                  op += 0.1;
              }, 20);

           } catch (e) {
              console.error("AI 로딩 에러:", e);
              const descEl = content.querySelector('.ai-desc');
              if (descEl) {
                  descEl.innerHTML = "<span style='color:red;'>⚠️ AI 요약 실패</span>";
                  descEl.style.opacity = 1;
              }
           }
        }
      });
  
      searchMarkers.push(marker);
    });
  }
  
  const clearSearchMarkers = () => {
    searchMarkers.forEach(marker => marker.setMap(null))
    searchMarkers = []
  }
  
  const changeCategory = (id) => {
    currentCategory.value = id
    areMarkersVisible.value = true 
    fetchMarkersInBounds()
  }
  
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
  // 🎨 일정 경로(Polyline) 그리기 로직 (최적화)
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
    
    // ★ 계산된 시간 정보를 모아서 부모로 보낼 배열
    const calculatedTimes = []
  
    for (let day of itinerary) {
      if (currentId !== drawRequestId) return 
  
      const items = day.items || day.places
      if (!items || items.length === 0) continue
      
      const dayIdNum = parseInt(day.id.replace(/[^0-9]/g, "")) || 1; 
      const color = routeColors[(dayIdNum - 1) % routeColors.length]
  
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
        let timeText = '' 
  
        if (distMeters > 2000) { 
          const result = await fetchCarPath(sLat, sLng, eLat, eLng)
          if (result) {
            linePath = result.path
            durationSec = result.duration
            realDistanceM = result.distance
            
            const minutes = Math.round(durationSec / 60);
            timeText = minutes + '분';
          }
        } else {
          isWalking = true
          realDistanceM = distMeters
          durationSec = (realDistanceM / 67) * 60 
          
          const minutes = Math.round(durationSec / 60);
          timeText = minutes + '분';
        }
  
        // ★ [최적화] 이미 저장된 시간과 다를 때만 업데이트 리스트에 추가
        // (이미 '15분'이라고 떠 있는데 또 '15분'을 보내면 깜빡거림 발생)
        if (timeText && end.travelTime !== timeText) {
            calculatedTimes.push({
               dayId: day.id,
               itemId: end.id || end.scheduleId,
               travelTime: timeText
            });
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
  
    // ★ [핵심] 모든 경로 계산 후 일괄 전송 (깜빡임 방지)
    if (calculatedTimes.length > 0) {
       emit('update-travel-times', calculatedTimes);
    }
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