<template>
  <section class="page-card">
    <div class="page-copy">
      <p class="page-tag">桌面工作台</p>
      <h2>角色入口已经收口到桌面端导航</h2>
      <p>
        登录后会先进入这一层，再按角色流向教师或学生工作区。现在教师端已经接入试卷中心、导题建卷和学生中心，
        学生端也开始迁入考试中心和成绩中心。
      </p>
    </div>
    <div class="card-grid">
      <article class="mini-card">
        <h3>当前用户</h3>
        <p>{{ userCopy }}</p>
      </article>
      <article class="mini-card">
        <h3>建议入口</h3>
        <p>{{ nextCopy }}</p>
      </article>
      <article class="mini-card">
        <h3>当前栈</h3>
        <p>Vue3 渲染层、Electron 桌面壳、Spring Boot API</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { useSessionStore } from "@/stores/session";

const sessionStore = useSessionStore();

const userCopy = computed(() => {
  if (!sessionStore.user) {
    return "当前尚未登录";
  }

  return `${sessionStore.user.realName} / ${sessionStore.isTeacher ? "教师" : "学生"}`;
});

const nextCopy = computed(() => {
  if (!sessionStore.isLoggedIn) {
    return "请先完成登录";
  }

  return sessionStore.isTeacher
    ? "进入教师工作区：试卷中心、导题建卷、学生中心"
    : "进入学生工作区：考试中心、成绩中心、记录详情";
});
</script>
