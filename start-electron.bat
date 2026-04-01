@echo off
setlocal

cd /d "%~dp0desktop"

if not exist "node_modules" (
    echo Electron dependencies are missing.
    echo Run: npm install
    exit /b 1
)

npm run dev
