<template>
  <section class="page-card">
    <div class="page-copy">
      <p class="page-tag">教师端</p>
      <h2>教师工作台</h2>
      <p>
        这一层会参考原 Swing 的主页和导航，后续承接试卷管理、导题建卷、学生管理三个核心区。
      </p>
    </div>
    <div class="card-grid">
      <article class="mini-card">
        <h3>试卷管理</h3>
        <p>当前试卷 {{ stats.paperCount ?? 0 }} 份，已发布 {{ stats.publishedCount ?? 0 }} 份。</p>
      </article>
      <article class="mini-card">
        <h3>导题建卷</h3>
        <p>下一步接入文件选择、导题解析和自动建卷流程。</p>
      </article>
      <article class="mini-card">
        <h3>学生管理</h3>
        <p>当前学生 {{ stats.studentCount ?? 0 }} 人。</p>
      </article>
    </div>
    <div class="list-grid">
      <article class="list-card">
        <h3>最近试卷</h3>
        <div v-if="recentPapers.length" class="list-stack">
          <div v-for="paper in recentPapers" :key="paper.paperId" class="list-row">
            <strong>{{ paper.paperName }}</strong>
            <span>{{ paper.subject }} / {{ paper.questionCount }} 题 / {{ paper.published ? '已发布' : '未发布' }}</span>
          </div>
        </div>
        <p v-else class="empty-copy">暂无试卷数据。</p>
      </article>
      <article class="list-card">
        <h3>最近学生</h3>
        <div v-if="recentStudents.length" class="list-stack">
          <div v-for="student in recentStudents" :key="student.userId" class="list-row">
            <strong>{{ student.realName }}</strong>
            <span>{{ student.studentNumber }}{{ student.email ? ` / ${student.email}` : '' }}</span>
          </div>
        </div>
        <p v-else class="empty-copy">暂无学生数据。</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { useSessionStore } from "@/stores/session";

const sessionStore = useSessionStore();

const stats = computed(() => sessionStore.workbench?.stats || {});
const recentPapers = computed(() => sessionStore.workbench?.recentPapers || []);
const recentStudents = computed(() => sessionStore.workbench?.recentStudents || []);
</script>
