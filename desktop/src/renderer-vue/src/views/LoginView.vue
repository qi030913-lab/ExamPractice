<template>
  <section class="page-card">
    <div class="page-copy">
      <p class="page-tag">第一阶段</p>
      <h2>登录页面迁移完成第一版</h2>
      <p>
        这里会替代原来的 Swing 登录窗口。当前已经接入 Spring Boot 登录接口，
        登录成功后会按角色进入教师或学生工作台。
      </p>
    </div>
    <form class="page-form" @submit.prevent="handleSubmit">
      <StatusBanner v-if="sessionStore.errorMessage" tone="danger">
        {{ sessionStore.errorMessage }}
      </StatusBanner>
      <label>
        <span>角色</span>
        <select v-model="form.role">
          <option value="STUDENT">学生</option>
          <option value="TEACHER">教师</option>
        </select>
      </label>
      <label>
        <span>姓名</span>
        <input v-model="form.realName" type="text" placeholder="请输入姓名" />
      </label>
      <label>
        <span>{{ form.role === "TEACHER" ? "教工号" : "学号" }}</span>
        <input
          v-model="form.account"
          type="text"
          :placeholder="form.role === 'TEACHER' ? '请输入教工号' : '请输入学号'"
        />
      </label>
      <label>
        <span>密码</span>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </label>
      <button type="submit" :disabled="sessionStore.loading">
        {{ sessionStore.loading ? "登录中..." : "登录并进入工作台" }}
      </button>
    </form>
  </section>
</template>

<script setup>
import { reactive } from "vue";
import { useRouter } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";

const router = useRouter();
const sessionStore = useSessionStore();

const form = reactive({
  role: "STUDENT",
  realName: "",
  account: "",
  password: ""
});

async function handleSubmit() {
  await sessionStore.loginWithPassword({
    role: form.role,
    realName: form.realName.trim(),
    account: form.account.trim(),
    password: form.password
  });

  router.push(form.role === "TEACHER" ? "/teacher" : "/student");
}
</script>
