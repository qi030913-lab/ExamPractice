<template>
  <section class="workspace-page">
    <div class="section-head">
      <div class="page-copy">
        <p class="page-tag">记录详情</p>
        <h2>{{ record?.paperName || "考试记录详情" }}</h2>
        <p>这里展示学生自己这次考试的结果、统计和逐题答案，方便在桌面端复盘错题。</p>
      </div>
      <RouterLink class="text-link" to="/student/records">返回我的记录</RouterLink>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>

    <div v-if="loading" class="empty-copy">正在加载记录详情...</div>
    <template v-else-if="record">
      <div class="detail-layout">
        <article class="list-card">
          <h3>成绩摘要</h3>
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
        </article>

        <article class="list-card">
          <h3>考试信息</h3>
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
              <span>总分</span>
              <strong>{{ record.totalScore ?? "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>及格分</span>
              <strong>{{ record.passScore ?? "-" }}</strong>
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
            <p class="section-copy">当前先提供结果查看和错题复盘，后面再继续迁考试作答流程。</p>
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

            <div v-if="hasOptions(answer)" class="option-list">
              <p v-if="answer.optionA">A. {{ answer.optionA }}</p>
              <p v-if="answer.optionB">B. {{ answer.optionB }}</p>
              <p v-if="answer.optionC">C. {{ answer.optionC }}</p>
              <p v-if="answer.optionD">D. {{ answer.optionD }}</p>
            </div>

            <div class="answer-grid">
              <div class="answer-meta-card">
                <span>我的答案</span>
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
    <p v-else class="empty-copy">未找到这条考试记录。</p>
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
const record = ref(null);
const answers = ref([]);
const errorMessage = ref("");

async function loadRecordDetail() {
  if (!sessionStore.user?.userId || !route.params.recordId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await getStudentRecordDetail(sessionStore.user.userId, route.params.recordId);
    if (!result?.success) {
      throw new Error(result?.message || "加载记录详情失败");
    }

    record.value = result.data?.record || null;
    answers.value = result.data?.answers || [];
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载记录详情失败";
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

function formatQuestionType(type) {
  const typeMap = {
    SINGLE: "单选题",
    MULTIPLE: "多选题",
    JUDGE: "判断题",
    BLANK: "填空题",
    SHORT_ANSWER: "简答题",
    APPLICATION: "应用题",
    ALGORITHM: "算法题",
    COMPREHENSIVE: "综合题",
    ESSAY: "论述题",
    MATERIAL_ANALYSIS: "材料分析题",
    CLOZE: "选词填空",
    READING_ANALYSIS: "阅读分析",
    ENGLISH_TO_CHINESE: "英译汉",
    CHINESE_TO_ENGLISH: "汉译英",
    WRITING: "写作"
  };
  return typeMap[type] || type || "未知题型";
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

function hasOptions(answer) {
  return Boolean(answer.optionA || answer.optionB || answer.optionC || answer.optionD);
}

onMounted(loadRecordDetail);
</script>
