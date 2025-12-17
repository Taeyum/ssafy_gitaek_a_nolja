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

  // 동시성 제어용
  const currentEditorName = ref(null);
  const isLocked = ref(false);
  let pollingInterval = null;

  // --- 액션 (Actions) ---

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
    // 1. 변수명 통일 (Normalization)
    // 백엔드가 어떤 형태(DTO/Map)로 주든 다 받아냅니다.
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
      // DB에 duration 없으면 0으로 둠 (아래서 계산)
      duration: rawTrip.duration || 0,
    };

    // 2. 기간(Duration) 정밀 계산
    // DB에 duration 컬럼이 없으므로, 날짜 차이를 계산해서 복원합니다.
    let dayCount = normalizedData.duration;

    // 기간 정보가 없으면 날짜 차이로 계산
    if (!dayCount || dayCount < 1) {
      const start = new Date(normalizedData.startDate);
      const end = new Date(normalizedData.endDate);

      if (!isNaN(start) && !isNaN(end)) {
        const diffTime = end.getTime() - start.getTime();
        // (종료일 - 시작일) / 하루 + 1일 = 기간
        dayCount = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
      } else {
        dayCount = 1; // 날짜 깨지면 기본 1일
      }
    }

    // 최소 1일 보장 (안전장치)
    dayCount = Math.max(1, dayCount);

    // 3. 상태 업데이트
    tripInfo.value = {
      ...normalizedData,
      duration: dayCount,
    };

    // 4. 일정 배열 틀(Itinerary) 생성
    // (계산된 기간만큼 반복문을 돕니다)
    const newItinerary = [];
    const startDateObj = new Date(normalizedData.startDate);

    for (let i = 0; i < dayCount; i++) {
      const currentDate = new Date(startDateObj);
      // 날짜 유효성 체크 후 더하기
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

    // 화면 먼저 그리기
    itinerary.value = newItinerary;

    // 5. DB 세부 일정 데이터 채우기
    await refreshItinerary(newItinerary);
  };

  // ★ [수정 2] createNewTrip: 종료일 직접 계산해서 전송 (핵심!)
  const createNewTrip = async (info) => {
    try {
      // 1. 기간 안전장치 (0이나 빈값이면 1일로 강제)
      let safeDuration = parseInt(info.duration);
      if (isNaN(safeDuration) || safeDuration < 1) {
        safeDuration = 1;
      }

      // 2. 종료일 계산 (시작일 + 기간 - 1)
      const start = new Date(info.startDate);
      const end = new Date(start);
      // 예: 23일 시작, 1일 기간 -> 23 + 0 = 23일 종료 (정상)
      end.setDate(start.getDate() + (safeDuration - 1));

      const endDateStr = end.toISOString().split("T")[0];

      // 3. 백엔드 전송 (endDate 필수 포함!)
      const payload = {
        title: info.title,
        startDate: info.startDate,
        endDate: endDateStr, // ★ 계산된 종료일 전송
        duration: safeDuration, // ★ 안전한 기간 전송
        maxParticipants: info.members,
        style: info.style,
      };

      console.log("🚀 여행 생성 요청:", payload);

      const response = await createTripApi(payload);
      const savedTrip = response.data;

      // 4. 저장 직후에는 백엔드 응답을 기다리지 말고, 내가 계산한 값으로 즉시 로딩
      // (백엔드가 duration을 안 줘도 화면은 정상 작동하게 함)
      const optimisticTripData = {
        ...savedTrip, // ID나 코드 등은 백엔드꺼 사용
        title: info.title,
        startDate: info.startDate,
        endDate: endDateStr,
        duration: safeDuration, // 내가 보낸 기간 그대로 사용
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

      // 내용 비우기
      currentItinerary.forEach((day) => {
        day.items = [];
      });

      dbSchedules.forEach((item) => {
        // [변수명 방어] DB: trip_day vs JS: tripDay
        const tripDay = item.tripDay || item.trip_day || 1;
        const dayIndex = tripDay - 1;

        if (currentItinerary[dayIndex]) {
          // [변수명 방어] DB: schedule_time
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
          });
        }
      });

      // 시간순 정렬
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

  // ... (이하 기존 기능 유지) ...

  const addPlace = async (dayId, place, time) => {
    const targetDay = itinerary.value.find((d) => d.id === dayId);
    if (!targetDay) return;

    const inputTime = time ? time : "12:00";

    // 중복 체크
    if (
      targetDay.items.some((item) => {
        // 시간 비교 시 초 단위 제거
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
    };

    targetDay.items.push(newItem);
    targetDay.items.sort((a, b) => a.time.localeCompare(b.time));

    try {
      const dayNumber = parseInt(dayId); // 숫자 변환
      await addScheduleApi({
        tripId: tripInfo.value.tripId,
        poiId: place.poiId,
        tripDay: dayNumber,
        visitOrder: targetDay.items.length,
        memo: "",
        scheduleTime: inputTime,
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
    messages,
    fetchMessages,
    sendMessage,
    refreshItinerary,
  };
});
