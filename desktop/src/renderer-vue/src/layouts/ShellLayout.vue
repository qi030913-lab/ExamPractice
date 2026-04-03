<template>
  <router-view v-if="route.name === 'login'" />
  <div v-else class="shell-layout">
    <aside class="shell-sidebar">
      <div class="shell-brand">
        <p class="shell-eyebrow">桌面考试平台</p>
        <h1>在线考试系统桌面端</h1>
        <p class="shell-subcopy">
          {{ sidebarCopy }}
        </p>
      </div>

      <nav class="shell-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :class="['shell-nav-link', isNavItemActive(item) ? 'shell-nav-link-active' : '']"
          :to="item.to"
        >
          <strong>{{ item.label }}</strong>
          <span>{{ item.note }}</span>
        </RouterLink>
      </nav>
    </aside>

    <div class="shell-stage">
      <header class="shell-topbar">
        <div>
          <p class="shell-topbar-tag">{{ currentPageTag }}</p>
          <h2>{{ currentPageTitle }}</h2>
        </div>
        <p class="shell-topbar-copy">{{ headerCopy }}</p>
      </header>
      <main class="shell-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink, useRoute } from "vue-router";
import { useSessionStore } from "@/stores/session";

const route = useRoute();
const sessionStore = useSessionStore();

const routeTitleMap = {
  home: { tag: "工作台", title: "统一工作区入口" },
  "teacher-workbench": { tag: "教师端", title: "教师工作台" },
  "teacher-papers": { tag: "教师端", title: "试卷中心" },
  "teacher-paper-detail": { tag: "教师端", title: "试卷详情" },
  "teacher-import": { tag: "教师端", title: "导题建卷" },
  "teacher-students": { tag: "教师端", title: "学生中心" },
  "teacher-student-detail": { tag: "教师端", title: "学生详情" },
  "teacher-record-detail": { tag: "教师端", title: "考试记录详情" },
  "student-workbench": { tag: "学生端", title: "学生工作台" },
  "student-papers": { tag: "学生端", title: "考试中心" },
  "student-exam": { tag: "学生端", title: "在线考试" },
  "student-records": { tag: "学生端", title: "成绩中心" },
  "student-record-detail": { tag: "学生端", title: "记录详情" },
  "student-submit-result": { tag: "学生端", title: "考试结果" },
  "student-achievement": { tag: "学生端", title: "学生成就" }
};

const navItems = computed(() => {
  const items = [
    {
      to: "/home",
      label: "总览",
      note: "查看当前角色入口",
      routeNames: ["home"]
    }
  ];

  if (sessionStore.isTeacher) {
    items.push(
      {
        to: "/teacher",
        label: "教师首页",
        note: "快速进入常用功能",
        routeNames: ["teacher-workbench"]
      },
      {
        to: "/teacher/papers",
        label: "试卷中心",
        note: "管理试卷与发布状态",
        routeNames: ["teacher-papers", "teacher-paper-detail"]
      },
      {
        to: "/teacher/import",
        label: "导题建卷",
        note: "导入题目生成试卷",
        routeNames: ["teacher-import"]
      },
      {
        to: "/teacher/students",
        label: "学生中心",
        note: "查看学生与考试记录",
        routeNames: ["teacher-students", "teacher-student-detail", "teacher-record-detail"]
      }
    );
  }

  if (sessionStore.isStudent) {
    items.push(
      {
        to: "/student",
        label: "学生首页",
        note: "进入考试与成绩入口",
        routeNames: ["student-workbench"]
      },
      {
        to: "/student/papers",
        label: "考试中心",
        note: "开始考试或继续作答",
        routeNames: ["student-papers", "student-exam"]
      },
      {
        to: "/student/records",
        label: "成绩中心",
        note: "查看记录与成绩详情",
        routeNames: ["student-records", "student-record-detail", "student-submit-result"]
      },
      {
        to: "/student/achievement",
        label: "学生成就",
        note: "查看图表与趋势分析",
        routeNames: ["student-achievement"]
      }
    );
  }

  return items;
});

const headerCopy = computed(() => {
  if (!sessionStore.isLoggedIn) {
    return "登录后可直接进入对应角色工作区，完成出卷、考试与成绩查看。";
  }

  return `${displayName.value} 已登录，当前角色为 ${roleName.value}。`;
});

const sidebarCopy = computed(() => {
  if (sessionStore.isTeacher) {
    return "左侧集中放置教师端导航，右侧保留内容工作区，切换试卷、导题和学生管理会更顺手。";
  }

  if (sessionStore.isStudent) {
    return "左侧集中放置学生端导航，右侧用于考试、记录和成就内容展示，减少来回跳转。";
  }

  return "登录后可直接进入对应角色工作区，完成出卷、考试与成绩查看。";
});

const roleName = computed(() => (sessionStore.isTeacher ? "教师" : sessionStore.isStudent ? "学生" : "未登录"));

const displayName = computed(() => sessionStore.user?.realName || "当前用户");

const currentPageMeta = computed(() => {
  return routeTitleMap[route.name] || {
    tag: "工作区",
    title: "在线考试系统"
  };
});

const currentPageTag = computed(() => currentPageMeta.value.tag);
const currentPageTitle = computed(() => currentPageMeta.value.title);

function isNavItemActive(item) {
  return item.routeNames.includes(String(route.name || ""));
}
</script>
