<template>
  <section class="workspace-page">
    <div class="section-head">
      <div class="page-copy">
        <p class="page-tag">考试记录</p>
        <h2>{{ record?.paperName || "考试记录详情" }}</h2>
        <p>这一页承接教师查看学生考试过程的需求，重点展示成绩结果、作答统计和逐题答案。</p>
      </div>
      <RouterLink class="text-link" :to="backToStudentDetail">返回学生详情</RouterLink>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>

    <div v-if="loading" class="empty-copy">正在加载考试记录详情...</div>
    <template v-else-if="record">
      <div class="detail-layout">
        <article class="list-card">
          <h3>学生信息</h3>
          <div class="detail-list">
            <div class="detail-row">
              <span>姓名</span>
              <strong>{{ student?.realName || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>学号</span>
              <strong>{{ student?.studentNumber || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>邮箱</span>
              <strong>{{ student?.email || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>电话</span>
              <strong>{{ student?.phone || "-" }}</strong>
            </div>
          </div>
        </article>

        <article class="list-card">
          <h3>记录摘要</h3>
          <div class="summary-row compact-summary">
            <article class="mini-card">
              <h3>成绩</h3>
              <p>{{ formatNullableScore(record.score) }}</p>
            </article>
            <article class="mini-card">
              <h3>答题数</h3>
              <p>{{ record.answeredCount ?? 0 }}/{{ record.questionCount ?? 0 }}</p>
            </article>
            <article class="mini-card">
              <h3>正确数</h3>
              <p>{{ record.correctCount ?? 0 }}</p>
            </article>
          </div>

          <div class="detail-list">
            <div class="detail-row">
              <span>试卷名称</span>
              <strong>{{ record.paperName || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>科目</span>
              <strong>{{ record.subject || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>状态</span>
              <strong>{{ formatStatus(record.status) }}</strong>
            </div>
            <div class="detail-row">
              <span>是否通过</span>
              <strong>{{ record.passed ? "已通过" : "未通过" }}</strong>
            </div>
            <div class="detail-row">
              <span>及格分</span>
              <strong>{{ record.passScore ?? "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>总分</span>
              <strong>{{ record.totalScore ?? "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>错误数</span>
              <strong>{{ record.wrongCount ?? 0 }}</strong>
            </div>
            <div class="detail-row">
              <span>作答耗时</span>
              <strong>{{ formatDuration(record.durationSeconds) }}</strong>
            </div>
            <div class="detail-row">
              <span>开始时间</span>
              <strong>{{ formatDateTime(record.startTime) }}</strong>
            </div>
            <div class="detail-row">
              <span>提交时间</span>
              <strong>{{ formatDateTime(record.submitTime) }}</strong>
            </div>
          </div>
        </article>
      </div>

      <article class="list-card">
        <div class="section-head">
          <div>
            <h3>逐题作答</h3>
            <p class="section-copy">这里集中展示学生答案与参考解析，方便教师快速核对考试过程。</p>
          </div>
          <span class="pill pill-muted">共 {{ answers.length }} 题</span>
        </div>

        <div v-if="answers.length" class="answer-list">
          <article
            v-for="(answer, index) in answers"
            :key="answer.answerId || `${answer.questionId}-${index}`"
            class="answer-card"
          >
            <div class="record-card-head">
              <strong>第 {{ index + 1 }} 题</strong>
              <span :class="['pill', answer.isCorrect ? 'pill-success' : 'pill-muted']">
                {{ answer.isCorrect ? "答对" : "待改/答错" }}
              </span>
            </div>

            <p class="question-meta">
              {{ formatQuestionType(answer.questionType) }} · 分值 {{ answer.score ?? 0 }}
            </p>
            <h4>{{ answer.content || "题目内容缺失" }}</h4>

            <div
              v-if="hasOptions(answer)"
              class="option-list"
            >
              <p v-if="answer.optionA">A. {{ answer.optionA }}</p>
              <p v-if="answer.optionB">B. {{ answer.optionB }}</p>
              <p v-if="answer.optionC">C. {{ answer.optionC }}</p>
              <p v-if="answer.optionD">D. {{ answer.optionD }}</p>
            </div>

            <div class="answer-grid">
              <div class="answer-meta-card">
                <span>学生答案</span>
                <strong>{{ answer.studentAnswer || "未作答" }}</strong>
              </div>
              <div class="answer-meta-card">
                <span>正确答案</span>
                <strong>{{ answer.correctAnswer || "-" }}</strong>
              </div>
            </div>

            <p v-if="answer.analysis" class="question-analysis">解析：{{ answer.analysis }}</p>
          </article>
        </div>
        <p v-else class="empty-copy">当前记录暂无答题明细。</p>
      </article>
    </template>
    <p v-else class="empty-copy">未找到这次考试记录。</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getTeacherStudentRecordDetail } from "@/services/teacher-api";

const route = useRoute();
const sessionStore = useSessionStore();

const loading = ref(false);
const student = ref(null);
const record = ref(null);
const answers = ref([]);
const errorMessage = ref("");

const backToStudentDetail = computed(() => `/teacher/students/${route.params.studentId}`);

async function loadRecordDetail() {
  if (!sessionStore.user?.userId || !route.params.studentId || !route.params.recordId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await getTeacherStudentRecordDetail(
      sessionStore.user.userId,
      route.params.studentId,
      route.params.recordId
    );

    if (!result?.success) {
      throw new Error(result?.message || "加载考试记录详情失败");
    }

    student.value = result.data?.student || null;
    record.value = result.data?.record || null;
    answers.value = result.data?.answers || [];
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载考试记录详情失败";
  } finally {
    loading.value = false;
  }
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

function formatStatus(status) {
  const statusMap = {
    IN_PROGRESS: "进行中",
    SUBMITTED: "已提交",
    TIMEOUT: "超时提交",
    NOT_STARTED: "未开始"
  };
  return statusMap[status] || status || "未知状态";
}

function formatQuestionType(type) {
  const typeMap = {
    SINGLE: "单选题",
    MULTIPLE: "多选题",
    JUDGE: "判断题",
    FILL_BLANK: "填空题",
    SHORT_ANSWER: "简答题"
  };
  return typeMap[type] || type || "未知题型";
}

function hasOptions(answer) {
  return Boolean(answer.optionA || answer.optionB || answer.optionC || answer.optionD);
}

onMounted(loadRecordDetail);
</script>
