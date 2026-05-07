# 中医特色患者管理系统

## 🛠 技术栈
- Frontend: Vue 3 + Vite + Element Plus + Pinia + Vue Router + ECharts
- Backend: Java Spring Boot (2.7) + Spring Security + JWT + JPA/Hibernate
- DB: MySQL 8.0
- DevOps: Docker Compose + Nginx（前端容器内反向代理 /api 到后端）

## 🚀 快速启动（Docker 一键）
1. 安装并启动 Docker Desktop
2. 在项目根目录执行：
   - `docker compose up -d --build`
3. 访问前端（Nginx 服务）：
   - http://localhost/
4. 访问后端（直连调试用）：
   - http://localhost:8080/api
5. 数据库（本机端口映射）：
   - MySQL: `localhost:3307`（容器内部端口 3306）

说明：
- 前端容器已内置 Nginx 反向代理，所有 `/api` 请求会代理到后端容器 `backend:8080`，配置文件位于 `frontend/nginx.conf`。
- 数据库初始化脚本 `db/init.sql` 会在容器首次启动时自动导入（含建表与示例数据）。

## 🧪 测试账号
- 管理员：admin / 123456
- 医生：doctor1 / 123456
- 患者：patient1 / 123456

## ⚙️ 本地开发（不使用 Docker）
后端（端口 8080）：
- 启动 MySQL，确保：
  - 地址：`localhost:3307`
  - 数据库：`tcm_db`
  - 账号：`root`
  - 密码：`123456`（或通过环境变量 DB_PASSWORD 覆盖）
- 运行后端：
  - 开发运行：`mvn spring-boot:run`
  - 或打包运行：`mvn -DskipTests package` 后 `java -jar target/*.jar`
- 端口与数据源配置：`backend/src/main/resources/application.yml`

前端（端口 3000）：
- 安装 Node.js（建议 v18+）
- 安装依赖：`npm ci`
- 启动开发服务：`npm run dev`
- Vite 代理（开发模式）：`/api` 自动代理到 `http://localhost:8080`，配置文件 `frontend/vite.config.js`

可选：接口基址
- 生产/预览可通过环境变量覆盖接口基址：
  - `VITE_API_BASE=http://你的后端地址/api`
  - 默认使用 `/api`；在 Docker 部署中由 Nginx 负责转发

## 🔐 权限与安全
- 登录认证：JWT（登录成功返回 token）
- 角色与权限：
  - ADMIN（管理员）
  - DOCTOR（医生）
  - PATIENT（患者）
- 主要权限拦截在 `SecurityConfig` 中配置，开放 `/api/auth/**` 登录与注册
- 后端统一返回格式：`{ code, message, data }`

## 📸 功能介绍
- 登录/注册
  - 登录接口：`POST /api/auth/login`（返回 `token, role, username, userId, patientId`）
  - 注册接口：`POST /api/auth/register`（患者角色自动创建 Patient 档案）
  - 登录失败会在输入框下方显示对应错误（账号不存在/密码错误），并弹出顶部提示
- 首页数据概览（Dashboard）
  - 核心统计：总患者数、今日就诊、健康档案、体质辨识
  - 图表：体质类型分布（饼图）、本周就诊趋势（折线图）
  - 接口：`GET /api/stats`（按角色范围返回数据）
- 账号管理（管理员）
  - 列表、搜索、创建、编辑、删除用户
  - 页面：`frontend/src/views/admin/UserList.vue`
  - 接口：
    - `GET /api/admin/users`
    - `POST /api/admin/users/create`
    - `PUT /api/admin/users/{id}`
    - `DELETE /api/admin/users/{id}`
- 健康档案
  - 记录维护与查询（按医生/患者）
  - 相关实体与仓库：`HealthRecord`, `HealthRecordRepository`
- 体质辨识问卷与结果
  - 问卷题库、体质判定与结果保存 `TcmController` / `TcmResultRepository`
- 登录日志（管理员）
  - 查看登录历史：`GET /api/auth/logs`

## 📂 目录结构（关键文件）
- `docker-compose.yml`：一键编排数据库、后端与前端
- `db/init.sql`：数据库初始化与示例数据（包含 3 个测试账号）
- `backend/Dockerfile`：多阶段构建 Spring Boot 可运行镜像
- `frontend/Dockerfile`：构建静态资源并在 Nginx 中部署
- `frontend/nginx.conf`：将 `/api` 反向代理到后端容器
- 前端主要页面：
  - 首页概览：[frontend/src/views/Dashboard.vue](file:///Users/lh/Tare/493/frontend/src/views/Dashboard.vue)
  - 登录页：[frontend/src/views/Login.vue](file:///Users/lh/Tare/493/frontend/src/views/Login.vue)
  - 账号管理：[frontend/src/views/admin/UserList.vue](file:///Users/lh/Tare/493/frontend/src/views/admin/UserList.vue)
- 后端主要模块：
  - 认证接口：[backend/src/main/java/com/example/tcm/controller/AuthController.java](file:///Users/lh/Tare/493/backend/src/main/java/com/example/tcm/controller/AuthController.java)
  - 概览统计：[backend/src/main/java/com/example/tcm/controller/StatsController.java](file:///Users/lh/Tare/493/backend/src/main/java/com/example/tcm/controller/StatsController.java)
  - 安全配置：[backend/src/main/java/com/example/tcm/security/SecurityConfig.java](file:///Users/lh/Tare/493/backend/src/main/java/com/example/tcm/security/SecurityConfig.java)

## 🧭 启动后如何体验
1. 使用测试账号登录（例如管理员：admin/123456）
2. 进入首页查看数据概览与图表
3. 管理员进入“账号管理”，进行用户的搜索、创建与编辑
4. 医生进入患者或健康档案相关页面进行记录维护
5. 患者查看个人资料与体质辨识结果

## ❓常见问题与排查
- 浏览器显示 `net::ERR_CONNECTION_REFUSED`：
  - 确认后端服务已启动（Docker 中 `tcm-backend` 应为 Up 状态）
  - 本地开发时确认端口（后端默认 8080）
- 显示 `502` 或 `Network Error`：
  - 前端与后端未连通或网关未配置
  - Docker 部署下由 Nginx 转发 `/api` 到 `backend:8080`；请确保后端容器正常运行
  - 若非 Docker 环境，请设置 `VITE_API_BASE=http://后端地址/api`
- 登录失败无提示：
  - 前端已优化：错误弹窗与输入框下方提示并存；若未看到提示请刷新页面后重试
- 数据库连接失败：
  - 确认 Docker 映射端口 3307 未被占用，容器状态为 `healthy`
  - 本地开发请更新 `application.yml` 中的数据源配置或通过环境变量覆盖

## 🗺 路线建议
- 初学者建议按以下顺序阅读与实践：
  1) 运行 Docker 编排并使用测试账号登录
  2) 阅读前端页面 `Login.vue` 与 `Dashboard.vue`，理解 Axios 拦截与 ECharts 渲染
  3) 阅读后端 `AuthController` 与 `SecurityConfig`，理解认证流程与权限控制
  4) 扩展统计接口 `StatsController`，体验数据聚合与图表联动
