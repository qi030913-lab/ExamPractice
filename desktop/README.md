# Electron Migration Shell

This folder is the new desktop entry layer for the project.

## What it does now

- Creates a standalone Electron shell for the repository.
- Shows local environment and legacy artifact status.
- Runs `mvn -q -DskipTests compile` from Electron.
- Runs `mvn -q -Dmaven.test.skip=true package` from Electron to build legacy runtime artifacts.
- Reads and updates `src/main/resources/db.properties` from Electron.
- Logs in and loads role-based overview data through `exam-electron-bridge.jar`.
- Launches legacy student or teacher JARs when those artifacts exist.
- Keeps the migration work isolated from the current Java Swing code.

## What it does not do yet

- It does not replace the Swing UI yet.
- It does not expose Java services as a local API yet.
- It does not package the full Electron app yet.

## Run

1. Open a terminal in `desktop`.
2. Run `npm install`.
3. Run `npm run dev`.
4. Use `Build JARs and EXEs` in the Electron workbench when you need fresh legacy artifacts.
5. Update `db.properties` from the Electron screen before using bridge-backed login if your local MySQL password differs.

## Migration plan

1. Electron becomes the new desktop shell and launcher.
2. Java business logic is exposed through the Electron bridge jar.
3. Renderer pages replace Swing login and dashboard flows.
4. Legacy Swing windows are removed after functional parity.
