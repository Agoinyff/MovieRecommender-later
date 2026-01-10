@echo off
REM Spring Boot 应用启动脚本（Windows）

echo 正在启动 Movie Recommender 后端服务...

REM JVM 参数（3200万数据建议4GB+）
set JVM_OPTS=-Xms1024m -Xmx4096m -Xmn1024m
set JVM_OPTS=%JVM_OPTS% -XX:+UseG1GC
set JVM_OPTS=%JVM_OPTS% -XX:MaxGCPauseMillis=200
set JVM_OPTS=%JVM_OPTS% -XX:MaxMetaspaceSize=256m
set JVM_OPTS=%JVM_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JVM_OPTS=%JVM_OPTS% -XX:HeapDumpPath=logs/heapdump.hprof
set JVM_OPTS=%JVM_OPTS% -XX:+UseStringDeduplication

REM 应用名称
set APP_NAME=movie-recommender-backend
set JAR_FILE=target\%APP_NAME%-1.0.0.jar

REM 检查 JAR 文件是否存在
if not exist "%JAR_FILE%" (
    echo 错误: JAR 文件不存在: %JAR_FILE%
    echo 请先运行: mvn clean package
    pause
    exit /b 1
)

REM 创建日志目录
if not exist "logs" mkdir logs

REM 启动应用
echo JVM 参数: %JVM_OPTS%
java %JVM_OPTS% -jar %JAR_FILE%

echo 应用已启动！
pause

