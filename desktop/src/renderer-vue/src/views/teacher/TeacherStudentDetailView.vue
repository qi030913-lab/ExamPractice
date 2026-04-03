<template>
  <section class="workspace-page">
    <div class="section-head">
      <div class="page-copy">
        <p class="page-tag">学生详情</p>
        <h2>{{ student?.realName || "学生详情" }}</h2>
        <p>从学生管理列表进入后，可以在这里查看学生资料、考试概览，以及历次考试记录。</p>
      </div>
      <RouterLink class="text-link" to="/teacher/students">返回学生中心</RouterLink>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>

    <div v-if="loading" class="empty-copy">正在加载学生详情...</div>
    <template v-else-if="student">
      <div class="detail-layout">
        <article class="list-card">
          <h3>学生资料</h3>
          <div class="detail-list">
            <div class="detail-row">
              <span>姓名</span>
              <strong>{{ student.realName || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>学号</span>
              <strong>{{ student.loginId || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>邮箱</span>
              <strong>{{ student.email || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>电话</span>
              <strong>{{ student.phone || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>性别</span>
              <strong>{{ student.gender || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>状态</span>
              <strong>{{ student.status || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>创建时间</span>
              <strong>{{ formatDateTime(student.createTime) }}</strong>
            </div>
            <div class="detail-row">
              <span>更新时间</span>
              <strong>{{ formatDateTime(student.updateTime) }}</strong>
            </div>
          </div>
        </article>

        <article class="list-card">
          <h3>考试概览</h3>
          <div class="summary-row compact-summary">
            <article class="mini-card">
              <h3>考试次数</h3>
              <p>{{ summary?.recordCount ?? 0 }}</p>
            </article>
            <article class="mini-card">
              <h3>已提交</h3>
              <p>{{ summary?.submittedCount ?? 0 }}</p>
            </article>
            <article class="mini-card">
              <h3>平均分</h3>
              <p>{{ formatScore(summary?.averageScore) }}</p>
            </article>
          </div>

          <div class="detail-tips">
            <p>通过下方记录可以继续进入单次考试答题详情，查看每一道题的作答与解析。</p>
          </div>
        </article>
      </div>

      <article class="list-card">
        <div class="section-head">
          <div>
            <h3>考试记录</h3>
            <p class="section-copy">点击某条记录，继续查看试卷结果与逐题作答详情。</p>
          </div>
          <span class="pill pill-muted">共 {{ totalRecords }} 条</span>
        </div>

        <template v-if="totalRecords">
          <WorkspacePagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-size-options="pageSizeOptions"
            :start="pageSummary.start"
            :end="pageSummary.end"
            :total-pages="totalPages"
            :total-items="totalRecords"
            item-label="条记录"
            @change-page="goToPage"
            @update:page-size="pageSize = $event"
          />

          <div class="record-list">
            <RouterLink
              v-for="record in paginatedRecords"
              :key="record.recordId"
              class="record-card"
              :to="`/teacher/students/${student.userId}/records/${record.recordId}`"
            >
              <div class="record-card-head">
                <strong>{{ record.paperName || "未命名试卷" }}</strong>
                <span :class="['pill', isSubmitted(record.status) ? 'pill-success' : 'pill-muted']">
                  {{ formatStatus(record.status) }}
                </span>
              </div>
              <div class="record-card-meta">
                <span>成绩：{{ formatNullableScore(record.score) }}</span>
                <span>开始：{{ formatDateTime(record.startTime) }}</span>
                <span>提交：{{ formatDateTime(record.submitTime) }}</span>
                <span>耗时：{{ formatDuration(record.durationSeconds) }}</span>
              </div>
            </RouterLink>
          </div>
        </template>
        <p v-else class="empty-copy">该学生暂时还没有考试记录。</p>
      </article>
    </template>
    <p v-else class="empty-copy">未找到该学生的信息。</p>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import WorkspacePagination from "@/components/WorkspacePagination.vue";
import { usePagination } from "@/composables/usePagination";
import { useSessionStore } from "@/stores/session";
import { getTeacherStudentDetail } from "@/services/teacher-api";

const route = useRoute();
const sessionStore = useSessionStore();

const loading = ref(false);
const student = ref(null);
const summary = ref(null);
const records = ref([]);
const errorMessage = ref("");
const pageSizeOptions = [5, 10, 15];
const {
  currentPage,
  pageSize,
  totalItems: totalRecords,
  totalPages,
  paginatedItems: paginatedRecords,
  pageSummary,
  goToPage
} = usePagination(records, { initialPageSize: 5 });

async function loadStudentDetail() {
  if (!sessionStore.user?.userId || !route.params.studentId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await getTeacherStudentDetail(sessionStore.user.userId, route.params.studentId);
    if (!result?.success) {
      throw new Error(result?.message || "加载学生详情失败");
    }

    student.value = result.data?.student || null;
    summary.value = result.data?.summary || null;
    records.value = result.data?.records || [];
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载学生详情失败";
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

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("zh-CN", {
    hour12: false
  });
}

function formatDuration(value) {
  if (!value || value <= 0) {
    return "-";
  }

  const totalSeconds = Number(value);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  if (minutes <= 0) {
    return `${seconds} 秒`;
  }
  return `${minutes} 分 ${seconds} 秒`;
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

onMounted(loadStudentDetail);
</script>
