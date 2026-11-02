@echo off
title eSports Tournament Management System
echo ===================================================
echo Starting eSports Tournament Management System...
echo ===================================================

set "JAVA_CMD=java"

:: Check if java is available in PATH
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe"
    ) else if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\java.exe" (
            set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        )
    )
)

if exist "target\esports-tournament-system-1.0.0-jar-with-dependencies.jar" (
    "%JAVA_CMD%" -jar "target\esports-tournament-system-1.0.0-jar-with-dependencies.jar" %*
) else (
    echo [ERROR] JAR file not found in target/ folder.
    echo Please build first using Maven or IntelliJ IDEA.
    pause
)
