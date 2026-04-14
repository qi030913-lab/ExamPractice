import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { getCurrentSession, getStudentWorkbench, getTeacherWorkbench, login, logout, register } from "@/services/auth-api";

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
      user: normalizeAuthUser(parsed.user)
    };
  } catch (_error) {
    return null;
  }
}

function persistSession(session) {
  if (!session?.user) {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return;
  }

  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify({
    user: normalizeAuthUser(session.user)
  }));
}

export const useSessionStore = defineStore("session", () => {
  const session = ref(loadStoredSession());
  const workbench = ref(null);
  const loading = ref(false);
  const restoring = ref(false);
  const errorMessage = ref("");

  const isLoggedIn = computed(() => Boolean(session.value?.user));
  const role = computed(() => session.value?.user?.role || "");
  const isTeacher = computed(() => role.value === "TEACHER");
  const isStudent = computed(() => role.value === "STUDENT");
  const user = computed(() => session.value?.user || null);

  function setSessionUser(nextUser) {
    session.value = nextUser?.userId
      ? { user: normalizeAuthUser(nextUser) }
      : null;
    persistSession(session.value);
  }

  async function restoreSession() {
    if (restoring.value) {
      return session.value;
    }

    restoring.value = true;
    try {
      const result = await getCurrentSession();
      const currentUser = result?.data || null;
      setSessionUser(currentUser);
      return session.value;
    } catch (_error) {
      session.value = null;
      workbench.value = null;
      persistSession(null);
      return null;
    } finally {
      restoring.value = false;
    }
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
      const restored = await restoreSession();
      if (!restored?.user?.userId || !restored?.user?.role) {
        workbench.value = null;
        return null;
      }
    }

    const currentUser = session.value.user;
    const result = currentUser.role === "TEACHER"
      ? await getTeacherWorkbench(currentUser.userId)
      : await getStudentWorkbench(currentUser.userId);

    workbench.value = result.data;
    return workbench.value;
  }

  async function logoutSession() {
    session.value = null;
    workbench.value = null;
    errorMessage.value = "";
    persistSession(null);

    try {
      await logout();
    } catch (_error) {
      // Ignore backend logout failures and continue clearing local state.
    }
  }

  return {
    session,
    workbench,
    loading,
    restoring,
    errorMessage,
    isLoggedIn,
    isTeacher,
    isStudent,
    role,
    user,
    restoreSession,
    loginWithPassword,
    registerAccount,
    loadWorkbench,
    logout: logoutSession
  };
});
