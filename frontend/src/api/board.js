import http from "./http";

export const getBoardList = () => http.get("/board");
export const getBoardDetail = (id) => http.get(`/board/${id}`);
export const writeBoard = (data) => http.post("/board", data);
export const updateBoard = (id, data) => http.put(`/board/${id}`, data);
export const deleteBoard = (id) => http.delete(`/board/${id}`);
export const toggleLike = (id) => http.post(`/board/${id}/like`);
