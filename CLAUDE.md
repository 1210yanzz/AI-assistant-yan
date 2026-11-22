# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库结构

心理健康助手项目的前后端 monorepo：

```
AI-assistant-yan/
├── FrontendCode/   # Vue 3 + Vite 前端
└── BackendCode/    # Spring Boot 后端（详细约定见 BackendCode/CLAUDE.md）
```

前后端以 `/api` 前缀交互：前端 axios 的 `baseURL` 为 `/api`，后端 Controller 统一以 `/api` 开头，本地联调靠 Vite 代理转发，不需要额外处理 CORS。

## FrontendCode（前端）

Vite 8 + Vue 3.5 单页应用，Element Plus、Pinia、Vue Router、Axios、ECharts、wangEditor；SSE 使用 `@microsoft/fetch-event-source`。`@` 别名指向 `src`。

```bash
cd FrontendCode
npm install
npm run dev       # 默认 http://localhost:5173
npm run build     # 输出到 dist/
npm run preview
```

- 没有 lint / test 脚本，也没有测试框架。
- **路由与权限**：`src/router/index.js` 把页面分为前台（`/`，`FrontendLayout`）、后台（`/back`，`BackendLayout`）、认证（`/auth`，`AuthLayout`）。全局守卫读取 localStorage 的 `token` 和 `userInfo.userType`：`2`=管理员只能访问 `/back`，`1`=普通用户只能访问前台；未登录访问 `/back` 会被送到 `/auth/login`。登录成功后 `token`、`userInfo` 由 `src/views/login.vue` 写入 localStorage。
- **请求封装**：`src/utils/request.js` 自动附加 `Authorization: Bearer <token>`；后端业务码是字符串，成功 `'200'` 时直接返回 `data.data`，`'-1'` 时提示并清除登录态跳登录页。接口按模块定义在 `src/api/frontend.js`（前台）和 `src/api/admin.js`（后台）。
- **代理**：`vite.config.js` 的 `/api` 指向本地后端 `http://localhost:8080`（远程后端为 `http://159.75.169.224:1235`）。
- **文件地址**：`src/config/index.js` 的 `fileBaseUrl` 指向 `https://java-yzz-web.oss-cn-beijing.aliyuncs.com`；后端返回相对路径（如 `/article/cover/xxx.png`），由前端拼接。
- **流式对话**：`src/views/consultation.vue` 不用 axios，而是 `fetchEventSource('/api/psychological-chat/stream')`，请求头放 `Token`（HTTP 头大小写不敏感，等价于后端兼容的 `token` 头）；期望每条 SSE 数据是 `Result` JSON（取 `data.content` 追加），收到 `event: done` 后停止并拉取会话情绪分析。

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

- 分层：`controller → service（接口）→ service/Impl → mapper（MyBatis-Plus BaseMapper）→ entity → MySQL`；`DTO/command` 为请求对象，`DTO/response` 为返回对象；统一响应 `common/Result`（`{code, message, data}`，code 为字符串），异常由 `GlobalExceptionHandler` 处理。另有 `AiService`（大写目录）承载 AI 业务编排与结构化输出，不属于标准 controller→service 分层。
- 安全：Spring Security 无状态 + JWT，`utils/JwtAuthenticationFilter` 校验 token；公开路径为 `/api/user/login`、`/api/user/add`。
- 已实现：用户注册/登录/当前用户/退出登录；咨询会话创建、分页列表（普通用户只看自己，管理员看全部）、消息详情、删除会话；情绪日记新增（同一用户同一天重复提交覆盖并清空旧 AI 分析）、后台分页、删除；知识分类列表、文章分页/详情/增删改/发布下线（普通用户只能看已发布）；文章封面 OSS 上传；数据看板总览；会话情绪分析（DeepSeek 结构化结果缓存在会话表）；DeepSeek 流式对话。
- 仅设计未实现：收藏（目前只有 DTO）。
- **MyBatis-Plus 必须使用 `mybatis-plus-spring-boot4-starter`**，不要降级为 boot3 starter（Spring Boot 4 移包会导致 Mapper 扫描失败）。分页另外需要 `mybatis-plus-jsqlparser-4.9:3.5.18`（3.5.9+ 已拆为可选依赖），分页插件在 `config/MybatisPlusConfig`。
- 已通过 `spring-ai-starter-model-openai` 接入 DeepSeek（`spring.ai.openai.base-url=https://api.deepseek.com/v1`，模型 `deepseek-chat`），`POST /api/psychological-chat/stream` 以 SSE 流式返回 `Result` JSON 增量（`data.content`），结束时发送 `event: done` / `[DONE]`，异常发送 `event: error` + JSON `Result`；`DEEPSEEK_API_KEY` 通过环境变量提供。
- 文章封面通过 `aliyun-sdk-oss 3.18.4` 上传到 OSS（bucket `java-yzz-web`，地域 `oss-cn-beijing`）；`OSS_ACCESS_KEY_ID`、`OSS_ACCESS_KEY_SECRET` 用环境变量注入，bucket 需公共读。
- **没有 Maven Wrapper**，统一使用本地 Maven（本机为 `D:\develop\apache-maven-3.9.4`），不要重新生成 `mvnw`。
- 配置集中在 `src/main/resources/application.yml`（数据源、端口、`jwt.*`、`spring.ai.*`），其中包含数据库口令与 JWT 密钥，修改或分发时注意。

## 前后端对接注意

- 鉴权：前端普通请求发 `Authorization: Bearer <token>`，SSE 请求发 `Token: <token>`；后端 `JwtTokenUtil.getTokenFromRequest` 已兼容两者。
- **SSE 协议**：前后端统一使用 `/api/psychological-chat/stream`；普通增量是 `Result` JSON（`data.content`），收到 `event: done` 结束，`event: error` 时也是 JSON `Result`。
- 前端已调用的接口均已在后端实现（收藏功能前端暂未调用，后端也未实现）。
- 后端详细约定（包名大写目录、命名、依赖约束等）见 `BackendCode/CLAUDE.md`。
