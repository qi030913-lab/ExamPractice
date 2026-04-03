<template>
  <section class="workspace-page">
    <div class="page-copy">
      <p class="page-tag">学生中心</p>
      <h2>教师学生管理</h2>
      <p>集中查看学生资料、考试记录和成绩概览，方便快速了解学习情况。</p>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>

    <article class="list-card">
      <div class="section-head">
        <div>
          <h3>学生列表</h3>
          <p class="section-copy">点击学生卡片即可进入该学生的考试详情页。</p>
        </div>
        <span class="pill pill-muted">共 {{ summary?.studentCount ?? 0 }} 人</span>
      </div>

      <div v-if="loading" class="empty-copy">正在加载学生数据...</div>
      <template v-else-if="totalStudents">
        <WorkspacePagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-size-options="pageSizeOptions"
          :start="pageSummary.start"
          :end="pageSummary.end"
          :total-pages="totalPages"
          :total-items="totalStudents"
          item-label="位学生"
          @change-page="goToPage"
          @update:page-size="pageSize = $event"
        />

        <div class="student-list">
          <RouterLink
            v-for="student in paginatedStudents"
            :key="student.userId"
            class="student-card"
            :to="`/teacher/students/${student.userId}`"
          >
            <strong>{{ student.realName }}</strong>
            <span>{{ student.loginId }}</span>
            <span v-if="student.email">{{ student.email }}</span>
            <span>考试 {{ student.recordCount ?? 0 }} 次 / 已提交 {{ student.submittedCount ?? 0 }} 次</span>
            <span>平均分 {{ formatScore(student.averageScore) }}</span>
          </RouterLink>
        </div>
      </template>
      <p v-else class="empty-copy">暂无学生数据。</p>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import WorkspacePagination from "@/components/WorkspacePagination.vue";
import { usePagination } from "@/composables/usePagination";
import { useSessionStore } from "@/stores/session";
import { getTeacherStudents } from "@/services/teacher-api";

const sessionStore = useSessionStore();
const loading = ref(false);
const students = ref([]);
const summary = ref(null);
const errorMessage = ref("");
const pageSizeOptions = [6, 10, 12];
const {
  currentPage,
  pageSize,
  totalItems: totalStudents,
  totalPages,
  paginatedItems: paginatedStudents,
  pageSummary,
  goToPage
} = usePagination(students, { initialPageSize: 6 });

async function loadStudents() {
  if (!sessionStore.user?.userId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await getTeacherStudents(sessionStore.user.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载学生数据失败");
    }

    students.value = result.data?.students || [];
    summary.value = result.data?.summary || null;
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载学生数据失败";
  } finally {
    loading.value = false;
  }
}

function formatScore(value) {
  return Number.isFinite(value) ? Number(value).toFixed(1) : "0.0";
}

onMounted(loadStudents);
</script>
