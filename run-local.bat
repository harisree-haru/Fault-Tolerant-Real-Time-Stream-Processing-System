@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo ========================================================
echo   FRAUD DETECTION SYSTEM (Local Embedded Mode)
echo ========================================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-17"

echo Compiling...
call .\apache-maven-3.9.12\bin\mvn.cmd compile -q -DskipTests

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Done. Starting Fraud Detection...
echo.

REM Build classpath: target/classes + ALL Flink JARs + Maven dependencies
set "BASEDIR=%CD%"
set "CLASSPATH=%BASEDIR%\target\classes"

REM Add ALL Flink JARs from lib and opt directories (AT ROOT LEVEL)
for %%F in ("%BASEDIR%\lib\*.jar") do (
    set "CLASSPATH=!CLASSPATH!;%%F"
)
for %%F in ("%BASEDIR%\opt\*.jar") do (
    set "CLASSPATH=!CLASSPATH!;%%F"
)

REM Add essential Maven dependencies
set "M2=%USERPROFILE%\.m2\repository"
for %%F in (
    "!M2!\com\google\code\gson\gson\2.8.9\gson-2.8.9.jar"
    "!M2!\org\slf4j\slf4j-api\1.7.30\slf4j-api-1.7.30.jar"
    "!M2!\org\slf4j\slf4j-log4j12\1.7.30\slf4j-log4j12-1.7.30.jar"
    "!M2!\log4j\log4j\1.2.17\log4j-1.2.17.jar"
) do (
    if exist %%F (
        set "CLASSPATH=!CLASSPATH!;%%F"
    )
)

echo.
echo Classpath:
echo !CLASSPATH!
echo.

REM Run with proper module access
"!JAVA_HOME!\bin\java.exe" ^
  --add-opens=java.base/java.lang=ALL-UNNAMED ^
  --add-opens=java.base/java.util=ALL-UNNAMED ^
  -Dorg.apache.flink.shaded.io.netty.tryReflectionSetAccessible=true ^
  -cp "!CLASSPATH!" ^
  com.flink.fraud.LocalStreamFraudDetectorJob

pause
