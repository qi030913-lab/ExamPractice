const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("desktopApi", {
  getStatus: () => ipcRenderer.invoke("desktop:get-status"),
  getDbConfig: () => ipcRenderer.invoke("desktop:get-db-config"),
  saveDbConfig: (payload) => ipcRenderer.invoke("desktop:save-db-config", payload),
  bridgeHealth: () => ipcRenderer.invoke("desktop:bridge-health"),
  bridgeLogin: (payload) => ipcRenderer.invoke("desktop:bridge-login", payload),
  bridgeOverview: (payload) => ipcRenderer.invoke("desktop:bridge-overview", payload),
  bridgeStudentPapers: (payload) => ipcRenderer.invoke("desktop:bridge-student-papers", payload),
  bridgeStudentRecords: (payload) => ipcRenderer.invoke("desktop:bridge-student-records", payload),
  bridgePaperDetail: (payload) => ipcRenderer.invoke("desktop:bridge-paper-detail", payload),
  bridgeRecordDetail: (payload) => ipcRenderer.invoke("desktop:bridge-record-detail", payload),
  bridgeStartExam: (payload) => ipcRenderer.invoke("desktop:bridge-start-exam", payload),
  bridgeSubmitExam: (payload) => ipcRenderer.invoke("desktop:bridge-submit-exam", payload),
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
