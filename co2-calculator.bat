@echo off
setlocal

:: Launch the CO2 calculator JAR that was produced by the Maven build.
set SCRIPT_DIR=%~dp0
set JAR_PATH=%SCRIPT_DIR%target\co2-calculator-1.0.0.jar

:: Prompt the user to build the project when the jar is missing.
if not exist "%JAR_PATH%" (
  echo error: %JAR_PATH% not found. Build the project first with .\mvnw package. 1>&2
  exit /b 1
)

:: Forward the original command-line arguments to the JVM.
java -jar "%JAR_PATH%" %*
