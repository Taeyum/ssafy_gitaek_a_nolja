import { ref } from "vue";
import { defineStore } from "pinia";
import {
  createTripApi,
  getMyTripsApi,
  deleteTripApi,
  addScheduleApi,
  getSchedulesApi,
  deleteScheduleApi,
  requestEditApi,
  releaseEditApi,
  getTripStatusApi,
  joinTripApi,
  leaveTripApi,
  getMessagesApi,
  sendMessageApi,
} from "@/api/trip";

// ★ [추가] 웹소켓 라이브러리 임포트
import { Stomp } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export const useTripStore = defineStore("trip", () => {
  // --- 상태 (State) ---
  const tripInfo = ref({
    tripId: 0,
    title: "",
    duration: 2,
    startDate: "",
    endDate: "",
    maxMembers: 4,
    currentMembers: [],
    inviteCode: "",
    style: "friend",
    ownerId: 0,
    currentParticipants: 1,
  });

  const itinerary = ref([]);
  const myTrips = ref([]);
  const messages = ref([]);

  // ★ [추가] 알림 리스트 & 소켓 클라이언트 객체
  const notifications = ref([]);
  let stompClient = null;

  // 동시성 제어용
  const currentEditorName = ref(null);
  const isLocked = ref(false);
  let pollingInterval = null;

  // --- 액션 (Actions) ---

  // ★ [추가] 알림 추가 및 자동 삭제 로직
  const addNotification = (type, message, senderName) => {
    const id = Date.now(); // 고유 ID 생성
    notifications.value.push({ id, type, message, senderName });

    // 3초 뒤에 리스트에서 제거 (자동 사라짐 효과)
    setTimeout(() => {
      notifications.value = notifications.value.filter(n => n.id !== id);
    }, 3000);
  };

  // ★ [수정] 통합 소켓 연결 (알림 + 채팅)
  const connectTripSocket = (tripId) => {
    // 이미 연결되어 있다면 중복 연결 방지
    if (stompClient && stompClient.connected) return;

    const socket = new SockJS('https://gitaek.duckdns.org/ws-stomp');
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {}; // 콘솔 로그 끄기

    stompClient.connect({}, () => {
      console.log('🔗 통합 소켓 연결됨 (알림+채팅)');

      // 1. 알림 구독 (/sub/trip/{tripId}/notification)
      stompClient.subscribe(`/sub/trip/${tripId}/notification`, (res) => {
        const noti = JSON.parse(res.body);
        
        // 화면에 토스트 알림 띄우기
        addNotification(noti.type, noti.message, noti.senderName);

        // 편집 알림('EDIT')이 오면, 일정 목록을 최신으로 새로고침
        if (noti.type === 'EDIT') {
           refreshItinerary();
        }
      });

      // 2. 채팅 구독 (/sub/chat/{tripId})
      stompClient.subscribe(`/sub/chat/${tripId}`, (res) => {
        const received = JSON.parse(res.body);
        // 메시지 리스트에 추가 (ChatInterface나 뱃지에서 사용됨)
        messages.value.push(received);
      });
    });
  };

  // ★ [추가] 소켓으로 채팅 전송하는 함수
  const sendChatMessage = (content, userId, senderName) => {
    if (!stompClient || !stompClient.connected) return;
    
    const msgPayload = {
      tripId: tripInfo.value.tripId,
      userId: userId,
      content: content,
      senderName: senderName
    };
    
    // /pub/chat/message 로 전송
    stompClient.send("/pub/chat/message", {}, JSON.stringify(msgPayload));
  };

  // ★ [추가] 소켓 연결 해제
  const disconnectTripSocket = () => {
    if (stompClient) {
      stompClient.disconnect();
      stompClient = null;
      console.log('🔌 소켓 연결 해제');
    }
  };

  const fetchMyTrips = async () => {
    try {
      const response = await getMyTripsApi();
      myTrips.value = response.data;
    } catch (error) {
      console.error("여행 목록 로드 실패", error);
    }
  };

  // ★ [수정 1] loadTrip: 날짜 계산 + 변수명 방어 로직 (하이브리드)
  const loadTrip = async (rawTrip) => {
    const normalizedData = {
      tripId: rawTrip.tripId || rawTrip.trip_id,
      title: rawTrip.title,
      startDate: rawTrip.startDate || rawTrip.start_date,
      endDate: rawTrip.endDate || rawTrip.end_date,
      maxMembers:
        rawTrip.maxMembers ||
        rawTrip.maxParticipants ||
        rawTrip.max_members ||
        rawTrip.max_participants ||
        4,
      currentParticipants:
        rawTrip.currentParticipants ||
        rawTrip.currentMembers ||
        rawTrip.current_participants ||
        1,
      inviteCode: rawTrip.inviteCode || rawTrip.invite_code || "",
      ownerId: rawTrip.ownerId || rawTrip.owner_id,
      style: rawTrip.style || "friend",
      duration: rawTrip.duration || 0,
    };

    let dayCount = normalizedData.duration;

    if (!dayCount || dayCount < 1) {
      const start = new Date(normalizedData.startDate);
      const end = new Date(normalizedData.endDate);

      if (!isNaN(start) && !isNaN(end)) {
        const diffTime = end.getTime() - start.getTime();
        dayCount = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
      } else {
        dayCount = 1; 
      }
    }

    dayCount = Math.max(1, dayCount);

    tripInfo.value = {
      ...normalizedData,
      duration: dayCount,
    };

    const newItinerary = [];
    const startDateObj = new Date(normalizedData.startDate);

    for (let i = 0; i < dayCount; i++) {
      const currentDate = new Date(startDateObj);
      if (!isNaN(startDateObj)) {
        currentDate.setDate(startDateObj.getDate() + i);
      }

      let dateStr = "날짜 미정";
      try {
        dateStr = currentDate.toISOString().split("T")[0];
      } catch (e) {}

      newItinerary.push({
        id: (i + 1).toString(),
        day: `${i + 1}일차`,
        date: dateStr,
        items: [],
      });
    }

    itinerary.value = newItinerary;

    await refreshItinerary(newItinerary);
  };

  // ★ [수정 2] createNewTrip: 종료일 직접 계산해서 전송
  const createNewTrip = async (info) => {
    try {
      let safeDuration = parseInt(info.duration);
      if (isNaN(safeDuration) || safeDuration < 1) {
        safeDuration = 1;
      }

      const start = new Date(info.startDate);
      const end = new Date(start);
      end.setDate(start.getDate() + (safeDuration - 1));

      const endDateStr = end.toISOString().split("T")[0];

      const payload = {
        title: info.title,
        startDate: info.startDate,
        endDate: endDateStr, 
        duration: safeDuration, 
        maxParticipants: info.members,
        style: info.style,
      };

      console.log("🚀 여행 생성 요청:", payload);

      const response = await createTripApi(payload);
      const savedTrip = response.data;

      const optimisticTripData = {
        ...savedTrip, 
        title: info.title,
        startDate: info.startDate,
        endDate: endDateStr,
        duration: safeDuration, 
        maxMembers: info.members,
        style: info.style,
      };

      await loadTrip(optimisticTripData);
      return true;
    } catch (error) {
      console.error("여행 생성 실패:", error);
      alert("여행 생성 중 오류가 발생했습니다.");
      return false;
    }
  };

  // ★ [수정 3] refreshItinerary: 변수명 방어 로직 추가
  const refreshItinerary = async (targetItinerary = null) => {
    if (!tripInfo.value.tripId) return;

    const currentItinerary = targetItinerary || itinerary.value;

    try {
      const res = await getSchedulesApi(tripInfo.value.tripId);
      const dbSchedules = res.data || [];

      currentItinerary.forEach((day) => {
        day.items = [];
      });

      dbSchedules.forEach((item) => {
        const tripDay = item.tripDay || item.trip_day || 1;
        const dayIndex = tripDay - 1;

        if (currentItinerary[dayIndex]) {
          let rawTime = item.scheduleTime || item.schedule_time || "12:00";
          let cleanTime =
            rawTime.length > 5 ? rawTime.substring(0, 5) : rawTime;

          currentItinerary[dayIndex].items.push({
            id: item.scheduleId
              ? String(item.scheduleId)
              : String(Date.now() + Math.random()),
            time: cleanTime,
            name:
              item.placeName || item.place_name || item.name || "장소명 없음",
            location:
              item.placeAddress || item.place_address || item.address || "",
            lat: item.placeLat || item.latitude || 0,
            lng: item.placeLng || item.longitude || 0,
            poiId: item.poiId || item.poi_id,
            // ★ [추가] 서버에서 받아온 duration, travelTime 매핑
            duration: item.duration || "",
            travelTime: item.travelTime || item.travel_time || "",
          });
        }
      });

      currentItinerary.forEach((day) => {
        day.items.sort((a, b) => a.time.localeCompare(b.time));
      });

      if (targetItinerary) {
        itinerary.value = currentItinerary;
      }
    } catch (error) {
      console.error("일정 동기화 실패", error);
    }
  };

  const addPlace = async (dayId, place, time) => {
    const targetDay = itinerary.value.find((d) => d.id === dayId);
    if (!targetDay) return;

    const inputTime = time ? time : "12:00";

    if (
      targetDay.items.some((item) => {
        const itemTimeSimple = item.time.substring(0, 5);
        const inputTimeSimple = inputTime.substring(0, 5);
        return itemTimeSimple === inputTimeSimple && item.poiId !== place.poiId;
      })
    ) {
      alert(`선택하신 시간(${inputTime})에는 이미 다른 일정이 있습니다!`);
      return;
    }

    const tempId = Date.now().toString();
    const newItem = {
      id: tempId,
      time: inputTime,
      name: place.name,
      location: place.address || "",
      lat: place.lat || 0,
      lng: place.lng || 0,
      poiId: place.poiId,
      // ★ [추가] 화면에 즉시 반영할 데이터
      duration: place.duration || "",
      travelTime: place.travelTime || "",
    };

    targetDay.items.push(newItem);
    targetDay.items.sort((a, b) => a.time.localeCompare(b.time));

    try {
      const dayNumber = parseInt(dayId); 
      await addScheduleApi({
        tripId: tripInfo.value.tripId,
        poiId: place.poiId,
        tripDay: dayNumber,
        visitOrder: targetDay.items.length,
        memo: place.memo || "", // AI가 준 메모가 있으면 저장
        scheduleTime: inputTime,
        // ★ [추가] 서버로 보낼 데이터 (DB 저장용)
        duration: place.duration || "",
        travelTime: place.travelTime || "",
      });
    } catch (error) {
      alert("저장 실패. 다시 시도해주세요.");
      targetDay.items = targetDay.items.filter((item) => item.id !== tempId);
    }
  };

  const editItem = (dayId, itemId, newTime, newName) => {
    const targetDay = itinerary.value.find((d) => d.id === dayId);
    if (targetDay) {
      const item = targetDay.items.find((i) => i.id === itemId);
      if (item) {
        item.time = newTime;
        item.name = newName;
        targetDay.items.sort((a, b) => a.time.localeCompare(b.time));
      }
    }
  };

  const removePlace = async (dayId, itemId) => {
    const targetDay = itinerary.value.find((d) => d.id === dayId);
    if (!targetDay) return;
    const itemToRemove = targetDay.items.find((item) => item.id === itemId);
    if (!itemToRemove) return;

    try {
      const dayNumber = parseInt(dayId);
      await deleteScheduleApi(
        tripInfo.value.tripId,
        dayNumber,
        itemToRemove.poiId
      );
      targetDay.items = targetDay.items.filter((item) => item.id !== itemId);
    } catch (error) {
      alert("삭제 실패");
    }
  };

  const deleteTrip = async (tripId) => {
    try {
      await deleteTripApi(tripId);
      await fetchMyTrips();
      return true;
    } catch (error) {
      return false;
    }
  };

  const joinTrip = async (code) => {
    try {
      const response = await joinTripApi(code);
      return response.data;
    } catch (error) {
      alert(error.response?.data || "입장 실패");
      return null;
    }
  };

  const checkStatus = async (userId) => {
    if (!tripInfo.value.tripId) return;

    try {
      const res = await getTripStatusApi(tripInfo.value.tripId);
      const trip = res.data;

      if (trip.currentParticipants) {
        tripInfo.value.currentParticipants = trip.currentParticipants;
      }

      // 채팅 메시지 REST 조회 (소켓 연결 전 초기화용)
      await fetchMessages();

      if (trip.currentEditorId) {
        if (trip.currentEditorId !== userId) {
          currentEditorName.value = "다른 멤버";
          isLocked.value = true;
          await refreshItinerary();
        } else {
          currentEditorName.value = "나";
          isLocked.value = false;
        }
      } else {
        currentEditorName.value = null;
        isLocked.value = false;
        await refreshItinerary();
      }
    } catch (e) {}
  };

  const startPolling = (userId) => {
    if (pollingInterval) return;
    if (!userId) return;

    checkStatus(userId);
    pollingInterval = setInterval(() => {
      checkStatus(userId);
    }, 2000);
  };

  const stopPolling = () => {
    if (pollingInterval) {
      clearInterval(pollingInterval);
      pollingInterval = null;
    }
  };

  const tryRequestEdit = async () => {
    if (!tripInfo.value.tripId) return false;
    try {
      await requestEditApi(tripInfo.value.tripId);
      currentEditorName.value = "나";
      isLocked.value = false;
      return true;
    } catch (error) {
      if (error.response?.status === 409)
        alert("다른 사용자가 이미 수정 중입니다.");
      else alert("권한 요청 실패");
      return false;
    }
  };

  const finishEdit = async () => {
    try {
      await releaseEditApi(tripInfo.value.tripId);
      currentEditorName.value = null;
      return true;
    } catch (e) {
      console.error(e);
    }
  };

  const leaveTrip = async (targetTripId = null) => {
    const idToDelete = targetTripId || tripInfo.value.tripId;
    if (!idToDelete) return false;

    try {
      await leaveTripApi(idToDelete);
      await fetchMyTrips();
      return true;
    } catch (error) {
      alert(error.response?.data || "나가기 실패");
      return false;
    }
  };

  const fetchMessages = async () => {
    if (!tripInfo.value.tripId) return;
    try {
      const res = await getMessagesApi(tripInfo.value.tripId);
      messages.value = res.data;
    } catch (e) {}
  };

  const sendMessage = async (content) => {
    if (!content.trim()) return;
    try {
      await sendMessageApi(tripInfo.value.tripId, content);
      await fetchMessages();
    } catch (e) {
      alert("메시지 전송 실패");
    }
  };

  return {
    tripInfo,
    itinerary,
    myTrips,
    messages,
    // ★ [추가] 알림 및 소켓 관련 State & Action 리턴
    notifications,
    connectTripSocket,
    disconnectTripSocket,
    sendChatMessage, // 소켓 전송용 함수
    
    currentEditorName,
    isLocked,
    fetchMyTrips,
    loadTrip,
    createNewTrip,
    addPlace,
    editItem,
    removePlace,
    deleteTrip,
    joinTrip,
    startPolling,
    stopPolling,
    tryRequestEdit,
    finishEdit,
    leaveTrip,
    fetchMessages,
    sendMessage, // REST 전송용 (필요시 사용)
    refreshItinerary,
  };
});