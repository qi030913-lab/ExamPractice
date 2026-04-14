# Electron Desktop

This folder contains the current desktop application built with `Electron + Vue3`.

## Architecture

- `src/main`: Electron main process and preload bridge
- `src/renderer-vue`: Vue3 renderer application
- `dist/renderer`: built renderer assets

The Java backend is provided by the root project through `target/exam-desktop-api.jar`, which is started by Electron at runtime.
Database settings now come from Spring Boot configuration and environment variables such as `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `DB_DRIVER`.

## Development

```bash
cd desktop
npm install
npm run dev
```

## Production renderer build

```bash
cd desktop
npm run build:renderer
```

## Backend package

Run this from the project root to prepare the desktop backend artifact:

```bash
mvn -q -Dmaven.test.skip=true package
```
