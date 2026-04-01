const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("desktopApi", {
  getStatus: () => ipcRenderer.invoke("desktop:get-status"),
  getDbConfig: () => ipcRenderer.invoke("desktop:get-db-config"),
  saveDbConfig: (payload) => ipcRenderer.invoke("desktop:save-db-config", payload),
  bridgeHealth: () => ipcRenderer.invoke("desktop:bridge-health"),
  bridgeLogin: (payload) => ipcRenderer.invoke("desktop:bridge-login", payload),
  bridgeOverview: (payload) => ipcRenderer.invoke("desktop:bridge-overview", payload),
  compileJava: () => ipcRenderer.invoke("desktop:compile-java"),
  buildLegacyArtifacts: () => ipcRenderer.invoke("desktop:build-legacy-artifacts"),
  pickLegacyArtifact: () => ipcRenderer.invoke("desktop:pick-legacy-artifact"),
  launchLegacyApp: (payload) => ipcRenderer.invoke("desktop:launch-legacy-app", payload),
  openTarget: (target) => ipcRenderer.invoke("desktop:open-target", target),
  onDesktopEvent: (listener) => {
    const wrapped = (_event, payload) => listener(payload);
    ipcRenderer.on("desktop:event", wrapped);
    return () => {
      ipcRenderer.removeListener("desktop:event", wrapped);
    };
  }
});
