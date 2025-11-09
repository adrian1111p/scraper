@echo off
setlocal

REM === Define custom input/output paths ===
set "INPUT=D:/synapses"
set "OUTPUT=D:/Site/apass/scraper.txt"

REM === Path to built .jar ===
set "JAR=target/scraper-0.0.1-SNAPSHOT.jar"

echo 🚀 Running scraper from JAR...
java -jar %JAR% ^
 --scraper.input-folder="%INPUT%" ^
 --scraper.output-file="%OUTPUT%"

echo ✅ Output written to: %OUTPUT%
pause
