import axios from "axios";

const defaultBaseURL = "http://127.0.0.1:8080/api";

export const apiClient = axios.create({
  baseURL: window?.desktopApi?.getApiBaseUrl?.() || defaultBaseURL,
  timeout: 10000
});

export function unwrapResponse(response) {
  return response?.data;
}
