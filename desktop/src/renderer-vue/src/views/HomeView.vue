<template>
  <section class="page-card">
    <div class="page-copy">
      <p class="page-tag">第二阶段</p>
      <h2>桌面工作台</h2>
      <p>
        这里会承接你现在 Electron renderer 里的角色首页能力。
        后续迁移时，教师和学生都会先进入这个工作台层，再分流到各自业务区。
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
        <h3>系统状态</h3>
        <p>Spring Boot API、Electron 壳、Vue3 页面</p>
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
    ? "进入教师端：试卷管理、导题建卷、学生管理"
    : "进入学生端：考试中心、成绩中心、成就页";
});
</script>
