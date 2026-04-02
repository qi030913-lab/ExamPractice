<template>
  <section class="workspace-page">
    <div class="workspace-hero">
      <div class="page-copy">
        <p class="page-tag">学生工作台</p>
        <h2>把考试与成绩入口收口到桌面端</h2>
        <p>
          这一层承接原来 Swing 学生首页的主要导航。当前先把可参加试卷、考试记录和成绩复盘三块串起来，
          下一轮继续往在线答题界面推进。
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

    <div class="action-grid">
      <RouterLink class="action-card action-card-accent" to="/student/papers">
        <p class="action-tag">考试中心</p>
        <h3>查看可参加试卷</h3>
        <p>先查看已发布试卷、考试时长、及格分，以及最近一次考试状态。</p>
      </RouterLink>
      <RouterLink class="action-card" to="/student/records">
        <p class="action-tag">成绩中心</p>
        <h3>查看我的记录</h3>
        <p>统一查看历次考试成绩、正确题数和错误题数，并继续进入详情页复盘。</p>
      </RouterLink>
      <article class="action-card">
        <p class="action-tag">迁移进度</p>
        <h3>在线答题页下一轮继续</h3>
        <p>本轮先把学生端浏览链路迁完，确保桌面端已经能完整承接常用查看场景。</p>
      </article>
    </div>

    <div class="list-grid">
      <article class="list-card">
        <h3>最近可参加试卷</h3>
        <div v-if="availablePapers.length" class="list-stack">
          <div v-for="paper in availablePapers" :key="paper.paperId" class="list-row">
            <strong>{{ paper.paperName }}</strong>
            <span>{{ paper.subject }} / {{ paper.questionCount }} 题 / {{ paper.totalScore }} 分</span>
          </div>
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
            :to="`/student/records/${record.recordId}`"
          >
            <strong>{{ record.paperName || "未命名试卷" }}</strong>
            <span>{{ formatStatus(record.status) }} / {{ formatNullableScore(record.score) }}</span>
          </RouterLink>
        </div>
        <p v-else class="empty-copy">当前暂无考试记录。</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink } from "vue-router";
import { useSessionStore } from "@/stores/session";

const sessionStore = useSessionStore();

const stats = computed(() => sessionStore.workbench?.stats || {});
const availablePapers = computed(() => sessionStore.workbench?.availablePapers || []);
const recentRecords = computed(() => sessionStore.workbench?.recentRecords || []);
const averageScoreCopy = computed(() => {
  const value = stats.value?.averageScore;
  return Number.isFinite(value) ? Number(value).toFixed(1) : "0.0";
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
</script>
