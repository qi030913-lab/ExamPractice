import { apiClient, unwrapResponse } from "./api";

export async function getTeacherPapers(userId) {
  const response = await apiClient.get(`/teacher/${userId}/papers`);
  return unwrapResponse(response);
}

export async function getTeacherPaperDetail(userId, paperId) {
  const response = await apiClient.get(`/teacher/${userId}/papers/${paperId}`);
  return unwrapResponse(response);
}

export async function updateTeacherPaper(userId, paperId, payload) {
  const response = await apiClient.put(`/teacher/${userId}/papers/${paperId}`, payload);
  return unwrapResponse(response);
}

export async function publishTeacherPaper(userId, paperId) {
  const response = await apiClient.post(`/teacher/${userId}/papers/${paperId}/publish`);
  return unwrapResponse(response);
}

export async function unpublishTeacherPaper(userId, paperId) {
  const response = await apiClient.post(`/teacher/${userId}/papers/${paperId}/unpublish`);
  return unwrapResponse(response);
}

export async function deleteTeacherPaper(userId, paperId) {
  const response = await apiClient.delete(`/teacher/${userId}/papers/${paperId}`);
  return unwrapResponse(response);
}

export async function getTeacherStudents(userId) {
  const response = await apiClient.get(`/teacher/${userId}/students`);
  return unwrapResponse(response);
}

export async function getTeacherStudentDetail(userId, studentId) {
  const response = await apiClient.get(`/teacher/${userId}/students/${studentId}`);
  return unwrapResponse(response);
}

export async function getTeacherStudentRecords(userId, studentId) {
  const response = await apiClient.get(`/teacher/${userId}/students/${studentId}/records`);
  return unwrapResponse(response);
}

export async function getTeacherStudentRecordDetail(userId, studentId, recordId) {
  const response = await apiClient.get(`/teacher/${userId}/students/${studentId}/records/${recordId}`);
  return unwrapResponse(response);
}

export async function importTeacherPaper(userId, payload) {
  const response = await apiClient.post(`/teacher/${userId}/import-paper`, payload);
  return unwrapResponse(response);
}
