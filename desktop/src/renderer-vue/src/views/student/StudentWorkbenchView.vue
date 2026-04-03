<template>
  <section class="workspace-page">
    <div class="workspace-hero">
      <div class="page-copy">
        <p class="page-tag">学生工作台</p>
        <h2>集中进入考试、记录与成绩分析</h2>
        <p>
          这里汇总了学生端最常用的入口，你可以从这里开始考试、继续未完成作答，
          查看历史记录，并通过图表复盘学习表现。
        </p>
      </div>
      <div class="hero-metrics">
        <article class="mini-card">
          <h3>可参加试卷</h3>
          <p>{{ stats.publishedPaperCount ?? 0 }}</p>
        </article>
        <article class="mini-card">
          <h3>考试记录</h3>
          <p>{{ stats.recordCount ?? 0 }}</p>
        </article>
        <article class="mini-card">
          <h3>平均分</h3>
          <p>{{ averageScoreCopy }}</p>
        </article>
      </div>
    </div>

    <article v-if="ongoingRecord" class="ongoing-banner">
      <div>
        <p class="action-tag">进行中考试</p>
        <h3>{{ ongoingRecord.paperName || "你有一场未提交的考试" }}</h3>
        <p>系统检测到你有一场进行中的考试。你可以从这里直接恢复作答，无需重新开始。</p>
      </div>
      <div class="action-row">
        <RouterLink class="ghost-link-button" :to="`/student/papers/${ongoingRecord.paperId}/exam`">
          重新进入考试
        </RouterLink>
        <RouterLink class="text-link" to="/student/records">
          查看我的记录
        </RouterLink>
      </div>
    </article>

    <div class="action-grid">
      <RouterLink class="action-card action-card-accent" to="/student/papers">
        <p class="action-tag">考试中心</p>
        <h3>{{ ongoingRecord ? "继续未完成考试" : "查看可参加试卷" }}</h3>
        <p>查看当前可参加的试卷，并直接开始考试或继续未完成的作答。</p>
      </RouterLink>
      <RouterLink class="action-card" to="/student/records">
        <p class="action-tag">成绩中心</p>
        <h3>查看我的记录</h3>
        <p>统一查看历次考试成绩、正确题数和错误题数，并继续进入详情页复盘。</p>
      </RouterLink>
      <RouterLink class="action-card" to="/student/achievement">
        <p class="action-tag">学生成就</p>
        <h3>查看趋势与统计图表</h3>
        <p>使用图表查看成绩走势、题型准确率和各学科表现，方便长期复盘自己的考试状态。</p>
      </RouterLink>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { RouterLink } from "vue-router";
import { useSessionStore } from "@/stores/session";

const sessionStore = useSessionStore();

const stats = computed(() => sessionStore.workbench?.stats || {});
const ongoingRecord = computed(() => sessionStore.workbench?.ongoingRecord || null);
const averageScoreCopy = computed(() => {
  const value = Number(stats.value?.averageScore);
  return Number.isFinite(value) ? value.toFixed(1) : "0.0";
});

onMounted(() => {
  if (sessionStore.isStudent) {
    sessionStore.loadWorkbench().catch(() => undefined);
  }
});
</script>
