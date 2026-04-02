import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { getStudentWorkbench, getTeacherWorkbench, login, register } from "@/services/auth-api";

const SESSION_STORAGE_KEY = "exampractice.desktop.session";

function normalizeAuthUser(user) {
  if (!user) {
    return null;
  }

  return {
    ...user,
    loginId: user.loginId || user.studentNumber || ""
  };
}

function loadStoredSession() {
  try {
    const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
    if (!raw) {
      return null;
    }

    const parsed = JSON.parse(raw);
    if (!parsed?.user) {
      return null;
    }

    return {
      ...parsed,
      user: normalizeAuthUser(parsed.user)
    };
  } catch (_error) {
    return null;
  }
}

function persistSession(session) {
  if (!session) {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return;
  }

  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify({
    ...session,
    user: normalizeAuthUser(session.user)
  }));
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

  function setSessionUser(nextUser) {
    session.value = nextUser
      ? { user: normalizeAuthUser(nextUser) }
      : null;
    persistSession(session.value);
  }

  async function loginWithPassword(payload) {
    loading.value = true;
    errorMessage.value = "";

    try {
      const result = await login(payload);
      setSessionUser(result.data);
      await loadWorkbench();
      return session.value;
    } catch (error) {
      session.value = null;
      workbench.value = null;
      persistSession(null);
      errorMessage.value = error?.message || "登录失败";
      throw error;
    } finally {
      loading.value = false;
    }
  }

  async function registerAccount(payload) {
    loading.value = true;
    errorMessage.value = "";

    try {
      return await register(payload);
    } catch (error) {
      errorMessage.value = error?.message || "注册失败";
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

    const currentUser = session.value.user;
    const result = currentUser.role === "TEACHER"
      ? await getTeacherWorkbench(currentUser.userId)
      : await getStudentWorkbench(currentUser.userId);

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
    registerAccount,
    loadWorkbench,
    logout
  };
});
