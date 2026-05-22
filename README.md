# 微信读书笔记个人网站

个人微信读书标注与想法展示、导出工具。

## 功能

- 微信读书扫码登录
- 书架展示（最后阅读时间倒序，10 条/页）
- 书籍详情：标注 + 想法（按章节聚合，20 条/页）
- 一键导出单本书的所有标注与想法为 Markdown 文件
- 手动触发数据同步

## 技术栈

| 层     | 选型                                              |
| ------ | ------------------------------------------------- |
| 后端   | Java 21 + Spring Boot 3.2                         |
| 前端   | Next.js 14 + React 18 + TypeScript + Tailwind CSS |
| 数据库 | MySQL 8.0                                         |
| 容器化 | Docker + docker-compose                           |

## 本地启动

### 前置条件

- Docker & docker-compose
- （可选）Java 21 + Maven、Node 20 + pnpm（直接跑源码时需要）

### 步骤

```bash
# 1. 克隆项目
git clone <your-repo-url>
cd weread-personal

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，修改 DB_PASSWORD 和 JWT_SECRET

# 3. 一键启动
docker-compose up -d

# 4. 访问
open http://localhost:3000
```

### 本地开发（不用 Docker）

**后端**

```bash
cd backend
# 先启动 MySQL，或修改 application.yml 指向已有实例
mvn spring-boot:run
```

**前端**

```bash
cd frontend
cp .env.local.example .env.local
pnpm install
pnpm dev
```

## 环境变量说明

| 变量            | 说明                  | 示例                                 |
| --------------- | --------------------- | ------------------------------------ |
| `DB_URL`        | MySQL JDBC URL        | `jdbc:mysql://mysql:3306/weread?...` |
| `DB_USERNAME`   | 数据库用户名          | `weread`                             |
| `DB_PASSWORD`   | 数据库密码            | _(自定义)_                           |
| `JWT_SECRET`    | JWT 签名密钥（≥32位） | _(自定义长字符串)_                   |
| `SERVER_PORT`   | 后端端口（默认 8080） | `8080`                               |
| `FRONTEND_PORT` | 前端端口（默认 3000） | `3000`                               |

> **安全提示**：`.env` 已加入 `.gitignore`，请勿将含真实密钥的文件提交到 Git。

## 使用流程

1. 打开 `http://localhost:3000`，用微信扫码登录
2. 登录后点击右上角「同步数据」拉取书架、标注、想法
3. 同步完成后书架自动刷新
4. 点击书籍进入详情页查看标注与想法
5. 点击「导出 Markdown」下载 `.md` 文件

## 部署到云端

将 `.env` 中的变量配置到云平台的环境变量/Secret 管理中，使用 `docker-compose.prod.yml` 或平台原生容器服务部署。
