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
  bridgeTeacherPapers: (payload) => ipcRenderer.invoke("desktop:bridge-teacher-papers", payload),
  bridgeTeacherStudents: (payload) => ipcRenderer.invoke("desktop:bridge-teacher-students", payload),
  bridgePaperDetail: (payload) => ipcRenderer.invoke("desktop:bridge-paper-detail", payload),
  bridgeRecordDetail: (payload) => ipcRenderer.invoke("desktop:bridge-record-detail", payload),
  bridgeTeacherRecordDetail: (payload) => ipcRenderer.invoke("desktop:bridge-teacher-record-detail", payload),
  bridgeTeacherStudentDetail: (payload) => ipcRenderer.invoke("desktop:bridge-teacher-student-detail", payload),
  bridgeTogglePaperPublish: (payload) => ipcRenderer.invoke("desktop:bridge-toggle-paper-publish", payload),
  bridgeDeletePaper: (payload) => ipcRenderer.invoke("desktop:bridge-delete-paper", payload),
  bridgeUpdatePaper: (payload) => ipcRenderer.invoke("desktop:bridge-update-paper", payload),
  bridgeCreateStudent: (payload) => ipcRenderer.invoke("desktop:bridge-create-student", payload),
  bridgeUpdateStudent: (payload) => ipcRenderer.invoke("desktop:bridge-update-student", payload),
  bridgeDeleteStudent: (payload) => ipcRenderer.invoke("desktop:bridge-delete-student", payload),
  bridgeImportPaper: (payload) => ipcRenderer.invoke("desktop:bridge-import-paper", payload),
  bridgeStartExam: (payload) => ipcRenderer.invoke("desktop:bridge-start-exam", payload),
  bridgeSubmitExam: (payload) => ipcRenderer.invoke("desktop:bridge-submit-exam", payload),
  getApiBaseUrl: () => "http://127.0.0.1:8080/api",
  compileJava: () => ipcRenderer.invoke("desktop:compile-java"),
  buildLegacyArtifacts: () => ipcRenderer.invoke("desktop:build-legacy-artifacts"),
  pickLegacyArtifact: () => ipcRenderer.invoke("desktop:pick-legacy-artifact"),
  pickTeacherImportFile: () => ipcRenderer.invoke("desktop:pick-teacher-import-file"),
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
