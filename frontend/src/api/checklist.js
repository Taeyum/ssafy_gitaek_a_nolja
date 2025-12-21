import http from "./http";

// 1. 내 여행 계획 목록 가져오기 (★ 신규 추가)
export const getMyPlans = () => http.get("/checklist/plans");

// 2. 체크리스트 조회 (planId 없으면 0)
export const getChecklist = (planId = 0) =>
  http.get("/checklist", { params: { planId } });

// 3. 아이템 추가 (★ { content, planId } 객체로 묶어서 전송)
export const addCheckItem = (content, planId = 0) =>
  http.post("/checklist", { content, planId });

// 4. 삭제, 토글 (ID로 작동하므로 변경 없음)
export const deleteCheckItem = (id) => http.delete(`/checklist/${id}`);
export const toggleCheckItem = (id) => http.put(`/checklist/${id}`);

// 5. 전체 삭제/완료 (planId 필요)
export const deleteAllCheckItems = (planId = 0) =>
  http.delete("/checklist/all", { params: { planId } });
export const checkAllCheckItems = (planId = 0) =>
  http.put("/checklist/all", null, { params: { planId } });
