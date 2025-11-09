@echo off
setlocal

REM === Configuration ===
set "MAIN_CLASS=com.example.scraper.ScraperApplication"
set "CONFIG_FILE=src/main/resources/application.yml"

echo 🚀 Building project...
mvn clean compile
if %errorlevel% neq 0 (
    echo ❌ Maven build failed.
    pause
    exit /b 1
)

echo ▶️ Running Spring Boot application with main config...
mvn spring-boot:run ^
 -Dspring-boot.run.main-class=%MAIN_CLASS% ^
 -Dspring-boot.run.arguments=--spring.config.location=file:%CONFIG_FILE%

echo ✅ Output written to: D:\Site\apass\scraper.txt
pause
