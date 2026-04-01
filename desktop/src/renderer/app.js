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

const reloadStudentPapersButton = document.getElementById("reload-student-papers");
const studentExamSummary = document.getElementById("student-exam-summary");
const studentPaperList = document.getElementById("student-paper-list");
const studentExamWorkspace = document.getElementById("student-exam-workspace");
const reloadStudentRecordsButton = document.getElementById("reload-student-records");
const studentRecordSummary = document.getElementById("student-record-summary");
const studentRecordList = document.getElementById("student-record-list");
const studentRecordWorkspace = document.getElementById("student-record-workspace");

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
    "Paper detail loaded.": "试卷详情加载成功。",
    "Exam record detail loaded.": "考试详情加载成功。",
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
  state.studentRecords.selectedRecordId = summary.recordId || exam.recordId;
  await loadStudentPaperCenter({ preserveSelection: true });
  await loadStudentRecordCenter({ preserveSelection: true });
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
reloadStudentPapersButton.addEventListener("click", async () => loadStudentPaperCenter({ preserveSelection: true }));
reloadStudentRecordsButton.addEventListener("click", async () => loadStudentRecordCenter({ preserveSelection: true }));

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
    resetDashboard(result.message);
    resetStudentExamState();
    resetStudentRecordState();
    renderSession();
    renderStudentExamCenter();
    renderStudentRecordCenter();
    await refreshStatus();
    return;
  }

  state.currentSession = result;
  authPasswordInput.value = "";
  renderSession();
  await loadOverview();
  await loadStudentPaperCenter({ preserveSelection: false });
  await loadStudentRecordCenter({ preserveSelection: false });
  await refreshStatus();
});

authLogoutButton.addEventListener("click", async () => {
  if (state.studentExam.activeExam) {
    appendLog("当前正在考试，请先提交试卷后再退出登录。");
    return;
  }
  state.currentSession = null;
  resetDashboard();
  resetStudentExamState();
  resetStudentRecordState();
  renderSession();
  renderStudentExamCenter();
  renderStudentRecordCenter();
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

studentPaperList.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-paper-action]");
  if (!button) {
    return;
  }
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

  const recordId = Number(button.dataset.recordId);
  if (!recordId) {
    return;
  }

  await loadStudentRecordDetail(recordId);
});

studentExamWorkspace.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-workspace-action]");
  if (!button) {
    return;
  }

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

updateAccountFieldCopy();
resetDashboard();
renderStudentExamCenter();
renderStudentRecordCenter();
refreshStatus();
