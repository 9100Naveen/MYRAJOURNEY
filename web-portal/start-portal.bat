@echo off
title MYRA Web Portal
cd /d "%~dp0"
echo Starting MYRA Web Portal at http://localhost:5174 ...
npm run dev
pause
