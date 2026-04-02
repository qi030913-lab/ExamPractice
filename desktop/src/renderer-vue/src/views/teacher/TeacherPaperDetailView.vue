<template>
  <section class="workspace-page">
    <div class="section-head">
      <div class="page-copy">
        <p class="page-tag">试卷详情</p>
        <h2>{{ paper?.paperName || "试卷详情" }}</h2>
        <p>先迁试卷基础信息编辑和题目明细查看，后续再继续补换题、排题和手动组卷。</p>
      </div>
      <RouterLink class="text-link" to="/teacher/papers">返回试卷中心</RouterLink>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>
    <StatusBanner v-if="successMessage" tone="info">
      {{ successMessage }}
    </StatusBanner>

    <div v-if="loading" class="empty-copy">正在加载试卷详情...</div>
    <template v-else-if="paper">
      <div class="paper-detail-layout">
        <article class="list-card">
          <h3>基础信息</h3>
          <form class="page-form compact-form" @submit.prevent="savePaper">
            <label>
              <span>试卷名称</span>
              <input v-model="form.paperName" type="text" />
            </label>
            <label>
              <span>科目</span>
              <input v-model="form.subject" type="text" />
            </label>
            <div class="field-grid">
              <label>
                <span>及格分</span>
                <input v-model.number="form.passScore" type="number" min="0" />
              </label>
              <label>
                <span>考试时长（分钟）</span>
                <input v-model.number="form.duration" type="number" min="1" />
              </label>
            </div>
            <label>
              <span>试卷说明</span>
              <textarea v-model="form.description" rows="4" />
            </label>
            <div class="summary-row compact-summary">
              <article class="mini-card">
                <h3>总分</h3>
                <p>{{ paper.totalScore ?? 0 }}</p>
              </article>
              <article class="mini-card">
                <h3>题目数</h3>
                <p>{{ paper.questionCount ?? 0 }}</p>
              </article>
              <article class="mini-card">
                <h3>状态</h3>
                <p>{{ paper.published ? "已发布" : "未发布" }}</p>
              </article>
            </div>
            <button type="submit" :disabled="saving">
              {{ saving ? "保存中..." : "保存试卷信息" }}
            </button>
          </form>
        </article>

        <article class="list-card">
          <div class="section-head">
            <div>
              <h3>题目明细</h3>
              <p class="section-copy">当前先只读展示题目结构，帮助我们确认导题建卷后的桌面端查看链路。</p>
            </div>
          </div>

          <div v-if="questions.length" class="question-list">
            <article v-for="(question, index) in questions" :key="question.questionId" class="question-card">
              <p class="question-meta">
                第 {{ index + 1 }} 题 · {{ question.questionType || "未知题型" }} · {{ question.score }} 分
              </p>
              <h4>{{ question.content }}</h4>
              <div v-if="hasOptions(question)" class="option-list">
                <p v-if="question.optionA">A. {{ question.optionA }}</p>
                <p v-if="question.optionB">B. {{ question.optionB }}</p>
                <p v-if="question.optionC">C. {{ question.optionC }}</p>
                <p v-if="question.optionD">D. {{ question.optionD }}</p>
              </div>
              <p class="question-answer">答案：{{ question.correctAnswer || "-" }}</p>
              <p v-if="question.analysis" class="question-analysis">解析：{{ question.analysis }}</p>
            </article>
          </div>
          <p v-else class="empty-copy">该试卷暂无题目明细。</p>
        </article>
      </div>
    </template>
    <p v-else class="empty-copy">未找到试卷信息。</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getTeacherPaperDetail, updateTeacherPaper } from "@/services/teacher-api";

const route = useRoute();
const sessionStore = useSessionStore();

const loading = ref(false);
const saving = ref(false);
const paper = ref(null);
const questions = ref([]);
const errorMessage = ref("");
const successMessage = ref("");

const form = reactive({
  paperName: "",
  subject: "",
  passScore: 60,
  duration: 90,
  description: ""
});

async function loadPaperDetail() {
  if (!sessionStore.user?.userId || !route.params.paperId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const result = await getTeacherPaperDetail(sessionStore.user.userId, route.params.paperId);
    if (!result?.success) {
      throw new Error(result?.message || "加载试卷详情失败");
    }

    paper.value = result.data?.paper || null;
    questions.value = result.data?.questions || [];

    form.paperName = paper.value?.paperName || "";
    form.subject = paper.value?.subject || "";
    form.passScore = paper.value?.passScore ?? 60;
    form.duration = paper.value?.duration ?? 90;
    form.description = paper.value?.description || "";
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载试卷详情失败";
  } finally {
    loading.value = false;
  }
}

async function savePaper() {
  if (!sessionStore.user?.userId || !route.params.paperId) {
    return;
  }

  saving.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const result = await updateTeacherPaper(sessionStore.user.userId, route.params.paperId, {
      paperName: form.paperName.trim(),
      subject: form.subject.trim(),
      passScore: form.passScore,
      duration: form.duration,
      description: form.description.trim()
    });

    if (!result?.success) {
      throw new Error(result?.message || "保存试卷失败");
    }

    paper.value = result.data?.paper || paper.value;
    questions.value = result.data?.questions || questions.value;
    successMessage.value = result.message || "试卷已更新";
    await sessionStore.loadWorkbench();
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "保存试卷失败";
  } finally {
    saving.value = false;
  }
}

function hasOptions(question) {
  return Boolean(question.optionA || question.optionB || question.optionC || question.optionD);
}

onMounted(loadPaperDetail);
</script>
