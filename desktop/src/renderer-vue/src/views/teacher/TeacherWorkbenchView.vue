<template>
  <section class="workspace-page">
    <div class="workspace-hero">
      <div class="page-copy">
        <p class="page-tag">教师工作台</p>
        <h2>快速进入常用教学管理功能</h2>
        <p>
          这里集中展示教师最常用的工作入口，包括试卷管理、导题建卷和学生管理，
          方便你在同一套界面流程中完成日常操作。
        </p>
      </div>
      <div class="hero-metrics">
        <article class="mini-card">
          <h3>试卷总数</h3>
          <p>{{ stats.paperCount ?? 0 }}</p>
        </article>
        <article class="mini-card">
          <h3>已发布试卷</h3>
          <p>{{ stats.publishedCount ?? 0 }}</p>
        </article>
        <article class="mini-card">
          <h3>学生人数</h3>
          <p>{{ stats.studentCount ?? 0 }}</p>
        </article>
      </div>
    </div>

    <div class="action-grid">
      <RouterLink class="action-card" to="/teacher/papers">
        <p class="action-tag">试卷管理</p>
        <h3>进入试卷中心</h3>
        <p>查看所有试卷、调整发布状态、删除旧试卷。</p>
      </RouterLink>
      <RouterLink class="action-card action-card-accent" to="/teacher/import">
        <p class="action-tag">导题建卷</p>
        <h3>批量导入题目并建卷</h3>
        <p>支持粘贴题目文本或读取本地 txt 文件，直接生成新试卷。</p>
      </RouterLink>
      <RouterLink class="action-card" to="/teacher/students">
        <p class="action-tag">学生管理</p>
        <h3>查看学生与考试记录</h3>
        <p>查看学生资料、考试记录和作答详情，便于快速跟进学习情况。</p>
      </RouterLink>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink } from "vue-router";
import { useSessionStore } from "@/stores/session";

const sessionStore = useSessionStore();

const stats = computed(() => sessionStore.workbench?.stats || {});
</script>
