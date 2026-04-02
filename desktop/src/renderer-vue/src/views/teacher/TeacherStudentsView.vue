<template>
  <section class="workspace-page">
    <div class="page-copy">
      <p class="page-tag">学生中心</p>
      <h2>教师学生管理</h2>
      <p>先串起学生列表和考试记录入口，后续继续在这个基础上扩展编辑、归档和更多统计能力。</p>
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
      <div v-else-if="students.length" class="student-list">
        <RouterLink
          v-for="student in students"
          :key="student.userId"
          class="student-card"
          :to="`/teacher/students/${student.userId}`"
        >
          <strong>{{ student.realName }}</strong>
          <span>{{ student.studentNumber }}</span>
          <span v-if="student.email">{{ student.email }}</span>
          <span>考试 {{ student.recordCount ?? 0 }} 次 / 已提交 {{ student.submittedCount ?? 0 }} 次</span>
          <span>平均分 {{ formatScore(student.averageScore) }}</span>
        </RouterLink>
      </div>
      <p v-else class="empty-copy">暂无学生数据。</p>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getTeacherStudents } from "@/services/teacher-api";

const sessionStore = useSessionStore();
const loading = ref(false);
const students = ref([]);
const summary = ref(null);
const errorMessage = ref("");

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
