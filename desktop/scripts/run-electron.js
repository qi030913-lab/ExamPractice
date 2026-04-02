const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");

function resolveElectronBinary() {
  const basePath = path.join(__dirname, "..", "node_modules", "electron", "dist");

  if (process.platform === "win32") {
    return path.join(basePath, "electron.exe");
  }

  if (process.platform === "darwin") {
    return path.join(basePath, "Electron.app", "Contents", "MacOS", "Electron");
  }

  return path.join(basePath, "electron");
}

const binaryPath = resolveElectronBinary();
if (!fs.existsSync(binaryPath)) {
  console.error("Electron binary was not found.");
  console.error(`Expected path: ${binaryPath}`);
  process.exit(1);
}

const workingDirectory = path.join(__dirname, "..");
const cliArgs = process.argv.slice(2);
const electronArgs = cliArgs.length > 0 ? cliArgs : ["."];
const childEnv = {
  ...process.env
};

delete childEnv.ELECTRON_RUN_AS_NODE;

const child = spawn(binaryPath, electronArgs, {
  cwd: workingDirectory,
  stdio: "inherit",
  env: childEnv
});

child.on("error", (error) => {
  console.error(error.message);
  process.exit(1);
});

child.on("exit", (code) => {
  process.exit(code ?? 0);
});
