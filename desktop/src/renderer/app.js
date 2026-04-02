const toolStatus = document.getElementById("tool-status");
const artifactStatus = document.getElementById("artifact-status");
const runningApps = document.getElementById("running-apps");
const activityLog = document.getElementById("activity-log");
const dashboardSummary = document.getElementById("dashboard-summary");
const dashboardPrimaryTitle = document.getElementById("dashboard-primary-title");
const dashboardPrimaryList = document.getElementById("dashboard-primary-list");
const dashboardSecondaryTitle = document.getElementById("dashboard-secondary-title");
const dashboardSecondaryList = document.getElementById("dashboard-secondary-list");
const workspaceHubSummary = document.getElementById("workspace-hub-summary");
const workspaceHubActions = document.getElementById("workspace-hub-actions");
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

const reloadStudentPapersButton = document.getElementById("reload-student-papers");
const studentExamSummary = document.getElementById("student-exam-summary");
const studentPaperList = document.getElementById("student-paper-list");
const studentExamWorkspace = document.getElementById("student-exam-workspace");
const reloadTeacherPapersButton = document.getElementById("reload-teacher-papers");
const teacherPaperSummary = document.getElementById("teacher-paper-summary");
const teacherPaperList = document.getElementById("teacher-paper-list");
const teacherPaperWorkspace = document.getElementById("teacher-paper-workspace");
const teacherImportFileStatus = document.getElementById("teacher-import-file-status");
const teacherImportResult = document.getElementById("teacher-import-result");
const teacherImportForm = document.getElementById("teacher-import-form");
const pickTeacherImportFileButton = document.getElementById("pick-teacher-import-file");
const clearTeacherImportFileButton = document.getElementById("clear-teacher-import-file");
const teacherImportPaperNameInput = document.getElementById("teacher-import-paper-name");
const teacherImportSubjectInput = document.getElementById("teacher-import-subject");
const teacherImportPassScoreInput = document.getElementById("teacher-import-pass-score");
const teacherImportDurationInput = document.getElementById("teacher-import-duration");
const teacherImportDescriptionInput = document.getElementById("teacher-import-description");
const createTeacherStudentButton = document.getElementById("create-teacher-student");
const reloadTeacherStudentsButton = document.getElementById("reload-teacher-students");
const teacherStudentSummary = document.getElementById("teacher-student-summary");
const teacherStudentList = document.getElementById("teacher-student-list");
const teacherStudentWorkspace = document.getElementById("teacher-student-workspace");
const reloadStudentRecordsButton = document.getElementById("reload-student-records");
const studentRecordSummary = document.getElementById("student-record-summary");
const studentRecordList = document.getElementById("student-record-list");
const studentRecordWorkspace = document.getElementById("student-record-workspace");

const authSection = authForm.closest("section");
const actionPanelSection = buildLegacyButton.closest("section");
const dbConfigSection = dbConfigForm.closest("section");
const dashboardSection = dashboardSummary.closest("section");
const workspaceHubSection = workspaceHubSummary.closest("section");
const teacherPaperSection = teacherPaperSummary.closest("section");
const teacherImportSection = teacherImportFileStatus.closest("section");
const teacherStudentSection = teacherStudentSummary.closest("section");
const studentExamSection = studentExamSummary.closest("section");
const studentRecordSection = studentRecordSummary.closest("section");

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
  studentCount: "学生人数",
  completedCount: "已完成考试",
  inProgressCount: "进行中记录",
  unpublishedCount: "未发布数量",
  correctCount: "答对题数",
  wrongCount: "答错题数",
  questionCount: "题目数量",
  answeredCount: "已作答题数"
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
  TIMEOUT: "已超时"
};

const questionTypeLabels = {
  SINGLE: "单选题",
  MULTIPLE: "多选题",
  JUDGE: "判断题",
  BLANK: "填空题",
  APPLICATION: "应用题",
  ALGORITHM: "算法设计题",
  SHORT_ANSWER: "简答题",
  COMPREHENSIVE: "综合题",
  ESSAY: "论述题",
  MATERIAL_ANALYSIS: "材料分析题",
  CLOZE: "选词填空",
  READING_ANALYSIS: "阅读分析",
  ENGLISH_TO_CHINESE: "英译中",
  CHINESE_TO_ENGLISH: "中译英",
  WRITING: "写作题"
};

const workspaceTargetLabels = {
  login: "登录工作台",
  operations: "操作面板",
  "db-config": "数据库配置",
  dashboard: "桥接概览",
  "workspace-hub": "角色首页",
  "teacher-paper": "试卷管理",
  "teacher-import": "导题建卷",
  "teacher-student": "学生管理",
  "student-exam": "考试中心",
  "student-record": "成绩中心"
};

const teacherSubjectOptions = ["Java", "Vue", "Data Structures", "Marxism", "Computer Network", "Operating System", "Database", "English", "Branding and Marketing", "Other"]; /*
  "Java",
  "Vue",
  "鏁版嵁缁撴瀯",
  "椹厠鎬濅富涔?,
  "璁＄畻鏈虹綉缁?,
  "鎿嶄綔绯荤粺",
  "鏁版嵁搴?,
  "鑻辫",
  "鍝佺墝涓庤惀閿€",
  "鍏朵粬"
];

*/
const state = {
  currentSession: null,
  studentExam: {
    loading: false,
    loadingPaperId: null,
    summary: null,
    papers: [],
    selectedPaperId: null,
    paperDetails: {},
    activeExam: null
  },
  teacherPapers: {
    loading: false,
    actionPaperId: null,
    summary: null,
    papers: [],
    selectedPaperId: null,
    paperDetails: {}
  },
  teacherImport: {
    filePath: "",
    fileName: "",
    importing: false,
    lastResult: null
  },
  teacherStudents: {
    loading: false,
    actionStudentId: null,
    loadingRecordId: null,
    createMode: false,
    summary: null,
    students: [],
    selectedStudentId: null,
    selectedRecordId: null,
    studentDetails: {},
    recordDetails: {}
  },
  workspaceNav: {
    activeTarget: "login"
  },
  studentRecords: {
    loading: false,
    loadingRecordId: null,
    summary: null,
    records: [],
    selectedRecordId: null,
    recordDetails: {}
  }
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
    "Student paper center loaded.": "学生试卷中心加载成功。",
    "Student record center loaded.": "学生成绩中心加载成功。",
    "Teacher paper center loaded.": "教师试卷中心加载成功。",
    "Teacher student center loaded.": "教师学生中心加载成功。",
    "Paper detail loaded.": "试卷详情加载成功。",
    "Exam record detail loaded.": "考试详情加载成功。",
    "Teacher student detail loaded.": "教师学生详情加载成功。",
    "Paper published successfully.": "试卷发布成功。",
    "Paper unpublished successfully.": "试卷已取消发布。",
    "Paper deleted successfully.": "试卷删除成功。",
    "Student created successfully.": "学生创建成功。",
    "Student updated successfully.": "学生信息更新成功。",
    "Student deleted successfully.": "学生删除成功。",
    "Exam started successfully.": "考试已开始。",
    "Exam submitted successfully.": "试卷提交成功。",
    "Bridge returned no output.": "桥接服务没有返回内容。",
    "Failed to load overview.": "加载概览失败。",
    "Electron session cleared.": "Electron 会话已清除。",
    "Selected paper is not published.": "所选试卷尚未发布。",
    "Requested user is not a student.": "当前用户不是学生。",
    "Requested user is not a teacher.": "当前用户不是教师。",
    "Requested exam record does not belong to current student.": "所选考试记录不属于当前学生。",
    "Requested exam record does not exist.": "所选考试记录不存在。",
    "Answer arguments must be provided as <questionId> <answer> pairs.": "答题参数格式不正确。"
  };

  exactMap["Paper updated successfully."] = "璇曞嵎淇℃伅鏇存柊鎴愬姛銆?";
  exactMap["Paper imported successfully."] = "瀵煎叆骞剁敓鎴愯瘯鍗锋垚鍔熴€?";
  exactMap["Selected import file does not exist."] = "鎵€閫夌殑瀵煎叆鏂囦欢涓嶅瓨鍦ㄣ€?";
  exactMap["No valid questions could be linked to the paper."] = "娌℃湁鍙敤浜庣敓鎴愯瘯鍗风殑鏈夋晥棰樼洰銆?";

  exactMap["Teacher record detail loaded."] = "教师查看考试详情成功。";
  exactMap["Requested exam record does not belong to selected student."] = "所选考试记录不属于当前查看的学生。";

  if (exactMap[text]) {
    return exactMap[text];
  }
  if (text.startsWith("Bridge returned invalid JSON:")) {
    return text.replace("Bridge returned invalid JSON:", "桥接服务返回了无效 JSON：");
  }
  if (text.startsWith("Unknown bridge command:")) {
    return text.replace("Unknown bridge command:", "未知的桥接命令：");
  }
  return text;
}

function localizeRole(role) {
  return roleLabels[role] || role || "未知";
}

function isTeacherSession() {
  return Boolean(state.currentSession && state.currentSession.data.user.role === "TEACHER");
}

function isStudentSession() {
  return Boolean(state.currentSession && state.currentSession.data.user.role === "STUDENT");
}

function setSectionVisibility(sections, hidden) {
  for (const section of sections) {
    if (section) {
      section.hidden = hidden;
    }
  }
}

function placeWorkspaceSections() {
  const layout = teacherPaperSection?.parentElement;
  if (!layout || !teacherPaperSection || !teacherImportSection || !teacherStudentSection) {
    return;
  }

  layout.insertBefore(teacherImportSection, teacherPaperSection.nextSibling);
  layout.insertBefore(teacherStudentSection, teacherImportSection.nextSibling);
}

function syncWorkspaceSections() {
  placeWorkspaceSections();

  const teacherSections = [teacherPaperSection, teacherImportSection, teacherStudentSection];
  const studentSections = [studentExamSection, studentRecordSection];

  if (isTeacherSession()) {
    setSectionVisibility(teacherSections, false);
    setSectionVisibility(studentSections, true);
    return;
  }

  if (isStudentSession()) {
    setSectionVisibility(teacherSections, true);
    setSectionVisibility(studentSections, false);
    return;
  }

  setSectionVisibility(teacherSections, false);
  setSectionVisibility(studentSections, false);
}

function getWorkspaceTargetLabel(target) {
  return workspaceTargetLabels[target] || "未选择";
}

function normalizeWorkspaceTarget(target) {
  const nextTarget = target || state.workspaceNav.activeTarget || "login";

  if (!state.currentSession) {
    if (["teacher-paper", "teacher-import", "teacher-student", "student-exam", "student-record"].includes(nextTarget)) {
      return "login";
    }
    return nextTarget;
  }

  if (isTeacherSession()) {
    if (["student-exam", "student-record"].includes(nextTarget)) {
      return "workspace-hub";
    }
    return nextTarget;
  }

  if (isStudentSession()) {
    if (["teacher-paper", "teacher-import", "teacher-student"].includes(nextTarget)) {
      return "workspace-hub";
    }
    return nextTarget;
  }

  return nextTarget;
}

function setActiveWorkspaceTarget(target) {
  state.workspaceNav.activeTarget = normalizeWorkspaceTarget(target);
}

function navigateToWorkspaceTarget(target, { scroll = true, behavior = "smooth" } = {}) {
  setActiveWorkspaceTarget(target);
  renderWorkspaceHub();
  if (scroll) {
    scrollToWorkspaceTarget(state.workspaceNav.activeTarget, { behavior });
  }
}

function localizeExamStatus(status) {
  return examStatusLabels[status] || status || "未知";
}

function localizeQuestionType(type) {
  return questionTypeLabels[type] || type || "未知题型";
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function formatDisplayTime(value) {
  if (!value) {
    return "未知";
  }
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? value : parsed.toLocaleString("zh-CN", { hour12: false });
}

function formatSeconds(seconds) {
  const total = Math.max(0, Number(seconds) || 0);
  const minutes = Math.floor(total / 60);
  const remainSeconds = total % 60;
  return `${String(minutes).padStart(2, "0")}:${String(remainSeconds).padStart(2, "0")}`;
}

function formatDurationCopy(seconds) {
  const total = Math.max(0, Number(seconds) || 0);
  const minutes = Math.floor(total / 60);
  const remainSeconds = total % 60;
  return `${minutes} 分 ${remainSeconds} 秒`;
}

function appendLog(text) {
  const timestamp = new Date().toLocaleTimeString("zh-CN", { hour12: false });
  activityLog.textContent += `\n[${timestamp}] ${localizeMessage(text)}`;
  activityLog.scrollTop = activityLog.scrollHeight;
}

function buildStatusCard(title, available, text) {
  const card = document.createElement("article");
  card.className = "status-card";
  card.innerHTML = `
    <div class="status-pill ${available ? "" : "missing"}">${available ? "可用" : "缺失"}</div>
    <h3>${escapeHtml(title)}</h3>
    <div class="status-line">${escapeHtml(text)}</div>
  `;
  return card;
}

function renderToolStatus(tools) {
  toolStatus.innerHTML = "";
  toolStatus.appendChild(buildStatusCard("Node.js", tools.node.available, tools.node.output || "不可用"));
  toolStatus.appendChild(buildStatusCard("Java", tools.java.available, tools.java.output || "不可用"));
  toolStatus.appendChild(buildStatusCard("Maven", tools.maven.available, tools.maven.output || "不可用"));
}

function renderArtifactStatus(artifacts) {
  artifactStatus.innerHTML = "";
  const entries = [
    ["学生端 JAR", artifacts.studentJar, "target/exam-student.jar"],
    ["教师端 JAR", artifacts.teacherJar, "target/exam-teacher.jar"],
    ["桥接 JAR", artifacts.bridgeJar, "target/exam-electron-bridge.jar"],
    ["学生端 EXE", artifacts.studentExe, "target/在线考试系统-学生端.exe"],
    ["教师端 EXE", artifacts.teacherExe, "target/在线考试系统-教师端.exe"],
    ["编译产物目录", artifacts.compiledClasses, "target/classes"],
    ["数据库配置", artifacts.dbConfig, "src/main/resources/db.properties"],
    ["target 目录", artifacts.targetDir, "target"]
  ];

  for (const [label, artifact, displayPath] of entries) {
    const detail = artifact.exists ? `${displayPath}\n${artifact.isDirectory ? "目录" : `${artifact.size} 字节`}` : displayPath;
    artifactStatus.appendChild(buildStatusCard(label, artifact.exists, detail));
  }
}

function renderRunningApps(apps) {
  if (!apps.length) {
    runningApps.className = "empty-state";
    runningApps.textContent = "当前还没有运行中的旧版程序。";
    return;
  }

  runningApps.className = "";
  runningApps.innerHTML = apps.map((app) => `
    <div class="running-app-card">
      <strong>${escapeHtml(localizeRole(app.role))}</strong><br />
      进程号：${escapeHtml(app.pid)}<br />
      启动时间：${escapeHtml(formatDisplayTime(app.startedAt))}<br />
      运行文件：${escapeHtml(app.artifactPath)}
    </div>
  `).join("");
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
    return;
  }

  authAccountLabel.textContent = "学号";
  authAccountInput.placeholder = "请输入学号";
  authAccountInput.disabled = false;
  authAccountHelp.textContent = "学生当前使用“姓名 + 学号 + 密码”登录。";
}

function renderBridgeStatus(result) {
  bridgeStatusPill.className = `status-pill ${result.ok ? "" : "missing"}`.trim();
  bridgeStatusPill.textContent = result.ok ? "桥接已就绪" : "桥接不可用";
}

function renderSummaryCards(summary, target) {
  target.innerHTML = "";
  for (const [key, value] of Object.entries(summary)) {
    const title = summaryLabels[key] || key;
    const content = key === "role" ? localizeRole(String(value)) : String(value);
    target.appendChild(buildStatusCard(title, true, content));
  }
}

function renderList(target, emptyText, items, formatter) {
  if (!items || !items.length) {
    target.className = "empty-state";
    target.textContent = emptyText;
    return;
  }

  target.className = "list-items";
  target.innerHTML = items.map(formatter).join("");
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
  if (!state.currentSession) {
    sessionTitle.textContent = "当前没有活跃的 Electron 会话";
    sessionDescription.textContent = "先构建桥接产物，然后在这里登录，即可在不打开 Swing 的情况下加载实时概览数据。";
    return;
  }

  const user = state.currentSession.data.user;
  sessionTitle.textContent = `${user.realName} 已在 Electron 中登录`;
  sessionDescription.textContent = user.role === "TEACHER"
    ? `当前为${localizeRole(user.role)}会话，数据通过 Java 桥接层加载。旧版账号：${user.studentNumber}。`
    : `当前为${localizeRole(user.role)}会话，数据通过 Java 桥接层加载。学号：${user.studentNumber}。`;
}

function renderOverview(result) {
  if (!result.ok || !result.data) {
    resetDashboard(result.message || "加载概览失败。");
    return;
  }

  renderSummaryCards(result.data.summary || {}, dashboardSummary);

  if (result.data.summary?.role === "TEACHER") {
    dashboardPrimaryTitle.textContent = "最近试卷";
    dashboardSecondaryTitle.textContent = "最近学生";
    renderList(
      dashboardPrimaryList,
      "暂无试卷数据。",
      result.data.papers,
      (paper) => `<div class="list-item"><strong>${escapeHtml(paper.paperName)}</strong><small>科目：${escapeHtml(paper.subject)}<br />时长：${escapeHtml(paper.duration)} 分钟<br />是否发布：${paper.isPublished ? "是" : "否"}</small></div>`
    );
    renderList(
      dashboardSecondaryList,
      "暂无学生数据。",
      result.data.students,
      (student) => `<div class="list-item"><strong>${escapeHtml(student.realName)}</strong><small>学号：${escapeHtml(student.studentNumber)}<br />创建时间：${escapeHtml(student.createTime || "未知")}</small></div>`
    );
    return;
  }

  dashboardPrimaryTitle.textContent = "已发布试卷";
  dashboardSecondaryTitle.textContent = "最近记录";
  renderList(
    dashboardPrimaryList,
    "暂无试卷数据。",
    result.data.papers,
    (paper) => `<div class="list-item"><strong>${escapeHtml(paper.paperName)}</strong><small>科目：${escapeHtml(paper.subject)}<br />时长：${escapeHtml(paper.duration)} 分钟<br />题目数：${escapeHtml(paper.questionCount)}</small></div>`
  );
  renderList(
    dashboardSecondaryList,
    "暂无考试记录。",
    result.data.records,
    (record) => `<div class="list-item"><strong>${escapeHtml(record.paperName || "未知试卷")}</strong><small>状态：${escapeHtml(localizeExamStatus(record.status))}<br />分数：${escapeHtml(record.score ?? "暂无")}<br />提交时间：${escapeHtml(record.submitTime || "未交卷")}</small></div>`
  );
}

async function loadOverview() {
  if (!state.currentSession) {
    resetDashboard();
    return;
  }

  const result = await window.desktopApi.bridgeOverview({
    role: state.currentSession.data.user.role,
    userId: state.currentSession.data.user.userId
  });
  appendLog(result.message);
  renderOverview(result);
}

function getWorkspaceTargetSection(target) {
  switch (target) {
    case "login":
      return authSection;
    case "operations":
      return actionPanelSection;
    case "db-config":
      return dbConfigSection;
    case "dashboard":
      return dashboardSection;
    case "workspace-hub":
      return workspaceHubSection;
    case "teacher-paper":
      return teacherPaperSection;
    case "teacher-import":
      return teacherImportSection;
    case "teacher-student":
      return teacherStudentSection;
    case "student-exam":
      return studentExamSection;
    case "student-record":
      return studentRecordSection;
    default:
      return null;
  }
}

function scrollToWorkspaceTarget(target, { behavior = "smooth", block = "start" } = {}) {
  const section = getWorkspaceTargetSection(target);
  if (!section) {
    return;
  }

  section.scrollIntoView({
    behavior,
    block
  });
}

function buildWorkspaceActionCard({ title, description, meta, buttonLabel, target, accent, active = false }) {
  const metaCopy = meta ? `<div class="workspace-hub-meta">${escapeHtml(meta).replace(/\r?\n/g, "<br />")}</div>` : "";
  const actionCopy = buttonLabel && target
    ? `<div class="button-row"><button class="button ${active ? "button-secondary" : "button-primary"}" type="button" data-workspace-target="${escapeHtml(target)}">${escapeHtml(active ? "查看当前工作区" : buttonLabel)}</button></div>`
    : "";
  const statusCopy = active ? `<div class="workspace-hub-pill">当前工作区</div>` : "";

  return `
    <article class="workspace-hub-card${active ? " active" : ""}" style="--hub-accent:${accent}">
      ${statusCopy}
      <h3>${escapeHtml(title)}</h3>
      <p>${escapeHtml(description)}</p>
      ${metaCopy}
      ${actionCopy}
    </article>
  `;
}

function renderWorkspaceHub() {
  setActiveWorkspaceTarget();
  workspaceHubSummary.innerHTML = "";
  const activeTarget = state.workspaceNav.activeTarget;

  if (!state.currentSession) {
    workspaceHubSummary.appendChild(buildStatusCard("当前状态", true, "等待登录"));
    workspaceHubSummary.appendChild(buildStatusCard("当前工作区", true, getWorkspaceTargetLabel(activeTarget)));
    workspaceHubSummary.appendChild(buildStatusCard("桌面架构", true, "Electron + Java Bridge"));
    workspaceHubSummary.appendChild(buildStatusCard("教师端进度", true, "试卷 / 导题 / 学生管理已迁入"));

    workspaceHubActions.className = "workspace-hub-grid";
    workspaceHubActions.innerHTML = [
      buildWorkspaceActionCard({
        title: "登录工作台",
        description: "先建立 Electron 会话，再进入教师端或学生端业务工作区。",
        meta: "支持教师与学生双角色登录",
        buttonLabel: "前往登录",
        target: "login",
        accent: "#0f766e",
        active: activeTarget === "login"
      }),
      buildWorkspaceActionCard({
        title: "准备运行环境",
        description: "如果还没有构建 legacy 产物，可以先从这里跳到构建与启动面板。",
        meta: "可执行打包、编译和旧版 JAR 启动",
        buttonLabel: "查看操作面板",
        target: "operations",
        accent: "#d97706",
        active: activeTarget === "operations"
      }),
      buildWorkspaceActionCard({
        title: "检查数据库配置",
        description: "桌面端已经接管数据库配置，启动前可以先检查连接参数。",
        meta: "当前使用 Electron 直接保存 db.properties",
        buttonLabel: "前往数据库配置",
        target: "db-config",
        accent: "#7c3aed",
        active: activeTarget === "db-config"
      })
    ].join("");
    return;
  }

  const user = state.currentSession.data.user;
  workspaceHubSummary.appendChild(buildStatusCard("当前角色", true, localizeRole(user.role)));
  workspaceHubSummary.appendChild(buildStatusCard("当前用户", true, user.realName || "未命名"));
  workspaceHubSummary.appendChild(buildStatusCard("当前工作区", true, getWorkspaceTargetLabel(activeTarget)));

  if (user.role === "TEACHER") {
    const paperSummary = state.teacherPapers.summary || {};
    const studentSummary = state.teacherStudents.summary || {};
    const latestImport = state.teacherImport.lastResult?.summary || null;

    workspaceHubSummary.appendChild(buildStatusCard("试卷总数", true, String(paperSummary.paperCount ?? state.teacherPapers.papers.length ?? 0)));
    workspaceHubSummary.appendChild(buildStatusCard("已发布试卷", true, String(paperSummary.publishedCount ?? 0)));
    workspaceHubSummary.appendChild(buildStatusCard("学生人数", true, String(studentSummary.studentCount ?? state.teacherStudents.students.length ?? 0)));

    workspaceHubActions.className = "workspace-hub-grid";
    workspaceHubActions.innerHTML = [
      buildWorkspaceActionCard({
        title: "试卷管理",
        description: "继续维护试卷信息，查看详情、保存修改、发布或删除试卷。",
        meta: `当前试卷 ${paperSummary.paperCount ?? state.teacherPapers.papers.length ?? 0} 份`,
        buttonLabel: "进入试卷管理",
        target: "teacher-paper",
        accent: "#16a34a",
        active: activeTarget === "teacher-paper"
      }),
      buildWorkspaceActionCard({
        title: "导题建卷",
        description: "直接从 txt 文件导入题目，并在 Electron 中自动生成新试卷。",
        meta: latestImport
          ? `最近导入：${latestImport.fileName || "未命名文件"}\n关联题目：${latestImport.linkedQuestionCount ?? 0} 道`
          : "当前还没有导题结果，可继续在这里完成建卷",
        buttonLabel: "进入导题中心",
        target: "teacher-import",
        accent: "#9333ea",
        active: activeTarget === "teacher-import"
      }),
      buildWorkspaceActionCard({
        title: "学生管理",
        description: "查看学生档案、考试记录，并继续在 Electron 中维护学生信息。",
        meta: `当前学生 ${studentSummary.studentCount ?? state.teacherStudents.students.length ?? 0} 人`,
        buttonLabel: "进入学生管理",
        target: "teacher-student",
        accent: "#dc2626",
        active: activeTarget === "teacher-student"
      }),
      buildWorkspaceActionCard({
        title: "桥接概览",
        description: "查看当前教师会话的桥接概览数据，确认迁移后的整体状态。",
        meta: "这里会展示最近试卷和最近学生摘要",
        buttonLabel: "查看概览",
        target: "dashboard",
        accent: "#0f766e",
        active: activeTarget === "dashboard"
      })
    ].join("");
    return;
  }

  const examSummary = state.studentExam.summary || {};
  const recordSummary = state.studentRecords.summary || {};
  const activeExam = state.studentExam.activeExam;

  workspaceHubSummary.appendChild(buildStatusCard("学号", true, user.studentNumber || "未设置"));
  workspaceHubSummary.appendChild(buildStatusCard("可考试卷", true, String(examSummary.paperCount ?? state.studentExam.papers.length ?? 0)));
  workspaceHubSummary.appendChild(buildStatusCard("历史记录", true, String(recordSummary.recordCount ?? state.studentRecords.records.length ?? 0)));

  workspaceHubActions.className = "workspace-hub-grid";
  workspaceHubActions.innerHTML = [
    buildWorkspaceActionCard({
      title: "考试中心",
      description: "查看可考试卷、进入考试并在 Electron 中完成答题和交卷。",
      meta: activeExam
        ? `当前正在进行：${activeExam.paperName || "未命名试卷"}`
        : `当前可考试卷 ${examSummary.paperCount ?? state.studentExam.papers.length ?? 0} 份`,
      buttonLabel: "进入考试中心",
      target: "student-exam",
      accent: "#0f766e",
      active: activeTarget === "student-exam"
    }),
    buildWorkspaceActionCard({
      title: "成绩中心",
      description: "查看历史成绩、逐题回顾与判分结果。",
      meta: `当前成绩记录 ${recordSummary.recordCount ?? state.studentRecords.records.length ?? 0} 条`,
      buttonLabel: "进入成绩中心",
      target: "student-record",
      accent: "#d97706",
      active: activeTarget === "student-record"
    }),
    buildWorkspaceActionCard({
      title: "桥接概览",
      description: "查看当前学生会话的桥接概览数据，确认试卷与成绩摘要。",
      meta: "这里会展示已发布试卷和最近记录摘要",
      buttonLabel: "查看概览",
      target: "dashboard",
      accent: "#2563eb",
      active: activeTarget === "dashboard"
    })
  ].join("");
}

/* function buildTeacherSubjectOptions(selectedSubject = "") {
  return teacherSubjectOptions.map((subject) => {
    const selected = subject === selectedSubject ? " selected" : "";
    return `<option value="${escapeHtml(subject)}"${selected}>${escapeHtml(subject)}</option>`;
  }).join("");
}

function resetTeacherImportForm() {
  teacherImportForm.reset();
  teacherImportSubjectInput.value = teacherSubjectOptions[0];
  teacherImportPassScoreInput.value = "60";
  teacherImportDurationInput.value = "90";
}

function resetTeacherImportState({ keepFile = false } = {}) {
  state.teacherImport.importing = false;
  state.teacherImport.lastResult = null;
  if (!keepFile) {
    state.teacherImport.filePath = "";
    state.teacherImport.fileName = "";
  }
}

function renderTeacherImportCenter() {
  const enabled = Boolean(state.currentSession && state.currentSession.data.user.role === "TEACHER");
  const busy = state.teacherImport.importing;

  if (!enabled) {
    teacherImportFileStatus.className = "empty-state";
    teacherImportFileStatus.textContent = "登录教师账号后可选择题目文件并导入生成试卷。";
    teacherImportResult.className = "empty-state";
    teacherImportResult.textContent = "当前还没有导入结果。";
  } else if (!state.teacherImport.filePath) {
    teacherImportFileStatus.className = "empty-state";
    teacherImportFileStatus.textContent = "还没有选择题目文件，请先选择 UTF-8 编码的 txt 文件。";
  } else {
    teacherImportFileStatus.className = "detail-copy";
    teacherImportFileStatus.innerHTML = `
      <strong>${escapeHtml(state.teacherImport.fileName || "已选择文件")}</strong><br />
      ${escapeHtml(state.teacherImport.filePath)}
    `;
  }

  if (state.teacherImport.lastResult?.summary) {
    const summary = state.teacherImport.lastResult.summary;
    teacherImportResult.className = "detail-copy";
    teacherImportResult.innerHTML = `
      <strong>最近一次导入结果</strong><br />
      来源文件：${escapeHtml(summary.fileName || "未知文件")}<br />
      原始题目数：${escapeHtml(summary.sourceQuestionCount ?? 0)}<br />
      关联入卷题目数：${escapeHtml(summary.linkedQuestionCount ?? 0)}<br />
      新增题目数：${escapeHtml(summary.createdQuestionCount ?? 0)}<br />
      复用题目数：${escapeHtml(summary.reusedQuestionCount ?? 0)}<br />
      文件内重复数：${escapeHtml(summary.duplicateQuestionCount ?? 0)}
    `;
  } else if (enabled) {
    teacherImportResult.className = "empty-state";
    teacherImportResult.textContent = busy ? "正在导入并生成试卷，请稍候。" : "选择文件并提交后，这里会显示最近一次导入结果。";
  }

  pickTeacherImportFileButton.disabled = !enabled || busy;
  clearTeacherImportFileButton.disabled = !enabled || busy || !state.teacherImport.filePath;
  teacherImportPaperNameInput.disabled = !enabled || busy;
  teacherImportSubjectInput.disabled = !enabled || busy;
  teacherImportPassScoreInput.disabled = !enabled || busy;
  teacherImportDurationInput.disabled = !enabled || busy;
  teacherImportDescriptionInput.disabled = !enabled || busy;
  teacherImportForm.querySelector("button[type='submit']").disabled = !enabled || busy || !state.teacherImport.filePath;
  renderWorkspaceHub();
}

*/
function buildTeacherSubjectOptions(selectedSubject = "") {
  const options = [...teacherSubjectOptions];
  if (selectedSubject && !options.includes(selectedSubject)) {
    options.unshift(selectedSubject);
  }

  return options.map((subject) => {
    const selected = subject === selectedSubject ? " selected" : "";
    return `<option value="${escapeHtml(subject)}"${selected}>${escapeHtml(subject)}</option>`;
  }).join("");
}

function resetTeacherImportForm() {
  teacherImportForm.reset();
  teacherImportSubjectInput.value = teacherSubjectOptions[0];
  teacherImportPassScoreInput.value = "60";
  teacherImportDurationInput.value = "90";
}

function resetTeacherImportState({ keepFile = false } = {}) {
  state.teacherImport.importing = false;
  state.teacherImport.lastResult = null;
  if (!keepFile) {
    state.teacherImport.filePath = "";
    state.teacherImport.fileName = "";
  }
}

function renderTeacherImportCenter() {
  const enabled = Boolean(state.currentSession && state.currentSession.data.user.role === "TEACHER");
  const busy = state.teacherImport.importing;

  if (!enabled) {
    teacherImportFileStatus.className = "empty-state";
    teacherImportFileStatus.textContent = "登录教师账号后可选择题目文件并导入生成试卷。";
    teacherImportResult.className = "empty-state";
    teacherImportResult.textContent = "当前还没有导入结果。";
  } else if (!state.teacherImport.filePath) {
    teacherImportFileStatus.className = "empty-state";
    teacherImportFileStatus.textContent = "还没有选择题目文件，请先选择 UTF-8 编码的 txt 文件。";
  } else {
    teacherImportFileStatus.className = "detail-copy";
    teacherImportFileStatus.innerHTML = `
      <strong>${escapeHtml(state.teacherImport.fileName || "已选择文件")}</strong><br />
      ${escapeHtml(state.teacherImport.filePath)}
    `;
  }

  if (state.teacherImport.lastResult?.summary) {
    const summary = state.teacherImport.lastResult.summary;
    teacherImportResult.className = "detail-copy";
    teacherImportResult.innerHTML = `
      <strong>最近一次导入结果</strong><br />
      来源文件：${escapeHtml(summary.fileName || "未知文件")}<br />
      原始题目数：${escapeHtml(summary.sourceQuestionCount ?? 0)}<br />
      关联入卷题目数：${escapeHtml(summary.linkedQuestionCount ?? 0)}<br />
      新增题目数：${escapeHtml(summary.createdQuestionCount ?? 0)}<br />
      复用题目数：${escapeHtml(summary.reusedQuestionCount ?? 0)}<br />
      文件内重复数：${escapeHtml(summary.duplicateQuestionCount ?? 0)}
    `;
  } else if (enabled) {
    teacherImportResult.className = "empty-state";
    teacherImportResult.textContent = busy ? "正在导入并生成试卷，请稍候。" : "选择文件并提交后，这里会显示最近一次导入结果。";
  }

  pickTeacherImportFileButton.disabled = !enabled || busy;
  clearTeacherImportFileButton.disabled = !enabled || busy || !state.teacherImport.filePath;
  teacherImportPaperNameInput.disabled = !enabled || busy;
  teacherImportSubjectInput.disabled = !enabled || busy;
  teacherImportPassScoreInput.disabled = !enabled || busy;
  teacherImportDurationInput.disabled = !enabled || busy;
  teacherImportDescriptionInput.disabled = !enabled || busy;
  teacherImportForm.querySelector("button[type='submit']").disabled = !enabled || busy || !state.teacherImport.filePath;
}

function resetTeacherPaperState({ keepSelection = false } = {}) {
  state.teacherPapers.loading = false;
  state.teacherPapers.actionPaperId = null;
  state.teacherPapers.summary = null;
  state.teacherPapers.papers = [];
  if (!keepSelection) {
    state.teacherPapers.selectedPaperId = null;
    state.teacherPapers.paperDetails = {};
  }
}

function getSelectedTeacherPaperDetail() {
  const paperId = state.teacherPapers.selectedPaperId;
  return paperId ? state.teacherPapers.paperDetails[paperId] || null : null;
}

function renderTeacherPaperSummary() {
  teacherPaperSummary.innerHTML = "";
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    return;
  }

  const summary = state.teacherPapers.summary || {};
  [
    ["paperCount", summary.paperCount ?? 0],
    ["publishedCount", summary.publishedCount ?? 0],
    ["unpublishedCount", summary.unpublishedCount ?? 0]
  ].forEach(([key, value]) => {
    teacherPaperSummary.appendChild(buildStatusCard(summaryLabels[key], true, String(value)));
  });
}

function renderTeacherPaperList() {
  if (!state.currentSession) {
    teacherPaperList.className = "empty-state";
    teacherPaperList.textContent = "登录教师账号后可查看试卷列表。";
    return;
  }
  if (state.currentSession.data.user.role !== "TEACHER") {
    teacherPaperList.className = "empty-state";
    teacherPaperList.textContent = "当前登录为学生账号，教师试卷管理中心仅对教师开放。";
    return;
  }
  if (state.teacherPapers.loading && !state.teacherPapers.papers.length) {
    teacherPaperList.className = "empty-state";
    teacherPaperList.textContent = "正在加载试卷列表，请稍候。";
    return;
  }
  if (!state.teacherPapers.papers.length) {
    teacherPaperList.className = "empty-state";
    teacherPaperList.textContent = "当前还没有试卷数据。";
    return;
  }

  teacherPaperList.className = "paper-card-list";
  teacherPaperList.innerHTML = state.teacherPapers.papers.map((paper) => {
    const selected = paper.paperId === state.teacherPapers.selectedPaperId ? " selected" : "";
    const actionBusy = paper.paperId === state.teacherPapers.actionPaperId;
    const publishCopy = paper.isPublished ? "已发布" : "未发布";
    return `
      <article class="exam-paper-card${selected}">
        <div class="paper-card-head">
          <div>
            <h4>${escapeHtml(paper.paperName)}</h4>
            <p>${escapeHtml(paper.subject)} · ${escapeHtml(publishCopy)}</p>
          </div>
          <span class="mini-pill">${escapeHtml(paper.questionCount ?? 0)} 题</span>
        </div>
        <div class="paper-meta-grid">
          <div><span>总分</span><strong>${escapeHtml(paper.totalScore ?? 0)}</strong></div>
          <div><span>时长</span><strong>${escapeHtml(paper.duration ?? 0)} 分钟</strong></div>
          <div><span>及格线</span><strong>${escapeHtml(paper.passScore ?? "暂无")}</strong></div>
        </div>
        <div class="paper-subcopy">更新时间：${escapeHtml(formatDisplayTime(paper.updateTime || paper.createTime))}</div>
        <div class="button-row">
          <button class="button button-secondary" data-teacher-paper-action="view" data-paper-id="${paper.paperId}" ${actionBusy ? "disabled" : ""}>查看详情</button>
          <button class="button button-secondary" data-teacher-paper-action="toggle-publish" data-paper-id="${paper.paperId}" ${actionBusy ? "disabled" : ""}>${paper.isPublished ? "取消发布" : "发布试卷"}</button>
          <button class="button button-secondary" data-teacher-paper-action="delete" data-paper-id="${paper.paperId}" ${actionBusy ? "disabled" : ""}>删除</button>
        </div>
      </article>
    `;
  }).join("");
}

function renderTeacherPaperWorkspace() {
  if (!state.currentSession) {
    teacherPaperWorkspace.className = "empty-state";
    teacherPaperWorkspace.textContent = "登录教师账号后可在这里查看试卷详情和管理操作。";
    return;
  }
  if (state.currentSession.data.user.role !== "TEACHER") {
    teacherPaperWorkspace.className = "empty-state";
    teacherPaperWorkspace.textContent = "学生账号暂不显示教师试卷管理工作区。";
    return;
  }

  const detail = getSelectedTeacherPaperDetail();
  if (!detail) {
    teacherPaperWorkspace.className = "empty-state";
    teacherPaperWorkspace.textContent = state.teacherPapers.papers.length
      ? "请选择一张试卷查看详情。"
      : "当前还没有可查看的试卷详情。";
    return;
  }

  const paper = detail.paper || {};
  const questions = detail.questions || [];
  const actionBusy = paper.paperId === state.teacherPapers.actionPaperId;
  const description = escapeHtml(paper.description || "暂无试卷说明").replace(/\r?\n/g, "<br />");
  const questionCards = questions.length
    ? questions.map((question) => `
      <article class="answer-review-card${record.recordId === state.teacherStudents.selectedRecordId ? " selected" : ""}" data-teacher-record-action="view" data-record-id="${record.recordId}">
        <div class="answer-review-head">
          <div>
            <h5>第 ${escapeHtml(question.position ?? "-")} 题</h5>
            <p>${escapeHtml(localizeQuestionType(question.questionType))}</p>
          </div>
          <span class="mini-pill">${escapeHtml(question.score ?? 0)} 分</span>
        </div>
        <div class="question-content">${escapeHtml(question.content || "暂无题目内容").replace(/\r?\n/g, "<br />")}</div>
      </article>
    `).join("")
    : '<div class="empty-state">当前试卷还没有关联题目。</div>';

  teacherPaperWorkspace.className = "exam-workspace-shell";
  teacherPaperWorkspace.innerHTML = `
    <article class="workspace-card">
      <div class="workspace-header">
        <div>
          <h4>${escapeHtml(paper.paperName || "未命名试卷")}</h4>
          <p>在 Electron 中查看试卷详情，并执行发布、取消发布或删除操作。</p>
        </div>
        <span class="mini-pill">${paper.isPublished ? "已发布" : "未发布"}</span>
      </div>
      <div class="workspace-metric-grid">
        <div class="workspace-metric"><span>科目</span><strong>${escapeHtml(paper.subject || "暂无")}</strong></div>
        <div class="workspace-metric"><span>题目数</span><strong>${escapeHtml(paper.questionCount ?? questions.length)}</strong></div>
        <div class="workspace-metric"><span>总分</span><strong>${escapeHtml(paper.totalScore ?? 0)}</strong></div>
        <div class="workspace-metric"><span>时长</span><strong>${escapeHtml(paper.duration ?? 0)} 分钟</strong></div>
        <div class="workspace-metric"><span>及格线</span><strong>${escapeHtml(paper.passScore ?? "暂无")}</strong></div>
        <div class="workspace-metric"><span>更新时间</span><strong>${escapeHtml(formatDisplayTime(paper.updateTime || paper.createTime))}</strong></div>
      </div>
      <div class="detail-copy">${description}</div>
      <div class="button-row">
        <button class="button button-secondary" data-teacher-workspace-action="toggle-publish" ${actionBusy ? "disabled" : ""}>${paper.isPublished ? "取消发布" : "发布试卷"}</button>
        <button class="button button-secondary" data-teacher-workspace-action="delete" ${actionBusy ? "disabled" : ""}>删除试卷</button>
      </div>
      <div class="answer-review-list">${questionCards}</div>
    </article>
  `;
}

function renderTeacherPaperWorkspaceV2() {
  if (!state.currentSession) {
    teacherPaperWorkspace.className = "empty-state";
    teacherPaperWorkspace.textContent = "登录教师账号后可在这里查看、编辑试卷并执行管理操作。";
    return;
  }
  if (state.currentSession.data.user.role !== "TEACHER") {
    teacherPaperWorkspace.className = "empty-state";
    teacherPaperWorkspace.textContent = "学生账号暂不显示教师试卷管理工作区。";
    return;
  }

  const detail = getSelectedTeacherPaperDetail();
  if (!detail) {
    teacherPaperWorkspace.className = "empty-state";
    teacherPaperWorkspace.textContent = state.teacherPapers.papers.length
      ? "请选择一张试卷查看详情。"
      : "当前还没有可查看的试卷详情。";
    return;
  }

  const paper = detail.paper || {};
  const questions = detail.questions || [];
  const actionBusy = paper.paperId === state.teacherPapers.actionPaperId;
  const subjectOptions = buildTeacherSubjectOptions(paper.subject || teacherSubjectOptions[0]);
  const questionCards = questions.length
    ? questions.map((question) => `
      <article class="answer-review-card">
        <div class="answer-review-head">
          <div>
            <h5>第 ${escapeHtml(question.position ?? "-")} 题</h5>
            <p>${escapeHtml(localizeQuestionType(question.questionType))}</p>
          </div>
          <span class="mini-pill">${escapeHtml(question.score ?? 0)} 分</span>
        </div>
        <div class="question-content">${escapeHtml(question.content || "暂无题目内容").replace(/\r?\n/g, "<br />")}</div>
      </article>
    `).join("")
    : '<div class="empty-state">当前试卷还没有关联题目。</div>';

  teacherPaperWorkspace.className = "exam-workspace-shell";
  teacherPaperWorkspace.innerHTML = `
    <article class="workspace-card">
      <div class="workspace-header">
        <div>
          <h4>${escapeHtml(paper.paperName || "未命名试卷")}</h4>
          <p>在 Electron 中编辑试卷基础信息，并执行发布、删除等教师常用操作。</p>
        </div>
        <span class="mini-pill">${paper.isPublished ? "已发布" : "未发布"}</span>
      </div>
      <form id="teacher-paper-form" class="config-form">
        <label class="field">
          <span>试卷名称</span>
          <input name="paperName" type="text" value="${escapeHtml(paper.paperName || "")}" placeholder="请输入试卷名称" ${actionBusy ? "disabled" : ""} />
        </label>
        <label class="field">
          <span>科目</span>
          <select name="subject" ${actionBusy ? "disabled" : ""}>${subjectOptions}</select>
        </label>
        <label class="field">
          <span>及格分数</span>
          <input name="passScore" type="number" min="0" max="100" step="1" value="${escapeHtml(paper.passScore ?? 60)}" ${actionBusy ? "disabled" : ""} />
        </label>
        <label class="field">
          <span>考试时长（分钟）</span>
          <input name="duration" type="number" min="1" max="300" step="5" value="${escapeHtml(paper.duration ?? 90)}" ${actionBusy ? "disabled" : ""} />
        </label>
        <label class="field field-wide">
          <span>试卷说明</span>
          <textarea name="description" class="exam-textarea" placeholder="可选：输入试卷说明" ${actionBusy ? "disabled" : ""}>${escapeHtml(paper.description || "")}</textarea>
        </label>
        <div class="workspace-metric-grid">
          <div class="workspace-metric"><span>题目数</span><strong>${escapeHtml(paper.questionCount ?? questions.length)}</strong></div>
          <div class="workspace-metric"><span>总分</span><strong>${escapeHtml(paper.totalScore ?? 0)}</strong></div>
          <div class="workspace-metric"><span>发布状态</span><strong>${paper.isPublished ? "已发布" : "未发布"}</strong></div>
          <div class="workspace-metric"><span>更新时间</span><strong>${escapeHtml(formatDisplayTime(paper.updateTime || paper.createTime))}</strong></div>
        </div>
        <div class="button-row">
          <button class="button button-primary" type="submit" ${actionBusy ? "disabled" : ""}>保存修改</button>
          <button class="button button-secondary" type="button" data-teacher-workspace-action="toggle-publish" ${actionBusy ? "disabled" : ""}>${paper.isPublished ? "取消发布" : "发布试卷"}</button>
          <button class="button button-secondary" type="button" data-teacher-workspace-action="delete" ${actionBusy ? "disabled" : ""}>删除试卷</button>
        </div>
      </form>
      <div class="answer-review-list">${questionCards}</div>
    </article>
  `;
}

function renderTeacherPaperCenter() {
  reloadTeacherPapersButton.disabled = !state.currentSession || state.currentSession.data.user.role !== "TEACHER" || state.teacherPapers.loading || Boolean(state.teacherPapers.actionPaperId);
  renderTeacherPaperSummary();
  renderTeacherPaperList();
  renderTeacherPaperWorkspaceV2();
  renderWorkspaceHub();
}

async function loadTeacherPaperDetail(paperId, { silent = false, force = false } = {}) {
  state.teacherPapers.selectedPaperId = paperId;
  if (!force && state.teacherPapers.paperDetails[paperId]) {
    renderTeacherPaperCenter();
    return;
  }

  state.teacherPapers.actionPaperId = paperId;
  renderTeacherPaperCenter();
  const result = await window.desktopApi.bridgePaperDetail({ paperId });
  if (!silent) {
    appendLog(result.message);
  }
  state.teacherPapers.actionPaperId = null;
  if (result.ok && result.data) {
    state.teacherPapers.paperDetails[paperId] = result.data;
  }
  renderTeacherPaperCenter();
}

async function loadTeacherPaperCenter({ preserveSelection = true } = {}) {
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    resetTeacherPaperState();
    renderTeacherPaperCenter();
    return;
  }

  state.teacherPapers.loading = true;
  renderTeacherPaperCenter();
  const result = await window.desktopApi.bridgeTeacherPapers({ userId: state.currentSession.data.user.userId });
  appendLog(result.message);
  state.teacherPapers.loading = false;

  if (!result.ok || !result.data) {
    state.teacherPapers.summary = null;
    state.teacherPapers.papers = [];
    state.teacherPapers.selectedPaperId = null;
    state.teacherPapers.paperDetails = {};
    renderTeacherPaperCenter();
    return;
  }

  state.teacherPapers.summary = result.data.summary || null;
  state.teacherPapers.papers = result.data.papers || [];

  const hasSelected = state.teacherPapers.papers.some((paper) => paper.paperId === state.teacherPapers.selectedPaperId);
  if (!preserveSelection || !hasSelected) {
    state.teacherPapers.selectedPaperId = state.teacherPapers.papers[0]?.paperId || null;
  }

  renderTeacherPaperCenter();
  if (state.teacherPapers.selectedPaperId) {
    await loadTeacherPaperDetail(state.teacherPapers.selectedPaperId, { silent: true });
  }
}

async function toggleTeacherPaperPublish(paperId) {
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    appendLog("请先登录教师账号。");
    return;
  }

  const paper = state.teacherPapers.papers.find((item) => item.paperId === paperId);
  if (!paper) {
    appendLog("未找到对应试卷。");
    return;
  }

  const actionCopy = paper.isPublished ? "取消发布" : "发布";
  if (!window.confirm(`确认要${actionCopy}《${paper.paperName}》吗？`)) {
    return;
  }

  state.teacherPapers.actionPaperId = paperId;
  renderTeacherPaperCenter();
  const result = await window.desktopApi.bridgeTogglePaperPublish({
    userId: state.currentSession.data.user.userId,
    paperId
  });
  appendLog(result.message);
  state.teacherPapers.actionPaperId = null;

  if (result.ok && result.data?.paper) {
    state.teacherPapers.paperDetails[paperId] = result.data;
  }

  await loadOverview();
  await loadTeacherPaperCenter({ preserveSelection: true });
}

async function deleteTeacherPaper(paperId) {
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    appendLog("请先登录教师账号。");
    return;
  }

  const paper = state.teacherPapers.papers.find((item) => item.paperId === paperId);
  if (!paper) {
    appendLog("未找到对应试卷。");
    return;
  }

  if (!window.confirm(`确认删除《${paper.paperName}》吗？删除后将无法恢复。`)) {
    return;
  }

  state.teacherPapers.actionPaperId = paperId;
  renderTeacherPaperCenter();
  const result = await window.desktopApi.bridgeDeletePaper({
    userId: state.currentSession.data.user.userId,
    paperId
  });
  appendLog(result.message);
  state.teacherPapers.actionPaperId = null;

  if (!result.ok) {
    renderTeacherPaperCenter();
    return;
  }

  delete state.teacherPapers.paperDetails[paperId];
  if (state.teacherPapers.selectedPaperId === paperId) {
    state.teacherPapers.selectedPaperId = null;
  }

  await loadOverview();
  await loadTeacherPaperCenter({ preserveSelection: false });
}

async function saveTeacherPaper(form) {
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    appendLog("请先登录教师账号。");
    return;
  }

  const paperId = state.teacherPapers.selectedPaperId;
  if (!paperId) {
    appendLog("请先选择一张试卷。");
    return;
  }

  const formData = new FormData(form);
  const payload = {
    paperName: String(formData.get("paperName") || "").trim(),
    subject: String(formData.get("subject") || "").trim(),
    passScore: Number(formData.get("passScore")),
    duration: Number(formData.get("duration")),
    description: String(formData.get("description") || "").trim()
  };

  if (!payload.paperName) {
    window.alert("请输入试卷名称。");
    return;
  }
  if (!payload.subject) {
    window.alert("请选择科目。");
    return;
  }
  if (!Number.isFinite(payload.passScore) || payload.passScore < 0) {
    window.alert("请输入有效的及格分数。");
    return;
  }
  if (!Number.isFinite(payload.duration) || payload.duration <= 0) {
    window.alert("请输入有效的考试时长。");
    return;
  }

  state.teacherPapers.actionPaperId = paperId;
  renderTeacherPaperCenter();
  const result = await window.desktopApi.bridgeUpdatePaper({
    userId: state.currentSession.data.user.userId,
    paperId,
    ...payload
  });
  appendLog(result.message);
  state.teacherPapers.actionPaperId = null;

  if (!result.ok) {
    renderTeacherPaperCenter();
    return;
  }

  if (result.data) {
    state.teacherPapers.paperDetails[paperId] = result.data;
  }

  await loadOverview();
  await loadTeacherPaperCenter({ preserveSelection: true });
}

async function chooseTeacherImportFile() {
  const selection = await window.desktopApi.pickTeacherImportFile();
  if (selection.canceled) {
    appendLog("已取消题目文件选择。");
    return;
  }

  state.teacherImport.filePath = selection.path || "";
  state.teacherImport.fileName = selection.name || "";
  state.teacherImport.lastResult = null;
  if (!teacherImportPaperNameInput.value.trim() && selection.name) {
    teacherImportPaperNameInput.value = selection.name.replace(/\.txt$/i, "");
  }
  renderTeacherImportCenter();
}

function clearTeacherImportSelection() {
  state.teacherImport.filePath = "";
  state.teacherImport.fileName = "";
  state.teacherImport.lastResult = null;
  renderTeacherImportCenter();
}

async function submitTeacherImportForm() {
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    appendLog("请先登录教师账号。");
    return;
  }
  if (!state.teacherImport.filePath) {
    window.alert("请先选择题目导入文件。");
    return;
  }

  const payload = {
    paperName: teacherImportPaperNameInput.value.trim(),
    subject: teacherImportSubjectInput.value.trim(),
    passScore: Number(teacherImportPassScoreInput.value),
    duration: Number(teacherImportDurationInput.value),
    description: teacherImportDescriptionInput.value.trim()
  };

  if (!payload.paperName) {
    window.alert("请输入试卷名称。");
    return;
  }
  if (!payload.subject) {
    window.alert("请选择科目。");
    return;
  }
  if (!Number.isFinite(payload.passScore) || payload.passScore < 0) {
    window.alert("请输入有效的及格分数。");
    return;
  }
  if (!Number.isFinite(payload.duration) || payload.duration <= 0) {
    window.alert("请输入有效的考试时长。");
    return;
  }

  state.teacherImport.importing = true;
  renderTeacherImportCenter();
  const result = await window.desktopApi.bridgeImportPaper({
    userId: state.currentSession.data.user.userId,
    filePath: state.teacherImport.filePath,
    ...payload
  });
  appendLog(result.message);
  state.teacherImport.importing = false;

  if (!result.ok) {
    renderTeacherImportCenter();
    return;
  }

  state.teacherImport.lastResult = result.data || null;
  const createdPaperId = result.data?.paper?.paperId;
  resetTeacherImportForm();
  state.teacherImport.filePath = "";
  state.teacherImport.fileName = "";

  if (createdPaperId && result.data) {
    state.teacherPapers.selectedPaperId = createdPaperId;
    state.teacherPapers.paperDetails[createdPaperId] = result.data;
  }

  await loadOverview();
  await loadTeacherPaperCenter({ preserveSelection: true });
  if (createdPaperId) {
    await loadTeacherPaperDetail(createdPaperId, { silent: true, force: true });
  }
  renderTeacherImportCenter();
}

function resetTeacherStudentState({ keepSelection = false } = {}) {
  state.teacherStudents.loading = false;
  state.teacherStudents.actionStudentId = null;
  state.teacherStudents.loadingRecordId = null;
  state.teacherStudents.createMode = false;
  state.teacherStudents.summary = null;
  state.teacherStudents.students = [];
  if (!keepSelection) {
    state.teacherStudents.selectedStudentId = null;
    state.teacherStudents.selectedRecordId = null;
    state.teacherStudents.studentDetails = {};
    state.teacherStudents.recordDetails = {};
  }
}

function getSelectedTeacherStudentDetail() {
  const studentId = state.teacherStudents.selectedStudentId;
  return studentId ? state.teacherStudents.studentDetails[studentId] || null : null;
}

function getSelectedTeacherRecordDetail() {
  const recordId = state.teacherStudents.selectedRecordId;
  return recordId ? state.teacherStudents.recordDetails[recordId] || null : null;
}

function buildTeacherRecordDetailPanel(records) {
  if (state.teacherStudents.createMode || !records.length) {
    return "";
  }

  if (state.teacherStudents.loadingRecordId && state.teacherStudents.loadingRecordId === state.teacherStudents.selectedRecordId) {
    return '<div class="empty-state">正在加载所选考试的答题详情，请稍候。</div>';
  }

  const detail = getSelectedTeacherRecordDetail();
  if (!detail) {
    return '<div class="empty-state">点击上方某条考试记录后，这里会显示逐题答题详情。</div>';
  }

  const record = detail.record || {};
  const answers = detail.answers || [];
  const outcomeCopy = ["SUBMITTED", "TIMEOUT"].includes(record.status)
    ? (record.passed ? "已通过" : "未通过")
    : "未完成";
  const reviewCards = answers.length
    ? answers.map((answer) => {
      const answered = String(answer.studentAnswer || "").trim().length > 0;
      const cardState = answered ? (answer.isCorrect ? " correct" : " wrong") : " empty";
      const stateCopy = answered ? (answer.isCorrect ? "作答正确" : "作答有误") : "未作答";
      return `
        <article class="answer-review-card${cardState}">
          <div class="answer-review-head">
            <div>
              <h5>第 ${escapeHtml(answer.position ?? "-")} 题</h5>
              <p>${escapeHtml(localizeQuestionType(answer.questionType))}</p>
            </div>
            <span class="mini-pill">${escapeHtml(stateCopy)}</span>
          </div>
          <div class="question-content">${formatMultilineCopy(answer.content, "暂无题目内容")}</div>
          <div class="answer-review-grid">
            <div class="answer-review-block">
              <strong>学生答案</strong>
              <div>${formatMultilineCopy(answer.studentAnswer, "未作答")}</div>
            </div>
            <div class="answer-review-block">
              <strong>参考答案</strong>
              <div>${formatMultilineCopy(answer.correctAnswer, "暂无")}</div>
            </div>
          </div>
          <div class="answer-review-grid">
            <div class="answer-review-block">
              <strong>本题得分</strong>
              <div>${escapeHtml(answer.score ?? 0)} 分</div>
            </div>
            <div class="answer-review-block">
              <strong>题目解析</strong>
              <div>${formatMultilineCopy(answer.analysis, "暂无解析")}</div>
            </div>
          </div>
        </article>
      `;
    }).join("")
    : '<div class="empty-state">当前记录还没有逐题答题明细。</div>';

  const warning = hasSubjectiveAnswerRecords(answers)
    ? '<div class="warning-note">当前记录包含主观题或应用题。Electron 已支持查看作答与得分，但底层仍沿用旧版 Java 判分逻辑，请结合教师复核结果判断。</div>'
    : "";

  return `
    <div class="detail-copy">
      <strong>本次考试详情</strong><br />
      科目：${escapeHtml(record.subject || "暂无")}<br />
      开始时间：${escapeHtml(formatDisplayTime(record.startTime))}<br />
      提交时间：${escapeHtml(formatDisplayTime(record.submitTime))}
    </div>
    <div class="workspace-metric-grid">
      <div class="workspace-metric"><span>考试成绩</span><strong>${escapeHtml(formatScoreCopy(record.score, record.totalScore))}</strong></div>
      <div class="workspace-metric"><span>及格线</span><strong>${escapeHtml(record.passScore ?? "暂无")}</strong></div>
      <div class="workspace-metric"><span>考试结果</span><strong>${outcomeCopy}</strong></div>
      <div class="workspace-metric"><span>作答时长</span><strong>${escapeHtml(formatDurationCopy(record.durationSeconds))}</strong></div>
      <div class="workspace-metric"><span>题目总数</span><strong>${escapeHtml(record.questionCount ?? 0)}</strong></div>
      <div class="workspace-metric"><span>已作答</span><strong>${escapeHtml(record.answeredCount ?? 0)}</strong></div>
      <div class="workspace-metric"><span>答对题数</span><strong>${escapeHtml(record.correctCount ?? 0)}</strong></div>
      <div class="workspace-metric"><span>答错题数</span><strong>${escapeHtml(record.wrongCount ?? 0)}</strong></div>
    </div>
    ${warning}
    <div class="answer-review-list">${reviewCards}</div>
  `;
}

function renderTeacherStudentSummary() {
  teacherStudentSummary.innerHTML = "";
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    return;
  }

  const summary = state.teacherStudents.summary || {};
  [["studentCount", summary.studentCount ?? 0]].forEach(([key, value]) => {
    teacherStudentSummary.appendChild(buildStatusCard(summaryLabels[key], true, String(value)));
  });
}

function renderTeacherStudentList() {
  if (!state.currentSession) {
    teacherStudentList.className = "empty-state";
    teacherStudentList.textContent = "登录教师账号后可查看学生列表。";
    return;
  }
  if (state.currentSession.data.user.role !== "TEACHER") {
    teacherStudentList.className = "empty-state";
    teacherStudentList.textContent = "当前登录为学生账号，教师学生管理中心仅对教师开放。";
    return;
  }
  if (state.teacherStudents.loading && !state.teacherStudents.students.length) {
    teacherStudentList.className = "empty-state";
    teacherStudentList.textContent = "正在加载学生列表，请稍候。";
    return;
  }
  if (!state.teacherStudents.students.length) {
    teacherStudentList.className = "empty-state";
    teacherStudentList.textContent = "当前还没有学生数据。";
    return;
  }

  teacherStudentList.className = "paper-card-list";
  teacherStudentList.innerHTML = state.teacherStudents.students.map((student) => {
    const selected = !state.teacherStudents.createMode && student.userId === state.teacherStudents.selectedStudentId ? " selected" : "";
    const actionBusy = student.userId === state.teacherStudents.actionStudentId;
    return `
      <article class="exam-paper-card${selected}">
        <div class="paper-card-head">
          <div>
            <h4>${escapeHtml(student.realName || "未命名学生")}</h4>
            <p>学号 ${escapeHtml(student.studentNumber || "暂无")} · ${escapeHtml(student.status || "ACTIVE")}</p>
          </div>
          <span class="mini-pill">${escapeHtml(student.gender || "未填写")}</span>
        </div>
        <div class="paper-meta-grid">
          <div><span>邮箱</span><strong>${escapeHtml(student.email || "暂无")}</strong></div>
          <div><span>电话</span><strong>${escapeHtml(student.phone || "暂无")}</strong></div>
          <div><span>创建时间</span><strong>${escapeHtml(formatDisplayTime(student.createTime))}</strong></div>
        </div>
        <div class="button-row">
          <button class="button button-secondary" data-teacher-student-action="view" data-student-id="${student.userId}" ${actionBusy ? "disabled" : ""}>查看详情</button>
          <button class="button button-secondary" data-teacher-student-action="delete" data-student-id="${student.userId}" ${actionBusy ? "disabled" : ""}>删除</button>
        </div>
      </article>
    `;
  }).join("");
}

function renderTeacherStudentWorkspace() {
  if (!state.currentSession) {
    teacherStudentWorkspace.className = "empty-state";
    teacherStudentWorkspace.textContent = "登录教师账号后可在这里查看学生档案和考试记录。";
    return;
  }
  if (state.currentSession.data.user.role !== "TEACHER") {
    teacherStudentWorkspace.className = "empty-state";
    teacherStudentWorkspace.textContent = "学生账号暂不显示教师学生管理工作区。";
    return;
  }

  const detail = getSelectedTeacherStudentDetail();
  const student = state.teacherStudents.createMode ? null : (detail?.student || null);
  const summary = detail?.summary || {};
  const records = detail?.records || [];
  const actionBusy = Boolean(state.teacherStudents.actionStudentId);

  const modeCopy = state.teacherStudents.createMode ? "新建学生" : "保存修改";
  const realName = student?.realName || "";
  const studentNumber = student?.studentNumber || "";
  const email = student?.email || "";
  const phone = student?.phone || "";
  const gender = student?.gender || "";

  const recordDetailPanel = buildTeacherRecordDetailPanel(records);
  const recordCards = records.length
    ? records.map((record) => `
      <article class="answer-review-card">
        <div class="answer-review-head">
          <div>
            <h5>${escapeHtml(record.paperName || "未命名试卷")}</h5>
            <p>${escapeHtml(localizeExamStatus(record.status))}</p>
          </div>
          <span class="mini-pill">${escapeHtml(formatScoreCopy(record.score, null))}</span>
        </div>
        <div class="answer-review-grid">
          <div class="answer-review-block">
            <strong>开始时间</strong>
            <div>${escapeHtml(formatDisplayTime(record.startTime))}</div>
          </div>
          <div class="answer-review-block">
            <strong>提交时间</strong>
            <div>${escapeHtml(formatDisplayTime(record.submitTime))}</div>
          </div>
        </div>
        <div class="answer-review-grid">
          <div class="answer-review-block">
            <strong>作答时长</strong>
            <div>${escapeHtml(formatDurationCopy(record.durationSeconds))}</div>
          </div>
          <div class="answer-review-block">
            <strong>成绩</strong>
            <div>${escapeHtml(record.score ?? "暂无")}</div>
          </div>
        </div>
      </article>
    `).join("")
    : '<div class="empty-state">当前学生还没有考试记录。</div>';

  teacherStudentWorkspace.className = "exam-workspace-shell";
  teacherStudentWorkspace.innerHTML = `
    <article class="workspace-card">
      <div class="workspace-header">
        <div>
          <h4>${state.teacherStudents.createMode ? "新建学生" : escapeHtml(student?.realName || "学生档案")}</h4>
          <p>在 Electron 中维护学生基础信息，并查看该学生的考试记录。</p>
        </div>
        <span class="mini-pill">${state.teacherStudents.createMode ? "新建模式" : "编辑模式"}</span>
      </div>
      <form id="teacher-student-form" class="config-form">
        <label class="field">
          <span>姓名</span>
          <input name="realName" type="text" value="${escapeHtml(realName)}" placeholder="请输入学生姓名" />
        </label>
        <label class="field">
          <span>学号</span>
          <input name="studentNumber" type="text" value="${escapeHtml(studentNumber)}" placeholder="请输入学号" ${state.teacherStudents.createMode ? "" : "disabled"} />
        </label>
        <label class="field">
          <span>${state.teacherStudents.createMode ? "初始密码" : "新密码"}</span>
          <input name="password" type="password" placeholder="${state.teacherStudents.createMode ? "请输入初始密码" : "留空则保持原密码"}" />
        </label>
        <label class="field">
          <span>邮箱</span>
          <input name="email" type="text" value="${escapeHtml(email)}" placeholder="例如：student@example.com" />
        </label>
        <label class="field">
          <span>电话</span>
          <input name="phone" type="text" value="${escapeHtml(phone)}" placeholder="请输入联系电话" />
        </label>
        <label class="field">
          <span>性别</span>
          <input name="gender" type="text" value="${escapeHtml(gender)}" placeholder="例如：男 / 女" />
        </label>
        <div class="button-row">
          <button class="button button-primary" type="submit" ${actionBusy ? "disabled" : ""}>${modeCopy}</button>
          <button class="button button-secondary" type="button" data-teacher-student-workspace-action="cancel-create" ${state.teacherStudents.createMode ? "" : "hidden"}>取消新建</button>
          <button class="button button-secondary" type="button" data-teacher-student-workspace-action="new-student" ${actionBusy ? "disabled" : ""}>新建学生</button>
          <button class="button button-secondary" type="button" data-teacher-student-workspace-action="delete-student" ${state.teacherStudents.createMode || !student ? "hidden" : ""}>删除学生</button>
        </div>
      </form>
      ${state.teacherStudents.createMode ? "" : `
        <div class="workspace-metric-grid">
          <div class="workspace-metric"><span>考试记录数</span><strong>${escapeHtml(summary.recordCount ?? 0)}</strong></div>
          <div class="workspace-metric"><span>已提交次数</span><strong>${escapeHtml(summary.submittedCount ?? 0)}</strong></div>
          <div class="workspace-metric"><span>平均成绩</span><strong>${escapeHtml(summary.averageScore ?? 0)}</strong></div>
          <div class="workspace-metric"><span>最近更新时间</span><strong>${escapeHtml(formatDisplayTime(student?.updateTime || student?.createTime))}</strong></div>
        </div>
        <div class="answer-review-list">${recordCards}</div>
        ${recordDetailPanel}
      `}
    </article>
  `;
}

function renderTeacherStudentCenter() {
  const disabled = !state.currentSession || state.currentSession.data.user.role !== "TEACHER" || state.teacherStudents.loading || Boolean(state.teacherStudents.actionStudentId);
  createTeacherStudentButton.disabled = disabled;
  reloadTeacherStudentsButton.disabled = disabled;
  renderTeacherStudentSummary();
  renderTeacherStudentList();
  renderTeacherStudentWorkspace();
  renderWorkspaceHub();
}

async function loadTeacherStudentRecordDetail(recordId, { silent = false, force = false } = {}) {
  state.teacherStudents.selectedRecordId = recordId;
  if (!force && state.teacherStudents.recordDetails[recordId]) {
    renderTeacherStudentCenter();
    return;
  }

  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER" || !state.teacherStudents.selectedStudentId) {
    renderTeacherStudentCenter();
    return;
  }

  state.teacherStudents.loadingRecordId = recordId;
  renderTeacherStudentCenter();
  const result = await window.desktopApi.bridgeTeacherRecordDetail({
    userId: state.currentSession.data.user.userId,
    studentId: state.teacherStudents.selectedStudentId,
    recordId
  });
  if (!silent) {
    appendLog(result.message);
  }
  state.teacherStudents.loadingRecordId = null;
  if (result.ok && result.data) {
    state.teacherStudents.recordDetails[recordId] = result.data;
  } else {
    delete state.teacherStudents.recordDetails[recordId];
  }
  renderTeacherStudentCenter();
}

async function loadTeacherStudentDetail(studentId, { silent = false, force = false } = {}) {
  state.teacherStudents.createMode = false;
  state.teacherStudents.selectedStudentId = studentId;
  if (!force && state.teacherStudents.studentDetails[studentId]) {
    const cachedRecords = state.teacherStudents.studentDetails[studentId]?.records || [];
    const hasSelectedRecord = cachedRecords.some((record) => record.recordId === state.teacherStudents.selectedRecordId);
    if (!hasSelectedRecord) {
      state.teacherStudents.selectedRecordId = cachedRecords[0]?.recordId || null;
    }
    renderTeacherStudentCenter();
    if (state.teacherStudents.selectedRecordId && !state.teacherStudents.recordDetails[state.teacherStudents.selectedRecordId]) {
      await loadTeacherStudentRecordDetail(state.teacherStudents.selectedRecordId, { silent: true });
    }
    return;
  }

  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    renderTeacherStudentCenter();
    return;
  }

  state.teacherStudents.actionStudentId = studentId;
  renderTeacherStudentCenter();
  const result = await window.desktopApi.bridgeTeacherStudentDetail({
    userId: state.currentSession.data.user.userId,
    studentId
  });
  if (!silent) {
    appendLog(result.message);
  }
  state.teacherStudents.actionStudentId = null;
  if (result.ok && result.data) {
    state.teacherStudents.studentDetails[studentId] = result.data;
    const records = result.data.records || [];
    const hasSelectedRecord = records.some((record) => record.recordId === state.teacherStudents.selectedRecordId);
    if (!hasSelectedRecord) {
      state.teacherStudents.selectedRecordId = records[0]?.recordId || null;
    }
  }
  renderTeacherStudentCenter();
  if (result.ok && state.teacherStudents.selectedRecordId) {
    await loadTeacherStudentRecordDetail(state.teacherStudents.selectedRecordId, { silent: true });
  }
}

async function loadTeacherStudentCenter({ preserveSelection = true } = {}) {
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    resetTeacherStudentState();
    renderTeacherStudentCenter();
    return;
  }

  state.teacherStudents.loading = true;
  renderTeacherStudentCenter();
  const result = await window.desktopApi.bridgeTeacherStudents({ userId: state.currentSession.data.user.userId });
  appendLog(result.message);
  state.teacherStudents.loading = false;

  if (!result.ok || !result.data) {
    state.teacherStudents.summary = null;
    state.teacherStudents.students = [];
    state.teacherStudents.selectedStudentId = null;
    state.teacherStudents.selectedRecordId = null;
    state.teacherStudents.studentDetails = {};
    state.teacherStudents.recordDetails = {};
    renderTeacherStudentCenter();
    return;
  }

  state.teacherStudents.summary = result.data.summary || null;
  state.teacherStudents.students = result.data.students || [];

  const hasSelected = state.teacherStudents.students.some((student) => student.userId === state.teacherStudents.selectedStudentId);
  if (!preserveSelection || !hasSelected) {
    state.teacherStudents.selectedStudentId = state.teacherStudents.students[0]?.userId || null;
  }
  if (!state.teacherStudents.selectedStudentId) {
    state.teacherStudents.selectedRecordId = null;
  }

  renderTeacherStudentCenter();
  if (!state.teacherStudents.createMode && state.teacherStudents.selectedStudentId) {
    await loadTeacherStudentDetail(state.teacherStudents.selectedStudentId, { silent: true });
  }
}

function openTeacherStudentCreateMode() {
  state.teacherStudents.createMode = true;
  state.teacherStudents.selectedStudentId = null;
  state.teacherStudents.selectedRecordId = null;
  renderTeacherStudentCenter();
}

async function saveTeacherStudent(form) {
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    appendLog("请先登录教师账号。");
    return;
  }

  const formData = new FormData(form);
  const payload = {
    userId: state.currentSession.data.user.userId,
    realName: String(formData.get("realName") || "").trim(),
    studentNumber: String(formData.get("studentNumber") || "").trim(),
    password: String(formData.get("password") || ""),
    email: String(formData.get("email") || "").trim(),
    phone: String(formData.get("phone") || "").trim(),
    gender: String(formData.get("gender") || "").trim()
  };

  if (!payload.realName) {
    window.alert("请输入学生姓名。");
    return;
  }
  if (state.teacherStudents.createMode && !payload.studentNumber) {
    window.alert("请输入学号。");
    return;
  }
  if (state.teacherStudents.createMode && !payload.password) {
    window.alert("请输入初始密码。");
    return;
  }

  const actionStudentId = state.teacherStudents.selectedStudentId || -1;
  state.teacherStudents.actionStudentId = actionStudentId;
  renderTeacherStudentCenter();

  const result = state.teacherStudents.createMode
    ? await window.desktopApi.bridgeCreateStudent(payload)
    : await window.desktopApi.bridgeUpdateStudent({
        ...payload,
        studentId: state.teacherStudents.selectedStudentId
      });

  appendLog(result.message);
  state.teacherStudents.actionStudentId = null;
  if (!result.ok) {
    renderTeacherStudentCenter();
    return;
  }

  state.teacherStudents.createMode = false;
  state.teacherStudents.selectedStudentId = result.data?.student?.userId || state.teacherStudents.selectedStudentId;
  await loadOverview();
  await loadTeacherStudentCenter({ preserveSelection: true });
}

async function deleteTeacherStudent(studentId) {
  if (!state.currentSession || state.currentSession.data.user.role !== "TEACHER") {
    appendLog("请先登录教师账号。");
    return;
  }

  const student = state.teacherStudents.students.find((item) => item.userId === studentId);
  if (!student) {
    appendLog("未找到对应学生。");
    return;
  }

  if (!window.confirm(`确认删除学生《${student.realName}》吗？删除后将无法恢复。`)) {
    return;
  }

  state.teacherStudents.actionStudentId = studentId;
  renderTeacherStudentCenter();
  const result = await window.desktopApi.bridgeDeleteStudent({
    userId: state.currentSession.data.user.userId,
    studentId
  });
  appendLog(result.message);
  state.teacherStudents.actionStudentId = null;
  if (!result.ok) {
    renderTeacherStudentCenter();
    return;
  }

  delete state.teacherStudents.studentDetails[studentId];
  if (state.teacherStudents.selectedStudentId === studentId) {
    state.teacherStudents.selectedStudentId = null;
    state.teacherStudents.selectedRecordId = null;
  }
  state.teacherStudents.createMode = false;

  await loadOverview();
  await loadTeacherStudentCenter({ preserveSelection: false });
}

function clearStudentExamTimer() {
  if (state.studentExam.activeExam?.timerHandle) {
    window.clearInterval(state.studentExam.activeExam.timerHandle);
    state.studentExam.activeExam.timerHandle = null;
  }
}

function resetStudentExamState({ keepSelection = false } = {}) {
  clearStudentExamTimer();
  state.studentExam.loading = false;
  state.studentExam.loadingPaperId = null;
  state.studentExam.summary = null;
  state.studentExam.papers = [];
  state.studentExam.activeExam = null;
  if (!keepSelection) {
    state.studentExam.selectedPaperId = null;
    state.studentExam.paperDetails = {};
  }
}

function getSelectedPaperDetail() {
  const paperId = state.studentExam.selectedPaperId;
  return paperId ? state.studentExam.paperDetails[paperId] || null : null;
}

function countAnsweredQuestions() {
  return Object.values(state.studentExam.activeExam?.answers || {}).filter((value) => String(value || "").trim()).length;
}

function renderStudentExamSummary() {
  studentExamSummary.innerHTML = "";
  if (!state.currentSession || state.currentSession.data.user.role !== "STUDENT") {
    return;
  }
  const summary = state.studentExam.summary || {};
  [["paperCount", summary.paperCount ?? 0], ["completedCount", summary.completedCount ?? 0], ["inProgressCount", summary.inProgressCount ?? 0]]
    .forEach(([key, value]) => studentExamSummary.appendChild(buildStatusCard(summaryLabels[key], true, String(value))));
}

function renderStudentPaperList() {
  if (!state.currentSession) {
    studentPaperList.className = "empty-state";
    studentPaperList.textContent = "登录学生账号后可查看试卷列表。";
    return;
  }
  if (state.currentSession.data.user.role !== "STUDENT") {
    studentPaperList.className = "empty-state";
    studentPaperList.textContent = "当前登录为教师账号，学生考试中心仅对学生开放。";
    return;
  }
  if (state.studentExam.loading && !state.studentExam.papers.length) {
    studentPaperList.className = "empty-state";
    studentPaperList.textContent = "正在加载试卷列表，请稍候。";
    return;
  }
  if (!state.studentExam.papers.length) {
    studentPaperList.className = "empty-state";
    studentPaperList.textContent = "当前没有可用试卷。";
    return;
  }

  const disabled = state.studentExam.activeExam ? "disabled" : "";
  studentPaperList.className = "paper-card-list";
  studentPaperList.innerHTML = state.studentExam.papers.map((paper) => {
    const latest = paper.latestRecord;
    const selected = paper.paperId === state.studentExam.selectedPaperId ? " selected" : "";
    const latestCopy = latest
      ? `最近记录：${localizeExamStatus(latest.status)} / 分数：${latest.score ?? "暂无"} / 时间：${formatDisplayTime(latest.submitTime || latest.startTime)}`
      : "还没有答题记录。";
    return `
      <article class="exam-paper-card${selected}">
        <div class="paper-card-head">
          <div>
            <h4>${escapeHtml(paper.paperName)}</h4>
            <p>${escapeHtml(paper.subject)} · ${escapeHtml(paper.duration)} 分钟 · ${escapeHtml(paper.totalScore)} 分</p>
          </div>
          <span class="mini-pill">${escapeHtml(latest ? localizeExamStatus(latest.status) : "未作答")}</span>
        </div>
        <div class="paper-meta-grid">
          <div><span>题量</span><strong>${escapeHtml(paper.questionCount)}</strong></div>
          <div><span>及格线</span><strong>${escapeHtml(paper.passScore ?? "暂无")}</strong></div>
          <div><span>状态</span><strong>${paper.isPublished ? "已发布" : "未发布"}</strong></div>
        </div>
        <div class="paper-subcopy">${escapeHtml(latestCopy)}</div>
        <div class="button-row">
          <button class="button button-secondary" data-paper-action="view" data-paper-id="${paper.paperId}" ${disabled}>查看详情</button>
          <button class="button button-primary" data-paper-action="start" data-paper-id="${paper.paperId}" ${disabled}>进入考试</button>
        </div>
      </article>
    `;
  }).join("");
}

function hasSubjectiveQuestions(questions = []) {
  return questions.some((question) => !["SINGLE", "MULTIPLE", "JUDGE"].includes(question.questionType));
}

function buildQuestionOptions(question, currentAnswer) {
  const disabled = state.studentExam.activeExam?.submitting ? "disabled" : "";
  const options = [["A", question.optionA], ["B", question.optionB], ["C", question.optionC], ["D", question.optionD]]
    .filter(([, label]) => label != null && String(label).trim() !== "");

  if (question.questionType === "SINGLE" || question.questionType === "JUDGE") {
    return options.map(([value, label]) => `
      <label class="answer-option">
        <input type="radio" name="question-${question.questionId}" value="${value}" data-question-id="${question.questionId}" data-question-type="${question.questionType}" ${currentAnswer === value ? "checked" : ""} ${disabled} />
        <span>${value}. ${escapeHtml(label)}</span>
      </label>
    `).join("");
  }
  if (question.questionType === "MULTIPLE") {
    return options.map(([value, label]) => `
      <label class="answer-option">
        <input type="checkbox" value="${value}" data-question-id="${question.questionId}" data-question-type="${question.questionType}" ${currentAnswer.includes(value) ? "checked" : ""} ${disabled} />
        <span>${value}. ${escapeHtml(label)}</span>
      </label>
    `).join("");
  }
  return `
    <label class="field">
      <span>作答内容</span>
      <textarea class="exam-textarea" data-question-id="${question.questionId}" data-question-type="${question.questionType}" placeholder="请输入你的答案" ${disabled}>${escapeHtml(currentAnswer)}</textarea>
    </label>
  `;
}

function renderStudentExamWorkspace() {
  if (!state.currentSession) {
    studentExamWorkspace.className = "empty-state";
    studentExamWorkspace.textContent = "登录学生账号后可在这里查看试卷详情与答题。";
    return;
  }
  if (state.currentSession.data.user.role !== "STUDENT") {
    studentExamWorkspace.className = "empty-state";
    studentExamWorkspace.textContent = "教师账号暂不显示学生考试工作区。";
    return;
  }
  if (state.studentExam.activeExam) {
    const exam = state.studentExam.activeExam;
    const question = exam.questions[exam.currentQuestionIndex];
    const currentAnswer = String(exam.answers[String(question.questionId)] || "");
    const questionNav = exam.questions.map((item, index) => {
      const current = index === exam.currentQuestionIndex ? " active" : "";
      const answered = exam.answers[String(item.questionId)] ? " answered" : "";
      return `<button type="button" class="question-jump${current}${answered}" data-workspace-action="jump-question" data-question-index="${index}">${index + 1}</button>`;
    }).join("");

    studentExamWorkspace.className = "exam-workspace-shell";
    studentExamWorkspace.innerHTML = `
      <article class="workspace-card active-exam-card">
        <div class="workspace-header">
          <div>
            <h4>正在考试：${escapeHtml(exam.paper.paperName)}</h4>
            <p>请在倒计时结束前完成答题并提交试卷。</p>
          </div>
          <span class="timer-badge ${exam.remainingSeconds <= 300 ? "danger" : ""}">剩余时间 ${formatSeconds(exam.remainingSeconds)}</span>
        </div>
        <div class="workspace-metric-grid">
          <div class="workspace-metric"><span>当前题号</span><strong>第 ${exam.currentQuestionIndex + 1} / ${exam.questions.length} 题</strong></div>
          <div class="workspace-metric"><span>已作答</span><strong>${countAnsweredQuestions()} / ${exam.questions.length}</strong></div>
          <div class="workspace-metric"><span>试卷总分</span><strong>${escapeHtml(exam.paper.totalScore)}</strong></div>
          <div class="workspace-metric"><span>及格线</span><strong>${escapeHtml(exam.paper.passScore ?? "暂无")}</strong></div>
        </div>
        <div class="question-nav-strip">${questionNav}</div>
        <div class="question-card">
          <div class="question-card-head">
            <span class="mini-pill">${escapeHtml(localizeQuestionType(question.questionType))}</span>
            <span class="mini-pill">${escapeHtml(question.score)} 分</span>
          </div>
          <h5>第 ${exam.currentQuestionIndex + 1} 题</h5>
          <div class="question-content">${escapeHtml(question.content || "暂无题目内容").replace(/\r?\n/g, "<br />")}</div>
          <div class="answer-options">${buildQuestionOptions(question, currentAnswer)}</div>
        </div>
        <div class="button-row">
          <button class="button button-secondary" data-workspace-action="prev-question" ${exam.currentQuestionIndex === 0 || exam.submitting ? "disabled" : ""}>上一题</button>
          <button class="button button-secondary" data-workspace-action="next-question" ${exam.currentQuestionIndex === exam.questions.length - 1 || exam.submitting ? "disabled" : ""}>下一题</button>
          <button class="button button-primary" data-workspace-action="submit-exam" ${exam.submitting ? "disabled" : ""}>${exam.submitting ? "正在提交..." : "提交试卷"}</button>
        </div>
      </article>
    `;
    return;
  }

  if (state.studentExam.loadingPaperId && state.studentExam.loadingPaperId === state.studentExam.selectedPaperId) {
    studentExamWorkspace.className = "empty-state";
    studentExamWorkspace.textContent = "正在加载试卷详情，请稍候。";
    return;
  }

  const detail = getSelectedPaperDetail();
  if (!detail) {
    studentExamWorkspace.className = "empty-state";
    studentExamWorkspace.textContent = "请选择一张试卷查看详情。";
    return;
  }

  const paper = detail.paper;
  const warning = hasSubjectiveQuestions(detail.questions)
    ? '<div class="warning-note">当前试卷包含主观题或应用题。Electron 已支持录入答案，但底层旧版自动判分逻辑仍主要面向客观题，请留意判分结果。</div>'
    : "";
  const description = paper.description
    ? escapeHtml(paper.description).replace(/\r?\n/g, "<br />")
    : "暂无试卷说明。";
  studentExamWorkspace.className = "exam-workspace-shell";
  studentExamWorkspace.innerHTML = `
    <article class="workspace-card">
      <div class="workspace-header">
        <div>
          <h4>${escapeHtml(paper.paperName)}</h4>
          <p>请先确认试卷信息，再决定是否进入考试。</p>
        </div>
        <span class="mini-pill">${paper.isPublished ? "可考试卷" : "未发布"}</span>
      </div>
      <div class="workspace-metric-grid">
        <div class="workspace-metric"><span>科目</span><strong>${escapeHtml(paper.subject)}</strong></div>
        <div class="workspace-metric"><span>时长</span><strong>${escapeHtml(paper.duration)} 分钟</strong></div>
        <div class="workspace-metric"><span>总分</span><strong>${escapeHtml(paper.totalScore)}</strong></div>
        <div class="workspace-metric"><span>及格线</span><strong>${escapeHtml(paper.passScore ?? "暂无")}</strong></div>
      </div>
      <div class="question-type-strip">
        <span class="mini-pill">单选 ${escapeHtml(paper.singleCount ?? 0)}</span>
        <span class="mini-pill">多选 ${escapeHtml(paper.multipleCount ?? 0)}</span>
        <span class="mini-pill">判断 ${escapeHtml(paper.judgeCount ?? 0)}</span>
        <span class="mini-pill">填空 ${escapeHtml(paper.blankCount ?? 0)}</span>
      </div>
      ${warning}
      <div class="detail-copy">${description}</div>
      <div class="button-row"><button class="button button-primary" data-workspace-action="start-selected-exam">进入考试</button></div>
    </article>
  `;
}

function renderStudentExamCenter() {
  reloadStudentPapersButton.disabled = !state.currentSession || state.currentSession.data.user.role !== "STUDENT" || state.studentExam.loading || Boolean(state.studentExam.activeExam);
  renderStudentExamSummary();
  renderStudentPaperList();
  renderStudentExamWorkspace();
  renderWorkspaceHub();
}

function resetStudentRecordState({ keepSelection = false } = {}) {
  state.studentRecords.loading = false;
  state.studentRecords.loadingRecordId = null;
  state.studentRecords.summary = null;
  state.studentRecords.records = [];
  if (!keepSelection) {
    state.studentRecords.selectedRecordId = null;
    state.studentRecords.recordDetails = {};
  }
}

function getSelectedRecordDetail() {
  const recordId = state.studentRecords.selectedRecordId;
  return recordId ? state.studentRecords.recordDetails[recordId] || null : null;
}

function formatMultilineCopy(value, fallback = "暂无") {
  const content = String(value || "").trim();
  return escapeHtml(content || fallback).replace(/\r?\n/g, "<br />");
}

function formatScoreCopy(score, totalScore) {
  if (score == null && totalScore == null) {
    return "暂无";
  }
  if (totalScore == null) {
    return String(score ?? "暂无");
  }
  return `${score ?? "暂无"} / ${totalScore}`;
}

function hasSubjectiveAnswerRecords(answers = []) {
  return answers.some((answer) => !["SINGLE", "MULTIPLE", "JUDGE"].includes(answer.questionType));
}

function renderStudentRecordSummary() {
  studentRecordSummary.innerHTML = "";
  if (!state.currentSession || state.currentSession.data.user.role !== "STUDENT") {
    return;
  }

  const summary = state.studentRecords.summary || {};
  [
    ["recordCount", summary.recordCount ?? 0],
    ["submittedCount", summary.submittedCount ?? 0],
    ["averageScore", summary.averageScore ?? 0]
  ].forEach(([key, value]) => {
    studentRecordSummary.appendChild(buildStatusCard(summaryLabels[key], true, String(value)));
  });
}

function renderStudentRecordList() {
  if (!state.currentSession) {
    studentRecordList.className = "empty-state";
    studentRecordList.textContent = "登录学生账号后可查看历史成绩记录。";
    return;
  }
  if (state.currentSession.data.user.role !== "STUDENT") {
    studentRecordList.className = "empty-state";
    studentRecordList.textContent = "当前登录为教师账号，学生成绩中心仅对学生开放。";
    return;
  }
  if (state.studentRecords.loading && !state.studentRecords.records.length) {
    studentRecordList.className = "empty-state";
    studentRecordList.textContent = "正在加载历史成绩，请稍候。";
    return;
  }
  if (!state.studentRecords.records.length) {
    studentRecordList.className = "empty-state";
    studentRecordList.textContent = "当前还没有考试记录。";
    return;
  }

  studentRecordList.className = "record-card-list";
  studentRecordList.innerHTML = state.studentRecords.records.map((record) => {
    const selected = record.recordId === state.studentRecords.selectedRecordId ? " selected" : "";
    const loading = record.recordId === state.studentRecords.loadingRecordId;
    const timeCopy = formatDisplayTime(record.submitTime || record.startTime);
    return `
      <article class="exam-paper-card${selected}">
        <div class="paper-card-head">
          <div>
            <h4>${escapeHtml(record.paperName || "未命名试卷")}</h4>
            <p>${escapeHtml(localizeExamStatus(record.status))} · ${escapeHtml(timeCopy)}</p>
          </div>
          <span class="mini-pill">${escapeHtml(formatScoreCopy(record.score, record.totalScore))}</span>
        </div>
        <div class="paper-meta-grid">
          <div><span>答对题数</span><strong>${escapeHtml(record.correctCount ?? 0)}</strong></div>
          <div><span>答错题数</span><strong>${escapeHtml(record.wrongCount ?? 0)}</strong></div>
          <div><span>作答时长</span><strong>${escapeHtml(formatDurationCopy(record.durationSeconds))}</strong></div>
        </div>
        <div class="paper-subcopy">开始时间：${escapeHtml(formatDisplayTime(record.startTime))}</div>
        <div class="button-row">
          <button class="button button-secondary" data-record-action="view" data-record-id="${record.recordId}" ${loading ? "disabled" : ""}>${loading ? "正在加载..." : "查看详情"}</button>
        </div>
      </article>
    `;
  }).join("");
}

function renderStudentRecordWorkspace() {
  if (!state.currentSession) {
    studentRecordWorkspace.className = "empty-state";
    studentRecordWorkspace.textContent = "登录学生账号后可在这里查看成绩详情和答题回顾。";
    return;
  }
  if (state.currentSession.data.user.role !== "STUDENT") {
    studentRecordWorkspace.className = "empty-state";
    studentRecordWorkspace.textContent = "教师账号暂不显示学生成绩详情工作区。";
    return;
  }
  if (state.studentRecords.loadingRecordId && state.studentRecords.loadingRecordId === state.studentRecords.selectedRecordId) {
    studentRecordWorkspace.className = "empty-state";
    studentRecordWorkspace.textContent = "正在加载成绩详情，请稍候。";
    return;
  }

  const detail = getSelectedRecordDetail();
  if (!detail) {
    studentRecordWorkspace.className = "empty-state";
    studentRecordWorkspace.textContent = state.studentRecords.records.length
      ? "请选择一条成绩记录查看详情。"
      : "当前还没有可查看的成绩详情。";
    return;
  }

  const record = detail.record || {};
  const answers = detail.answers || [];
  const outcomeCopy = ["SUBMITTED", "TIMEOUT"].includes(record.status)
    ? (record.passed ? "通过" : "未通过")
    : "待完成";
  const reviewCards = answers.length
    ? answers.map((answer) => {
      const answered = String(answer.studentAnswer || "").trim().length > 0;
      const cardState = answered ? (answer.isCorrect ? " correct" : " wrong") : " empty";
      const stateCopy = answered ? (answer.isCorrect ? "作答正确" : "作答有误") : "未作答";
      return `
        <article class="answer-review-card${cardState}">
          <div class="answer-review-head">
            <div>
              <h5>第 ${escapeHtml(answer.position ?? "-")} 题</h5>
              <p>${escapeHtml(localizeQuestionType(answer.questionType))}</p>
            </div>
            <span class="mini-pill">${escapeHtml(stateCopy)}</span>
          </div>
          <div class="question-content">${formatMultilineCopy(answer.content, "暂无题目内容")}</div>
          <div class="answer-review-grid">
            <div class="answer-review-block">
              <strong>你的答案</strong>
              <div>${formatMultilineCopy(answer.studentAnswer, "未作答")}</div>
            </div>
            <div class="answer-review-block">
              <strong>参考答案</strong>
              <div>${formatMultilineCopy(answer.correctAnswer, "暂无")}</div>
            </div>
          </div>
          <div class="answer-review-grid">
            <div class="answer-review-block">
              <strong>本题得分</strong>
              <div>${escapeHtml(answer.score ?? 0)} 分</div>
            </div>
            <div class="answer-review-block">
              <strong>题目解析</strong>
              <div>${formatMultilineCopy(answer.analysis, "暂无解析")}</div>
            </div>
          </div>
        </article>
      `;
    }).join("")
    : '<div class="empty-state">当前记录还没有逐题作答明细。</div>';

  const warning = hasSubjectiveAnswerRecords(answers)
    ? '<div class="warning-note">当前记录包含主观题或应用题。Electron 已支持查看作答与得分，但底层仍沿用旧版 Java 判分逻辑，请结合教师端复核结果。</div>'
    : "";

  studentRecordWorkspace.className = "exam-workspace-shell";
  studentRecordWorkspace.innerHTML = `
    <article class="workspace-card">
      <div class="workspace-header">
        <div>
          <h4>${escapeHtml(record.paperName || "未命名试卷")}</h4>
          <p>在 Electron 中查看这次考试的成绩汇总、作答明细和逐题判分结果。</p>
        </div>
        <span class="mini-pill">${escapeHtml(localizeExamStatus(record.status))}</span>
      </div>
      <div class="workspace-metric-grid">
        <div class="workspace-metric"><span>考试成绩</span><strong>${escapeHtml(formatScoreCopy(record.score, record.totalScore))}</strong></div>
        <div class="workspace-metric"><span>及格线</span><strong>${escapeHtml(record.passScore ?? "暂无")}</strong></div>
        <div class="workspace-metric"><span>考试结果</span><strong>${outcomeCopy}</strong></div>
        <div class="workspace-metric"><span>作答时长</span><strong>${escapeHtml(formatDurationCopy(record.durationSeconds))}</strong></div>
        <div class="workspace-metric"><span>题目总数</span><strong>${escapeHtml(record.questionCount ?? 0)}</strong></div>
        <div class="workspace-metric"><span>已作答</span><strong>${escapeHtml(record.answeredCount ?? 0)}</strong></div>
        <div class="workspace-metric"><span>答对题数</span><strong>${escapeHtml(record.correctCount ?? 0)}</strong></div>
        <div class="workspace-metric"><span>答错题数</span><strong>${escapeHtml(record.wrongCount ?? 0)}</strong></div>
      </div>
      <div class="detail-copy">
        科目：${escapeHtml(record.subject || "暂无")}<br />
        开始时间：${escapeHtml(formatDisplayTime(record.startTime))}<br />
        提交时间：${escapeHtml(formatDisplayTime(record.submitTime))}
      </div>
      ${warning}
      <div class="answer-review-list">${reviewCards}</div>
    </article>
  `;
}

function renderStudentRecordCenter() {
  reloadStudentRecordsButton.disabled = !state.currentSession || state.currentSession.data.user.role !== "STUDENT" || state.studentRecords.loading;
  renderStudentRecordSummary();
  renderStudentRecordList();
  renderStudentRecordWorkspace();
  renderWorkspaceHub();
}

async function loadStudentRecordDetail(recordId, { silent = false, force = false } = {}) {
  state.studentRecords.selectedRecordId = recordId;
  if (!force && state.studentRecords.recordDetails[recordId]) {
    renderStudentRecordCenter();
    return;
  }

  if (!state.currentSession || state.currentSession.data.user.role !== "STUDENT") {
    renderStudentRecordCenter();
    return;
  }

  state.studentRecords.loadingRecordId = recordId;
  renderStudentRecordCenter();
  const result = await window.desktopApi.bridgeRecordDetail({
    userId: state.currentSession.data.user.userId,
    recordId
  });
  if (!silent) {
    appendLog(result.message);
  }
  state.studentRecords.loadingRecordId = null;
  if (result.ok && result.data) {
    state.studentRecords.recordDetails[recordId] = result.data;
  } else {
    delete state.studentRecords.recordDetails[recordId];
  }
  renderStudentRecordCenter();
}

async function loadStudentRecordCenter({ preserveSelection = true } = {}) {
  if (!state.currentSession || state.currentSession.data.user.role !== "STUDENT") {
    resetStudentRecordState();
    renderStudentRecordCenter();
    return;
  }

  state.studentRecords.loading = true;
  renderStudentRecordCenter();
  const result = await window.desktopApi.bridgeStudentRecords({ userId: state.currentSession.data.user.userId });
  appendLog(result.message);
  state.studentRecords.loading = false;

  if (!result.ok || !result.data) {
    state.studentRecords.summary = null;
    state.studentRecords.records = [];
    state.studentRecords.selectedRecordId = null;
    state.studentRecords.recordDetails = {};
    renderStudentRecordCenter();
    return;
  }

  state.studentRecords.summary = result.data.summary || null;
  state.studentRecords.records = [...(result.data.records || [])].sort((left, right) => (right.recordId || 0) - (left.recordId || 0));

  const hasSelected = state.studentRecords.records.some((record) => record.recordId === state.studentRecords.selectedRecordId);
  if (!preserveSelection || !hasSelected) {
    state.studentRecords.selectedRecordId = state.studentRecords.records[0]?.recordId || null;
  }

  renderStudentRecordCenter();
  if (state.studentRecords.selectedRecordId) {
    await loadStudentRecordDetail(state.studentRecords.selectedRecordId, { silent: true });
  }
}

async function loadStudentPaperDetail(paperId, { silent = false, force = false } = {}) {
  state.studentExam.selectedPaperId = paperId;
  if (!force && state.studentExam.paperDetails[paperId]) {
    renderStudentExamCenter();
    return;
  }

  state.studentExam.loadingPaperId = paperId;
  renderStudentExamCenter();
  const result = await window.desktopApi.bridgePaperDetail({ paperId });
  if (!silent) {
    appendLog(result.message);
  }
  state.studentExam.loadingPaperId = null;
  if (result.ok && result.data) {
    state.studentExam.paperDetails[paperId] = result.data;
  }
  renderStudentExamCenter();
}

async function loadStudentPaperCenter({ preserveSelection = true } = {}) {
  if (!state.currentSession || state.currentSession.data.user.role !== "STUDENT") {
    resetStudentExamState();
    renderStudentExamCenter();
    return;
  }

  state.studentExam.loading = true;
  renderStudentExamCenter();
  const result = await window.desktopApi.bridgeStudentPapers({ userId: state.currentSession.data.user.userId });
  appendLog(result.message);
  state.studentExam.loading = false;

  if (!result.ok || !result.data) {
    state.studentExam.summary = null;
    state.studentExam.papers = [];
    state.studentExam.selectedPaperId = null;
    state.studentExam.paperDetails = {};
    renderStudentExamCenter();
    return;
  }

  state.studentExam.summary = result.data.summary || null;
  state.studentExam.papers = result.data.papers || [];

  const hasSelected = state.studentExam.papers.some((paper) => paper.paperId === state.studentExam.selectedPaperId);
  if (!preserveSelection || !hasSelected) {
    state.studentExam.selectedPaperId = state.studentExam.papers[0]?.paperId || null;
  }

  renderStudentExamCenter();
  if (!state.studentExam.activeExam && state.studentExam.selectedPaperId) {
    await loadStudentPaperDetail(state.studentExam.selectedPaperId, { silent: true });
  }
}

function startStudentExamTimer() {
  clearStudentExamTimer();
  if (!state.studentExam.activeExam) {
    return;
  }

  state.studentExam.activeExam.timerHandle = window.setInterval(async () => {
    const exam = state.studentExam.activeExam;
    if (!exam) {
      clearStudentExamTimer();
      return;
    }

    exam.remainingSeconds = Math.max(0, exam.remainingSeconds - 1);
    renderStudentExamWorkspace();
    if (exam.remainingSeconds <= 0) {
      clearStudentExamTimer();
      await submitActiveExam({ auto: true });
    }
  }, 1000);
}

async function startSelectedExam() {
  if (!state.currentSession || state.currentSession.data.user.role !== "STUDENT") {
    appendLog("请先登录学生账号。");
    return;
  }
  if (state.studentExam.activeExam) {
    appendLog("当前已有进行中的考试，请先提交试卷。");
    return;
  }

  const detail = getSelectedPaperDetail();
  if (!detail) {
    appendLog("请先选择一张试卷。");
    return;
  }

  if (!window.confirm(`确认开始《${detail.paper.paperName}》吗？开始后将创建新的考试记录。`)) {
    return;
  }

  const result = await window.desktopApi.bridgeStartExam({
    userId: state.currentSession.data.user.userId,
    paperId: detail.paper.paperId
  });
  appendLog(result.message);
  if (!result.ok) {
    return;
  }

  state.studentExam.activeExam = {
    recordId: result.data.record.recordId,
    paper: detail.paper,
    questions: detail.questions || [],
    answers: {},
    currentQuestionIndex: 0,
    remainingSeconds: (detail.paper.duration || 0) * 60,
    submitting: false,
    timerHandle: null
  };
  renderStudentExamCenter();
  startStudentExamTimer();
}

function persistActiveExamAnswer(target) {
  const exam = state.studentExam.activeExam;
  if (!exam) {
    return;
  }

  const questionId = target.dataset.questionId;
  const questionType = target.dataset.questionType;
  if (!questionId || !questionType) {
    return;
  }

  let answer = "";
  if (questionType === "MULTIPLE") {
    answer = Array.from(studentExamWorkspace.querySelectorAll(`input[type="checkbox"][data-question-id="${questionId}"]:checked`))
      .map((node) => node.value)
      .sort()
      .join("");
  } else {
    answer = target.value;
  }

  if (String(answer).trim()) {
    exam.answers[String(questionId)] = answer;
  } else {
    delete exam.answers[String(questionId)];
  }
}

async function submitActiveExam({ auto = false } = {}) {
  const exam = state.studentExam.activeExam;
  if (!exam || exam.submitting) {
    return;
  }

  if (!auto && !window.confirm(`你已作答 ${countAnsweredQuestions()}/${exam.questions.length} 题，确定要提交试卷吗？`)) {
    return;
  }

  exam.submitting = true;
  renderStudentExamWorkspace();
  clearStudentExamTimer();

  const result = await window.desktopApi.bridgeSubmitExam({
    recordId: exam.recordId,
    answers: exam.answers
  });
  appendLog(result.message);

  if (!result.ok) {
    exam.submitting = false;
    if (exam.remainingSeconds > 0) {
      startStudentExamTimer();
    }
    renderStudentExamWorkspace();
    return;
  }

  const summary = result.data?.result || {};
  window.alert(
    `交卷完成\n\n试卷：${summary.paperName || exam.paper.paperName}\n得分：${summary.score ?? "暂无"} / ${summary.totalScore ?? exam.paper.totalScore}\n及格线：${summary.passScore ?? exam.paper.passScore ?? "暂无"}\n结果：${summary.passed ? "通过" : "未通过"}`
  );

  state.studentExam.activeExam = null;
  setActiveWorkspaceTarget("student-record");
  state.studentRecords.selectedRecordId = summary.recordId || exam.recordId;
  await loadStudentPaperCenter({ preserveSelection: true });
  await loadStudentRecordCenter({ preserveSelection: true });
  navigateToWorkspaceTarget("student-record", { behavior: "smooth" });
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
  syncWorkspaceSections();
  renderTeacherImportCenter();
  renderTeacherPaperCenter();
  renderTeacherStudentCenter();
  renderStudentExamCenter();
  renderStudentRecordCenter();

  const buildBusy = status.compileTaskRunning || status.packageTaskRunning;
  compileJavaButton.disabled = buildBusy;
  buildLegacyButton.disabled = buildBusy;
  authForm.querySelector("button[type='submit']").disabled = buildBusy;
  authLogoutButton.disabled = !state.currentSession || Boolean(state.studentExam.activeExam);
}

async function pickAndLaunch(role) {
  const selection = await window.desktopApi.pickLegacyArtifact();
  if (selection.canceled) {
    appendLog(`已取消：未选择${localizeRole(role)}的 JAR 文件。`);
    return;
  }

  const result = await window.desktopApi.launchLegacyApp({ role, artifactPath: selection.path });
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
pickStudentButton.addEventListener("click", async () => pickAndLaunch("student"));
launchTeacherButton.addEventListener("click", async () => {
  const result = await window.desktopApi.launchLegacyApp({ role: "teacher" });
  appendLog(result.message);
  await refreshStatus();
});
pickTeacherButton.addEventListener("click", async () => pickAndLaunch("teacher"));

openProjectButton.addEventListener("click", async () => appendLog(`已打开：${(await window.desktopApi.openTarget("projectRoot")).path}`));
openDesktopButton.addEventListener("click", async () => appendLog(`已打开：${(await window.desktopApi.openTarget("desktopRoot")).path}`));
openTargetButton.addEventListener("click", async () => appendLog(`已打开：${(await window.desktopApi.openTarget("targetDir")).path}`));
openDbConfigButton.addEventListener("click", async () => appendLog(`已打开：${(await window.desktopApi.openTarget("dbConfig")).path}`));
workspaceHubActions.addEventListener("click", (event) => {
  const button = event.target.closest("[data-workspace-target]");
  if (!button) {
    return;
  }

  navigateToWorkspaceTarget(button.dataset.workspaceTarget);
});
reloadStudentPapersButton.addEventListener("click", async () => {
  setActiveWorkspaceTarget("student-exam");
  await loadStudentPaperCenter({ preserveSelection: true });
});
reloadTeacherPapersButton.addEventListener("click", async () => {
  setActiveWorkspaceTarget("teacher-paper");
  await loadTeacherPaperCenter({ preserveSelection: true });
});
pickTeacherImportFileButton.addEventListener("click", async () => {
  setActiveWorkspaceTarget("teacher-import");
  await chooseTeacherImportFile();
});
clearTeacherImportFileButton.addEventListener("click", () => {
  setActiveWorkspaceTarget("teacher-import");
  clearTeacherImportSelection();
});
teacherImportForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setActiveWorkspaceTarget("teacher-import");
  await submitTeacherImportForm();
});
createTeacherStudentButton.addEventListener("click", () => {
  setActiveWorkspaceTarget("teacher-student");
  openTeacherStudentCreateMode();
});
reloadTeacherStudentsButton.addEventListener("click", async () => {
  setActiveWorkspaceTarget("teacher-student");
  await loadTeacherStudentCenter({ preserveSelection: true });
});
reloadStudentRecordsButton.addEventListener("click", async () => {
  setActiveWorkspaceTarget("student-record");
  await loadStudentRecordCenter({ preserveSelection: true });
});

authRoleInput.addEventListener("change", updateAccountFieldCopy);
authForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const result = await window.desktopApi.bridgeLogin({
    role: authRoleInput.value,
    realName: authNameInput.value.trim(),
    account: isTeacherRole() ? "" : authAccountInput.value.trim(),
    password: authPasswordInput.value
  });
  appendLog(result.message);

  if (!result.ok) {
    state.currentSession = null;
    setActiveWorkspaceTarget("login");
    resetDashboard(result.message);
    resetTeacherImportState();
    resetTeacherPaperState();
    resetTeacherStudentState();
    resetStudentExamState();
    resetStudentRecordState();
    renderSession();
    syncWorkspaceSections();
    renderTeacherImportCenter();
    renderTeacherPaperCenter();
    renderTeacherStudentCenter();
    renderStudentExamCenter();
    renderStudentRecordCenter();
    await refreshStatus();
    return;
  }

  state.currentSession = result;
  setActiveWorkspaceTarget("workspace-hub");
  authPasswordInput.value = "";
  renderSession();
  syncWorkspaceSections();
  await loadOverview();
  await loadTeacherPaperCenter({ preserveSelection: false });
  await loadTeacherStudentCenter({ preserveSelection: false });
  await loadStudentPaperCenter({ preserveSelection: false });
  await loadStudentRecordCenter({ preserveSelection: false });
  await refreshStatus();
  navigateToWorkspaceTarget("workspace-hub", { behavior: "smooth" });
});

authLogoutButton.addEventListener("click", async () => {
  if (state.studentExam.activeExam) {
    appendLog("当前正在考试，请先提交试卷后再退出登录。");
    return;
  }
  state.currentSession = null;
  setActiveWorkspaceTarget("login");
  resetDashboard();
  resetTeacherImportState();
  resetTeacherPaperState();
  resetTeacherStudentState();
  resetStudentExamState();
  resetStudentRecordState();
  renderSession();
  syncWorkspaceSections();
  renderTeacherImportCenter();
  renderTeacherPaperCenter();
  renderTeacherStudentCenter();
  renderStudentExamCenter();
  renderStudentRecordCenter();
  appendLog("Electron 会话已清除。");
  await refreshStatus();
  navigateToWorkspaceTarget("login", { behavior: "smooth" });
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

studentPaperList.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-paper-action]");
  if (!button) {
    return;
  }
  setActiveWorkspaceTarget("student-exam");
  const paperId = Number(button.dataset.paperId);
  if (!paperId) {
    return;
  }
  if (button.dataset.paperAction === "view") {
    await loadStudentPaperDetail(paperId);
  } else {
    await loadStudentPaperDetail(paperId, { silent: true });
    await startSelectedExam();
  }
});

studentRecordList.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-record-action]");
  if (!button) {
    return;
  }
  setActiveWorkspaceTarget("student-record");

  const recordId = Number(button.dataset.recordId);
  if (!recordId) {
    return;
  }

  await loadStudentRecordDetail(recordId);
});

teacherStudentList.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-teacher-student-action]");
  if (!button) {
    return;
  }
  setActiveWorkspaceTarget("teacher-student");

  const studentId = Number(button.dataset.studentId);
  if (!studentId) {
    return;
  }

  switch (button.dataset.teacherStudentAction) {
    case "view":
      await loadTeacherStudentDetail(studentId);
      break;
    case "delete":
      await deleteTeacherStudent(studentId);
      break;
    default:
      break;
  }
});

teacherPaperList.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-teacher-paper-action]");
  if (!button) {
    return;
  }
  setActiveWorkspaceTarget("teacher-paper");

  const paperId = Number(button.dataset.paperId);
  if (!paperId) {
    return;
  }

  switch (button.dataset.teacherPaperAction) {
    case "view":
      await loadTeacherPaperDetail(paperId);
      break;
    case "toggle-publish":
      await toggleTeacherPaperPublish(paperId);
      break;
    case "delete":
      await deleteTeacherPaper(paperId);
      break;
    default:
      break;
  }
});

teacherPaperWorkspace.addEventListener("submit", async (event) => {
  const form = event.target.closest("#teacher-paper-form");
  if (!form) {
    return;
  }
  event.preventDefault();
  setActiveWorkspaceTarget("teacher-paper");
  await saveTeacherPaper(form);
});

teacherStudentWorkspace.addEventListener("submit", async (event) => {
  const form = event.target.closest("#teacher-student-form");
  if (!form) {
    return;
  }
  event.preventDefault();
  setActiveWorkspaceTarget("teacher-student");
  await saveTeacherStudent(form);
});

teacherStudentWorkspace.addEventListener("click", async (event) => {
  const recordTrigger = event.target.closest("[data-teacher-record-action]");
  if (recordTrigger) {
    setActiveWorkspaceTarget("teacher-student");
    const recordId = Number(recordTrigger.dataset.recordId);
    if (recordId) {
      await loadTeacherStudentRecordDetail(recordId);
    }
    return;
  }

  const button = event.target.closest("[data-teacher-student-workspace-action]");
  if (!button) {
    return;
  }
  setActiveWorkspaceTarget("teacher-student");

  switch (button.dataset.teacherStudentWorkspaceAction) {
    case "cancel-create":
      state.teacherStudents.createMode = false;
      if (state.teacherStudents.students[0]?.userId && !state.teacherStudents.selectedStudentId) {
        state.teacherStudents.selectedStudentId = state.teacherStudents.students[0].userId;
      }
      renderTeacherStudentCenter();
      if (state.teacherStudents.selectedStudentId) {
        await loadTeacherStudentDetail(state.teacherStudents.selectedStudentId, { silent: true });
      }
      break;
    case "new-student":
      openTeacherStudentCreateMode();
      break;
    case "delete-student":
      if (state.teacherStudents.selectedStudentId) {
        await deleteTeacherStudent(state.teacherStudents.selectedStudentId);
      }
      break;
    default:
      break;
  }
});

teacherPaperWorkspace.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-teacher-workspace-action]");
  if (!button) {
    return;
  }
  setActiveWorkspaceTarget("teacher-paper");

  const paperId = state.teacherPapers.selectedPaperId;
  if (!paperId) {
    return;
  }

  switch (button.dataset.teacherWorkspaceAction) {
    case "toggle-publish":
      await toggleTeacherPaperPublish(paperId);
      break;
    case "delete":
      await deleteTeacherPaper(paperId);
      break;
    default:
      break;
  }
});

studentExamWorkspace.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-workspace-action]");
  if (!button) {
    return;
  }
  setActiveWorkspaceTarget("student-exam");

  const exam = state.studentExam.activeExam;
  switch (button.dataset.workspaceAction) {
    case "start-selected-exam":
      await startSelectedExam();
      break;
    case "prev-question":
      exam.currentQuestionIndex = Math.max(0, exam.currentQuestionIndex - 1);
      renderStudentExamWorkspace();
      break;
    case "next-question":
      exam.currentQuestionIndex = Math.min(exam.questions.length - 1, exam.currentQuestionIndex + 1);
      renderStudentExamWorkspace();
      break;
    case "jump-question":
      exam.currentQuestionIndex = Number(button.dataset.questionIndex) || 0;
      renderStudentExamWorkspace();
      break;
    case "submit-exam":
      await submitActiveExam();
      break;
    default:
      break;
  }
});

studentExamWorkspace.addEventListener("change", (event) => {
  setActiveWorkspaceTarget("student-exam");
  const target = event.target;
  if (!target.matches("[data-question-id]")) {
    return;
  }
  persistActiveExamAnswer(target);
  if (target.type !== "textarea") {
    renderStudentExamWorkspace();
  }
});

studentExamWorkspace.addEventListener("input", (event) => {
  setActiveWorkspaceTarget("student-exam");
  const target = event.target;
  if (target.matches("textarea[data-question-id]")) {
    persistActiveExamAnswer(target);
  }
});

window.desktopApi.onDesktopEvent(async (payload) => {
  if (payload.text) {
    appendLog(payload.text);
  }
  if (["db-config-saved", "compile-started", "compile-finished", "package-started", "package-finished", "legacy-started", "legacy-exited", "legacy-error"].includes(payload.type)) {
    await refreshStatus();
  }
});

teacherImportSubjectInput.innerHTML = buildTeacherSubjectOptions(teacherSubjectOptions[0]);
resetTeacherImportForm();
updateAccountFieldCopy();
resetDashboard();
renderTeacherImportCenter();
renderTeacherPaperCenter();
renderTeacherStudentCenter();
renderStudentExamCenter();
renderStudentRecordCenter();
refreshStatus();
