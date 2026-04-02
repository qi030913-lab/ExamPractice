<template>
  <section ref="sceneRef" class="login-stage">
    <div ref="threeMountRef" class="login-stage__three"></div>
    <div class="login-stage__overlay"></div>

    <section class="login-card">
      <header class="login-card__header">
        <p class="login-card__tag">Exam Practice Desktop</p>
        <h2>{{ isRegisterMode ? registerTitle : loginTitle }}</h2>
        <p class="login-card__copy">
          {{ isRegisterMode ? "注册成功后会自动回填到登录表单。" : "背景复用 login 目录下旧版云层动画，表单继续使用当前项目字段与接口。" }}
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
import userIcon from "@/views/login/img/user_icon_copy.png";
import keyIcon from "@/views/login/img/key.png";
import lockIcon from "@/views/login/img/lock_icon_copy.png";
import cloudSpriteUrl from "@/views/login/img/cloud.png";
import DetectorScript from "@/views/login/img/Detector.js?raw";
import ThreeWebGLScript from "@/views/login/img/ThreeWebGL.js?raw";
import ThreeExtrasScript from "@/views/login/img/ThreeExtras.js?raw";
import RequestAnimationFrameScript from "@/views/login/img/RequestAnimationFrame.js?raw";

const router = useRouter();
const sessionStore = useSessionStore();

const sceneRef = ref(null);
const threeMountRef = ref(null);
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

const backgroundState = {
  cleanup: null
};

const loginIdPlaceholder = computed(() => form.role === "TEACHER" ? "教工号" : "学号");
const registerLoginIdPlaceholder = computed(() => registerForm.role === "TEACHER" ? "教工号" : "学号");
const loginTitle = computed(() => form.role === "TEACHER" ? "教师登录" : "学生登录");
const registerTitle = computed(() => registerForm.role === "TEACHER" ? "教师注册" : "学生注册");
const footerCopy = computed(() => {
  if (isRegisterMode.value) {
    return "注册后将自动回填到登录表单，背景继续保留旧版动态云层效果。";
  }

  return "当前背景直接复用 login 目录里的旧版 Three.js 云层方案。";
});

function injectLegacyThree() {
  if (window.__legacyLoginThreeLoaded) {
    return;
  }

  window.eval(`${DetectorScript}\n//# sourceURL=legacy-detector.js`);
  window.eval(`${ThreeWebGLScript}\n//# sourceURL=legacy-threewebgl.js`);
  window.eval(`${ThreeExtrasScript}\n//# sourceURL=legacy-threeextras.js`);
  window.eval(`${RequestAnimationFrameScript}\n//# sourceURL=legacy-requestAnimationFrame.js`);
  window.__legacyLoginThreeLoaded = true;
}

function mountLegacyBackground() {
  const mountEl = threeMountRef.value;
  const stageEl = sceneRef.value;
  if (!mountEl || !stageEl) {
    return;
  }

  injectLegacyThree();

  const THREE = window.THREE;
  const Detector = window.Detector;
  const GeometryUtils = window.GeometryUtils;

  if (!THREE || !Detector || !GeometryUtils || !Detector.webgl) {
    return;
  }

  let renderer;
  let camera;
  let scene;
  let geometry;
  let material;
  let meshFront;
  let meshBack;
  let frameId = 0;
  let startTime = Date.now();
  let mouseX = 0;
  let mouseY = 0;
  let container;

  const gradientCanvas = document.createElement("canvas");
  const gradientContext = gradientCanvas.getContext("2d");
  gradientCanvas.width = 32;
  gradientCanvas.height = Math.max(720, stageEl.clientHeight);
  const gradient = gradientContext.createLinearGradient(0, 0, 0, gradientCanvas.height);
  gradient.addColorStop(0, "#1e4877");
  gradient.addColorStop(0.52, "#4e88ba");
  gradient.addColorStop(1, "#8ec0e8");
  gradientContext.fillStyle = gradient;
  gradientContext.fillRect(0, 0, gradientCanvas.width, gradientCanvas.height);
  stageEl.style.backgroundImage = `url(${gradientCanvas.toDataURL("image/png")})`;
  stageEl.style.backgroundSize = "cover";

  container = document.createElement("div");
  container.className = "legacy-clouds";
  mountEl.appendChild(container);

  camera = new THREE.Camera(30, stageEl.clientWidth / stageEl.clientHeight, 1, 3000);
  camera.position.z = 6000;

  scene = new THREE.Scene();
  geometry = new THREE.Geometry();

  const texture = THREE.ImageUtils.loadTexture(cloudSpriteUrl);
  texture.magFilter = THREE.LinearMipMapLinearFilter;
  texture.minFilter = THREE.LinearMipMapLinearFilter;

  const fog = new THREE.Fog(0x4584b4, -100, 3000);

  material = new THREE.MeshShaderMaterial({
    uniforms: {
      map: { type: "t", value: 2, texture },
      fogColor: { type: "c", value: fog.color },
      fogNear: { type: "f", value: fog.near },
      fogFar: { type: "f", value: fog.far }
    },
    vertexShader: "varying vec2 vUv; void main() { vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 ); }",
    fragmentShader: "uniform sampler2D map; uniform vec3 fogColor; uniform float fogNear; uniform float fogFar; varying vec2 vUv; void main() { float depth = gl_FragCoord.z / gl_FragCoord.w; float fogFactor = smoothstep( fogNear, fogFar, depth ); gl_FragColor = texture2D( map, vUv ); gl_FragColor.w *= pow( gl_FragCoord.z, 20.0 ); gl_FragColor = mix( gl_FragColor, vec4( fogColor, gl_FragColor.w ), fogFactor ); }",
    depthTest: false
  });

  const plane = new THREE.Mesh(new THREE.Plane(64, 64));
  for (let index = 0; index < 8000; index += 1) {
    plane.position.x = Math.random() * 1000 - 500;
    plane.position.y = -Math.random() * Math.random() * 200 - 15;
    plane.position.z = index;
    plane.rotation.z = Math.random() * Math.PI;
    plane.scale.x = plane.scale.y = Math.random() * Math.random() * 1.5 + 0.5;
    GeometryUtils.merge(geometry, plane);
  }

  meshFront = new THREE.Mesh(geometry, material);
  scene.addObject(meshFront);
  meshBack = new THREE.Mesh(geometry, material);
  meshBack.position.z = -8000;
  scene.addObject(meshBack);

  renderer = new THREE.WebGLRenderer({
    antialias: false,
    alpha: true
  });
  renderer.setSize(stageEl.clientWidth, stageEl.clientHeight);
  container.appendChild(renderer.domElement);

  const onMouseMove = (event) => {
    const rect = stageEl.getBoundingClientRect();
    mouseX = (event.clientX - rect.left - rect.width / 2) * 0.25;
    mouseY = (event.clientY - rect.top - rect.height / 2) * 0.15;
  };

  const onResize = () => {
    if (!renderer || !camera) {
      return;
    }

    const width = stageEl.clientWidth;
    const height = stageEl.clientHeight;
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
    renderer.setSize(width, height);
  };

  const render = () => {
    const position = ((Date.now() - startTime) * 0.03) % 8000;

    camera.position.x += (mouseX - camera.target.position.x) * 0.01;
    camera.position.y += (-mouseY - camera.target.position.y) * 0.01;
    camera.position.z = -position + 8000;
    camera.target.position.x = camera.position.x;
    camera.target.position.y = camera.position.y;
    camera.target.position.z = camera.position.z - 1000;

    renderer.render(scene, camera);
    frameId = window.requestAnimationFrame(render);
  };

  stageEl.addEventListener("mousemove", onMouseMove);
  window.addEventListener("resize", onResize);
  render();

  backgroundState.cleanup = () => {
    window.cancelAnimationFrame(frameId);
    window.removeEventListener("resize", onResize);
    stageEl.removeEventListener("mousemove", onMouseMove);
    if (renderer?.domElement?.parentNode) {
      renderer.domElement.parentNode.removeChild(renderer.domElement);
    }
    if (container?.parentNode) {
      container.parentNode.removeChild(container);
    }
    stageEl.style.backgroundImage = "";
  };
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

onMounted(() => {
  mountLegacyBackground();
});

onBeforeUnmount(() => {
  backgroundState.cleanup?.();
});
</script>

<style scoped>
.login-stage {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 100vh;
  overflow: hidden;
  background: #3f76a8;
}

.login-stage__three,
.login-stage__overlay {
  position: absolute;
  inset: 0;
}

.login-stage__three {
  z-index: 0;
}

.login-stage__overlay {
  z-index: 1;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), transparent 28%),
    linear-gradient(180deg, rgba(6, 16, 30, 0.06), transparent 22%, transparent 72%, rgba(255, 255, 255, 0.04) 100%);
  pointer-events: none;
}

.login-card {
  position: relative;
  z-index: 2;
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
