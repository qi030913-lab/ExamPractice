import { apiClient, unwrapResponse } from "./api";

export async function login(payload) {
  const response = await apiClient.post("/auth/login", payload);
  return unwrapResponse(response);
}

export async function register(payload) {
  const response = await apiClient.post("/auth/register", payload);
  return unwrapResponse(response);
}

export async function getTeacherWorkbench(userId) {
  const response = await apiClient.get(`/workbench/teacher/${userId}`);
  return unwrapResponse(response);
}

export async function getStudentWorkbench(userId) {
  const response = await apiClient.get(`/workbench/student/${userId}`);
  return unwrapResponse(response);
}
