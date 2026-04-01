const toolStatus = document.getElementById("tool-status");
const artifactStatus = document.getElementById("artifact-status");
const runningApps = document.getElementById("running-apps");
const activityLog = document.getElementById("activity-log");
const dashboardSummary = document.getElementById("dashboard-summary");
const dashboardPrimaryTitle = document.getElementById("dashboard-primary-title");
const dashboardPrimaryList = document.getElementById("dashboard-primary-list");
const dashboardSecondaryTitle = document.getElementById("dashboard-secondary-title");
const dashboardSecondaryList = document.getElementById("dashboard-secondary-list");
const bridgeStatusPill = document.getElementById("bridge-status-pill");
const sessionTitle = document.getElementById("session-title");
const sessionDescription = document.getElementById("session-description");

const refreshButton = document.getElementById("refresh-status");
const buildLegacyButton = document.getElementById("build-legacy");
const compileJavaButton = document.getElementById("compile-java");
const launchStudentButton = document.getElementById("launch-student");
const pickStudentButton = document.getElementById("pick-student");
const launchTeacherButton = document.getElementById("launch-teacher");
const pickTeacherButton = document.getElementById("pick-teacher");
const openProjectButton = document.getElementById("open-project");
const openDesktopButton = document.getElementById("open-desktop");
const openTargetButton = document.getElementById("open-target");
const openDbConfigButton = document.getElementById("open-db-config");

const dbConfigForm = document.getElementById("db-config-form");
const dbUrlInput = document.getElementById("db-url");
const dbUsernameInput = document.getElementById("db-username");
const dbPasswordInput = document.getElementById("db-password");
const dbDriverInput = document.getElementById("db-driver");
const reloadDbConfigButton = document.getElementById("reload-db-config");

const authForm = document.getElementById("auth-form");
const authRoleInput = document.getElementById("auth-role");
const authNameInput = document.getElementById("auth-name");
const authAccountInput = document.getElementById("auth-account");
const authAccountHelp = document.getElementById("auth-account-help");
const authPasswordInput = document.getElementById("auth-password");
const authLogoutButton = document.getElementById("auth-logout");
const authAccountLabel = document.getElementById("auth-account-label");

let currentSession = null;

const summaryLabels = {
  role: "角色",
  displayName: "姓名",
  studentNumber: "学号",
  teacherNumber: "教师账号",
  publishedPaperCount: "已发布试卷数",
  recordCount: "考试记录数",
  submittedCount: "已交卷次数",
  averageScore: "平均分",
  paperCount: "试卷总数",
  publishedCount: "已发布数量",
  studentCount: "学生人数"
};

const roleLabels = {
  STUDENT: "学生",
  TEACHER: "教师",
  student: "学生端",
  teacher: "教师端"
};

const examStatusLabels = {
  NOT_STARTED: "未开始",
  IN_PROGRESS: "进行中",
  SUBMITTED: "已提交",
  TIMEOUT: "已超时",
  GRADED: "已阅卷"
};

function localizeMessage(text) {
  if (!text) {
    return "";
  }

  const exactMap = {
    "Bridge is ready.": "桥接服务已就绪。",
    "Login succeeded.": "登录成功。",
    "Student overview loaded.": "学生概览加载成功。",
    "Teacher overview loaded.": "教师概览加载成功。",
    "Bridge returned no output.": "桥接服务没有返回内容。",
    "Failed to load overview.": "加载概览失败。",
    "Electron session cleared.": "Electron 会话已清除。"
  };

  if (exactMap[text]) {
    return exactMap[text];
  }

  if (text.startsWith("Bridge returned invalid JSON:")) {
    return text.replace("Bridge returned invalid JSON:", "桥接服务返回了无效 JSON：");
  }

  return text;
}

function localizeRole(role) {
  return roleLabels[role] || role || "未知";
}

function localizeSummaryKey(key) {
  return summaryLabels[key] || key;
}

function localizeExamStatus(status) {
  return examStatusLabels[status] || status || "未知";
}

function formatDisplayTime(value) {
  if (!value) {
    return "未知";
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }

  return parsed.toLocaleString("zh-CN", { hour12: false });
}

function appendLog(text) {
  const timestamp = new Date().toLocaleTimeString();
  activityLog.textContent += `\n[${timestamp}] ${localizeMessage(text)}`;
  activityLog.scrollTop = activityLog.scrollHeight;
}

function formatMultiline(text) {
  if (!text) {
    return "不可用";
  }

  const compact = text.split(/\r?\n/).filter(Boolean).slice(0, 3).join("\n");
  return compact || "不可用";
}

function buildStatusCard(title, available, text) {
  const card = document.createElement("article");
  card.className = "status-card";

  const pill = document.createElement("div");
  pill.className = `status-pill ${available ? "" : "missing"}`.trim();
  pill.textContent = available ? "可用" : "缺失";

  const heading = document.createElement("h3");
  heading.textContent = title;

  const line = document.createElement("div");
  line.className = "status-line";
  line.textContent = text;

  card.appendChild(pill);
  card.appendChild(heading);
  card.appendChild(line);

  return card;
}

function renderToolStatus(tools) {
  toolStatus.innerHTML = "";
  toolStatus.appendChild(buildStatusCard("Node.js", tools.node.available, formatMultiline(tools.node.output)));
  toolStatus.appendChild(buildStatusCard("Java", tools.java.available, formatMultiline(tools.java.output)));
  toolStatus.appendChild(buildStatusCard("Maven", tools.maven.available, formatMultiline(tools.maven.output)));
}

function renderArtifactStatus(artifacts) {
  artifactStatus.innerHTML = "";

  const artifactEntries = [
    { label: "学生端 JAR", artifact: artifacts.studentJar, displayPath: "target/exam-student.jar" },
    { label: "教师端 JAR", artifact: artifacts.teacherJar, displayPath: "target/exam-teacher.jar" },
    { label: "桥接 JAR", artifact: artifacts.bridgeJar, displayPath: "target/exam-electron-bridge.jar" },
    { label: "学生端 EXE", artifact: artifacts.studentExe, displayPath: "target/在线考试系统-学生端.exe" },
    { label: "教师端 EXE", artifact: artifacts.teacherExe, displayPath: "target/在线考试系统-教师端.exe" },
    { label: "编译产物目录", artifact: artifacts.compiledClasses, displayPath: "target/classes" },
    { label: "数据库配置", artifact: artifacts.dbConfig, displayPath: "src/main/resources/db.properties" },
    { label: "target 目录", artifact: artifacts.targetDir, displayPath: "target" }
  ];

  for (const { label, artifact, displayPath } of artifactEntries) {
    const detail = artifact.exists
      ? `${displayPath}\n${artifact.isDirectory ? "目录" : `${artifact.size} 字节`}`
      : displayPath;

    artifactStatus.appendChild(buildStatusCard(label, artifact.exists, detail));
  }
}

function renderRunningApps(apps) {
  if (!apps.length) {
    runningApps.className = "empty-state";
    runningApps.textContent = "当前还没有运行中的旧版程序。";
    return;
  }

  const wrapper = document.createElement("div");
  for (const app of apps) {
    const card = document.createElement("div");
    card.className = "running-app-card";
    card.innerHTML = `
      <strong>${localizeRole(app.role)}</strong><br />
      进程号：${app.pid}<br />
      启动时间：${formatDisplayTime(app.startedAt)}<br />
      运行文件：${app.artifactPath}
    `;
    wrapper.appendChild(card);
  }

  runningApps.className = "";
  runningApps.innerHTML = "";
  runningApps.appendChild(wrapper);
}

function renderDatabaseConfig(config) {
  dbUrlInput.value = config.url || "";
  dbUsernameInput.value = config.username || "";
  dbPasswordInput.value = config.password || "";
  dbDriverInput.value = config.driver || "";
}

function isTeacherRole() {
  return authRoleInput.value === "TEACHER";
}

function updateAccountFieldCopy() {
  if (isTeacherRole()) {
    authAccountLabel.textContent = "旧版账号";
    authAccountInput.placeholder = "教师登录暂不需要填写";
    authAccountInput.disabled = true;
    authAccountInput.value = "";
    authAccountHelp.textContent = "教师当前使用“姓名 + 密码”登录，旧版服务暂不校验教师编号。";
  } else {
    authAccountLabel.textContent = "学号";
    authAccountInput.placeholder = "请输入学号";
    authAccountInput.disabled = false;
    authAccountHelp.textContent = "学生当前使用“姓名 + 学号 + 密码”登录。";
  }
}

function renderBridgeStatus(result) {
  if (result.ok) {
    bridgeStatusPill.className = "status-pill";
    bridgeStatusPill.textContent = "桥接已就绪";
  } else {
    bridgeStatusPill.className = "status-pill missing";
    bridgeStatusPill.textContent = "桥接不可用";
  }
}

function renderSummaryCards(summary) {
  dashboardSummary.innerHTML = "";

  for (const [key, value] of Object.entries(summary)) {
    const title = localizeSummaryKey(key);
    const text = key === "role" ? localizeRole(String(value)) : String(value);
    dashboardSummary.appendChild(buildStatusCard(title, true, text));
  }
}

function renderList(target, items, formatter) {
  if (!items || !items.length) {
    target.className = "empty-state";
    target.textContent = "暂无数据。";
    return;
  }

  const wrapper = document.createElement("div");
  wrapper.className = "list-items";

  for (const item of items) {
    const node = document.createElement("div");
    node.className = "list-item";
    node.innerHTML = formatter(item);
    wrapper.appendChild(node);
  }

  target.className = "";
  target.innerHTML = "";
  target.appendChild(wrapper);
}

function resetDashboard(message = "登录后可加载数据。") {
  dashboardSummary.innerHTML = "";
  dashboardPrimaryTitle.textContent = "主列表";
  dashboardSecondaryTitle.textContent = "副列表";
  dashboardPrimaryList.className = "empty-state";
  dashboardPrimaryList.textContent = localizeMessage(message);
  dashboardSecondaryList.className = "empty-state";
  dashboardSecondaryList.textContent = localizeMessage(message);
}

function renderSession() {
  if (!currentSession) {
    sessionTitle.textContent = "当前没有活跃的 Electron 会话";
    sessionDescription.textContent =
      "先构建桥接产物，然后在这里登录，即可在不打开 Swing 的情况下加载实时概览数据。";
    return;
  }

  const user = currentSession.data.user;
  sessionTitle.textContent = `${user.realName} 已在 Electron 中登录`;
  if (user.role === "TEACHER") {
    sessionDescription.textContent = `当前为${localizeRole(user.role)}会话，数据通过 Java 桥接层加载。旧版账号：${user.studentNumber}。`;
    return;
  }

  sessionDescription.textContent = `当前为${localizeRole(user.role)}会话，数据通过 Java 桥接层加载。学号：${user.studentNumber}。`;
}

function renderOverview(result) {
  if (!result.ok || !result.data) {
    resetDashboard(result.message || "加载概览失败。");
    return;
  }

  const summary = result.data.summary || {};
  renderSummaryCards(summary);

  if (summary.role === "TEACHER") {
    dashboardPrimaryTitle.textContent = "最近试卷";
    dashboardSecondaryTitle.textContent = "最近学生";

    renderList(dashboardPrimaryList, result.data.papers, (paper) => `
      <strong>${paper.paperName}</strong>
      <small>科目：${paper.subject}<br />时长：${paper.duration} 分钟<br />是否发布：${paper.isPublished ? "是" : "否"}</small>
    `);

    renderList(dashboardSecondaryList, result.data.students, (student) => `
      <strong>${student.realName}</strong>
      <small>学号：${student.studentNumber}<br />创建时间：${student.createTime || "未知"}</small>
    `);
  } else {
    dashboardPrimaryTitle.textContent = "已发布试卷";
    dashboardSecondaryTitle.textContent = "最近记录";

    renderList(dashboardPrimaryList, result.data.papers, (paper) => `
      <strong>${paper.paperName}</strong>
      <small>科目：${paper.subject}<br />时长：${paper.duration} 分钟<br />题目数：${paper.questionCount}</small>
    `);

    renderList(dashboardSecondaryList, result.data.records, (record) => `
      <strong>${record.paperName || "未知试卷"}</strong>
      <small>状态：${localizeExamStatus(record.status)}<br />分数：${record.score ?? "暂无"}<br />提交时间：${record.submitTime || "未交卷"}</small>
    `);
  }
}

async function loadOverview() {
  if (!currentSession) {
    resetDashboard();
    return;
  }

  const result = await window.desktopApi.bridgeOverview({
    role: currentSession.data.user.role,
    userId: currentSession.data.user.userId
  });

  appendLog(result.message);
  renderOverview(result);
}

async function refreshStatus() {
  const [status, config, bridgeHealth] = await Promise.all([
    window.desktopApi.getStatus(),
    window.desktopApi.getDbConfig(),
    window.desktopApi.bridgeHealth()
  ]);

  renderToolStatus(status.tools);
  renderArtifactStatus(status.artifacts);
  renderRunningApps(status.runningLegacyApps);
  renderDatabaseConfig(config);
  renderBridgeStatus(bridgeHealth);
  renderSession();

  const buildBusy = status.compileTaskRunning || status.packageTaskRunning;
  compileJavaButton.disabled = buildBusy;
  buildLegacyButton.disabled = buildBusy;
  authForm.querySelector("button[type='submit']").disabled = buildBusy;
  authLogoutButton.disabled = !currentSession;
}

async function pickAndLaunch(role) {
  const selection = await window.desktopApi.pickLegacyArtifact();
  if (selection.canceled) {
    appendLog(`已取消：未选择${localizeRole(role)}的 JAR 文件。`);
    return;
  }

  const result = await window.desktopApi.launchLegacyApp({
    role,
    artifactPath: selection.path
  });

  appendLog(result.message);
  await refreshStatus();
}

refreshButton.addEventListener("click", refreshStatus);

buildLegacyButton.addEventListener("click", async () => {
  const result = await window.desktopApi.buildLegacyArtifacts();
  appendLog(result.message);
  await refreshStatus();
});

compileJavaButton.addEventListener("click", async () => {
  const result = await window.desktopApi.compileJava();
  appendLog(result.message);
  await refreshStatus();
});

launchStudentButton.addEventListener("click", async () => {
  const result = await window.desktopApi.launchLegacyApp({ role: "student" });
  appendLog(result.message);
  await refreshStatus();
});

pickStudentButton.addEventListener("click", async () => {
  await pickAndLaunch("student");
});

launchTeacherButton.addEventListener("click", async () => {
  const result = await window.desktopApi.launchLegacyApp({ role: "teacher" });
  appendLog(result.message);
  await refreshStatus();
});

pickTeacherButton.addEventListener("click", async () => {
  await pickAndLaunch("teacher");
});

openProjectButton.addEventListener("click", async () => {
  const result = await window.desktopApi.openTarget("projectRoot");
  appendLog(`已打开：${result.path || result.message}`);
});

openDesktopButton.addEventListener("click", async () => {
  const result = await window.desktopApi.openTarget("desktopRoot");
  appendLog(`已打开：${result.path || result.message}`);
});

openTargetButton.addEventListener("click", async () => {
  const result = await window.desktopApi.openTarget("targetDir");
  appendLog(`已打开：${result.path || result.message}`);
});

openDbConfigButton.addEventListener("click", async () => {
  const result = await window.desktopApi.openTarget("dbConfig");
  appendLog(`已打开：${result.path || result.message}`);
});

authRoleInput.addEventListener("change", updateAccountFieldCopy);

authForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const role = authRoleInput.value;

  const result = await window.desktopApi.bridgeLogin({
    role,
    realName: authNameInput.value.trim(),
    account: isTeacherRole() ? "" : authAccountInput.value.trim(),
    password: authPasswordInput.value
  });

  appendLog(result.message);
  if (!result.ok) {
    currentSession = null;
    renderSession();
    resetDashboard(result.message);
    await refreshStatus();
    return;
  }

  currentSession = result;
  renderSession();
  authPasswordInput.value = "";
  await loadOverview();
  await refreshStatus();
});

authLogoutButton.addEventListener("click", async () => {
  currentSession = null;
  renderSession();
  resetDashboard();
  appendLog("Electron 会话已清除。");
  await refreshStatus();
});

reloadDbConfigButton.addEventListener("click", refreshStatus);

dbConfigForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const result = await window.desktopApi.saveDbConfig({
    url: dbUrlInput.value,
    username: dbUsernameInput.value,
    password: dbPasswordInput.value,
    driver: dbDriverInput.value
  });

  appendLog(result.message);
  renderDatabaseConfig(result.config);
});

window.desktopApi.onDesktopEvent(async (payload) => {
  if (payload.text) {
    appendLog(payload.text);
  }

  if (
    payload.type === "db-config-saved" ||
    payload.type === "compile-started" ||
    payload.type === "compile-finished" ||
    payload.type === "package-started" ||
    payload.type === "package-finished" ||
    payload.type === "legacy-started" ||
    payload.type === "legacy-exited" ||
    payload.type === "legacy-error"
  ) {
    await refreshStatus();
  }
});

updateAccountFieldCopy();
resetDashboard();
refreshStatus();
