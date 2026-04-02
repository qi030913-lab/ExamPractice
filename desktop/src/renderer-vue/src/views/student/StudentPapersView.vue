<template>
  <section class="workspace-page">
    <div class="page-copy">
      <p class="page-tag">考试中心</p>
      <h2>可参加试卷</h2>
      <p>这里展示学生当前可参加的试卷，并直接提供开始考试、断点恢复和查看最近记录的入口。</p>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>

    <div v-if="summary" class="summary-row compact-summary">
      <article class="mini-card">
        <h3>试卷数</h3>
        <p>{{ summary.paperCount ?? 0 }}</p>
      </article>
      <article class="mini-card">
        <h3>已完成</h3>
        <p>{{ summary.completedCount ?? 0 }}</p>
      </article>
      <article class="mini-card">
        <h3>进行中</h3>
        <p>{{ summary.inProgressCount ?? 0 }}</p>
      </article>
    </div>

    <article class="list-card">
      <div class="section-head">
        <div>
          <h3>试卷列表</h3>
          <p class="section-copy">已发布试卷会在这里集中展示，点击按钮即可进入桌面端考试页。</p>
        </div>
        <RouterLink class="text-link" to="/student/records">查看我的记录</RouterLink>
      </div>

      <div v-if="loading" class="empty-copy">正在加载试卷数据...</div>
      <div v-else-if="papers.length" class="record-list">
        <article v-for="paper in papers" :key="paper.paperId" class="record-card">
          <div class="record-card-head">
            <strong>{{ paper.paperName }}</strong>
            <span :class="['pill', paper.hasInProgressRecord ? 'pill-accent' : 'pill-success']">
              {{ paper.hasInProgressRecord ? "进行中" : "已发布" }}
            </span>
          </div>
          <div class="record-card-meta">
            <span>科目：{{ paper.subject || "-" }}</span>
            <span>题量：{{ paper.questionCount ?? 0 }}</span>
            <span>总分：{{ paper.totalScore ?? 0 }}</span>
            <span>及格分：{{ paper.passScore ?? 0 }}</span>
            <span>时长：{{ paper.duration ?? 0 }} 分钟</span>
            <span>说明：{{ paper.description || "暂无说明" }}</span>
          </div>

          <div v-if="paper.latestRecord" class="detail-tips">
            <p>
              最近记录：{{ formatStatus(paper.latestRecord.status) }}
              / {{ paper.latestRecord.resumeAvailable ? "可继续作答" : `成绩 ${formatNullableScore(paper.latestRecord.score)}` }}
              / {{ formatDateTime(paper.latestRecord.submitTime || paper.latestRecord.startTime) }}
            </p>
          </div>

          <div class="action-row">
            <RouterLink class="ghost-link-button" :to="`/student/papers/${paper.paperId}/exam`">
              {{ paper.hasInProgressRecord ? "继续作答" : "开始考试" }}
            </RouterLink>
            <RouterLink
              v-if="paper.latestRecord?.recordId"
              class="ghost-link-button"
              :to="paper.latestRecord.resumeAvailable ? `/student/papers/${paper.paperId}/exam` : `/student/records/${paper.latestRecord.recordId}`"
            >
              {{ paper.latestRecord.resumeAvailable ? "重新进入进行中考试" : "查看最近记录" }}
            </RouterLink>
          </div>
        </article>
      </div>
      <p v-else class="empty-copy">当前暂无可参加试卷。</p>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getStudentPapers } from "@/services/student-api";

const sessionStore = useSessionStore();
const loading = ref(false);
const summary = ref(null);
const papers = ref([]);
const errorMessage = ref("");

async function loadPapers() {
  if (!sessionStore.user?.userId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await getStudentPapers(sessionStore.user.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载试卷数据失败");
    }

    summary.value = result.data?.summary || null;
    papers.value = result.data?.papers || [];
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载试卷数据失败";
  } finally {
    loading.value = false;
  }
}

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

onMounted(loadPapers);
</script>
