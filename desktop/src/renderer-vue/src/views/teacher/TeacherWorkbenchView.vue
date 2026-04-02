<template>
  <section class="workspace-page">
    <div class="workspace-hero">
      <div class="page-copy">
        <p class="page-tag">教师工作台</p>
        <h2>把高频任务收进桌面端入口</h2>
        <p>
          这一层承接原来 Swing 教师首页的导航逻辑。先把试卷管理、导题建卷、学生管理三块串成连续桌面流程，
          后续再继续细化编辑和详情页。
        </p>
      </div>
      <div class="hero-metrics">
        <article class="mini-card">
          <h3>试卷总数</h3>
          <p>{{ stats.paperCount ?? 0 }}</p>
        </article>
        <article class="mini-card">
          <h3>已发布试卷</h3>
          <p>{{ stats.publishedCount ?? 0 }}</p>
        </article>
        <article class="mini-card">
          <h3>学生人数</h3>
          <p>{{ stats.studentCount ?? 0 }}</p>
        </article>
      </div>
    </div>

    <div class="action-grid">
      <RouterLink class="action-card" to="/teacher/papers">
        <p class="action-tag">试卷管理</p>
        <h3>进入试卷中心</h3>
        <p>查看所有试卷、调整发布状态、删除旧试卷。</p>
      </RouterLink>
      <RouterLink class="action-card action-card-accent" to="/teacher/import">
        <p class="action-tag">导题建卷</p>
        <h3>批量导入题目并建卷</h3>
        <p>支持粘贴题目文本或读取本地 txt 文件，直接生成新试卷。</p>
      </RouterLink>
      <RouterLink class="action-card" to="/teacher/students">
        <p class="action-tag">学生管理</p>
        <h3>查看学生与考试记录</h3>
        <p>先把学生列表和考试记录摘要迁到桌面端，便于后续继续扩展详情页。</p>
      </RouterLink>
    </div>

    <div class="list-grid">
      <article class="list-card">
        <h3>最近试卷</h3>
        <div v-if="recentPapers.length" class="list-stack">
          <div v-for="paper in recentPapers" :key="paper.paperId" class="list-row">
            <strong>{{ paper.paperName }}</strong>
            <span>{{ paper.subject }} / {{ paper.questionCount }} 题 / {{ paper.published ? "已发布" : "未发布" }}</span>
          </div>
        </div>
        <p v-else class="empty-copy">暂无试卷数据。</p>
      </article>
      <article class="list-card">
        <h3>最近学生</h3>
        <div v-if="recentStudents.length" class="list-stack">
          <div v-for="student in recentStudents" :key="student.userId" class="list-row">
            <strong>{{ student.realName }}</strong>
            <span>{{ student.studentNumber }}{{ student.email ? ` / ${student.email}` : "" }}</span>
          </div>
        </div>
        <p v-else class="empty-copy">暂无学生数据。</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink } from "vue-router";
import { useSessionStore } from "@/stores/session";

const sessionStore = useSessionStore();

const stats = computed(() => sessionStore.workbench?.stats || {});
const recentPapers = computed(() => sessionStore.workbench?.recentPapers || []);
const recentStudents = computed(() => sessionStore.workbench?.recentStudents || []);
</script>
