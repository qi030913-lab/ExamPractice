<template>
  <section class="workspace-page">
    <div class="page-copy">
      <p class="page-tag">成绩中心</p>
      <h2>我的考试记录</h2>
      <p>这里承接原来学生端成绩与记录查看场景，方便在桌面端快速查看历次考试结果。</p>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>

    <div v-if="summary" class="summary-row compact-summary">
      <article class="mini-card">
        <h3>记录数</h3>
        <p>{{ summary.recordCount ?? 0 }}</p>
      </article>
      <article class="mini-card">
        <h3>已提交</h3>
        <p>{{ summary.submittedCount ?? 0 }}</p>
      </article>
      <article class="mini-card">
        <h3>平均分</h3>
        <p>{{ formatScore(summary.averageScore) }}</p>
      </article>
    </div>

    <article class="list-card">
      <div class="section-head">
        <div>
          <h3>记录列表</h3>
          <p class="section-copy">点击任意一条记录，查看该次考试结果与逐题作答详情。</p>
        </div>
        <RouterLink class="text-link" to="/student/papers">返回考试中心</RouterLink>
      </div>

      <div v-if="loading" class="empty-copy">正在加载考试记录...</div>
      <div v-else-if="records.length" class="record-list">
        <RouterLink
          v-for="record in records"
          :key="record.recordId"
          class="record-card"
          :to="`/student/records/${record.recordId}`"
        >
          <div class="record-card-head">
            <strong>{{ record.paperName || "未命名试卷" }}</strong>
            <span :class="['pill', isSubmitted(record.status) ? 'pill-success' : 'pill-muted']">
              {{ formatStatus(record.status) }}
            </span>
          </div>
          <div class="record-card-meta">
            <span>成绩：{{ formatNullableScore(record.score) }}</span>
            <span>总分：{{ record.totalScore ?? 0 }}</span>
            <span>正确：{{ record.correctCount ?? 0 }}</span>
            <span>错误：{{ record.wrongCount ?? 0 }}</span>
            <span>开始：{{ formatDateTime(record.startTime) }}</span>
            <span>提交：{{ formatDateTime(record.submitTime) }}</span>
          </div>
        </RouterLink>
      </div>
      <p v-else class="empty-copy">你还没有考试记录。</p>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getStudentRecords } from "@/services/student-api";

const sessionStore = useSessionStore();
const loading = ref(false);
const summary = ref(null);
const records = ref([]);
const errorMessage = ref("");

async function loadRecords() {
  if (!sessionStore.user?.userId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await getStudentRecords(sessionStore.user.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载考试记录失败");
    }

    summary.value = result.data?.summary || null;
    records.value = result.data?.records || [];
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载考试记录失败";
  } finally {
    loading.value = false;
  }
}

function formatScore(value) {
  return Number.isFinite(value) ? Number(value).toFixed(1) : "0.0";
}

function formatNullableScore(value) {
  return value === null || value === undefined || value === "" ? "暂未评分" : value;
}

function isSubmitted(status) {
  return status === "SUBMITTED" || status === "TIMEOUT";
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

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("zh-CN", { hour12: false });
}

onMounted(loadRecords);
</script>
