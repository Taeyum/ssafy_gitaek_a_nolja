import http from "./http";

// 목록 조회
export const getReviewList = () => http.get("/review");

// 상세 조회
export const getReviewDetail = (reviewId) => http.get(`/review/${reviewId}`);

// 작성
export const writeReview = (data) => http.post("/review", data);

// 수정
export const updateReview = (reviewId, data) => http.put(`/review/${reviewId}`, data);

// 삭제
export const deleteReview = (reviewId) => http.delete(`/review/${reviewId}`);

// 좋아요 토글
export const toggleReviewLike = (reviewId) => http.post(`/review/${reviewId}/like`);

// [추가] 내가 좋아요한 리뷰 목록
export const getLikedReviewList = () => http.get("/review/liked");