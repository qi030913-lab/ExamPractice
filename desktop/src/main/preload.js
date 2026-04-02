const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("desktopApi", {
  getApiBaseUrl: () => "http://127.0.0.1:8080/api",
  saveTextFile: (payload) => ipcRenderer.invoke("desktop:save-text-file", payload)
});
