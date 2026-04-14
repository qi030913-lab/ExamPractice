<template>
  <section class="workspace-page">
    <div class="page-copy">
      <p class="page-tag">导题建卷</p>
      <h2>通过题目文本快速生成试卷</h2>
      <p>
        支持选择本地 `txt` 文件或直接粘贴题目文本，系统会自动解析内容、复用已有题目并生成新试卷。
        当前导题建卷仅支持单选、多选、判断三类客观题。
      </p>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>
    <StatusBanner v-if="successMessage" tone="info">
      {{ successMessage }}
    </StatusBanner>

    <div class="import-layout">
      <article class="list-card">
        <h3>建卷信息</h3>
        <form class="page-form compact-form" @submit.prevent="submitImport">
          <label>
            <span>试卷名称</span>
            <input v-model="form.paperName" type="text" placeholder="例如：Java 基础阶段测验" />
          </label>
          <label>
            <span>科目</span>
            <input v-model="form.subject" type="text" placeholder="例如：Java" />
          </label>
          <div class="field-grid">
            <label>
              <span>及格分</span>
              <input v-model.number="form.passScore" type="number" min="0" />
            </label>
            <label>
              <span>时长（分钟）</span>
              <input v-model.number="form.duration" type="number" min="1" />
            </label>
          </div>
          <label>
            <span>试卷说明</span>
            <input v-model="form.description" type="text" placeholder="可选" />
          </label>
          <label>
            <span>题目文本</span>
            <textarea
              v-model="form.sourceText"
              rows="14"
              placeholder="按模板格式粘贴题目内容，或先点击“读取本地 txt 文件”自动填入"
            />
          </label>
          <div class="action-row">
            <button class="ghost-button" type="button" @click="pickLocalFile">读取本地 txt 文件</button>
            <button type="submit" :disabled="loading">{{ loading ? "建卷中..." : "导题并创建试卷" }}</button>
          </div>
        </form>
      </article>

      <article class="list-card">
        <h3>格式说明</h3>
        <div class="detail-tips">
          <p>建议先下载模板文件，再按模板格式编辑题目内容，这样和后端解析规则能保持一致。</p>
          <p>目前模板和导入校验已经统一为客观题：`SINGLE`、`MULTIPLE`、`JUDGE`。</p>
          <div class="action-row">
            <button class="ghost-button" type="button" :disabled="templateLoading" @click="downloadTemplate">
              {{ templateLoading ? "准备模板中..." : "下载导题模板" }}
            </button>
          </div>
        </div>
        <div class="format-block">
          <p>每行一题，格式如下：</p>
          <code>题目类型|科目|题目内容|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析</code>
          <p>示例：</p>
          <pre>SINGLE|Java|Java 中用于定义常量的关键字是？|const|final|static|let|B|5|EASY|final 用于定义常量</pre>
          <pre>MULTIPLE|Java|Java 中哪些是访问修饰符？|public|private|protected|final|ABC|10|MEDIUM|访问修饰符包括 public、private、protected</pre>
          <pre>JUDGE|Java|Java 支持类的多继承|正确|错误|||B|5|EASY|Java 不支持类的多继承</pre>
        </div>

        <div v-if="importSummary" class="result-block">
          <h4>本次导入结果</h4>
          <p>来源题目数：{{ importSummary.sourceQuestionCount }}</p>
          <p>建卷题目数：{{ importSummary.linkedQuestionCount }}</p>
          <p>新建题目数：{{ importSummary.createdQuestionCount }}</p>
          <p>复用题目数：{{ importSummary.reusedQuestionCount }}</p>
          <p v-if="createdPaperName">创建试卷：{{ createdPaperName }}</p>
        </div>
      </article>
    </div>

    <input ref="fileInputRef" type="file" accept=".txt" class="hidden-input" @change="handleFileChange" />
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getTeacherImportTemplate, importTeacherPaper } from "@/services/teacher-api";

const sessionStore = useSessionStore();
const fileInputRef = ref(null);
const loading = ref(false);
const templateLoading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");
const importSummary = ref(null);
const createdPaperName = ref("");

const form = reactive({
  paperName: "",
  subject: "",
  passScore: 60,
  duration: 90,
  description: "",
  sourceText: ""
});

function pickLocalFile() {
  fileInputRef.value?.click();
}

async function downloadTemplate() {
  if (!sessionStore.user?.userId) {
    return;
  }

  templateLoading.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const result = await getTeacherImportTemplate(sessionStore.user.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载导题模板失败");
    }

    const fileName = result.data?.fileName || "题目导入模板.txt";
    const content = result.data?.content || "";
    if (!content.trim()) {
      throw new Error("导题模板内容为空");
    }

    if (window?.desktopApi?.saveTextFile) {
      const saveResult = await window.desktopApi.saveTextFile({
        title: "保存题目导入模板",
        defaultFileName: fileName,
        extension: "txt",
        content
      });

      if (saveResult?.canceled) {
        return;
      }
      if (!saveResult?.ok) {
        throw new Error(saveResult?.message || "保存模板文件失败");
      }

      successMessage.value = `模板文件已保存到：${saveResult.path}`;
      return;
    }

    const blob = new Blob([content], { type: "text/plain;charset=utf-8" });
    const objectUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = objectUrl;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(objectUrl);
    successMessage.value = `模板文件已下载：${fileName}`;
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载导题模板失败";
  } finally {
    templateLoading.value = false;
  }
}

async function handleFileChange(event) {
  const [file] = event.target.files || [];
  if (!file) {
    return;
  }

  try {
    form.sourceText = await file.text();
    successMessage.value = `已读取文件：${file.name}`;
    errorMessage.value = "";
  } catch (error) {
    errorMessage.value = error?.message || "读取文件失败";
  } finally {
    event.target.value = "";
  }
}

async function submitImport() {
  if (!sessionStore.user?.userId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const result = await importTeacherPaper(sessionStore.user.userId, {
      paperName: form.paperName.trim(),
      subject: form.subject.trim(),
      passScore: form.passScore,
      duration: form.duration,
      description: form.description.trim(),
      sourceText: form.sourceText
    });

    if (!result?.success) {
      throw new Error(result?.message || "导题建卷失败");
    }

    importSummary.value = result.data?.summary || null;
    createdPaperName.value = result.data?.paper?.paperName || "";
    successMessage.value = result.message || "导题建卷成功";
    await sessionStore.loadWorkbench();
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "导题建卷失败";
  } finally {
    loading.value = false;
  }
}
</script>
