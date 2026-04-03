<template>
  <section ref="sceneRef" class="login-stage">
    <div ref="threeMountRef" class="login-stage__three"></div>
    <div class="login-stage__overlay"></div>

    <section class="auth-shell">
      <article :class="['auth-card', { 'auth-card--register': isRegisterMode }]">
        <aside :class="['auth-card__welcome', { 'auth-card__welcome--right': isRegisterMode }]">
          <p class="auth-card__eyebrow">{{ welcomeEyebrow }}</p>
          <h2>{{ welcomeTitle }}</h2>
          <p class="auth-card__welcome-copy">{{ welcomeCopy }}</p>

          <button
            class="auth-card__switch"
            type="button"
            :disabled="sessionStore.loading"
            @click="isRegisterMode ? switchToLogin() : switchToRegister()"
          >
            {{ switchButtonText }}
          </button>

          <div class="auth-card__welcome-tags">
            <span v-for="tag in welcomeTags" :key="tag">{{ tag }}</span>
          </div>
        </aside>

        <div class="auth-card__panel">
          <form v-if="!isRegisterMode" class="auth-form" @submit.prevent="handleSubmit">
            <StatusBanner v-if="sessionStore.errorMessage" tone="danger">
              {{ sessionStore.errorMessage }}
            </StatusBanner>

            <div class="role-switch">
              <button
                type="button"
                :class="{ active: form.role === 'STUDENT' }"
                :disabled="sessionStore.loading"
                @click="form.role = 'STUDENT'; clearLoginErrors()"
              >
                学生
              </button>
              <button
                type="button"
                :class="{ active: form.role === 'TEACHER' }"
                :disabled="sessionStore.loading"
                @click="form.role = 'TEACHER'; clearLoginErrors()"
              >
                教师
              </button>
            </div>

            <label class="auth-field">
              <span class="auth-field__label">姓名</span>
              <div class="auth-field__box">
                <div :class="['auth-field__control', { 'auth-field__control--invalid': loginErrors.realName }]">
                  <input
                    ref="loginRealNameRef"
                  v-model="form.realName"
                  type="text"
                  placeholder="请输入姓名"
                  autocomplete="name"
                  @input="loginErrors.realName = ''"
                  :aria-invalid="Boolean(loginErrors.realName)"
                  :aria-describedby="loginErrors.realName ? 'login-real-name-error' : undefined"
                />
                  <img class="auth-field__icon" :src="userIcon" alt="" />
                </div>
                <span
                  v-if="loginErrors.realName"
                  id="login-real-name-error"
                  class="auth-field__error-bubble"
                  role="alert"
                >
                  <span class="auth-field__error-panel">
                    <span class="auth-field__error-icon" aria-hidden="true">!</span>
                    <span>{{ loginErrors.realName }}</span>
                  </span>
                </span>
              </div>
            </label>

            <label class="auth-field">
              <span class="auth-field__label">{{ loginIdLabel }}</span>
              <div class="auth-field__box">
                <div :class="['auth-field__control', { 'auth-field__control--invalid': loginErrors.loginId }]">
                  <input
                    ref="loginLoginIdRef"
                    v-model="form.loginId"
                    type="text"
                    :placeholder="loginIdPlaceholder"
                    autocomplete="username"
                    @input="loginErrors.loginId = ''"
                    :aria-invalid="Boolean(loginErrors.loginId)"
                    :aria-describedby="loginErrors.loginId ? 'login-id-error' : undefined"
                  />
                  <img class="auth-field__icon" :src="keyIcon" alt="" />
                </div>
                <span
                  v-if="loginErrors.loginId"
                  id="login-id-error"
                  class="auth-field__error-bubble"
                  role="alert"
                >
                  <span class="auth-field__error-panel">
                    <span class="auth-field__error-icon" aria-hidden="true">!</span>
                    <span>{{ loginErrors.loginId }}</span>
                  </span>
                </span>
              </div>
            </label>

            <label class="auth-field">
              <span class="auth-field__label">密码</span>
              <div class="auth-field__box">
                <div :class="['auth-field__control', { 'auth-field__control--invalid': loginErrors.password }]">
                  <input
                    ref="loginPasswordRef"
                    v-model="form.password"
                    type="password"
                    placeholder="请输入密码"
                    autocomplete="current-password"
                    @input="loginErrors.password = ''"
                    :aria-invalid="Boolean(loginErrors.password)"
                    :aria-describedby="loginErrors.password ? 'login-password-error' : undefined"
                  />
                  <img class="auth-field__icon" :src="lockIcon" alt="" />
                </div>
                <span
                  v-if="loginErrors.password"
                  id="login-password-error"
                  class="auth-field__error-bubble"
                  role="alert"
                >
                  <span class="auth-field__error-panel">
                    <span class="auth-field__error-icon" aria-hidden="true">!</span>
                    <span>{{ loginErrors.password }}</span>
                  </span>
                </span>
              </div>
            </label>

            <button class="auth-form__primary" type="submit" :disabled="sessionStore.loading">
              {{ sessionStore.loading ? "登录中..." : "登录" }}
            </button>
          </form>

          <form v-else class="auth-form" @submit.prevent="handleRegister">
            <StatusBanner v-if="registerErrorMessage" tone="danger">
              {{ registerErrorMessage }}
            </StatusBanner>

            <div class="role-switch">
              <button
                type="button"
                :class="{ active: registerForm.role === 'STUDENT' }"
                :disabled="sessionStore.loading"
                @click="registerForm.role = 'STUDENT'; clearRegisterErrors()"
              >
                学生
              </button>
              <button
                type="button"
                :class="{ active: registerForm.role === 'TEACHER' }"
                :disabled="sessionStore.loading"
                @click="registerForm.role = 'TEACHER'; clearRegisterErrors()"
              >
                教师
              </button>
            </div>

            <label class="auth-field">
              <span class="auth-field__label">姓名</span>
              <div class="auth-field__box">
                <div :class="['auth-field__control', { 'auth-field__control--invalid': registerErrors.realName }]">
                  <input
                    ref="registerRealNameRef"
                    v-model="registerForm.realName"
                    type="text"
                    placeholder="请输入姓名"
                    autocomplete="name"
                    @input="registerErrors.realName = ''"
                    :aria-invalid="Boolean(registerErrors.realName)"
                    :aria-describedby="registerErrors.realName ? 'register-real-name-error' : undefined"
                  />
                  <img class="auth-field__icon" :src="userIcon" alt="" />
                </div>
                <span
                  v-if="registerErrors.realName"
                  id="register-real-name-error"
                  class="auth-field__error-bubble"
                  role="alert"
                >
                  <span class="auth-field__error-panel">
                    <span class="auth-field__error-icon" aria-hidden="true">!</span>
                    <span>{{ registerErrors.realName }}</span>
                  </span>
                </span>
              </div>
            </label>

            <label class="auth-field">
              <span class="auth-field__label">{{ registerLoginIdLabel }}</span>
              <div class="auth-field__box">
                <div :class="['auth-field__control', { 'auth-field__control--invalid': registerErrors.loginId }]">
                  <input
                    ref="registerLoginIdRef"
                    v-model="registerForm.loginId"
                    type="text"
                    :placeholder="registerLoginIdPlaceholder"
                    autocomplete="username"
                    @input="registerErrors.loginId = ''"
                    :aria-invalid="Boolean(registerErrors.loginId)"
                    :aria-describedby="registerErrors.loginId ? 'register-id-error' : undefined"
                  />
                  <img class="auth-field__icon" :src="keyIcon" alt="" />
                </div>
                <span
                  v-if="registerErrors.loginId"
                  id="register-id-error"
                  class="auth-field__error-bubble"
                  role="alert"
                >
                  <span class="auth-field__error-panel">
                    <span class="auth-field__error-icon" aria-hidden="true">!</span>
                    <span>{{ registerErrors.loginId }}</span>
                  </span>
                </span>
              </div>
            </label>

            <label class="auth-field">
              <span class="auth-field__label">密码</span>
              <div class="auth-field__box">
                <div :class="['auth-field__control', { 'auth-field__control--invalid': registerErrors.password }]">
                  <input
                    ref="registerPasswordRef"
                    v-model="registerForm.password"
                    type="password"
                    placeholder="请设置密码"
                    autocomplete="new-password"
                    @input="registerErrors.password = ''"
                    :aria-invalid="Boolean(registerErrors.password)"
                    :aria-describedby="registerErrors.password ? 'register-password-error' : undefined"
                  />
                  <img class="auth-field__icon" :src="lockIcon" alt="" />
                </div>
                <span
                  v-if="registerErrors.password"
                  id="register-password-error"
                  class="auth-field__error-bubble"
                  role="alert"
                >
                  <span class="auth-field__error-panel">
                    <span class="auth-field__error-icon" aria-hidden="true">!</span>
                    <span>{{ registerErrors.password }}</span>
                  </span>
                </span>
              </div>
            </label>

            <label class="auth-field">
              <span class="auth-field__label">确认密码</span>
              <div class="auth-field__box">
                <div :class="['auth-field__control', { 'auth-field__control--invalid': registerErrors.confirmPassword }]">
                  <input
                    ref="registerConfirmPasswordRef"
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入密码"
                    autocomplete="new-password"
                    @input="registerErrors.confirmPassword = ''"
                    :aria-invalid="Boolean(registerErrors.confirmPassword)"
                    :aria-describedby="registerErrors.confirmPassword ? 'register-confirm-password-error' : undefined"
                  />
                  <img class="auth-field__icon" :src="lockIcon" alt="" />
                </div>
                <span
                  v-if="registerErrors.confirmPassword"
                  id="register-confirm-password-error"
                  class="auth-field__error-bubble"
                  role="alert"
                >
                  <span class="auth-field__error-panel">
                    <span class="auth-field__error-icon" aria-hidden="true">!</span>
                    <span>{{ registerErrors.confirmPassword }}</span>
                  </span>
                </span>
              </div>
            </label>

            <button class="auth-form__primary" type="submit" :disabled="sessionStore.loading">
              {{ sessionStore.loading ? "注册中..." : "注册" }}
            </button>
          </form>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import userIcon from "@/assets/legacy-login-bg/user_icon_copy.png";
import keyIcon from "@/assets/legacy-login-bg/key.png";
import lockIcon from "@/assets/legacy-login-bg/lock_icon_copy.png";
import cloudSpriteUrl from "@/assets/legacy-login-bg/cloud.png";
import DetectorScript from "@/assets/legacy-login-bg/Detector.js?raw";
import ThreeWebGLScript from "@/assets/legacy-login-bg/ThreeWebGL.js?raw";
import ThreeExtrasScript from "@/assets/legacy-login-bg/ThreeExtras.js?raw";
import RequestAnimationFrameScript from "@/assets/legacy-login-bg/RequestAnimationFrame.js?raw";

const router = useRouter();
const sessionStore = useSessionStore();

const sceneRef = ref(null);
const threeMountRef = ref(null);
const loginRealNameRef = ref(null);
const loginLoginIdRef = ref(null);
const loginPasswordRef = ref(null);
const registerRealNameRef = ref(null);
const registerLoginIdRef = ref(null);
const registerPasswordRef = ref(null);
const registerConfirmPasswordRef = ref(null);
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

const loginErrors = reactive({
  realName: "",
  loginId: "",
  password: ""
});

const registerErrors = reactive({
  realName: "",
  loginId: "",
  password: "",
  confirmPassword: ""
});

const activeRole = computed(() => isRegisterMode.value ? registerForm.role : form.role);
const activeRoleLabel = computed(() => activeRole.value === "TEACHER" ? "教师" : "学生");
const loginIdLabel = computed(() => form.role === "TEACHER" ? "教工号" : "学号");
const registerLoginIdLabel = computed(() => registerForm.role === "TEACHER" ? "教工号" : "学号");
const loginIdPlaceholder = computed(() => form.role === "TEACHER" ? "请输入教工号" : "请输入学号");
const registerLoginIdPlaceholder = computed(() => registerForm.role === "TEACHER" ? "请设置教工号" : "请设置学号");
const welcomeEyebrow = computed(() => isRegisterMode.value ? "已有账号" : "在线考试练习平台");
const welcomeTitle = computed(() => isRegisterMode.value ? "欢迎回来" : "你好，欢迎使用");
const welcomeCopy = computed(() => {
  if (isRegisterMode.value) {
    return "如果你已经拥有学生或教师账号，可以直接返回登录页，进入对应工作台。";
  }

  return "使用当前项目的学生或教师账号登录，进入桌面端在线考试练习平台。";
});
const switchButtonText = computed(() => isRegisterMode.value ? "去登录" : "去注册");
const welcomeTags = computed(() => {
  if (isRegisterMode.value) {
    return ["返回登录", "继续使用", "身份切换"];
  }

  return ["学生入口", "教师入口", "在线练习"];
});
const requiredFieldMessage = "请填入此字段";

function clearLoginErrors() {
  loginErrors.realName = "";
  loginErrors.loginId = "";
  loginErrors.password = "";
}

function clearRegisterErrors() {
  registerErrors.realName = "";
  registerErrors.loginId = "";
  registerErrors.password = "";
  registerErrors.confirmPassword = "";
}

function showSingleFieldError(errorMap, field, message, targetRef) {
  for (const key of Object.keys(errorMap)) {
    errorMap[key] = "";
  }

  errorMap[field] = message;
  targetRef?.value?.focus?.();
  return false;
}

function validateLoginForm() {
  clearLoginErrors();

  if (!form.realName.trim()) {
    return showSingleFieldError(loginErrors, "realName", requiredFieldMessage, loginRealNameRef);
  }
  if (!form.loginId.trim()) {
    return showSingleFieldError(loginErrors, "loginId", requiredFieldMessage, loginLoginIdRef);
  }
  if (!form.password) {
    return showSingleFieldError(loginErrors, "password", requiredFieldMessage, loginPasswordRef);
  }

  return true;
}

function validateRegisterForm() {
  clearRegisterErrors();

  if (!registerForm.realName.trim()) {
    return showSingleFieldError(registerErrors, "realName", requiredFieldMessage, registerRealNameRef);
  }
  if (!registerForm.loginId.trim()) {
    return showSingleFieldError(registerErrors, "loginId", requiredFieldMessage, registerLoginIdRef);
  }
  if (!registerForm.password) {
    return showSingleFieldError(registerErrors, "password", requiredFieldMessage, registerPasswordRef);
  }
  if (!registerForm.confirmPassword) {
    return showSingleFieldError(registerErrors, "confirmPassword", requiredFieldMessage, registerConfirmPasswordRef);
  } else if (registerForm.password && registerForm.password !== registerForm.confirmPassword) {
    return showSingleFieldError(registerErrors, "confirmPassword", "两次输入的密码不一致。", registerConfirmPasswordRef);
  }

  return true;
}

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
  const startTime = Date.now();
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
  clearLoginErrors();
  clearRegisterErrors();
  isRegisterMode.value = true;
  registerForm.role = form.role;
  registerForm.realName = form.realName;
  registerForm.loginId = form.loginId;
  registerForm.password = "";
  registerForm.confirmPassword = "";
}

function switchToLogin() {
  sessionStore.errorMessage = "";
  isRegisterMode.value = false;
  registerErrorMessage.value = "";
  clearLoginErrors();
  clearRegisterErrors();
}

function fillLoginFromRegister() {
  form.role = registerForm.role;
  form.realName = registerForm.realName.trim();
  form.loginId = registerForm.loginId.trim();
  form.password = "";
  registerForm.password = "";
  registerForm.confirmPassword = "";
  switchToLogin();
}

async function handleSubmit() {
  registerErrorMessage.value = "";
  sessionStore.errorMessage = "";

  if (!validateLoginForm()) {
    return;
  }

  try {
    await sessionStore.loginWithPassword({
      role: form.role,
      realName: form.realName.trim(),
      loginId: form.loginId.trim(),
      password: form.password
    });

    router.push(form.role === "TEACHER" ? "/teacher" : "/student");
  } catch (_error) {
    // Error state is already handled by the session store.
  }
}

async function handleRegister() {
  sessionStore.errorMessage = "";
  registerErrorMessage.value = "";

  if (!validateRegisterForm()) {
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
  height: 100vh;
  min-height: 100vh;
  padding: 16px;
  box-sizing: border-box;
  overflow-x: hidden;
  overflow-y: hidden;
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
  pointer-events: none;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), transparent 28%),
    linear-gradient(180deg, rgba(6, 16, 30, 0.06), transparent 22%, transparent 72%, rgba(255, 255, 255, 0.04) 100%);
}

.auth-shell {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  width: min(940px, 100%);
  height: calc(100vh - 32px);
  min-height: 0;
  padding: 0;
}

.auth-card {
  display: grid;
  grid-template-columns: minmax(320px, 0.92fr) minmax(420px, 1.08fr);
  grid-template-areas: "welcome panel";
  width: 100%;
  height: min(540px, 100%);
  min-height: 0;
  border-radius: 34px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: rgba(255, 255, 255, 0);
  box-shadow:
    0 38px 80px rgba(12, 31, 63, 0.3),
    0 16px 30px rgba(40, 68, 132, 0.16);
  backdrop-filter: blur(22px) saturate(1.12);
  animation: card-rise 420ms ease;
}

.auth-card--register {
  grid-template-columns: minmax(420px, 1.08fr) minmax(320px, 0.92fr);
  grid-template-areas: "panel welcome";
}

.auth-card--register .auth-card__panel {
  border-left: 0;
  border-right: 1px solid rgba(255, 255, 255, 0.16);
}

.auth-card__welcome {
  grid-area: welcome;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 18px;
  padding: 36px 30px;
  text-align: center;
  color: #000000;
  opacity: 0.5;
  background:
    linear-gradient(160deg, rgba(126, 156, 244, 0.18), rgba(95, 124, 220, 0.26)),
    rgba(111, 140, 232, 0.09);
  border-right: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.18),
    inset -18px 0 34px rgba(66, 92, 177, 0.08);
  border-radius: 34px 150px 150px 34px;
  backdrop-filter: blur(28px) saturate(1.15);
}

.auth-card__welcome--right {
  border-right: 0;
  border-left: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 150px 34px 34px 150px;
}

.auth-card__eyebrow {
  margin: 0;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.auth-card__eyebrow {
  font-size: 0.74rem;
  color: rgba(0, 0, 0, 0.78);
}

.auth-card__welcome h2,
.auth-panel__header h1 {
  margin: 0;
  font-family: "Trebuchet MS", "Avenir Next", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-weight: 700;
  line-height: 1.08;
}

.auth-card__welcome h2 {
  font-size: clamp(2rem, 3.4vw, 2.8rem);
}

.auth-card__welcome-copy {
  max-width: 280px;
  margin: 0;
  line-height: 1.68;
  color: rgba(0, 0, 0, 0.88);
}

.auth-card__switch {
  min-width: 168px;
  min-height: 52px;
  padding: 0 28px;
  border: 1.5px solid rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
  color: #000000;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition:
    transform 180ms ease,
    background 180ms ease,
    box-shadow 180ms ease;
}

.auth-card__switch:hover:not(:disabled) {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.14);
  box-shadow: 0 12px 20px rgba(60, 80, 160, 0.18);
}

.auth-card__switch:focus-visible {
  outline: 3px solid rgba(255, 255, 255, 0.78);
  outline-offset: 3px;
}

.auth-card__welcome-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.auth-card__welcome-tags span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 86px;
  min-height: 40px;
  padding: 0 12px;
  border-radius: 14px;
  font-size: 0.88rem;
}

.auth-card__welcome-tags span {
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: rgba(255, 255, 255, 0.1);
  color: rgba(0, 0, 0);
}

.auth-card__panel {
  grid-area: panel;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 16px 30px 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0), rgba(250, 250, 254, 0)),
    rgba(255, 255, 255, 0.06);
  border-left: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(30px) saturate(1.16);
}

.auth-form {
  display: grid;
  gap: 10px;
  width: min(100%, 360px);
  margin: 0 auto;
}

.role-switch {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 5px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(16px);
}

.role-switch button {
  min-height: 42px;
  border: 0;
  border-radius: 14px;
  background: transparent;
  color: #707899;
  font-size: 0.92rem;
  font-weight: 600;
  cursor: pointer;
  transition:
    color 160ms ease,
    background 160ms ease,
    box-shadow 160ms ease;
}

.role-switch button.active {
  background: linear-gradient(135deg, #8ea6f6, #748be6);
  color: #000000;
  box-shadow: 0 10px 18px rgba(120, 141, 226, 0.28);
}

.role-switch button:focus-visible {
  outline: 3px solid rgba(103, 126, 223, 0.42);
  outline-offset: 2px;
}

.auth-field {
  display: grid;
  gap: 6px;
}

.auth-field__box {
  position: relative;
}

.auth-field__label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #3e455f;
}

.auth-field__control {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 46px;
  border: 1px solid rgba(255, 255, 255, 0.26);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.24),
    0 10px 18px rgba(27, 56, 116, 0.1);
  backdrop-filter: blur(20px) saturate(1.12);
}

.auth-field__control:focus-within {
  border-color: rgba(103, 126, 223, 0.48);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    0 0 0 4px rgba(116, 139, 230, 0.12),
    0 12px 22px rgba(27, 56, 116, 0.12);
}

.auth-field__control--invalid {
  border-color: rgba(191, 74, 74, 0.5);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.24),
    0 0 0 4px rgba(214, 91, 91, 0.08),
    0 10px 18px rgba(143, 42, 42, 0.12);
}

.auth-field__control input {
  width: 100%;
  padding: 0 50px 0 15px;
  border: 0;
  outline: none;
  background: transparent;
  color: #2d3145;
  font-size: 0.96rem;
}

.auth-field__control input:focus-visible {
  outline: none;
}

.auth-field__control input::placeholder {
  color: rgba(72, 84, 116, 0.68);
}

.auth-field__icon {
  position: absolute;
  right: 16px;
  width: 16px;
  height: 16px;
  opacity: 0.48;
  filter: grayscale(1);
}

.auth-field__error-bubble {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 10px);
  z-index: 5;
  width: max-content;
  max-width: min(252px, calc(100% - 12px));
  transform: translateX(-30%);
}

.auth-field__error-bubble::after {
  content: "";
  position: absolute;
  left: 38%;
  top: 100%;
  width: 18px;
  height: 10px;
  background: rgba(40, 44, 54, 0.6);
  clip-path: polygon(50% 100%, 0 0, 100% 0);
  filter: drop-shadow(0 1px 0 rgba(255, 255, 255, 0.14));
  transform: translateY(-1px);
}

.auth-field__error-panel {
  transform: translateX(50px);
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 54px;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(54, 59, 70, 0.1), rgba(38, 42, 52, 0.1)),
    rgba(27, 31, 39, 0.5);
  box-shadow:
    0 18px 30px rgba(10, 16, 26, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.12);
  color: rgba(0, 0, 0, 0.96);
  font-size: 0.92rem;
  line-height: 1.5;
  backdrop-filter: blur(18px) saturate(1.08);
}

.auth-field__error-icon {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(180deg, #ffa62c, #ff8d10);
  color: #000000;
  font-size: 1.1rem;
  font-weight: 800;
  box-shadow: 0 8px 14px rgba(255, 149, 25, 0.24);
}

.auth-form__primary {
  min-height: 46px;
  border: 0;
  border-radius: 14px;
  background: linear-gradient(135deg, #8ea6f6, #748be6);
  color: #000000;
  font-size: 0.98rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 14px 24px rgba(118, 140, 227, 0.28);
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    filter 180ms ease;
}

.auth-form__primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 18px 28px rgba(118, 140, 227, 0.32);
  filter: saturate(1.06);
}

.auth-form__primary:focus-visible {
  outline: 3px solid rgba(103, 126, 223, 0.42);
  outline-offset: 3px;
}

.role-switch button:disabled,
.auth-card__switch:disabled,
.auth-form__primary:disabled {
  cursor: not-allowed;
  opacity: 0.2;
}

:deep(.status-banner) {
  border-radius: 16px;
  padding: 14px 16px;
  font-size: 0.95rem;
}

:deep(.status-banner.info) {
  background: rgba(116, 139, 230, 0.12);
  color: #4d63b9;
}

:deep(.status-banner.danger) {
  background: rgba(214, 91, 91, 0.14);
  color: #b64242;
}

@keyframes card-rise {
  from {
    opacity: 0;
    transform: translateY(18px) scale(0.985);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 900px) {
  .login-stage {
    height: auto;
    min-height: 100vh;
    padding: 12px;
    overflow-y: auto;
  }

  .auth-shell {
    height: auto;
    min-height: auto;
  }

  .auth-card,
  .auth-card--register {
    grid-template-columns: 1fr;
    grid-template-areas:
      "panel"
      "welcome";
    height: auto;
  }

  .auth-card__welcome,
  .auth-card__welcome--right {
    min-height: 220px;
    border-radius: 150px 150px 34px 34px;
  }

  .auth-card__panel {
    padding: 16px 24px 22px;
    border-left: 0;
    border-right: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.14);
  }
}

@media (max-width: 560px) {
  .auth-shell {
    width: 100%;
  }

  .auth-card__panel {
    padding: 14px 18px 20px;
  }

  .auth-card__welcome,
  .auth-card__welcome--right {
    min-height: 200px;
    padding: 28px 20px;
    border-radius: 120px 120px 28px 28px;
  }

  .auth-card__welcome h2 {
    font-size: 2rem;
  }

  .auth-card__welcome-tags span {
    min-width: calc(50% - 6px);
  }

  .auth-field__error-bubble {
    max-width: min(230px, calc(100% - 8px));
  }

  .auth-field__error-panel {
    font-size: 0.88rem;
  }
}
</style>
