<template>
  <section class="workspace-page">
    <div class="page-copy">
      <p class="page-tag">试卷中心</p>
      <h2>教师试卷管理</h2>
      <p>在这里统一查看试卷、进入详情、调整发布状态，并执行删除操作。</p>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>
    <StatusBanner v-if="successMessage" tone="info">
      {{ successMessage }}
    </StatusBanner>

    <div class="summary-row" v-if="summary">
      <article class="mini-card">
        <h3>试卷总数</h3>
        <p>{{ summary.paperCount ?? 0 }}</p>
      </article>
      <article class="mini-card">
        <h3>已发布</h3>
        <p>{{ summary.publishedCount ?? 0 }}</p>
      </article>
      <article class="mini-card">
        <h3>未发布</h3>
        <p>{{ summary.unpublishedCount ?? 0 }}</p>
      </article>
    </div>

    <article class="list-card">
      <div class="section-head">
        <div>
          <h3>试卷列表</h3>
          <p class="section-copy">切换发布状态后会同步刷新教师工作台统计。</p>
        </div>
        <RouterLink class="text-link" to="/teacher/import">去导题建卷</RouterLink>
      </div>

      <div v-if="loading" class="empty-copy">正在加载试卷数据...</div>
      <template v-else-if="totalPapers">
        <WorkspacePagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-size-options="pageSizeOptions"
          :start="pageSummary.start"
          :end="pageSummary.end"
          :total-pages="totalPages"
          :total-items="totalPapers"
          item-label="份试卷"
          @change-page="goToPage"
          @update:page-size="pageSize = $event"
        />

        <div class="table-wrap">
        <table class="workspace-table">
          <thead>
            <tr>
              <th>试卷名称</th>
              <th>科目</th>
              <th>题目数</th>
              <th>总分</th>
              <th>时长</th>
              <th>及格分</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="paper in paginatedPapers" :key="paper.paperId">
              <td>
                <RouterLink class="inline-link" :to="`/teacher/papers/${paper.paperId}`">
                  {{ paper.paperName }}
                </RouterLink>
              </td>
              <td>{{ paper.subject }}</td>
              <td>{{ paper.questionCount }}</td>
              <td>{{ paper.totalScore }}</td>
              <td>{{ paper.duration }} 分钟</td>
              <td>{{ paper.passScore }}</td>
              <td>
                <span :class="['pill', paper.published ? 'pill-success' : 'pill-muted']">
                  {{ paper.published ? "已发布" : "未发布" }}
                </span>
              </td>
              <td>
                <div class="action-row">
                  <RouterLink class="ghost-link-button" :to="`/teacher/papers/${paper.paperId}`">
                    详情
                  </RouterLink>
                  <button
                    class="ghost-button"
                    type="button"
                    :disabled="actionLoadingId === paper.paperId"
                    @click="togglePublish(paper)"
                  >
                    {{ paper.published ? "取消发布" : "发布" }}
                  </button>
                  <button
                    class="danger-button"
                    type="button"
                    :disabled="actionLoadingId === paper.paperId"
                    @click="removePaper(paper)"
                  >
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </template>
      <p v-else class="empty-copy">暂无试卷数据。</p>
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
import {
  deleteTeacherPaper,
  getTeacherPapers,
  publishTeacherPaper,
  unpublishTeacherPaper
} from "@/services/teacher-api";

const sessionStore = useSessionStore();
const loading = ref(false);
const actionLoadingId = ref(null);
const papers = ref([]);
const summary = ref(null);
const errorMessage = ref("");
const successMessage = ref("");
const pageSizeOptions = [6, 10, 15];
const {
  currentPage,
  pageSize,
  totalItems: totalPapers,
  totalPages,
  paginatedItems: paginatedPapers,
  pageSummary,
  goToPage
} = usePagination(papers, { initialPageSize: 6 });

async function loadPapers() {
  if (!sessionStore.user?.userId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await getTeacherPapers(sessionStore.user.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载试卷数据失败");
    }
    papers.value = result.data?.papers || [];
    summary.value = result.data?.summary || null;
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载试卷数据失败";
  } finally {
    loading.value = false;
  }
}

async function togglePublish(paper) {
  if (!sessionStore.user?.userId) {
    return;
  }

  actionLoadingId.value = paper.paperId;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const result = paper.published
      ? await unpublishTeacherPaper(sessionStore.user.userId, paper.paperId)
      : await publishTeacherPaper(sessionStore.user.userId, paper.paperId);

    if (!result?.success) {
      throw new Error(result?.message || "更新试卷状态失败");
    }

    successMessage.value = result.message || "试卷状态已更新";
    await loadPapers();
    await sessionStore.loadWorkbench();
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "更新试卷状态失败";
  } finally {
    actionLoadingId.value = null;
  }
}

async function removePaper(paper) {
  if (!sessionStore.user?.userId) {
    return;
  }

  const confirmed = window.confirm(`确认删除试卷“${paper.paperName}”吗？`);
  if (!confirmed) {
    return;
  }

  actionLoadingId.value = paper.paperId;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const result = await deleteTeacherPaper(sessionStore.user.userId, paper.paperId);
    if (!result?.success) {
      throw new Error(result?.message || "删除试卷失败");
    }

    successMessage.value = result.message || "试卷已删除";
    await loadPapers();
    await sessionStore.loadWorkbench();
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "删除试卷失败";
  } finally {
    actionLoadingId.value = null;
  }
}

onMounted(loadPapers);
</script>
