<template>
  <section
    ref="sceneRef"
    class="login-stage"
    @mousemove="handlePointerMove"
    @mouseleave="resetPointer"
  >
    <canvas ref="canvasRef" class="login-stage__canvas"></canvas>
    <div class="login-stage__sky"></div>
    <div class="login-stage__mist"></div>

    <section class="login-card">
      <header class="login-card__header">
        <p class="login-card__tag">Exam Practice Desktop</p>
        <h2>{{ isRegisterMode ? registerTitle : loginTitle }}</h2>
        <p class="login-card__copy">
          {{ isRegisterMode ? "注册成功后会自动回填到登录表单。" : "保留当前项目字段与接口，接入旧版登录页的视觉效果。" }}
        </p>
      </header>

      <div class="mode-tabs">
        <button
          type="button"
          :class="{ active: !isRegisterMode }"
          :disabled="sessionStore.loading"
          @click="switchToLogin"
        >
          登录
        </button>
        <button
          type="button"
          :class="{ active: isRegisterMode }"
          :disabled="sessionStore.loading"
          @click="switchToRegister"
        >
          注册
        </button>
      </div>

      <form v-if="!isRegisterMode" class="login-form" @submit.prevent="handleSubmit">
        <StatusBanner v-if="sessionStore.errorMessage" tone="danger">
          {{ sessionStore.errorMessage }}
        </StatusBanner>
        <StatusBanner v-if="successMessage" tone="info">
          {{ successMessage }}
        </StatusBanner>

        <div class="role-switch">
          <button
            type="button"
            :class="{ active: form.role === 'STUDENT' }"
            :disabled="sessionStore.loading"
            @click="form.role = 'STUDENT'"
          >
            学生
          </button>
          <button
            type="button"
            :class="{ active: form.role === 'TEACHER' }"
            :disabled="sessionStore.loading"
            @click="form.role = 'TEACHER'"
          >
            教师
          </button>
        </div>

        <label class="login-field">
          <img class="login-field__icon" :src="userIcon" alt="" />
          <input
            v-model="form.realName"
            type="text"
            placeholder="姓名"
            autocomplete="name"
          />
        </label>

        <label class="login-field">
          <img class="login-field__icon" :src="keyIcon" alt="" />
          <input
            v-model="form.loginId"
            type="text"
            :placeholder="loginIdPlaceholder"
            autocomplete="username"
          />
        </label>

        <label class="login-field">
          <img class="login-field__icon" :src="lockIcon" alt="" />
          <input
            v-model="form.password"
            type="password"
            placeholder="密码"
            autocomplete="current-password"
          />
        </label>

        <div class="login-card__actions">
          <button class="primary-button" type="submit" :disabled="sessionStore.loading">
            {{ sessionStore.loading ? "登录中..." : "登录" }}
          </button>
          <button
            class="ghost-button"
            type="button"
            :disabled="sessionStore.loading"
            @click="switchToRegister"
          >
            去注册
          </button>
        </div>
      </form>

      <form v-else class="login-form" @submit.prevent="handleRegister">
        <StatusBanner v-if="registerErrorMessage" tone="danger">
          {{ registerErrorMessage }}
        </StatusBanner>

        <div class="role-switch">
          <button
            type="button"
            :class="{ active: registerForm.role === 'STUDENT' }"
            :disabled="sessionStore.loading"
            @click="registerForm.role = 'STUDENT'"
          >
            学生
          </button>
          <button
            type="button"
            :class="{ active: registerForm.role === 'TEACHER' }"
            :disabled="sessionStore.loading"
            @click="registerForm.role = 'TEACHER'"
          >
            教师
          </button>
        </div>

        <label class="login-field">
          <img class="login-field__icon" :src="userIcon" alt="" />
          <input
            v-model="registerForm.realName"
            type="text"
            placeholder="姓名"
            autocomplete="name"
          />
        </label>

        <label class="login-field">
          <img class="login-field__icon" :src="keyIcon" alt="" />
          <input
            v-model="registerForm.loginId"
            type="text"
            :placeholder="registerLoginIdPlaceholder"
            autocomplete="username"
          />
        </label>

        <label class="login-field">
          <img class="login-field__icon" :src="lockIcon" alt="" />
          <input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            autocomplete="new-password"
          />
        </label>

        <label class="login-field">
          <img class="login-field__icon" :src="lockIcon" alt="" />
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            autocomplete="new-password"
          />
        </label>

        <div class="login-card__actions">
          <button class="primary-button" type="submit" :disabled="sessionStore.loading">
            {{ sessionStore.loading ? "注册中..." : "完成注册" }}
          </button>
          <button
            class="ghost-button"
            type="button"
            :disabled="sessionStore.loading"
            @click="fillLoginFromRegister"
          >
            回填到登录
          </button>
        </div>
      </form>

      <footer class="login-card__footer">
        <p>{{ footerCopy }}</p>
      </footer>
    </section>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import cloudSprite from "@/views/login/img/cloud.png";
import userIcon from "@/views/login/img/user_icon_copy.png";
import keyIcon from "@/views/login/img/key.png";
import lockIcon from "@/views/login/img/lock_icon_copy.png";

const router = useRouter();
const sessionStore = useSessionStore();

const sceneRef = ref(null);
const canvasRef = ref(null);
const successMessage = ref("");
const isRegisterMode = ref(false);
const registerErrorMessage = ref("");

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

const pointerTarget = reactive({ x: 0, y: 0 });
const pointer = reactive({ x: 0, y: 0 });
const animationState = {
  frameId: 0,
  lastTime: 0,
  image: null,
  clouds: [],
  width: 0,
  height: 0,
  dpr: 1
};

const loginIdPlaceholder = computed(() => form.role === "TEACHER" ? "教工号" : "学号");
const registerLoginIdPlaceholder = computed(() => registerForm.role === "TEACHER" ? "教工号" : "学号");
const loginTitle = computed(() => form.role === "TEACHER" ? "教师登录" : "学生登录");
const registerTitle = computed(() => registerForm.role === "TEACHER" ? "教师注册" : "学生注册");
const footerCopy = computed(() => {
  if (isRegisterMode.value) {
    return "注册后将自动回填到登录表单，不额外引入旧页面里的验证码字段。";
  }

  return "云层背景按旧版页面的 Three.js 思路复现，layui 在该效果里不再需要。";
});

function createCloud(width, height) {
  const layerRoll = Math.random();
  let y;
  let z;
  let size;
  let opacity;
  let speed;

  if (layerRoll < 0.64) {
    y = height * (0.72 + Math.random() * 0.2);
    z = 820 + Math.pow(Math.random(), 1.65) * 4700;
    size = 1.28 + Math.random() * 1.36;
    opacity = 0.12 + Math.random() * 0.11;
    speed = 42 + Math.random() * 50;
  } else if (layerRoll < 0.9) {
    y = height * (0.6 + Math.random() * 0.14);
    z = 1800 + Math.random() * 4700;
    size = 0.82 + Math.random() * 0.72;
    opacity = 0.06 + Math.random() * 0.07;
    speed = 54 + Math.random() * 68;
  } else {
    y = height * (0.52 + Math.random() * 0.08);
    z = 3200 + Math.random() * 4200;
    size = 0.56 + Math.random() * 0.4;
    opacity = 0.025 + Math.random() * 0.03;
    speed = 72 + Math.random() * 64;
  }

  return {
    x: (Math.random() - 0.5) * width * 2.8,
    y,
    z,
    size,
    opacity,
    speed
  };
}

function resetCloud(cloud, width, height) {
  const nextCloud = createCloud(width, height);
  cloud.x = nextCloud.x;
  cloud.y = nextCloud.y;
  cloud.z = 6800 + Math.random() * 1400;
  cloud.size = nextCloud.size;
  cloud.opacity = nextCloud.opacity;
  cloud.speed = nextCloud.speed;
}

function resizeScene() {
  const canvas = canvasRef.value;
  const scene = sceneRef.value;
  if (!canvas || !scene) {
    return;
  }

  const rect = scene.getBoundingClientRect();
  const dpr = window.devicePixelRatio || 1;
  animationState.width = rect.width;
  animationState.height = rect.height;
  animationState.dpr = dpr;

  canvas.width = Math.round(rect.width * dpr);
  canvas.height = Math.round(rect.height * dpr);
  canvas.style.width = `${rect.width}px`;
  canvas.style.height = `${rect.height}px`;

  const desiredCount = Math.max(56, Math.min(104, Math.round(rect.width / 13)));
  if (animationState.clouds.length === 0) {
    animationState.clouds = Array.from({ length: desiredCount }, () => createCloud(rect.width, rect.height));
    return;
  }

  if (animationState.clouds.length < desiredCount) {
    while (animationState.clouds.length < desiredCount) {
      animationState.clouds.push(createCloud(rect.width, rect.height));
    }
    return;
  }

  animationState.clouds.length = desiredCount;
}

function drawClouds(timestamp) {
  const canvas = canvasRef.value;
  if (!canvas || !animationState.image) {
    return;
  }

  if (!animationState.lastTime) {
    animationState.lastTime = timestamp;
  }

  const delta = Math.min(0.04, (timestamp - animationState.lastTime) / 1000);
  animationState.lastTime = timestamp;

  const context = canvas.getContext("2d");
  if (!context) {
    return;
  }

  const { width, height, dpr, image, clouds } = animationState;
  const perspective = 1400;

  pointer.x += (pointerTarget.x - pointer.x) * 0.065;
  pointer.y += (pointerTarget.y - pointer.y) * 0.065;

  context.setTransform(1, 0, 0, 1, 0, 0);
  context.clearRect(0, 0, canvas.width, canvas.height);
  context.scale(dpr, dpr);

  clouds.sort((left, right) => right.z - left.z);

  for (const cloud of clouds) {
    cloud.z -= cloud.speed * delta;

    if (cloud.z < 260) {
      resetCloud(cloud, width, height);
    }

    const scale = perspective / cloud.z;
    const size = image.width * cloud.size * scale * 1.96;
    const parallaxX = pointer.x * 118 * scale;
    const parallaxY = pointer.y * 52 * scale;
    const screenX = width / 2 + cloud.x * scale + parallaxX;
    const screenY = cloud.y * scale + parallaxY;
    const alpha = Math.min(0.68, cloud.opacity + (1 - cloud.z / 8200) * 0.12);

    context.globalAlpha = Math.max(0.04, alpha);
    context.drawImage(
      image,
      screenX - size * 0.5,
      screenY - size * 0.3,
      size,
      size
    );
  }

  context.globalAlpha = 1;
  animationState.frameId = window.requestAnimationFrame(drawClouds);
}

function handlePointerMove(event) {
  const scene = sceneRef.value;
  if (!scene) {
    return;
  }

  const rect = scene.getBoundingClientRect();
  pointerTarget.x = ((event.clientX - rect.left) / rect.width - 0.5) * 2;
  pointerTarget.y = ((event.clientY - rect.top) / rect.height - 0.5) * 2;
}

function resetPointer() {
  pointerTarget.x = 0;
  pointerTarget.y = 0;
}

function switchToRegister() {
  sessionStore.errorMessage = "";
  registerErrorMessage.value = "";
  successMessage.value = "";
  isRegisterMode.value = true;
  registerForm.role = form.role;
  registerForm.realName = form.realName;
  registerForm.loginId = form.loginId;
  registerForm.password = "";
  registerForm.confirmPassword = "";
}

function switchToLogin() {
  isRegisterMode.value = false;
  registerErrorMessage.value = "";
}

function fillLoginFromRegister() {
  form.role = registerForm.role;
  form.realName = registerForm.realName.trim();
  form.loginId = registerForm.loginId.trim();
  form.password = registerForm.password;
  successMessage.value = "注册信息已回填到登录表单，可以直接登录。";
  switchToLogin();
}

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

async function handleRegister() {
  sessionStore.errorMessage = "";
  registerErrorMessage.value = "";
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

    fillLoginFromRegister();
    successMessage.value = "注册成功，登录表单已自动回填。";
  } catch (error) {
    registerErrorMessage.value = error?.message || "注册失败";
  }
}

function handleResize() {
  resizeScene();
}

onMounted(() => {
  const image = new Image();
  image.onload = () => {
    animationState.image = image;
    resizeScene();
    animationState.frameId = window.requestAnimationFrame(drawClouds);
  };
  image.src = cloudSprite;

  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  window.cancelAnimationFrame(animationState.frameId);
});
</script>

<style scoped>
.login-stage {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 100vh;
  overflow: hidden;
  background:
    linear-gradient(180deg, #2b588b 0%, #3b74a7 62%, #6ea6d4 100%);
}

.login-stage__canvas,
.login-stage__sky,
.login-stage__mist {
  position: absolute;
  inset: 0;
}

.login-stage__canvas {
  z-index: 0;
  pointer-events: none;
}

.login-stage__sky {
  z-index: 1;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.04), transparent 30%),
    radial-gradient(circle at center 84%, rgba(255, 255, 255, 0.08), transparent 16rem),
    radial-gradient(circle at 16% 86%, rgba(255, 255, 255, 0.06), transparent 12rem),
    radial-gradient(circle at 84% 86%, rgba(255, 255, 255, 0.05), transparent 12rem);
  mix-blend-mode: screen;
  pointer-events: none;
}

.login-stage__mist {
  z-index: 2;
  background:
    linear-gradient(180deg, rgba(10, 23, 42, 0.14) 0%, transparent 18%, transparent 52%, rgba(255, 255, 255, 0.08) 100%);
  pointer-events: none;
}

.login-card {
  position: relative;
  z-index: 3;
  width: min(430px, calc(100% - 32px));
  padding: 34px 34px 28px;
  border: 1px solid rgba(136, 178, 217, 0.16);
  background:
    linear-gradient(210deg, rgba(27, 64, 105, 0.6), rgba(8, 10, 14, 0.72)),
    rgba(10, 15, 24, 0.62);
  box-shadow:
    -20px 20px 24px rgba(11, 24, 47, 0.42),
    0 22px 28px rgba(11, 24, 47, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(12px);
}

.login-card__header {
  text-align: center;
}

.login-card__tag {
  margin: 0;
  font-size: 0.76rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: rgba(195, 215, 239, 0.62);
}

.login-card__header h2 {
  margin: 18px 0 10px;
  font-size: 2rem;
  font-weight: 500;
  color: rgba(230, 235, 246, 0.94);
}

.login-card__copy {
  margin: 0;
  line-height: 1.7;
  color: rgba(210, 218, 233, 0.72);
}

.mode-tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin: 24px 0 18px;
  padding: 6px;
  border: 1px solid rgba(125, 167, 210, 0.18);
  background: rgba(15, 28, 49, 0.42);
}

.mode-tabs button,
.role-switch button {
  border: 0;
  background: transparent;
  color: rgba(210, 221, 239, 0.76);
  cursor: pointer;
  transition: background 160ms ease, color 160ms ease, box-shadow 160ms ease;
}

.mode-tabs button {
  padding: 10px 12px;
}

.mode-tabs button.active,
.role-switch button.active {
  color: #eef6ff;
  background: rgba(78, 160, 227, 0.22);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.mode-tabs button:disabled,
.role-switch button:disabled,
.primary-button:disabled,
.ghost-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-form {
  display: grid;
  gap: 16px;
}

.role-switch {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 6px;
  border: 1px solid rgba(125, 167, 210, 0.14);
  background: rgba(12, 22, 39, 0.42);
}

.role-switch button {
  padding: 10px 12px;
}

.login-field {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 58px;
  border-bottom: 1px solid rgba(143, 170, 203, 0.18);
  background: rgba(12, 18, 28, 0.08);
}

.login-field__icon {
  width: 18px;
  height: 18px;
  margin: 0 18px 0 8px;
  opacity: 0.62;
}

.login-field input {
  width: 100%;
  padding: 16px 12px 16px 0;
  border: 0;
  outline: none;
  background: transparent;
  color: #49b3ff;
  font-size: 1rem;
}

.login-field input::placeholder {
  color: rgba(231, 235, 244, 0.76);
}

.login-card__actions {
  display: grid;
  gap: 12px;
  margin-top: 10px;
}

.primary-button,
.ghost-button {
  min-height: 54px;
  border-radius: 999px;
  font-size: 1rem;
}

.primary-button {
  border: 2px solid #4fa1d9;
  background: rgba(79, 161, 217, 0.1);
  color: #46aef9;
  cursor: pointer;
}

.primary-button:hover:not(:disabled) {
  background: rgba(79, 161, 217, 0.18);
}

.ghost-button {
  border: 1px solid rgba(128, 177, 222, 0.24);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(221, 232, 248, 0.88);
  cursor: pointer;
}

.ghost-button:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.08);
}

.login-card__footer {
  margin-top: 22px;
  text-align: center;
}

.login-card__footer p {
  margin: 0;
  font-size: 0.96rem;
  color: rgba(229, 235, 244, 0.84);
}

:deep(.status-banner) {
  border-radius: 0;
  background: rgba(255, 255, 255, 0.06);
}

:deep(.status-banner.info) {
  background: rgba(74, 163, 226, 0.16);
  color: #d7efff;
}

:deep(.status-banner.danger) {
  background: rgba(173, 54, 54, 0.18);
  color: #ffd8d8;
}

@media (max-width: 640px) {
  .login-card {
    padding: 26px 22px 22px;
  }

  .login-card__header h2 {
    font-size: 1.72rem;
  }
}
</style>
