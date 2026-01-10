#!/bin/bash
# Spring Boot 应用启动脚本（Linux/Mac）

echo "正在启动 Movie Recommender 后端服务..."

# JVM 参数（3200万数据建议4GB+）
JVM_OPTS="-Xms1024m -Xmx4096m -Xmn1024m"
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"
JVM_OPTS="$JVM_OPTS -XX:MaxGCPauseMillis=200"
JVM_OPTS="$JVM_OPTS -XX:MaxMetaspaceSize=256m"
JVM_OPTS="$JVM_OPTS -XX:+HeapDumpOnOutOfMemoryError"
JVM_OPTS="$JVM_OPTS -XX:HeapDumpPath=logs/heapdump.hprof"
JVM_OPTS="$JVM_OPTS -XX:+UseStringDeduplication"

# 应用名称
APP_NAME="movie-recommender-backend"
JAR_FILE="target/${APP_NAME}-1.0.0.jar"

# 检查 JAR 文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: JAR 文件不存在: $JAR_FILE"
    echo "请先运行: mvn clean package"
    exit 1
fi

# 创建日志目录
mkdir -p logs

# 启动应用
echo "JVM 参数: $JVM_OPTS"
java $JVM_OPTS -jar $JAR_FILE

echo "应用已启动！"

