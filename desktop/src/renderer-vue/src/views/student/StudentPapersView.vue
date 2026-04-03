<template>
  <section class="workspace-page">
    <div class="page-copy">
      <p class="page-tag">考试中心</p>
      <h2>可参加试卷</h2>
      <p>这里展示学生当前可参加的试卷，并直接提供科目筛选、自动刷新、开始考试和断点恢复入口。</p>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>
    <StatusBanner v-if="successMessage" tone="info">
      {{ successMessage }}
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
          <p class="section-copy">
            已发布试卷会在这里集中展示。支持按科目筛选，并会在页面聚焦时和每 30 秒自动刷新一次。
          </p>
        </div>
        <div class="section-tools">
          <span class="refresh-meta">最近同步：{{ formatDateTime(lastUpdatedAt) }}</span>
          <button class="ghost-button" type="button" :disabled="loading" @click="handleManualRefresh">
            {{ loading ? "刷新中..." : "刷新列表" }}
          </button>
          <RouterLink class="text-link" to="/student/records">查看我的记录</RouterLink>
        </div>
      </div>

      <div v-if="subjectFilters.length > 1" class="subject-filter-wrap">
        <button
          v-for="subject in subjectFilters"
          :key="subject"
          type="button"
          :class="['subject-chip', activeSubject === subject ? 'subject-chip-active' : '']"
          @click="activeSubject = subject"
        >
          {{ subject }}
        </button>
      </div>

      <div class="detail-tips">
        <p>
          当前分类：{{ activeSubject }}
          / 试卷 {{ totalFilteredPapers }} 张
          / 进行中 {{ filteredInProgressCount }} 张
          / 已完成 {{ filteredCompletedCount }} 张
        </p>
      </div>

      <div v-if="loading" class="empty-copy">正在加载试卷数据...</div>
      <template v-else-if="totalFilteredPapers">
        <WorkspacePagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-size-options="pageSizeOptions"
          :start="pageSummary.start"
          :end="pageSummary.end"
          :total-pages="totalPages"
          :total-items="totalFilteredPapers"
          item-label="张试卷"
          @change-page="goToPage"
          @update:page-size="pageSize = $event"
        />

        <div class="record-list">
          <article v-for="paper in paginatedPapers" :key="paper.paperId" class="record-card">
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
      </template>
      <p v-else class="empty-copy">当前分类下暂无可参加试卷。</p>
    </article>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { RouterLink } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import WorkspacePagination from "@/components/WorkspacePagination.vue";
import { usePagination } from "@/composables/usePagination";
import { useSessionStore } from "@/stores/session";
import { getStudentPapers } from "@/services/student-api";

const sessionStore = useSessionStore();
const loading = ref(false);
const summary = ref(null);
const papers = ref([]);
const errorMessage = ref("");
const successMessage = ref("");
const activeSubject = ref("全部");
const lastUpdatedAt = ref(null);
const pageSizeOptions = [6, 9, 12];
let refreshTimer = null;
let focusHandler = null;

const subjectFilters = computed(() => {
  const subjectSet = new Set(["全部"]);
  for (const paper of papers.value) {
    subjectSet.add(normalizeSubject(paper.subject));
  }
  return Array.from(subjectSet);
});

const filteredPapers = computed(() => {
  if (activeSubject.value === "全部") {
    return papers.value;
  }
  return papers.value.filter((paper) => normalizeSubject(paper.subject) === activeSubject.value);
});

const filteredInProgressCount = computed(() =>
  filteredPapers.value.filter((paper) => Boolean(paper.hasInProgressRecord)).length
);

const filteredCompletedCount = computed(() =>
  filteredPapers.value.filter((paper) =>
    ["SUBMITTED", "TIMEOUT"].includes(String(paper.latestRecord?.status || ""))
  ).length
);
const {
  currentPage,
  pageSize,
  totalItems: totalFilteredPapers,
  totalPages,
  paginatedItems: paginatedPapers,
  pageSummary,
  goToPage
} = usePagination(filteredPapers, { initialPageSize: 6 });

watch(subjectFilters, (subjects) => {
  if (!subjects.includes(activeSubject.value)) {
    activeSubject.value = "全部";
  }
});

async function loadPapers(options = {}) {
  const { silent = false, showSuccessMessage = false } = options;
  if (!sessionStore.user?.userId) {
    return;
  }

  if (!silent) {
    loading.value = true;
  }
  errorMessage.value = "";
  if (showSuccessMessage) {
    successMessage.value = "";
  }

  try {
    const result = await getStudentPapers(sessionStore.user.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载试卷数据失败");
    }

    summary.value = result.data?.summary || null;
    papers.value = result.data?.papers || [];
    lastUpdatedAt.value = new Date();

    if (showSuccessMessage) {
      successMessage.value = "试卷列表已刷新，已同步最新发布和作答状态。";
    }
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载试卷数据失败";
  } finally {
    if (!silent) {
      loading.value = false;
    }
  }
}

function handleManualRefresh() {
  loadPapers({ silent: false, showSuccessMessage: true });
}

function bindAutoRefresh() {
  focusHandler = () => {
    loadPapers({ silent: true }).catch(() => undefined);
  };
  window.addEventListener("focus", focusHandler);

  refreshTimer = window.setInterval(() => {
    if (document.visibilityState === "visible") {
      loadPapers({ silent: true }).catch(() => undefined);
    }
  }, 30000);
}

function unbindAutoRefresh() {
  if (focusHandler) {
    window.removeEventListener("focus", focusHandler);
    focusHandler = null;
  }
  if (refreshTimer) {
    window.clearInterval(refreshTimer);
    refreshTimer = null;
  }
}

function normalizeSubject(value) {
  if (!value || !String(value).trim()) {
    return "未分类";
  }
  return String(value).trim();
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

onMounted(async () => {
  await loadPapers();
  bindAutoRefresh();
});

onBeforeUnmount(() => {
  unbindAutoRefresh();
});
</script>
