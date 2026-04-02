<template>
  <section class="workspace-page">
    <div class="section-head">
      <div class="page-copy">
        <p class="page-tag">提交结果</p>
        <h2>{{ result?.paperName || "考试已提交" }}</h2>
        <p>这里会优先展示本次提交后的即时结果，并提供继续查看记录详情和错题解析的入口。</p>
      </div>
      <RouterLink class="text-link" to="/student/papers">返回考试中心</RouterLink>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>

    <div v-if="loading" class="empty-copy">正在加载提交结果...</div>
    <template v-else-if="result">
      <article :class="['result-hero', result.passed ? 'result-hero-pass' : 'result-hero-fail']">
        <div>
          <p class="action-tag">{{ result.passed ? "考试通过" : "已完成提交" }}</p>
          <h2>{{ result.paperName || "考试结果" }}</h2>
          <p>
            {{ result.passed
              ? "本次考试已达到及格线，可以继续进入详情页查看逐题结果。"
              : "试卷已成功提交，建议继续查看记录详情和错题解析。" }}
          </p>
        </div>
        <div class="result-score">
          <strong>{{ formatScore(result.score) }}</strong>
          <span>/ {{ result.totalScore ?? 0 }} 分</span>
          <p>{{ result.passed ? "已通过" : "未通过" }}</p>
        </div>
      </article>

      <div class="summary-row compact-summary">
        <article class="mini-card">
          <h3>作答进度</h3>
          <p>{{ result.answeredCount ?? 0 }}/{{ result.questionCount ?? 0 }}</p>
        </article>
        <article class="mini-card">
          <h3>答对题数</h3>
          <p>{{ result.correctCount ?? 0 }}</p>
        </article>
        <article class="mini-card">
          <h3>作答用时</h3>
          <p>{{ formatDuration(result.durationSeconds) }}</p>
        </article>
      </div>

      <article class="list-card">
        <div class="detail-list">
          <div class="detail-row">
            <span>科目</span>
            <strong>{{ result.subject || "-" }}</strong>
          </div>
          <div class="detail-row">
            <span>及格分</span>
            <strong>{{ result.passScore ?? "-" }}</strong>
          </div>
          <div class="detail-row">
            <span>状态</span>
            <strong>{{ formatStatus(result.status) }}</strong>
          </div>
          <div class="detail-row">
            <span>错题数</span>
            <strong>{{ result.wrongCount ?? 0 }}</strong>
          </div>
          <div class="detail-row">
            <span>提交时间</span>
            <strong>{{ formatDateTime(result.submitTime) }}</strong>
          </div>
        </div>

        <div class="action-row">
          <RouterLink class="ghost-link-button" :to="`/student/records/${result.recordId}?submitted=1`">
            查看记录详情
          </RouterLink>
          <RouterLink class="ghost-link-button" :to="`/student/records/${result.recordId}`">
            查看逐题解析
          </RouterLink>
          <RouterLink class="ghost-link-button" to="/student/papers">
            返回考试中心
          </RouterLink>
        </div>
      </article>
    </template>
    <p v-else class="empty-copy">暂时没有可展示的提交结果。</p>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getStudentRecordDetail } from "@/services/student-api";

const route = useRoute();
const sessionStore = useSessionStore();

const loading = ref(false);
const result = ref(null);
const errorMessage = ref("");

async function loadResult() {
  if (!sessionStore.user?.userId || !route.params.recordId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const restoredResult = restoreStoredResult(route.params.recordId);
    if (restoredResult) {
      result.value = restoredResult;
      return;
    }

    const detailResult = await getStudentRecordDetail(sessionStore.user.userId, route.params.recordId);
    if (!detailResult?.success) {
      throw new Error(detailResult?.message || "加载提交结果失败");
    }

    result.value = mapRecordToResult(detailResult.data?.record);
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载提交结果失败";
  } finally {
    loading.value = false;
  }
}

function restoreStoredResult(recordId) {
  try {
    const raw = window.localStorage.getItem(`exampractice.desktop.submit-result.${recordId}`);
    return raw ? JSON.parse(raw) : null;
  } catch (_error) {
    window.localStorage.removeItem(`exampractice.desktop.submit-result.${recordId}`);
    return null;
  }
}

function mapRecordToResult(record) {
  if (!record) {
    return null;
  }

  return {
    recordId: record.recordId,
    paperId: record.paperId,
    paperName: record.paperName,
    subject: record.subject,
    score: record.score,
    totalScore: record.totalScore,
    passScore: record.passScore,
    passed: record.passed,
    status: record.status,
    submitTime: record.submitTime,
    durationSeconds: record.durationSeconds,
    questionCount: record.questionCount,
    answeredCount: record.answeredCount,
    correctCount: record.correctCount,
    wrongCount: record.wrongCount
  };
}

function formatScore(value) {
  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue : value ?? "-";
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

function formatDuration(value) {
  const numericValue = Number(value);
  if (!Number.isFinite(numericValue) || numericValue <= 0) {
    return "-";
  }

  const minutes = Math.floor(numericValue / 60);
  const seconds = numericValue % 60;
  if (minutes <= 0) {
    return `${seconds} 秒`;
  }
  return `${minutes} 分 ${seconds} 秒`;
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

onMounted(loadResult);
</script>
