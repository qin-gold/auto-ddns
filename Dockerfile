FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 添加应用jar包
COPY target/auto-ddns-1.0.0.jar /app/app.jar

# 暴露健康检查端口
EXPOSE 8080

# 设置时区为Asia/Shanghai
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 设置启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 