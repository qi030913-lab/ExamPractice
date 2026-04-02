<template>
  <section class="page-card">
    <div class="page-copy">
      <p class="page-tag">学生端</p>
      <h2>学生工作台</h2>
      <p>
        这一层会参考原 Swing 学生主页，后续承接考试中心、成绩中心、成就页。
      </p>
    </div>
    <div class="card-grid">
      <article class="mini-card">
        <h3>考试中心</h3>
        <p>当前可考试卷 {{ stats.publishedPaperCount ?? 0 }} 份。</p>
      </article>
      <article class="mini-card">
        <h3>成绩中心</h3>
        <p>已有考试记录 {{ stats.recordCount ?? 0 }} 条，已提交 {{ stats.submittedCount ?? 0 }} 次。</p>
      </article>
      <article class="mini-card">
        <h3>成就页</h3>
        <p>当前平均分 {{ averageScoreCopy }}。</p>
      </article>
    </div>
    <div class="list-grid">
      <article class="list-card">
        <h3>可考试卷</h3>
        <div v-if="availablePapers.length" class="list-stack">
          <div v-for="paper in availablePapers" :key="paper.paperId" class="list-row">
            <strong>{{ paper.paperName }}</strong>
            <span>{{ paper.subject }} / {{ paper.questionCount }} 题 / {{ paper.totalScore }} 分</span>
          </div>
        </div>
        <p v-else class="empty-copy">暂无试卷数据。</p>
      </article>
      <article class="list-card">
        <h3>最近记录</h3>
        <div v-if="recentRecords.length" class="list-stack">
          <div v-for="record in recentRecords" :key="record.recordId" class="list-row">
            <strong>{{ record.paperName || '未知试卷' }}</strong>
            <span>{{ record.status || '未知状态' }} / {{ record.score ?? '暂无成绩' }}</span>
          </div>
        </div>
        <p v-else class="empty-copy">暂无考试记录。</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { useSessionStore } from "@/stores/session";

const sessionStore = useSessionStore();

const stats = computed(() => sessionStore.workbench?.stats || {});
const availablePapers = computed(() => sessionStore.workbench?.availablePapers || []);
const recentRecords = computed(() => sessionStore.workbench?.recentRecords || []);
const averageScoreCopy = computed(() => {
  const value = stats.value?.averageScore;
  return Number.isFinite(value) ? Number(value).toFixed(1) : "0.0";
});
</script>
