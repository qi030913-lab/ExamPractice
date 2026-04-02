<template>
  <section class="workspace-page">
    <div class="page-copy">
      <p class="page-tag">学生中心</p>
      <h2>教师学生管理</h2>
      <p>先迁学生列表和考试记录摘要，后续再继续细化学生详情与成绩分析页。</p>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>

    <div class="students-layout">
      <article class="list-card">
        <div class="section-head">
          <div>
            <h3>学生列表</h3>
            <p class="section-copy">点击左侧学生，右侧查看该学生的考试记录摘要。</p>
          </div>
          <span class="pill pill-muted">共 {{ summary?.studentCount ?? 0 }} 人</span>
        </div>

        <div v-if="loadingStudents" class="empty-copy">正在加载学生数据...</div>
        <div v-else-if="students.length" class="student-list">
          <button
            v-for="student in students"
            :key="student.userId"
            type="button"
            :class="['student-card', selectedStudent?.userId === student.userId ? 'student-card-active' : '']"
            @click="selectStudent(student)"
          >
            <strong>{{ student.realName }}</strong>
            <span>{{ student.studentNumber }}</span>
            <span>考试 {{ student.recordCount ?? 0 }} 次 / 已提交 {{ student.submittedCount ?? 0 }} 次</span>
          </button>
        </div>
        <p v-else class="empty-copy">暂无学生数据。</p>
      </article>

      <article class="list-card">
        <div class="section-head">
          <div>
            <h3>考试记录</h3>
            <p class="section-copy">
              {{ selectedStudent ? `${selectedStudent.realName} 的考试记录` : "请选择一个学生查看记录" }}
            </p>
          </div>
        </div>

        <div v-if="selectedStudentSummary" class="summary-row compact-summary">
          <article class="mini-card">
            <h3>考试次数</h3>
            <p>{{ selectedStudentSummary.recordCount ?? 0 }}</p>
          </article>
          <article class="mini-card">
            <h3>已提交</h3>
            <p>{{ selectedStudentSummary.submittedCount ?? 0 }}</p>
          </article>
          <article class="mini-card">
            <h3>平均分</h3>
            <p>{{ averageScoreCopy }}</p>
          </article>
        </div>

        <div v-if="loadingRecords" class="empty-copy">正在加载考试记录...</div>
        <div v-else-if="records.length" class="table-wrap">
          <table class="workspace-table">
            <thead>
              <tr>
                <th>试卷</th>
                <th>状态</th>
                <th>得分</th>
                <th>开始时间</th>
                <th>提交时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in records" :key="record.recordId">
                <td>{{ record.paperName || "未知试卷" }}</td>
                <td>{{ record.status || "未知状态" }}</td>
                <td>{{ record.score ?? "-" }}</td>
                <td>{{ formatDateTime(record.startTime) }}</td>
                <td>{{ formatDateTime(record.submitTime) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="empty-copy">
          {{ selectedStudent ? "该学生暂无考试记录。" : "请选择学生后查看记录。" }}
        </p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getTeacherStudentRecords, getTeacherStudents } from "@/services/teacher-api";

const sessionStore = useSessionStore();
const loadingStudents = ref(false);
const loadingRecords = ref(false);
const students = ref([]);
const summary = ref(null);
const selectedStudent = ref(null);
const selectedStudentSummary = ref(null);
const records = ref([]);
const errorMessage = ref("");

const averageScoreCopy = computed(() => {
  const value = selectedStudentSummary.value?.averageScore;
  return Number.isFinite(value) ? Number(value).toFixed(1) : "0.0";
});

async function loadStudents() {
  if (!sessionStore.user?.userId) {
    return;
  }

  loadingStudents.value = true;
  errorMessage.value = "";

  try {
    const result = await getTeacherStudents(sessionStore.user.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载学生数据失败");
    }

    students.value = result.data?.students || [];
    summary.value = result.data?.summary || null;

    if (students.value.length) {
      await selectStudent(students.value[0]);
    }
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载学生数据失败";
  } finally {
    loadingStudents.value = false;
  }
}

async function selectStudent(student) {
  selectedStudent.value = student;
  records.value = [];
  selectedStudentSummary.value = null;

  if (!sessionStore.user?.userId || !student?.userId) {
    return;
  }

  loadingRecords.value = true;
  errorMessage.value = "";

  try {
    const result = await getTeacherStudentRecords(sessionStore.user.userId, student.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载考试记录失败");
    }

    records.value = result.data?.records || [];
    selectedStudentSummary.value = result.data?.summary || null;
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载考试记录失败";
  } finally {
    loadingRecords.value = false;
  }
}

function formatDateTime(value) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }
  return date.toLocaleString("zh-CN", { hour12: false });
}

onMounted(loadStudents);
</script>
