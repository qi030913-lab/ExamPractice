<template>
  <section class="workspace-page">
    <div class="section-head">
      <div class="page-copy">
        <p class="page-tag">在线考试</p>
        <h2>{{ paper?.paperName || "考试作答" }}</h2>
        <p>这一页接管桌面端在线考试流程，支持倒计时、切题、本地草稿保存和提交试卷。</p>
      </div>
      <RouterLink class="text-link" to="/student/papers">返回考试中心</RouterLink>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>
    <StatusBanner v-if="successMessage" tone="info">
      {{ successMessage }}
    </StatusBanner>

    <div v-if="loading" class="empty-copy">正在准备考试...</div>
    <template v-else-if="paper && questions.length">
      <div class="exam-hero">
        <article class="mini-card">
          <h3>剩余时间</h3>
          <p :class="['timer-copy', remainingSeconds <= 300 ? 'timer-copy-danger' : '']">
            {{ formatCountdown(remainingSeconds) }}
          </p>
        </article>
        <article class="mini-card">
          <h3>答题进度</h3>
          <p>{{ answeredCount }}/{{ questions.length }}</p>
        </article>
        <article class="mini-card">
          <h3>当前题号</h3>
          <p>{{ currentQuestionIndex + 1 }}/{{ questions.length }}</p>
        </article>
      </div>

      <div class="exam-layout">
        <article class="list-card">
          <div class="section-head">
            <div>
              <h3>考试信息</h3>
              <p class="section-copy">你可以随时切题，答案会先保存在当前页面和本地草稿中。</p>
            </div>
          </div>
          <div class="detail-list">
            <div class="detail-row">
              <span>科目</span>
              <strong>{{ paper.subject || "-" }}</strong>
            </div>
            <div class="detail-row">
              <span>总分</span>
              <strong>{{ paper.totalScore ?? 0 }}</strong>
            </div>
            <div class="detail-row">
              <span>及格分</span>
              <strong>{{ paper.passScore ?? 0 }}</strong>
            </div>
            <div class="detail-row">
              <span>时长</span>
              <strong>{{ paper.duration ?? 0 }} 分钟</strong>
            </div>
            <div class="detail-row">
              <span>开始时间</span>
              <strong>{{ formatDateTime(record?.startTime) }}</strong>
            </div>
            <div class="detail-row">
              <span>截止时间</span>
              <strong>{{ formatDateTime(deadlineTime) }}</strong>
            </div>
          </div>
        </article>

        <article class="list-card">
          <div class="section-head">
            <div>
              <h3>题目导航</h3>
              <p class="section-copy">已作答题会高亮，方便快速回看。</p>
            </div>
          </div>
          <div class="exam-index-grid">
            <button
              v-for="(question, index) in questions"
              :key="question.questionId"
              type="button"
              :class="[
                'exam-index-item',
                index === currentQuestionIndex ? 'exam-index-item-active' : '',
                isAnswered(question.questionId) ? 'exam-index-item-done' : ''
              ]"
              @click="currentQuestionIndex = index"
            >
              {{ index + 1 }}
            </button>
          </div>
        </article>
      </div>

      <article class="list-card exam-question-card">
        <div class="section-head">
          <div>
            <h3>第 {{ currentQuestionIndex + 1 }} 题</h3>
            <p class="section-copy">
              {{ formatQuestionType(currentQuestion.questionType) }} / {{ currentQuestion.score ?? 0 }} 分
            </p>
          </div>
          <span class="pill pill-muted">{{ currentQuestion.subject || "未分类" }}</span>
        </div>

        <h4 class="exam-question-title">{{ currentQuestion.content }}</h4>

        <div v-if="isChoiceQuestion(currentQuestion)" class="exam-option-list">
          <label
            v-for="option in buildOptions(currentQuestion)"
            :key="option.value"
            class="exam-option"
          >
            <input
              v-if="isMultipleQuestion(currentQuestion)"
              type="checkbox"
              :checked="getMultipleSelection(currentQuestion.questionId).includes(option.value)"
              @change="toggleMultipleAnswer(currentQuestion.questionId, option.value)"
            />
            <input
              v-else
              type="radio"
              :name="`question-${currentQuestion.questionId}`"
              :checked="getAnswer(currentQuestion.questionId) === option.value"
              @change="setAnswer(currentQuestion.questionId, option.value)"
            />
            <span>{{ option.label }}</span>
          </label>
        </div>

        <label v-else class="page-form">
          <span>你的答案</span>
          <textarea
            :value="getAnswer(currentQuestion.questionId)"
            rows="6"
            placeholder="请输入答案"
            @input="setAnswer(currentQuestion.questionId, $event.target.value)"
          />
        </label>

        <div class="action-row">
          <button
            class="ghost-button"
            type="button"
            :disabled="currentQuestionIndex === 0"
            @click="currentQuestionIndex -= 1"
          >
            上一题
          </button>
          <button
            class="ghost-button"
            type="button"
            :disabled="currentQuestionIndex === questions.length - 1"
            @click="currentQuestionIndex += 1"
          >
            下一题
          </button>
          <button
            type="button"
            :disabled="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? "提交中..." : "提交试卷" }}
          </button>
        </div>
      </article>
    </template>
    <p v-else class="empty-copy">当前试卷没有可作答题目。</p>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getStudentExamSession, startStudentExam, submitStudentExam } from "@/services/student-api";

const route = useRoute();
const router = useRouter();
const sessionStore = useSessionStore();

const loading = ref(false);
const submitting = ref(false);
const paper = ref(null);
const record = ref(null);
const questions = ref([]);
const answers = ref({});
const currentQuestionIndex = ref(0);
const remainingSeconds = ref(0);
const deadlineTime = ref(null);
const errorMessage = ref("");
const successMessage = ref("");
let countdownTimer = null;
let beforeUnloadHandler = null;

const currentQuestion = computed(() => questions.value[currentQuestionIndex.value] || {});
const answeredCount = computed(() =>
  questions.value.filter((question) => isAnswered(question.questionId)).length
);
const draftStorageKey = computed(() =>
  record.value?.recordId ? `exampractice.desktop.exam-draft.${record.value.recordId}` : ""
);

watch(remainingSeconds, (value) => {
  if (value === 0 && record.value?.recordId && !submitting.value) {
    handleSubmit(true);
  }
});

watch(answers, () => {
  persistDraft();
}, { deep: true });

watch(currentQuestionIndex, () => {
  persistDraft();
});

async function loadExam() {
  if (!sessionStore.user?.userId || !route.params.paperId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const startResult = await startStudentExam(sessionStore.user.userId, route.params.paperId);
    if (!startResult?.success) {
      throw new Error(startResult?.message || "开始考试失败");
    }

    const sessionResult = await getStudentExamSession(
      sessionStore.user.userId,
      startResult.data?.record?.recordId
    );
    if (!sessionResult?.success) {
      throw new Error(sessionResult?.message || "加载考试作答页失败");
    }

    record.value = sessionResult.data?.record || null;
    paper.value = sessionResult.data?.paper || null;
    questions.value = sessionResult.data?.questions || [];
    remainingSeconds.value = Number(sessionResult.data?.remainingSeconds || 0);
    deadlineTime.value = sessionResult.data?.deadlineTime || null;
    answers.value = {};
    currentQuestionIndex.value = 0;
    restoreDraft();

    startCountdown();
    bindBeforeUnload();
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载考试失败";
  } finally {
    loading.value = false;
  }
}

function startCountdown() {
  stopCountdown();
  countdownTimer = window.setInterval(() => {
    remainingSeconds.value = Math.max(0, remainingSeconds.value - 1);
  }, 1000);
}

function stopCountdown() {
  if (countdownTimer) {
    window.clearInterval(countdownTimer);
    countdownTimer = null;
  }
}

function persistDraft() {
  if (!draftStorageKey.value) {
    return;
  }

  window.localStorage.setItem(draftStorageKey.value, JSON.stringify({
    answers: answers.value,
    currentQuestionIndex: currentQuestionIndex.value
  }));
}

function restoreDraft() {
  if (!draftStorageKey.value) {
    return;
  }

  try {
    const raw = window.localStorage.getItem(draftStorageKey.value);
    if (!raw) {
      return;
    }

    const parsed = JSON.parse(raw);
    answers.value = parsed?.answers || {};
    currentQuestionIndex.value = Number.isInteger(parsed?.currentQuestionIndex)
      ? Math.min(Math.max(parsed.currentQuestionIndex, 0), Math.max(questions.value.length - 1, 0))
      : 0;
    successMessage.value = "已恢复本地草稿。";
  } catch (_error) {
    window.localStorage.removeItem(draftStorageKey.value);
  }
}

function clearDraft() {
  if (!draftStorageKey.value) {
    return;
  }
  window.localStorage.removeItem(draftStorageKey.value);
}

function bindBeforeUnload() {
  unbindBeforeUnload();
  beforeUnloadHandler = (event) => {
    if (!record.value?.recordId || submitting.value) {
      return;
    }
    event.preventDefault();
    event.returnValue = "";
  };
  window.addEventListener("beforeunload", beforeUnloadHandler);
}

function unbindBeforeUnload() {
  if (!beforeUnloadHandler) {
    return;
  }
  window.removeEventListener("beforeunload", beforeUnloadHandler);
  beforeUnloadHandler = null;
}

function getAnswer(questionId) {
  return answers.value[questionId] || "";
}

function setAnswer(questionId, value) {
  answers.value = {
    ...answers.value,
    [questionId]: value
  };
}

function toggleMultipleAnswer(questionId, optionValue) {
  const selected = getMultipleSelection(questionId);
  const next = selected.includes(optionValue)
    ? selected.filter((item) => item !== optionValue)
    : [...selected, optionValue];
  setAnswer(questionId, next.sort().join(""));
}

function getMultipleSelection(questionId) {
  return getAnswer(questionId).split("").filter(Boolean);
}

function isAnswered(questionId) {
  const value = getAnswer(questionId);
  return Boolean(value && String(value).trim());
}

function buildOptions(question) {
  return [
    { value: "A", text: question.optionA },
    { value: "B", text: question.optionB },
    { value: "C", text: question.optionC },
    { value: "D", text: question.optionD }
  ]
    .filter((option) => option.text)
    .map((option) => ({
      value: option.value,
      label: `${option.value}. ${option.text}`
    }));
}

function isChoiceQuestion(question) {
  return ["SINGLE", "MULTIPLE", "JUDGE"].includes(question.questionType);
}

function isMultipleQuestion(question) {
  return question.questionType === "MULTIPLE";
}

async function handleSubmit(isAutoSubmit = false) {
  if (!record.value?.recordId || submitting.value) {
    return;
  }

  if (!isAutoSubmit) {
    const confirmed = window.confirm(
      `已作答 ${answeredCount.value}/${questions.value.length} 题，确认提交试卷吗？`
    );
    if (!confirmed) {
      return;
    }
  }

  submitting.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const payload = {
      answers: questions.value.map((question) => ({
        questionId: question.questionId,
        answer: getAnswer(question.questionId)
      }))
    };

    const result = await submitStudentExam(sessionStore.user.userId, record.value.recordId, payload);
    if (!result?.success) {
      throw new Error(result?.message || "提交试卷失败");
    }

    stopCountdown();
    clearDraft();
    unbindBeforeUnload();
    successMessage.value = result.message || "考试提交成功";
    await sessionStore.loadWorkbench();
    await router.replace(`/student/records/${record.value.recordId}`);
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "提交试卷失败";
  } finally {
    submitting.value = false;
  }
}

function formatCountdown(value) {
  const totalSeconds = Number(value || 0);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
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

onMounted(loadExam);
onBeforeUnmount(() => {
  stopCountdown();
  unbindBeforeUnload();
});
</script>
