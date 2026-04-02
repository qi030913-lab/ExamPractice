<template>
  <section class="page-card">
    <div class="page-copy">
      <p class="page-tag">第一阶段</p>
      <h2>登录页已经切到桌面端</h2>
      <p>
        这里替代原来的 Swing 登录窗口。当前已经接入 Spring Boot 登录与注册接口，成功后会按角色进入教师或学生工作台。
      </p>
      <div class="detail-tips">
        <p>
          如果你还没有账号，现在可以直接在桌面端完成学生或教师注册，不需要再回到旧的 Swing 对话框。
        </p>
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
        <span>{{ accountLabel }}</span>
        <input
          v-model="form.account"
          type="text"
          :placeholder="accountPlaceholder"
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
            <p class="section-copy">这里对齐原来 Swing 的注册对话框，注册成功后会自动回填登录表单。</p>
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
            <span>{{ registerAccountLabel }}</span>
            <input
              v-model="registerForm.account"
              type="text"
              :placeholder="registerAccountPlaceholder"
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
  account: "",
  password: ""
});

const registerForm = reactive({
  role: "STUDENT",
  realName: "",
  account: "",
  password: "",
  confirmPassword: ""
});

const accountLabel = computed(() => form.role === "TEACHER" ? "教工号" : "学号");
const accountPlaceholder = computed(() => form.role === "TEACHER" ? "请输入教工号" : "请输入学号");
const registerAccountLabel = computed(() => registerForm.role === "TEACHER" ? "教工号" : "学号");
const registerAccountPlaceholder = computed(() => registerForm.role === "TEACHER" ? "请输入教工号" : "请输入学号");

async function handleSubmit() {
  successMessage.value = "";

  await sessionStore.loginWithPassword({
    role: form.role,
    realName: form.realName.trim(),
    account: form.account.trim(),
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
  registerForm.account = form.account;
  registerForm.password = "";
  registerForm.confirmPassword = "";
}

function closeRegisterDialog() {
  registerVisible.value = false;
}

function fillLoginFromRegister() {
  form.role = registerForm.role;
  form.realName = registerForm.realName.trim();
  form.account = registerForm.account.trim();
  form.password = registerForm.password;
  successMessage.value = "已将注册信息回填到登录表单，你可以直接登录。";
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
      account: registerForm.account.trim(),
      password: registerForm.password
    });

    fillLoginFromRegister();
    sessionStore.errorMessage = "";
    successMessage.value = "注册成功，登录表单已自动回填。";
    closeRegisterDialog();
  } catch (error) {
    registerErrorMessage.value = error?.response?.data?.message || error?.message || "注册失败";
  }
}
</script>
