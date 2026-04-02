import { apiClient, unwrapResponse } from "./api";

export async function getStudentPapers(userId) {
  const response = await apiClient.get(`/student/${userId}/papers`);
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
