<template>
  <section class="page-card">
    <div class="page-copy">
      <p class="page-tag">统一登录</p>
      <h2>桌面端登录与注册入口</h2>
      <p>
        当前桌面端已经直接对接 Spring Boot API。登录成功后，会按角色进入教师或学生工作台。
      </p>
      <div class="detail-tips">
        <p>如果你还没有账号，也可以直接在这里完成教师或学生注册。</p>
      </div>
    </div>

    <form class="page-form" @submit.prevent="handleSubmit">
      <StatusBanner v-if="sessionStore.errorMessage" tone="danger">
        {{ sessionStore.errorMessage }}
      </StatusBanner>
      <StatusBanner v-if="successMessage" tone="info">
        {{ successMessage }}
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
        <span>{{ loginIdLabel }}</span>
        <input
          v-model="form.loginId"
          type="text"
          :placeholder="loginIdPlaceholder"
        />
      </label>
      <label>
        <span>密码</span>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </label>

      <div class="action-row">
        <button type="submit" :disabled="sessionStore.loading">
          {{ sessionStore.loading ? "登录中..." : "登录并进入工作台" }}
        </button>
        <button class="ghost-button" type="button" :disabled="sessionStore.loading" @click="openRegisterDialog">
          注册新账号
        </button>
      </div>
    </form>

    <div v-if="registerVisible" class="dialog-backdrop" @click.self="closeRegisterDialog">
      <section class="dialog-card">
        <div class="section-head">
          <div>
            <p class="page-tag">注册账号</p>
            <h3>{{ registerForm.role === "TEACHER" ? "教师注册" : "学生注册" }}</h3>
            <p class="section-copy">注册成功后会自动回填到登录表单，方便你继续登录。</p>
          </div>
          <button class="ghost-button" type="button" :disabled="sessionStore.loading" @click="closeRegisterDialog">
            关闭
          </button>
        </div>

        <form class="page-form compact-form" @submit.prevent="handleRegister">
          <StatusBanner v-if="registerErrorMessage" tone="danger">
            {{ registerErrorMessage }}
          </StatusBanner>
          <StatusBanner v-if="registerSuccessMessage" tone="info">
            {{ registerSuccessMessage }}
          </StatusBanner>

          <label>
            <span>角色</span>
            <select v-model="registerForm.role">
              <option value="STUDENT">学生</option>
              <option value="TEACHER">教师</option>
            </select>
          </label>
          <label>
            <span>姓名</span>
            <input v-model="registerForm.realName" type="text" placeholder="请输入姓名" />
          </label>
          <label>
            <span>{{ registerLoginIdLabel }}</span>
            <input
              v-model="registerForm.loginId"
              type="text"
              :placeholder="registerLoginIdPlaceholder"
            />
          </label>
          <label>
            <span>密码</span>
            <input v-model="registerForm.password" type="password" placeholder="请输入至少 6 位密码" />
          </label>
          <label>
            <span>确认密码</span>
            <input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" />
          </label>

          <div class="action-row">
            <button type="submit" :disabled="sessionStore.loading">
              {{ sessionStore.loading ? "注册中..." : "完成注册" }}
            </button>
            <button class="ghost-button" type="button" :disabled="sessionStore.loading" @click="fillLoginFromRegister">
              回填到登录表单
            </button>
          </div>
        </form>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";

const router = useRouter();
const sessionStore = useSessionStore();

const successMessage = ref("");
const registerVisible = ref(false);
const registerErrorMessage = ref("");
const registerSuccessMessage = ref("");

const form = reactive({
  role: "STUDENT",
  realName: "",
  loginId: "",
  password: ""
});

const registerForm = reactive({
  role: "STUDENT",
  realName: "",
  loginId: "",
  password: "",
  confirmPassword: ""
});

const loginIdLabel = computed(() => form.role === "TEACHER" ? "教工号" : "学号");
const loginIdPlaceholder = computed(() => form.role === "TEACHER" ? "请输入教工号" : "请输入学号");
const registerLoginIdLabel = computed(() => registerForm.role === "TEACHER" ? "教工号" : "学号");
const registerLoginIdPlaceholder = computed(() => registerForm.role === "TEACHER" ? "请输入教工号" : "请输入学号");

async function handleSubmit() {
  successMessage.value = "";

  await sessionStore.loginWithPassword({
    role: form.role,
    realName: form.realName.trim(),
    loginId: form.loginId.trim(),
    password: form.password
  });

  router.push(form.role === "TEACHER" ? "/teacher" : "/student");
}

function openRegisterDialog() {
  sessionStore.errorMessage = "";
  successMessage.value = "";
  registerVisible.value = true;
  registerErrorMessage.value = "";
  registerSuccessMessage.value = "";
  registerForm.role = form.role;
  registerForm.realName = form.realName;
  registerForm.loginId = form.loginId;
  registerForm.password = "";
  registerForm.confirmPassword = "";
}

function closeRegisterDialog() {
  registerVisible.value = false;
}

function fillLoginFromRegister() {
  form.role = registerForm.role;
  form.realName = registerForm.realName.trim();
  form.loginId = registerForm.loginId.trim();
  form.password = registerForm.password;
  successMessage.value = "已将注册信息回填到登录表单，你可以直接继续登录。";
}

async function handleRegister() {
  sessionStore.errorMessage = "";
  registerErrorMessage.value = "";
  registerSuccessMessage.value = "";
  successMessage.value = "";

  if (registerForm.password !== registerForm.confirmPassword) {
    registerErrorMessage.value = "两次输入的密码不一致";
    return;
  }

  try {
    await sessionStore.registerAccount({
      role: registerForm.role,
      realName: registerForm.realName.trim(),
      loginId: registerForm.loginId.trim(),
      password: registerForm.password
    });

    registerSuccessMessage.value = "注册成功";
    fillLoginFromRegister();
    sessionStore.errorMessage = "";
    successMessage.value = "注册成功，登录表单已自动回填。";
    closeRegisterDialog();
  } catch (error) {
    registerErrorMessage.value = error?.message || "注册失败";
  }
}
</script>
