import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { getStudentWorkbench, getTeacherWorkbench, login } from "@/services/auth-api";

const SESSION_STORAGE_KEY = "exampractice.desktop.session";

function loadStoredSession() {
  try {
    const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (_error) {
    return null;
  }
}

function persistSession(session) {
  if (!session) {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return;
  }
  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
}

export const useSessionStore = defineStore("session", () => {
  const session = ref(loadStoredSession());
  const workbench = ref(null);
  const loading = ref(false);
  const errorMessage = ref("");

  const isLoggedIn = computed(() => Boolean(session.value?.user));
  const role = computed(() => session.value?.user?.role || "");
  const isTeacher = computed(() => role.value === "TEACHER");
  const isStudent = computed(() => role.value === "STUDENT");
  const user = computed(() => session.value?.user || null);

  async function loginWithPassword(payload) {
    loading.value = true;
    errorMessage.value = "";

    try {
      const result = await login(payload);
      if (!result?.success) {
        throw new Error(result?.message || "登录失败");
      }

      session.value = {
        user: result.data
      };
      persistSession(session.value);
      await loadWorkbench();
      return session.value;
    } catch (error) {
      session.value = null;
      workbench.value = null;
      persistSession(null);
      errorMessage.value = error?.response?.data?.message || error?.message || "登录失败";
      throw error;
    } finally {
      loading.value = false;
    }
  }

  async function loadWorkbench() {
    if (!session.value?.user?.userId || !session.value?.user?.role) {
      workbench.value = null;
      return null;
    }

    const { user: currentUser } = session.value;
    const result = currentUser.role === "TEACHER"
      ? await getTeacherWorkbench(currentUser.userId)
      : await getStudentWorkbench(currentUser.userId);

    if (!result?.success) {
      throw new Error(result?.message || "加载工作台失败");
    }

    workbench.value = result.data;
    return workbench.value;
  }

  function logout() {
    session.value = null;
    workbench.value = null;
    errorMessage.value = "";
    persistSession(null);
  }

  return {
    session,
    workbench,
    loading,
    errorMessage,
    isLoggedIn,
    isTeacher,
    isStudent,
    role,
    user,
    loginWithPassword,
    loadWorkbench,
    logout
  };
});
