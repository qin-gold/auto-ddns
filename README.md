# Auto DDNS

一个基于Spring Boot的自动DDNS（动态域名解析）更新工具，支持多个DNS服务提供商。

## 功能特点

- 自动检测并更新域名解析记录
- 支持多个DNS服务提供商（目前支持腾讯云，计划支持阿里云）
- 可配置的更新时间间隔
- 多个IP地址查询服务源，保证可用性
- 详细的日志记录
- 基于工厂模式的可扩展设计

## 系统要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- Spring Boot 3.x

## 快速开始

### 1. 配置文件

在 `src/main/resources/application.yml` 中配置你的DNS服务信息：
```yaml
spring:
  application:
    name: auto-ddns
    
#DDNS Configuration
ddns:
  provider: tencent # DNS服务提供商：tencent/aliyun
  access-key: your_key # 访问密钥
  secret-key: your_secret # 密钥
  domain: example.com # 主域名
  sub-domain: home # 子域名
  update-interval: 300000 # 更新间隔（毫秒），默认5分钟
```

### 2. 编译打包
```shell
bash
mvn clean package
```

### 3. 运行

#### 直接运行
```shell
# 使用默认配置运行
java -jar target/auto-ddns-1.0.0.jar

# 指定内存大小运行
java -Xms128m -Xmx256m -jar target/auto-ddns-1.0.0.jar
```

#### 后台运行（推荐）
```shell
# 创建日志目录
mkdir -p logs

# nohup后台运行，指定内存大小，输出日志到文件
nohup java -Xms128m -Xmx256m -jar target/auto-ddns-1.0.0.jar > logs/auto-ddns.log 2>&1 &

# 查看运行状态
ps -ef | grep auto-ddns

# 查看日志
tail -f logs/auto-ddns.log
```

#### 运行参数说明
| 参数 | 说明 | 推荐值 |
|------|------|--------|
| -Xms | 初始堆内存大小 | 128m |
| -Xmx | 最大堆内存大小 | 256m |
| -XX:MetaspaceSize | 元空间初始大小 | 128m |
| -XX:MaxMetaspaceSize | 元空间最大大小 | 256m |

完整的启动命令示例：
```shell
nohup java -Xms128m -Xmx256m \
  -XX:MetaspaceSize=128m \
  -XX:MaxMetaspaceSize=256m \
  -jar target/auto-ddns-1.0.0.jar \
  --spring.profiles.active=prod \
  > logs/auto-ddns.log 2>&1 &
```

#### 停止服务
```shell
# 查找进程ID
ps -ef | grep auto-ddns

# 停止服务
kill -15 进程ID
```

#### 开机自启动
创建系统服务文件：
```shell
sudo vim /etc/systemd/system/auto-ddns.service
```

添加以下内容：
```ini
[Unit]
Description=Auto DDNS Service
After=network.target

[Service]
Type=simple
User=your_user
ExecStart=/usr/bin/java -Xms128m -Xmx256m -jar /path/to/auto-ddns-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用服务：
```shell
# 重载系统服务
sudo systemctl daemon-reload

# 启用开机自启
sudo systemctl enable auto-ddns

# 启动服务
sudo systemctl start auto-ddns

# 查看服务状态
sudo systemctl status auto-ddns
```

## 支持的DNS服务提供商

### 腾讯云DNS（DNSPod）
- 需要在腾讯云控制台获取 AccessKey 和 SecretKey
- 支持自动创建和更新DNS记录
- 支持A记录类型

### 阿里云DNS（计划支持）
- 开发中...

## 配置说明

### 基础配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| provider | DNS服务提供商（tencent/aliyun） | 无 |
| access-key | 访问密钥 | 无 |
| secret-key | 密钥 | 无 |
| domain | 主域名 | 无 |
| sub-domain | 子域名 | 无 |
| update-interval | 更新间隔（毫秒） | 300000 |

### 邮件通知配置

| 配置项 | 说明 | 默认值 | 示例 |
|--------|------|--------|------|
| email.enabled | 是否启用邮件通知 | false | true |
| email.host | SMTP服务器地址 | smtp.qq.com | smtp.qq.com |
| email.port | SMTP服务器端口 | 465 | 465 |
| email.username | SMTP用户名 | 无 | your_qq@qq.com |
| email.password | SMTP密码/授权码 | 无 | abcdefghijklmn |
| email.from | 发件人地址 | 无 | your_qq@qq.com |
| email.to | 收件人地址 | 无 | notify@example.com |

#### QQ邮箱配置示例
```yaml
email:
  enabled: true
  host: smtp.qq.com
  port: 465
  username: your_qq@qq.com
  password: your_smtp_password  # QQ邮箱授权码，不是QQ密码
  from: your_qq@qq.com
  to: notify_to@example.com
```

#### 163邮箱配置示例
```yaml
email:
  enabled: true
  host: smtp.163.com
  port: 465
  username: your_mail@163.com
  password: your_smtp_password  # 163邮箱授权码
  from: your_mail@163.com
  to: notify_to@example.com
```

### 通知说明

邮件通知在以下情况下触发：
1. DNS记录更新成功
2. DNS记录更新失败
3. 无法获取公网IP地址

未发生IP变化时不会发送通知。

## 日志说明

系统运行时会输出以下关键日志：

- 服务启动配置信息
- IP地址检查和更新记录
- DNS记录更新结果
- 错误和异常信息

示例日志：
```log
2024-03-21 10:00:00 INFO DDNS服务已启动，当前配置：
2024-03-21 10:00:00 INFO 域名: home.example.com
2024-03-21 10:00:00 INFO 服务提供商: tencent
2024-03-21 10:00:00 INFO 更新间隔: 300000 毫秒
2024-03-21 10:00:00 INFO 检查DNS记录: home.example.com
2024-03-21 10:00:00 INFO 当前公网IP: 1.2.3.4
2024-03-21 10:00:00 INFO 当前DNS记录: 5.6.7.8
2024-03-21 10:00:00 INFO IP地址发生变化，开始更新DNS记录...
2024-03-21 10:00:01 INFO DNS记录更新成功:
2024-03-21 10:00:01 INFO 域名: home.example.com
2024-03-21 10:00:01 INFO 新IP: 1.2.3.4
2024-03-21 10:00:01 INFO 原IP: 5.6.7.8
```
## IP地址查询服务

系统使用以下服务获取公网IP地址：
- checkip.amazonaws.com
- icanhazip.com
- ifconfig.me/ip
- ipinfo.io/ip

如果某个服务不可用，系统会自动尝试下一个服务。

## 扩展开发

### 添加新的DNS服务提供商

1. 在 `DNSProvider` 枚举中添加新的服务商
2. 实现 `DDNSService` 接口
3. 在 `DDNSFactory` 中添加新服务的创建逻辑

示例：
```java
@Service
public class NewProviderDDNSServiceImpl implements DDNSService {
    @Override
    public boolean updateDNSRecord(String domain, String subDomain, String recordType, String value) {
    // 实现DNS记录更新逻辑
    return true;
}

@Override
    public String getCurrentRecord(String domain, String subDomain) {
    // 实现获取当前记录逻辑
    return null;
    }
}

```
## 常见问题

1. 无法获取公网IP
    - 检查网络连接
    - 查看是否所有IP查询服务都无法访问
    - 检查防火墙设置

2. DNS记录更新失败
    - 验证AccessKey和SecretKey是否正确
    - 检查域名和子域名配置
    - 查看详细错误日志
    - 确认DNS服务提供商API是否正常

## 贡献指南

欢迎提交Pull Request或Issue来帮助改进这个项目。

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## Docker 部署

### 1. 构建镜像

```shell
# 先编译打包
mvn clean package

# 构建Docker镜像
docker build -t auto-ddns:1.0.0 .
```

### 2. 运行容器

基础运行方式：
```shell
docker run -d \
  --name auto-ddns \
  --restart always \
  -e DDNS_PROVIDER=tencent \
  -e DDNS_ACCESS_KEY=your_access_key \
  -e DDNS_SECRET_KEY=your_secret_key \
  -e DDNS_DOMAIN=example.com \
  -e DDNS_SUB_DOMAIN=home \
  -e DDNS_UPDATE_INTERVAL=300000 \
  auto-ddns:1.0.0
```

使用配置文件运行：
```shell
# 创建配置目录
mkdir -p /opt/auto-ddns/config

# 复制配置文件
cp application.yml /opt/auto-ddns/config/

# 运行容器
docker run -d \
  --name auto-ddns \
  --restart always \
  -v /opt/auto-ddns/config:/app/config \
  -e TZ=Asia/Shanghai \
  auto-ddns:1.0.0
```

### 3. 使用Docker Compose

创建 `docker-compose.yml` 文件：

```yaml
version: '3'
services:
  auto-ddns:
    image: auto-ddns:1.0.0
    container_name: auto-ddns
    restart: always
    environment:
      - TZ=Asia/Shanghai
      - DDNS_PROVIDER=tencent
      - DDNS_ACCESS_KEY=your_access_key
      - DDNS_SECRET_KEY=your_secret_key
      - DDNS_DOMAIN=example.com
      - DDNS_SUB_DOMAIN=home
      - DDNS_UPDATE_INTERVAL=300000
    volumes:
      - /opt/auto-ddns/config:/app/config
      - /opt/auto-ddns/logs:/app/logs
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

运行：
```shell
docker-compose up -d
```

### 4. 推送到远程服务器

```shell
# 标记镜像
docker tag auto-ddns:1.0.0 your-registry.com/auto-ddns:1.0.0

# 推送镜像
docker push your-registry.com/auto-ddns:1.0.0

# 在远程服务器拉取和运行
docker pull your-registry.com/auto-ddns:1.0.0
docker run -d --name auto-ddns --restart always [环境变量参数] your-registry.com/auto-ddns:1.0.0
```

### 5. Docker部署注意事项

1. 环境变量配置
   - 可以使用环境变量文件：
     ```shell
     docker run --env-file=.env -d auto-ddns:1.0.0
     ```
   - .env文件示例：
     ```
     DDNS_PROVIDER=tencent
     DDNS_ACCESS_KEY=your_key
     DDNS_SECRET_KEY=your_secret
     ```

2. 持久化
   - 配置文件: `/app/config`
   - 日志文件: `/app/logs`
   ```shell
   docker run -v /opt/auto-ddns/config:/app/config -v /opt/auto-ddns/logs:/app/logs -d auto-ddns:1.0.0
   ```

3. 健康检查
   - 容器内置健康检查接口：`/health`
   - 可以通过Docker命令查看状态：
     ```shell
     docker inspect --format='{{.State.Health.Status}}' auto-ddns
     ```

4. 日志查看
   ```shell
   # 查看容器日志
   docker logs auto-ddns
   
   # 实时查看日志
   docker logs -f auto-ddns
   ```

5. 容器管理
   ```shell
   # 停止容器
   docker stop auto-ddns
   
   # 启动容器
   docker start auto-ddns
   
   # 重启容器
   docker restart auto-ddns
   
   # 删除容器
   docker rm -f auto-ddns
   ```

6. 多架构支持
   ```shell
   # 构建多架构镜像
   docker buildx build --platform linux/amd64,linux/arm64 -t auto-ddns:1.0.0 .
   ```