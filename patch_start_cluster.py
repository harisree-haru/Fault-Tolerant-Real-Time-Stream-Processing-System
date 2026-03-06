import sys
import re

def extract_opts(filename):
    jvm_lines = []
    dyn_lines = []
    with open(filename, 'r', encoding='utf-16') as f:
        for line in f:
            if "BASH_JAVA_UTILS_EXEC_RESULT:" in line:
                payload = line.split("BASH_JAVA_UTILS_EXEC_RESULT:")[1].strip()
                if payload.startswith('-X'):
                    jvm_lines.append(payload)
                else:
                    dyn_lines.append(payload)
    
    jvm_str = " ".join(jvm_lines).replace("'", "").replace('"', "")
    dyn_str = " ".join(dyn_lines).replace("-D ", "-D").replace("'", "").replace('"', "")
    return jvm_str, dyn_str

jm_jvm_opts, jm_dyn_opts = extract_opts('jm_opts.txt')
tm_jvm_opts, tm_dyn_opts = extract_opts('tm_opts.txt')

bat_content = f"""@echo off
setlocal enabledelayedexpansion
set "FLINK_HOME=%~dp0.."
set "FLINK_CONF=%FLINK_HOME%\\conf"
set "FLINK_LOG=%FLINK_HOME%\\log"

if not exist "%FLINK_LOG%" mkdir "%FLINK_LOG%"

set "CLASSPATH=%FLINK_CONF%"
for %%F in ("%FLINK_HOME%\\lib\\*.jar") do (
    set "CLASSPATH=!CLASSPATH!;%%F"
)


set "JAVA_MODULE_OPTS=--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.net.util=ALL-UNNAMED --add-exports=java.base/sun.net.util=ALL-UNNAMED --add-opens=java.base/sun.net.spi=ALL-UNNAMED"

echo Starting JobManager...
start /b "" java %JAVA_MODULE_OPTS% -Dlog.file=C:\\flink_logs\\jobmanager.log "-Dlog4j.configurationFile=file:%FLINK_CONF%\\log4j.properties" {jm_jvm_opts} -cp "!CLASSPATH!" org.apache.flink.runtime.entrypoint.StandaloneSessionClusterEntrypoint --configDir "%FLINK_CONF%" {jm_dyn_opts} > "%FLINK_LOG%\\jobmanager.out" 2>&1

echo Starting TaskManager...
start /b "" java %JAVA_MODULE_OPTS% -Dlog.file=C:\\flink_logs\\taskmanager.log "-Dlog4j.configurationFile=file:%FLINK_CONF%\\log4j.properties" {tm_jvm_opts} -cp "!CLASSPATH!" org.apache.flink.runtime.taskexecutor.TaskManagerRunner --configDir "%FLINK_CONF%" {tm_dyn_opts} > "%FLINK_LOG%\\taskmanager.out" 2>&1

echo Cluster started successfully! Check log directory for output.
"""

with open('flink-1.17.1/bin/start-cluster.bat', 'w') as f:
    f.write(bat_content)

print("start-cluster.bat patched successfully.")
