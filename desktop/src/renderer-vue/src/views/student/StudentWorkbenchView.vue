<template>
  <section class="workspace-page">
    <div class="workspace-hero">
      <div class="page-copy">
        <p class="page-tag">学生工作台</p>
        <h2>把考试、恢复作答和成绩复盘收口到桌面端</h2>
        <p>
          这一层承接原来 Swing 学生首页的主要导航。现在已经能从这里进入考试中心、重新进入进行中的考试、查看记录和复盘成绩。
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
        <p>已发布试卷会统一收口在这里，并直接提供开始考试或继续作答的入口。</p>
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

    <div class="list-grid">
      <article class="list-card">
        <h3>最近可参加试卷</h3>
        <div v-if="availablePapers.length" class="list-stack">
          <RouterLink
            v-for="paper in availablePapers"
            :key="paper.paperId"
            class="list-row"
            :to="`/student/papers/${paper.paperId}/exam`"
          >
            <strong>{{ paper.paperName }}</strong>
            <span>{{ paper.subject || "-" }} / {{ paper.questionCount ?? 0 }} 题 / {{ paper.totalScore ?? 0 }} 分</span>
          </RouterLink>
        </div>
        <p v-else class="empty-copy">当前暂无试卷数据。</p>
      </article>

      <article class="list-card">
        <h3>最近考试记录</h3>
        <div v-if="recentRecords.length" class="list-stack">
          <RouterLink
            v-for="record in recentRecords"
            :key="record.recordId"
            class="list-row"
            :to="record.resumeAvailable ? `/student/papers/${record.paperId}/exam` : `/student/records/${record.recordId}`"
          >
            <strong>{{ record.paperName || "未命名试卷" }}</strong>
            <span>
              {{ formatStatus(record.status) }}
              / {{ record.resumeAvailable ? "可继续作答" : formatNullableScore(record.score) }}
            </span>
          </RouterLink>
        </div>
        <p v-else class="empty-copy">当前暂无考试记录。</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { RouterLink } from "vue-router";
import { useSessionStore } from "@/stores/session";

const sessionStore = useSessionStore();

const stats = computed(() => sessionStore.workbench?.stats || {});
const availablePapers = computed(() => sessionStore.workbench?.availablePapers || []);
const recentRecords = computed(() => sessionStore.workbench?.recentRecords || []);
const ongoingRecord = computed(() =>
  recentRecords.value.find((record) => record.resumeAvailable || record.status === "IN_PROGRESS") || null
);
const averageScoreCopy = computed(() => {
  const value = Number(stats.value?.averageScore);
  return Number.isFinite(value) ? value.toFixed(1) : "0.0";
});

function formatNullableScore(value) {
  return value === null || value === undefined || value === "" ? "暂未评分" : value;
}

function formatStatus(status) {
  const statusMap = {
    IN_PROGRESS: "进行中",
    SUBMITTED: "已提交",
    TIMEOUT: "超时提交",
    NOT_STARTED: "未开始"
  };
  return statusMap[status] || status || "未知状态";
}

onMounted(() => {
  if (sessionStore.isStudent) {
    sessionStore.loadWorkbench().catch(() => undefined);
  }
});
</script>
