import { apiClient, unwrapResponse } from "./api";

export async function getStudentPapers(userId) {
  const response = await apiClient.get(`/student/${userId}/papers`);
  return unwrapResponse(response);
}

export async function getStudentAchievement(userId) {
  const response = await apiClient.get(`/student/${userId}/achievement`);
  return unwrapResponse(response);
}

export async function getStudentPaperDetail(userId, paperId) {
  const response = await apiClient.get(`/student/${userId}/papers/${paperId}`);
  return unwrapResponse(response);
}

export async function startStudentExam(userId, paperId) {
  const response = await apiClient.post(`/student/${userId}/papers/${paperId}/start`);
  return unwrapResponse(response);
}

export async function getStudentExamSession(userId, recordId) {
  const response = await apiClient.get(`/student/${userId}/records/${recordId}/exam`);
  return unwrapResponse(response);
}

export async function submitStudentExam(userId, recordId, payload) {
  const response = await apiClient.post(`/student/${userId}/records/${recordId}/submit`, payload);
  return unwrapResponse(response);
}

export async function getStudentRecords(userId) {
  const response = await apiClient.get(`/student/${userId}/records`);
  return unwrapResponse(response);
}

export async function getStudentRecordDetail(userId, recordId) {
  const response = await apiClient.get(`/student/${userId}/records/${recordId}`);
  return unwrapResponse(response);
}
