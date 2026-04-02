import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";

export default defineConfig({
  plugins: [vue()],
  root: path.resolve("src/renderer-vue"),
  publicDir: path.resolve("src/renderer-vue/public"),
  server: {
    host: "127.0.0.1",
    port: 5173,
    strictPort: true
  },
  resolve: {
    alias: {
      "@": path.resolve("src/renderer-vue/src")
    }
  },
  build: {
    outDir: path.resolve("dist/renderer"),
    emptyOutDir: true
  }
});
