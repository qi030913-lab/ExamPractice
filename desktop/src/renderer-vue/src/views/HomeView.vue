<template>
  <section class="page-card">
    <div class="page-copy">
      <p class="page-tag">桌面工作台</p>
      <h2>统一进入教师与学生工作区</h2>
      <p>
        登录后会先进入这一层，再根据当前角色进入对应工作区。教师端可前往试卷中心、导题建卷和学生中心，
        学生端可前往考试中心、成绩中心和学习成就页。
      </p>
    </div>
    <div class="card-grid">
      <article class="mini-card">
        <h3>{{ accountLabel }}</h3>
        <p>{{ displayName }}</p>
        <span class="overview-meta">{{ loginIdCopy }}</span>
        <button
          v-if="sessionStore.isLoggedIn"
          class="overview-logout"
          type="button"
          @click="handleLogout"
        >
          退出登录
        </button>
      </article>
      <article class="mini-card">
        <h3>建议入口</h3>
        <p>{{ nextCopy }}</p>
      </article>
      <article class="mini-card">
        <h3>使用方式</h3>
        <p>从工作台进入对应角色功能区，教师可管理试卷与学生，学生可参加考试并查看成绩。</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useSessionStore } from "@/stores/session";

const router = useRouter();
const sessionStore = useSessionStore();

const accountLabel = computed(() => {
  if (sessionStore.isTeacher) {
    return "教师账号";
  }

  if (sessionStore.isStudent) {
    return "学生账号";
  }

  return "当前账号";
});

const displayName = computed(() => sessionStore.user?.realName || "当前尚未登录");
const loginIdCopy = computed(() => sessionStore.user?.loginId || "未检测到账号信息");

const nextCopy = computed(() => {
  if (!sessionStore.isLoggedIn) {
    return "请先完成登录";
  }

  return sessionStore.isTeacher
    ? "进入教师工作区：试卷中心、导题建卷、学生中心"
    : "进入学生工作区：考试中心、成绩中心、记录详情";
});

function handleLogout() {
  sessionStore.logout();
  router.push("/login");
}
</script>
