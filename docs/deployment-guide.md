# 部署指南

> 本文档提供 B2C 电商平台的生产环境部署方案，包括 Docker Compose 部署和 Nginx 反向代理配置。

---

## 目录

- [1. Docker Compose 部署](#1-docker-compose-部署)
- [2. Nginx 反向代理配置](#2-nginx-反向代理配置)
- [3. 生产环境注意事项](#3-生产环境注意事项)

---

## 1. Docker Compose 部署

### 1.1 创建 `docker-compose.yml`

在项目根目录创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: jd-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: jd_ecommerce
      MYSQL_CHARSET: utf8mb4
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/src/main/resources/sql/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./backend/src/main/resources/sql/data.sql:/docker-entrypoint-initdb.d/02-data.sql
    networks:
      - jd-network

  backend:
    build: ./backend
    container_name: jd-backend
    depends_on:
      - mysql
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jd_ecommerce?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
    ports:
      - "8080:8080"
    networks:
      - jd-network

  frontend-user:
    build: ./frontend-user
    container_name: jd-frontend-user
    depends_on:
      - backend
    ports:
      - "5173:5173"
    networks:
      - jd-network

  frontend-admin:
    build: ./frontend-admin
    container_name: jd-frontend-admin
    depends_on:
      - backend
    ports:
      - "5174:5174"
    networks:
      - jd-network

  frontend-merchant:
    build: ./frontend-merchant
    container_name: jd-frontend-merchant
    depends_on:
      - backend
    ports:
      - "5175:5175"
    networks:
      - jd-network

volumes:
  mysql_data:

networks:
  jd-network:
    driver: bridge
```

### 1.2 后端 Dockerfile

创建 `backend/Dockerfile`：

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/jd-ecommerce-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 1.3 前端 Dockerfile

创建 `frontend-user/Dockerfile`：

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
RUN npm install -g vite
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/vite.config.js .
EXPOSE 5173
CMD ["npx", "vite", "preview", "--host", "0.0.0.0", "--port", "5173"]
```

管理后台 Dockerfile 同理，端口改为 5174。

商家后台 Dockerfile 同理，端口改为 5175：

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
RUN npm install -g vite
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/vite.config.js .
EXPOSE 5175
CMD ["npx", "vite", "preview", "--host", "0.0.0.0", "--port", "5175"]
```

### 1.4 启动

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f backend

# 停止
docker-compose down
```

---

## 2. Nginx 反向代理配置

生产环境推荐使用 Nginx 作为反向代理，统一入口：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 用户端前端
    location / {
        proxy_pass http://127.0.0.1:5173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 管理后台前端
    location /admin {
        proxy_pass http://127.0.0.1:5174;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 商家后台前端
    location /merchant {
        proxy_pass http://127.0.0.1:5175;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 后端 API
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### HTTPS 配置（推荐）

使用 Let's Encrypt 免费证书：

```bash
# 安装 certbot
sudo apt install certbot python3-certbot-nginx

# 申请证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

---

## 3. 生产环境注意事项

| 事项 | 说明 |
|------|------|
| 数据库密码 | 修改默认 root 密码，创建专用数据库用户并限制权限 |
| JWT Secret | 修改 `application.yml` 中的 `jwt.secret` 为高强度随机字符串 |
| CORS 配置 | 生产环境将 `WebConfig` 中的允许来源改为具体域名 |
| 日志级别 | 生产环境将 `logging.level.com.jd.ecommerce` 改为 `info` 或 `warn` |
| 前端构建 | 生产环境使用 `npm run build` 生成静态文件，用 Nginx 直接托管 |
| 数据库备份 | 定期备份 MySQL 数据：`mysqldump -u root -p jd_ecommerce > backup.sql` |
| HTTPS | 生产环境必须启用 HTTPS，使用 Nginx + Let's Encrypt |
| 防火墙 | 仅开放 80/443 端口，后端 8080 端口不对外暴露 |
