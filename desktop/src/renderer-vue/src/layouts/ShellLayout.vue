<template>
  <div class="shell-layout">
    <header class="shell-header">
      <div>
        <p class="shell-eyebrow">Vue3 + Electron + Spring Boot</p>
        <h1>在线考试系统桌面端</h1>
        <p class="shell-subcopy">
          {{ headerCopy }}
        </p>
      </div>
      <nav class="shell-nav">
        <RouterLink to="/login">登录</RouterLink>
        <RouterLink to="/home">工作台</RouterLink>
        <RouterLink v-if="sessionStore.isTeacher" to="/teacher">教师首页</RouterLink>
        <RouterLink v-if="sessionStore.isTeacher" to="/teacher/papers">试卷中心</RouterLink>
        <RouterLink v-if="sessionStore.isTeacher" to="/teacher/import">导题建卷</RouterLink>
        <RouterLink v-if="sessionStore.isTeacher" to="/teacher/students">学生中心</RouterLink>
        <RouterLink v-if="sessionStore.isStudent" to="/student">学生首页</RouterLink>
        <button v-if="sessionStore.isLoggedIn" class="shell-logout" type="button" @click="handleLogout">
          退出
        </button>
      </nav>
    </header>
    <main class="shell-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { useSessionStore } from "@/stores/session";

const router = useRouter();
const sessionStore = useSessionStore();

const headerCopy = computed(() => {
  if (!sessionStore.isLoggedIn) {
    return "当前处于桌面端迁移阶段，我们正在逐步用 Vue3 页面替代原来的 Swing 界面。";
  }

  const roleLabel = sessionStore.isTeacher ? "教师" : "学生";
  const name = sessionStore.user?.realName || "当前用户";
  return `${name} 已登录，当前角色为 ${roleLabel}。`;
});

function handleLogout() {
  sessionStore.logout();
  router.push("/login");
}
</script>
