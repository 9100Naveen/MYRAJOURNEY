' MYRA Web Portal - Silent Auto-Starter
' This script launches the Vite dev server silently at Windows login (no console window)

Dim shell
Set shell = CreateObject("WScript.Shell")

' Use full node.exe path to avoid PATH not being loaded at login
shell.Run """C:\Program Files\nodejs\node.exe"" ""C:\Users\HP\Downloads\MYRAJOURNEYBACK\MYRAJOURNEYBACK\web-portal\node_modules\vite\bin\vite.js""", 0, False

Set shell = Nothing
