import axios from "axios";

const defaultBaseURL = "http://127.0.0.1:8080/api";
const SESSION_STORAGE_KEY = "exampractice.desktop.session";

function resolveBaseURL() {
  const configuredBaseURL = typeof import.meta.env.VITE_API_BASE_URL === "string"
    ? import.meta.env.VITE_API_BASE_URL.trim()
    : "";

  return configuredBaseURL || defaultBaseURL;
}

function createApiError(message, extras = {}) {
  const error = new Error(message || "请求失败");
  Object.assign(error, extras);
  return error;
}

export const apiClient = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 10000
});

apiClient.interceptors.request.use((config) => {
  try {
    const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
    const parsed = raw ? JSON.parse(raw) : null;
    const token = parsed?.token;

    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
  } catch (_error) {
    // Ignore malformed local session data and let the request proceed unauthenticated.
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    const payload = response?.data;
    if (payload && typeof payload === "object" && Object.prototype.hasOwnProperty.call(payload, "success") && !payload.success) {
      throw createApiError(payload.message, {
        response,
        payload
      });
    }

    return response;
  },
  (error) => Promise.reject(createApiError(
    error?.response?.data?.message || error?.message || "请求失败",
    {
      response: error?.response,
      payload: error?.response?.data
    }
  ))
);

export function unwrapResponse(response) {
  return response?.data;
}
