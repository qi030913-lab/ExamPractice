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
        <RouterLink v-if="sessionStore.isTeacher" to="/teacher">教师端</RouterLink>
        <RouterLink v-if="sessionStore.isStudent" to="/student">学生端</RouterLink>
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
import { RouterLink } from "vue-router";
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useSessionStore } from "@/stores/session";

const router = useRouter();
const sessionStore = useSessionStore();

const headerCopy = computed(() => {
  if (!sessionStore.isLoggedIn) {
    return "当前处于 Vue3 桌面端迁移阶段，正在逐步替换 Swing 页面。";
  }

  return `${sessionStore.user?.realName || "当前用户"} 已登录，当前角色为 ${sessionStore.isTeacher ? "教师" : "学生"}。`;
});

function handleLogout() {
  sessionStore.logout();
  router.push("/login");
}
</script>
