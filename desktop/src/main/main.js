const { app, BrowserWindow, dialog, ipcMain, shell } = require("electron");
const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");

const desktopEventChannel = "desktop:event";
const projectRoot = path.resolve(__dirname, "../../..");
const legacyArtifacts = {
  student: path.join(projectRoot, "target", "exam-student.jar"),
  teacher: path.join(projectRoot, "target", "exam-teacher.jar"),
  bridge: path.join(projectRoot, "target", "exam-electron-bridge.jar"),
  studentExe: path.join(projectRoot, "target", "在线考试系统-学生端.exe"),
  teacherExe: path.join(projectRoot, "target", "在线考试系统-教师端.exe"),
  compiledClasses: path.join(projectRoot, "target", "classes"),
  dbConfig: path.join(projectRoot, "src", "main", "resources", "db.properties"),
  targetDir: path.join(projectRoot, "target")
};

let mainWindow = null;
let compileProcess = null;
let packageProcess = null;
const activeLegacyProcesses = new Map();

function getLegacyRoleLabel(role) {
  if (role === "student") {
    return "学生端";
  }

  if (role === "teacher") {
    return "教师端";
  }

  return role || "未知角色";
}

function inspectPath(targetPath) {
  const exists = fs.existsSync(targetPath);
  if (!exists) {
    return {
      exists: false,
      path: targetPath
    };
  }

  const stats = fs.statSync(targetPath);
  return {
    exists: true,
    isDirectory: stats.isDirectory(),
    size: stats.isFile() ? stats.size : null,
    modifiedAt: stats.mtime.toISOString(),
    path: targetPath
  };
}

function parsePropertiesFile(filePath) {
  if (!fs.existsSync(filePath)) {
    return {};
  }

  const content = fs.readFileSync(filePath, "utf-8");
  const lines = content.split(/\r?\n/);
  const properties = {};

  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#") || !trimmed.includes("=")) {
      continue;
    }

    const separatorIndex = trimmed.indexOf("=");
    const key = trimmed.slice(0, separatorIndex).trim();
    const value = trimmed.slice(separatorIndex + 1).trim();
    properties[key] = value;
  }

  return properties;
}

function updatePropertiesFile(filePath, updates) {
  const content = fs.existsSync(filePath) ? fs.readFileSync(filePath, "utf-8") : "";
  const lines = content ? content.split(/\r?\n/) : [];
  const pendingKeys = new Set(Object.keys(updates));
  const nextLines = lines.map((line) => {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#") || !trimmed.includes("=")) {
      return line;
    }

    const separatorIndex = line.indexOf("=");
    const key = line.slice(0, separatorIndex).trim();
    if (!pendingKeys.has(key)) {
      return line;
    }

    pendingKeys.delete(key);
    return `${key}=${updates[key]}`;
  });

  for (const key of pendingKeys) {
    nextLines.push(`${key}=${updates[key]}`);
  }

  const finalContent = `${nextLines.join("\n").replace(/\n+$/u, "")}\n`;
  fs.writeFileSync(filePath, finalContent, "utf-8");
}

function runVersionCommand(command, args) {
  return new Promise((resolve) => {
    const child = spawn(command, args, {
      cwd: projectRoot,
      windowsHide: true,
      shell: process.platform === "win32"
    });

    let output = "";

    child.stdout.on("data", (chunk) => {
      output += chunk.toString();
    });

    child.stderr.on("data", (chunk) => {
      output += chunk.toString();
    });

    child.on("error", (error) => {
      resolve({
        available: false,
        output: error.message
      });
    });

    child.on("close", (code) => {
      resolve({
        available: code === 0,
        output: output.trim()
      });
    });
  });
}

function broadcastDesktopEvent(payload) {
  for (const window of BrowserWindow.getAllWindows()) {
    if (!window.isDestroyed()) {
      window.webContents.send(desktopEventChannel, payload);
    }
  }
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1340,
    height: 860,
    minWidth: 1160,
    minHeight: 760,
    backgroundColor: "#f6f1e8",
    title: "在线考试系统桌面工作台",
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  const rendererUrl = process.env.ELECTRON_RENDERER_URL;
  if (rendererUrl) {
    mainWindow.loadURL(rendererUrl);
    return;
  }

  const builtRendererIndex = path.join(projectRoot, "desktop", "dist", "renderer", "index.html");
  if (fs.existsSync(builtRendererIndex)) {
    mainWindow.loadFile(builtRendererIndex);
    return;
  }

  mainWindow.loadFile(path.join(__dirname, "../renderer/index.html"));
}

async function collectStatus() {
  const [java, maven] = await Promise.all([
    runVersionCommand("java", ["-version"]),
    runVersionCommand("mvn", ["-v"])
  ]);

  return {
    platform: process.platform,
    projectRoot,
    tools: {
      node: {
        available: true,
        output: process.version
      },
      java,
      maven
    },
    artifacts: {
      studentJar: inspectPath(legacyArtifacts.student),
      teacherJar: inspectPath(legacyArtifacts.teacher),
      bridgeJar: inspectPath(legacyArtifacts.bridge),
      studentExe: inspectPath(legacyArtifacts.studentExe),
      teacherExe: inspectPath(legacyArtifacts.teacherExe),
      compiledClasses: inspectPath(legacyArtifacts.compiledClasses),
      dbConfig: inspectPath(legacyArtifacts.dbConfig),
      targetDir: inspectPath(legacyArtifacts.targetDir)
    },
    runningLegacyApps: Array.from(activeLegacyProcesses.entries()).map(([role, info]) => ({
      role,
      pid: info.pid,
      artifactPath: info.artifactPath,
      startedAt: info.startedAt
    })),
    compileTaskRunning: Boolean(compileProcess),
    packageTaskRunning: Boolean(packageProcess)
  };
}

function getDatabaseConfig() {
  const raw = parsePropertiesFile(legacyArtifacts.dbConfig);
  return {
    url: raw["db.url"] || "",
    username: raw["db.username"] || "",
    password: raw["db.password"] || "",
    driver: raw["db.driver"] || ""
  };
}

function saveDatabaseConfig(payload) {
  const updates = {
    "db.url": (payload.url || "").trim(),
    "db.username": (payload.username || "").trim(),
    "db.password": payload.password || "",
    "db.driver": (payload.driver || "").trim()
  };

  updatePropertiesFile(legacyArtifacts.dbConfig, updates);

  broadcastDesktopEvent({
    type: "db-config-saved",
    text: "数据库配置已保存到 src/main/resources/db.properties"
  });

  return {
    ok: true,
    message: "数据库配置保存成功。",
    config: getDatabaseConfig()
  };
}

function buildJavaRuntimeEnv() {
  const config = getDatabaseConfig();
  return {
    ...process.env,
    DB_URL: config.url,
    DB_USERNAME: config.username,
    DB_PASSWORD: config.password,
    DB_DRIVER: config.driver
  };
}

function summarizeProcessError(stderr, fallbackMessage) {
  const lines = (stderr || "")
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);

  if (!lines.length) {
    return fallbackMessage;
  }

  const preferredPatterns = [
    /Access denied/i,
    /Communications link failure/i,
    /Unknown database/i,
    /Unknown host/i,
    /Exception/i,
    /ERROR/i
  ];

  for (const pattern of preferredPatterns) {
    const matchedLine = lines.find((line) => pattern.test(line));
    if (matchedLine) {
      return matchedLine;
    }
  }

  return lines[lines.length - 1];
}

function startCompileTask() {
  if (compileProcess || packageProcess) {
    return {
      ok: false,
      message: "已有其他 Java 构建任务正在运行。"
    };
  }

  compileProcess = spawn("mvn", ["-q", "-DskipTests", "compile"], {
    cwd: projectRoot,
    windowsHide: true,
    shell: process.platform === "win32"
  });

  broadcastDesktopEvent({
    type: "compile-started",
    text: "已开始执行：mvn -q -DskipTests compile"
  });

  compileProcess.stdout.on("data", (chunk) => {
    broadcastDesktopEvent({
      type: "compile-log",
      stream: "stdout",
      text: chunk.toString()
    });
  });

  compileProcess.stderr.on("data", (chunk) => {
    broadcastDesktopEvent({
      type: "compile-log",
      stream: "stderr",
      text: chunk.toString()
    });
  });

  compileProcess.on("error", (error) => {
    broadcastDesktopEvent({
      type: "compile-error",
      text: error.message
    });
    compileProcess = null;
  });

  compileProcess.on("close", (code) => {
    broadcastDesktopEvent({
      type: "compile-finished",
      code,
      text: code === 0 ? "Java 编译完成。" : `Java 编译失败，退出码为 ${code}。`
    });
    compileProcess = null;
  });

  return {
    ok: true,
    message: "Java 编译任务已启动。"
  };
}

function startPackageTask() {
  if (compileProcess || packageProcess) {
    return {
      ok: false,
      message: "已有其他 Java 构建任务正在运行。"
    };
  }

  packageProcess = spawn("mvn", ["-q", "-Dmaven.test.skip=true", "package"], {
    cwd: projectRoot,
    windowsHide: true,
    shell: process.platform === "win32"
  });

  broadcastDesktopEvent({
    type: "package-started",
    text: "已开始执行：mvn -q -Dmaven.test.skip=true package"
  });

  packageProcess.stdout.on("data", (chunk) => {
    broadcastDesktopEvent({
      type: "package-log",
      stream: "stdout",
      text: chunk.toString()
    });
  });

  packageProcess.stderr.on("data", (chunk) => {
    broadcastDesktopEvent({
      type: "package-log",
      stream: "stderr",
      text: chunk.toString()
    });
  });

  packageProcess.on("error", (error) => {
    broadcastDesktopEvent({
      type: "package-error",
      text: error.message
    });
    packageProcess = null;
  });

  packageProcess.on("close", (code) => {
    broadcastDesktopEvent({
      type: "package-finished",
      code,
      text: code === 0
        ? "旧版桌面端产物构建完成。"
        : `旧版桌面端产物构建失败，退出码为 ${code}。`
    });
    packageProcess = null;
  });

  return {
    ok: true,
    message: "旧版桌面端产物构建任务已启动。"
  };
}

function runBridgeCommand(args) {
  return new Promise((resolve) => {
    if (!fs.existsSync(legacyArtifacts.bridge)) {
      resolve({
        ok: false,
        message: "未找到 bridge 产物，请先构建旧版产物。"
      });
      return;
    }

    const child = spawn("java", ["-Dfile.encoding=UTF-8", "-jar", legacyArtifacts.bridge, ...args], {
      cwd: projectRoot,
      windowsHide: true,
      env: buildJavaRuntimeEnv()
    });

    let stdout = "";
    let stderr = "";

    child.stdout.on("data", (chunk) => {
      stdout += chunk.toString();
    });

    child.stderr.on("data", (chunk) => {
      stderr += chunk.toString();
    });

    child.on("error", (error) => {
      resolve({
        ok: false,
        message: error.message
      });
    });

    child.on("close", () => {
      const trimmed = stdout.trim();
      if (trimmed) {
        try {
          resolve(JSON.parse(trimmed));
          return;
        } catch (error) {
          resolve({
            ok: false,
            message: `Bridge 返回了无效 JSON：${error.message}`,
            raw: trimmed,
            stderr: stderr.trim()
          });
          return;
        }
      }

      resolve({
        ok: false,
        message: summarizeProcessError(stderr, "Bridge 没有返回内容。"),
        stderr: stderr.trim()
      });
    });
  });
}

function resolveLegacyArtifact(role, customArtifactPath) {
  if (customArtifactPath && fs.existsSync(customArtifactPath)) {
    return customArtifactPath;
  }

  if (role === "student") {
    return legacyArtifacts.student;
  }

  if (role === "teacher") {
    return legacyArtifacts.teacher;
  }

  return null;
}

function launchLegacyApp(role, customArtifactPath) {
  const artifactPath = resolveLegacyArtifact(role, customArtifactPath);

  if (!artifactPath || !fs.existsSync(artifactPath)) {
    return {
      ok: false,
      message: "未找到旧版产物，请先构建或手动选择 JAR。"
    };
  }

  const existingProcess = activeLegacyProcesses.get(role);
  if (existingProcess && existingProcess.process && !existingProcess.process.killed) {
    return {
      ok: false,
      message: `${getLegacyRoleLabel(role)}已经在运行中。`
    };
  }

  const child = spawn("java", ["-Dfile.encoding=UTF-8", "-jar", artifactPath], {
    cwd: projectRoot,
    windowsHide: false,
    env: buildJavaRuntimeEnv()
  });

  activeLegacyProcesses.set(role, {
    process: child,
    pid: child.pid,
    artifactPath,
    startedAt: new Date().toISOString()
  });

  broadcastDesktopEvent({
    type: "legacy-started",
    role,
    pid: child.pid,
    text: `已启动${getLegacyRoleLabel(role)}，运行文件：${artifactPath}`
  });

  child.stdout.on("data", (chunk) => {
    broadcastDesktopEvent({
      type: "legacy-log",
      role,
      stream: "stdout",
      text: chunk.toString()
    });
  });

  child.stderr.on("data", (chunk) => {
    broadcastDesktopEvent({
      type: "legacy-log",
      role,
      stream: "stderr",
      text: chunk.toString()
    });
  });

  child.on("error", (error) => {
    activeLegacyProcesses.delete(role);
    broadcastDesktopEvent({
      type: "legacy-error",
      role,
      text: error.message
    });
  });

  child.on("close", (code) => {
    activeLegacyProcesses.delete(role);
    broadcastDesktopEvent({
      type: "legacy-exited",
      role,
      code,
      text: `${getLegacyRoleLabel(role)}已退出，退出码为 ${code}。`
    });
  });

  return {
    ok: true,
    message: `${getLegacyRoleLabel(role)}启动成功。`,
    pid: child.pid,
    artifactPath
  };
}

async function pickLegacyArtifact() {
  const result = await dialog.showOpenDialog({
    title: "选择旧版 JAR 文件",
    properties: ["openFile"],
    filters: [
      { name: "JAR 文件", extensions: ["jar"] },
      { name: "所有文件", extensions: ["*"] }
    ]
  });

  if (result.canceled || result.filePaths.length === 0) {
    return {
      canceled: true
    };
  }

  return {
    canceled: false,
    path: result.filePaths[0]
  };
}

async function pickTeacherImportFile() {
  const result = await dialog.showOpenDialog({
    title: "选择题目导入文件",
    properties: ["openFile"],
    filters: [
      { name: "TXT 文件", extensions: ["txt"] },
      { name: "所有文件", extensions: ["*"] }
    ]
  });

  if (result.canceled || result.filePaths.length === 0) {
    return {
      canceled: true
    };
  }

  return {
    canceled: false,
    path: result.filePaths[0],
    name: path.basename(result.filePaths[0])
  };
}

function openTarget(target) {
  const targetMap = {
    projectRoot,
    dbConfig: legacyArtifacts.dbConfig,
    desktopRoot: path.join(projectRoot, "desktop"),
    targetDir: legacyArtifacts.targetDir
  };

  const targetPath = targetMap[target];
  if (!targetPath) {
    return {
      ok: false,
      message: "未知的路径目标。"
    };
  }

  shell.openPath(targetPath);
  return {
    ok: true,
    path: targetPath
  };
}

ipcMain.handle("desktop:get-status", async () => collectStatus());
ipcMain.handle("desktop:get-db-config", async () => getDatabaseConfig());
ipcMain.handle("desktop:save-db-config", async (_event, payload) => saveDatabaseConfig(payload));
ipcMain.handle("desktop:bridge-health", async () => runBridgeCommand(["health"]));
ipcMain.handle("desktop:bridge-login", async (_event, payload) => {
  return runBridgeCommand([
    "login",
    payload.role,
    payload.realName,
    payload.account,
    payload.password
  ]);
});
ipcMain.handle("desktop:bridge-overview", async (_event, payload) => {
  const command = payload.role === "TEACHER" ? "teacher-overview" : "student-overview";
  return runBridgeCommand([command, String(payload.userId)]);
});
ipcMain.handle("desktop:bridge-student-papers", async (_event, payload) => {
  return runBridgeCommand(["student-papers", String(payload.userId)]);
});
ipcMain.handle("desktop:bridge-student-records", async (_event, payload) => {
  return runBridgeCommand(["student-records", String(payload.userId)]);
});
ipcMain.handle("desktop:bridge-teacher-papers", async (_event, payload) => {
  return runBridgeCommand(["teacher-papers", String(payload.userId)]);
});
ipcMain.handle("desktop:bridge-teacher-students", async (_event, payload) => {
  return runBridgeCommand(["teacher-students", String(payload.userId)]);
});
ipcMain.handle("desktop:bridge-paper-detail", async (_event, payload) => {
  return runBridgeCommand(["paper-detail", String(payload.paperId)]);
});
ipcMain.handle("desktop:bridge-record-detail", async (_event, payload) => {
  return runBridgeCommand([
    "record-detail",
    String(payload.userId),
    String(payload.recordId)
  ]);
});
ipcMain.handle("desktop:bridge-teacher-record-detail", async (_event, payload) => {
  return runBridgeCommand([
    "teacher-record-detail",
    String(payload.userId),
    String(payload.studentId),
    String(payload.recordId)
  ]);
});
ipcMain.handle("desktop:bridge-teacher-student-detail", async (_event, payload) => {
  return runBridgeCommand([
    "teacher-student-detail",
    String(payload.userId),
    String(payload.studentId)
  ]);
});
ipcMain.handle("desktop:bridge-toggle-paper-publish", async (_event, payload) => {
  return runBridgeCommand([
    "toggle-paper-publish",
    String(payload.userId),
    String(payload.paperId)
  ]);
});
ipcMain.handle("desktop:bridge-delete-paper", async (_event, payload) => {
  return runBridgeCommand([
    "delete-paper",
    String(payload.userId),
    String(payload.paperId)
  ]);
});
ipcMain.handle("desktop:bridge-update-paper", async (_event, payload) => {
  return runBridgeCommand([
    "update-paper",
    String(payload.userId),
    String(payload.paperId),
    payload.paperName ?? "",
    payload.subject ?? "",
    String(payload.passScore ?? ""),
    String(payload.duration ?? ""),
    payload.description ?? ""
  ]);
});
ipcMain.handle("desktop:bridge-create-student", async (_event, payload) => {
  return runBridgeCommand([
    "create-student",
    String(payload.userId),
    payload.realName ?? "",
    payload.studentNumber ?? "",
    payload.password ?? "",
    payload.email ?? "",
    payload.phone ?? "",
    payload.gender ?? ""
  ]);
});
ipcMain.handle("desktop:bridge-update-student", async (_event, payload) => {
  return runBridgeCommand([
    "update-student",
    String(payload.userId),
    String(payload.studentId),
    payload.realName ?? "",
    payload.password ?? "",
    payload.email ?? "",
    payload.phone ?? "",
    payload.gender ?? ""
  ]);
});
ipcMain.handle("desktop:bridge-delete-student", async (_event, payload) => {
  return runBridgeCommand([
    "delete-student",
    String(payload.userId),
    String(payload.studentId)
  ]);
});
ipcMain.handle("desktop:bridge-import-paper", async (_event, payload) => {
  return runBridgeCommand([
    "import-paper",
    String(payload.userId),
    payload.filePath ?? "",
    payload.paperName ?? "",
    payload.subject ?? "",
    String(payload.passScore ?? ""),
    String(payload.duration ?? ""),
    payload.description ?? ""
  ]);
});
ipcMain.handle("desktop:bridge-start-exam", async (_event, payload) => {
  return runBridgeCommand([
    "start-exam",
    String(payload.userId),
    String(payload.paperId)
  ]);
});
ipcMain.handle("desktop:bridge-submit-exam", async (_event, payload) => {
  const args = ["submit-exam", String(payload.recordId)];
  const answers = payload.answers || {};

  for (const [questionId, answer] of Object.entries(answers)) {
    args.push(String(questionId), String(answer ?? ""));
  }

  return runBridgeCommand(args);
});
ipcMain.handle("desktop:compile-java", async () => startCompileTask());
ipcMain.handle("desktop:build-legacy-artifacts", async () => startPackageTask());
ipcMain.handle("desktop:pick-legacy-artifact", async () => pickLegacyArtifact());
ipcMain.handle("desktop:pick-teacher-import-file", async () => pickTeacherImportFile());
ipcMain.handle("desktop:launch-legacy-app", async (_event, payload) => {
  return launchLegacyApp(payload.role, payload.artifactPath);
});
ipcMain.handle("desktop:open-target", async (_event, target) => openTarget(target));

app.whenReady().then(() => {
  createWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
