const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("desktopApi", {
  saveTextFile: (payload) => ipcRenderer.invoke("desktop:save-text-file", payload)
});
