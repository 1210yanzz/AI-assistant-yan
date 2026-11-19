# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库结构

心理健康助手项目的前后端 monorepo：

```
AI-assistant-yan/
├── FrontendCode/   # Vue 3 + Vite 前端
└── BackendCode/    # Spring Boot 后端（详细说明见 BackendCode/CLAUDE.md）
```

前后端通过 `/api` 前缀交互：前端 axios 的 `baseURL` 为 `/api`，后端接口统一以 `/api/...` 开头。

## FrontendCode（前端）

Vite 8 + Vue 3.5 单页应用，Element Plus、Pinia、Vue Router、Axios、ECharts、wangEditor；SSE 使用 `@microsoft/fetch-event-source`。

```bash
cd FrontendCode
npm install
npm run dev       # 默认 http://localhost:5173
npm run build     # 输出到 dist/
npm run preview
```

- 没有 lint / test 脚本。
- 页面在 `src/views/`（首页、登录/注册、咨询对话、咨询记录、情绪日记、知识文章、后台 dashboard 等），路由在 `src/router/index.js`。
- 请求封装在 `src/utils/request.js`：`baseURL: '/api'`，登录后自动附加 `Authorization: Bearer <token>`；业务码非 `200` 时统一处理，`-1` 会清除登录态并跳转登录页。
- `vite.config.js` 中 `/api` 代理目标是 `http://159.75.169.224:1235`；本地联调后端时需改为 `http://localhost:8080`。
- 接口定义按模块放在 `src/api/`（如 `frontend.js`、`admin.js`）。

## BackendCode（后端）

Spring Boot 4.1.2 + Java 17 + MyBatis-Plus + MySQL + Spring Security/JWT + Spring AI，端口 8080，数据库 `ai_mental_health_assistant`。

```bash
cd BackendCode
mvn spring-boot:run
mvn clean package
mvn -DskipTests package
mvn test
mvn -Dtest=AiSpringbootYanApplicationTests#contextLoads test
```

- **没有 Maven Wrapper**，统一使用本地 Maven（本机为 `D:\develop\apache-maven-3.9.4`），不要重新生成 `mvnw`。
- 分层：`controller → service（接口）→ service/Impl → mapper（MyBatis-Plus BaseMapper）→ entity → MySQL`；`DTO/command` 为请求对象，`DTO/response` 为返回对象；统一响应 `common/Result`（`{code, message, data}`），异常由 `GlobalExceptionHandler` 处理。
- 安全：Spring Security 无状态 + JWT，`utils/JwtAuthenticationFilter` 校验 token；公开路径为 `/api/user/login`、`/api/user/add`。
- 已实现：用户注册/登录/当前用户、咨询会话创建、咨询消息保存与查询；实体已映射 `user`、`consultation_session`、`consultation_message`。
- 仅设计未实现：情绪日记、知识文章/分类、文件上传、收藏、统计看板等（目前只有 DTO）。
- **MyBatis-Plus 必须使用 `mybatis-plus-spring-boot4-starter`**，不要降级为 boot3 starter（Spring Boot 4 移包会导致 Mapper 扫描失败）。
- Spring AI 的 `spring-ai-starter-model-openai` 已引入，用于接入 OpenAI 兼容的大模型接口（如 DeepSeek），当前尚未完成实际调用。
- 配置集中在 `src/main/resources/application.yml`（数据源、端口、`jwt.*`），其中包含数据库口令与 JWT 密钥，修改或分发时注意。

## 前后端对接注意

- 前端发送 `Authorization: Bearer <token>`，后端 `JwtTokenUtil.getTokenFromRequest` 当前读取的请求头是 `token`，两端不一致时受保护接口会返回未授权。
- 前端已调用但后端尚未实现的接口包括：咨询会话列表/删除/消息详情、会话情绪分析、情绪日记、知识文章列表与详情。
- 后端详细约定（包名大写目录、命名、依赖约束等）见 `BackendCode/CLAUDE.md`。
