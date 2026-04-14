const { app, BrowserWindow, dialog, ipcMain } = require("electron");
const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");
const http = require("http");

const projectRoot = path.resolve(__dirname, "../../..");
const desktopArtifacts = {
  serverHeadless: path.join(projectRoot, "target", "exam-desktop-api.jar")
};

let mainWindow = null;
let serverProcess = null;
let serverReadyPromise = null;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1340,
    height: 860,
    minWidth: 1160,
    minHeight: 760,
    backgroundColor: "#f6f1e8",
    title: "在线考试系统桌面端",
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

  const sourceRendererIndex = path.join(projectRoot, "desktop", "src", "renderer-vue", "index.html");
  if (fs.existsSync(sourceRendererIndex)) {
    mainWindow.loadFile(sourceRendererIndex);
    return;
  }

  throw new Error("未找到 Vue 桌面端入口，请先执行 npm run build:renderer 或使用 npm run dev。");
}

function waitForApiHealth(timeoutMs = 25000) {
  return new Promise((resolve) => {
    const startedAt = Date.now();

    function probe() {
      const request = http.get("http://127.0.0.1:8080/api/health", (response) => {
        if (response.statusCode && response.statusCode >= 200 && response.statusCode < 300) {
          response.resume();
          resolve(true);
          return;
        }

        response.resume();
        if (Date.now() - startedAt >= timeoutMs) {
          resolve(false);
          return;
        }
        setTimeout(probe, 600);
      });

      request.on("error", () => {
        if (Date.now() - startedAt >= timeoutMs) {
          resolve(false);
          return;
        }
        setTimeout(probe, 600);
      });

      request.setTimeout(3000, () => {
        request.destroy();
      });
    }

    probe();
  });
}

async function ensureBackendServer() {
  if (serverReadyPromise) {
    return serverReadyPromise;
  }

  serverReadyPromise = (async () => {
    const serverAlreadyReady = await waitForApiHealth(1500);
    if (serverAlreadyReady) {
      return {
        ok: true,
        reused: true
      };
    }

    if (!fs.existsSync(desktopArtifacts.serverHeadless)) {
      return {
        ok: false,
        message: "未找到 exam-desktop-api.jar，请先执行 mvn -q -Dmaven.test.skip=true package。"
      };
    }

    serverProcess = spawn("java", ["-Dfile.encoding=UTF-8", "-jar", desktopArtifacts.serverHeadless], {
      cwd: projectRoot,
      windowsHide: true,
      env: {
        ...process.env
      }
    });

    serverProcess.on("close", () => {
      serverProcess = null;
      serverReadyPromise = null;
    });

    const healthy = await waitForApiHealth();
    if (!healthy) {
      const runningProcess = serverProcess;
      if (runningProcess && !runningProcess.killed) {
        runningProcess.kill();
      }
      serverProcess = null;
      serverReadyPromise = null;
      return {
        ok: false,
        message: "Spring Boot API 启动超时，请检查数据库配置或后端日志。"
      };
    }

    return {
      ok: true,
      reused: false
    };
  })();

  const result = await serverReadyPromise;
  if (!result.ok) {
    serverReadyPromise = null;
  }
  return result;
}

async function saveTextFile(payload = {}) {
  const defaultFileName = typeof payload.defaultFileName === "string" && payload.defaultFileName.trim()
    ? payload.defaultFileName.trim()
    : "未命名.txt";
  const extension = typeof payload.extension === "string" ? payload.extension.replace(/^\./u, "").trim() : "";
  const result = await dialog.showSaveDialog({
    title: payload.title || "保存文件",
    defaultPath: defaultFileName,
    filters: Array.isArray(payload.filters) && payload.filters.length
      ? payload.filters
      : [
          { name: "文本文件", extensions: ["txt"] },
          { name: "所有文件", extensions: ["*"] }
        ]
  });

  if (result.canceled || !result.filePath) {
    return {
      canceled: true
    };
  }

  let finalPath = result.filePath;
  if (extension && !path.extname(finalPath)) {
    finalPath = `${finalPath}.${extension}`;
  }

  try {
    fs.writeFileSync(finalPath, String(payload.content ?? ""), payload.encoding || "utf-8");
    return {
      ok: true,
      canceled: false,
      path: finalPath
    };
  } catch (error) {
    return {
      ok: false,
      canceled: false,
      message: error.message
    };
  }
}

ipcMain.handle("desktop:save-text-file", async (_event, payload) => saveTextFile(payload));

app.whenReady().then(() => {
  ensureBackendServer()
    .then((result) => {
      if (!result?.ok) {
        dialog.showErrorBox("桌面端启动失败", result?.message || "Spring Boot API 未能成功启动。");
        app.quit();
        return;
      }

      createWindow();

      app.on("activate", () => {
        if (BrowserWindow.getAllWindows().length === 0) {
          createWindow();
        }
      });
    })
    .catch((error) => {
      dialog.showErrorBox("桌面端启动失败", error?.message || "Electron 初始化失败。");
      app.quit();
    });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});

app.on("before-quit", () => {
  if (serverProcess && !serverProcess.killed) {
    serverProcess.kill();
  }
});
