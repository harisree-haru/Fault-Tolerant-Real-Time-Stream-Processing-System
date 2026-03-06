@echo off
setlocal enabledelayedexpansion
set "FLINK_HOME=%~dp0flink-1.17.1"
set "FLINK_CONF=%FLINK_HOME%\conf"

set "CLASSPATH=%FLINK_CONF%"
for %%F in ("%FLINK_HOME%\lib\*.jar") do (
    set "CLASSPATH=!CLASSPATH!;%%F"
)

set "JAVA_MODULE_OPTS=--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.net.util=ALL-UNNAMED --add-exports=java.base/sun.net.util=ALL-UNNAMED --add-opens=java.base/sun.net.spi=ALL-UNNAMED"

echo Starting TaskManager...
start /b "" java %JAVA_MODULE_OPTS% -Xmx536870902 -Xms536870902 -XX:MaxDirectMemorySize=268435458 -XX:MaxMetaspaceSize=268435456 -cp "!CLASSPATH!" org.apache.flink.runtime.taskexecutor.TaskManagerRunner --configDir "%FLINK_CONF%" -Dtaskmanager.memory.network.min=134217730b -Dtaskmanager.cpu.cores=4.0 -Dtaskmanager.memory.task.off-heap.size=0b -Dtaskmanager.memory.jvm-metaspace.size=268435456b -Dexternal-resources=none -Dtaskmanager.memory.jvm-overhead.min=201326592b -Dtaskmanager.memory.framework.off-heap.size=134217728b -Dtaskmanager.memory.network.max=134217730b -Dtaskmanager.memory.framework.heap.size=134217728b -Dtaskmanager.memory.managed.size=536870920b -Dtaskmanager.memory.task.heap.size=402653174b -Dtaskmanager.numberOfTaskSlots=4 -Dtaskmanager.memory.jvm-overhead.max=201326592b
