# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

心理健康助手后端服务：Spring Boot 4.1.2 + Java 17 + MyBatis-Plus + MySQL + Spring Security/JWT。Maven 构建，启动端口 8080，数据库 `ai_mental_health_assistant`。

## 构建与运行

```bash
mvn clean package                  # 构建并跑测试
mvn -DskipTests package            # 跳过测试构建
mvn spring-boot:run                # 启动服务（需本机 MySQL 可连接）

mvn test                           # 全部测试
mvn -Dtest=AiSpringbootYanApplicationTests#contextLoads test   # 单个测试
```

- 项目**没有 Maven Wrapper**（`mvnw`/`.mvn` 已移除），统一使用本地 Maven（本机为 `D:\develop\apache-maven-3.9.4`，IDEA 的 Maven home path 也指向它）。不要重新生成 wrapper。
- 没有配置 lint 工具。
- `@SpringBootTest` 测试会启动完整上下文并连接 `application.yml` 中配置的 MySQL，数据库不可用时测试会失败。

## 架构

单体分层结构，包根为 `com.yanzz.aispringbootyan`：

```
controller → service（接口）→ service/Impl（实现）→ mapper（MyBatis-Plus BaseMapper）→ entity → MySQL
```

- **DTO 分层**：`DTO/command` 是请求入参（带 JSR-303 校验注解），`DTO/response` 是返回对象（部分含静态内部类，如 `UserLoginResponseDTO.UserDetailResponseDTO`）。响应对象只暴露前端所需字段，并会附带展示用字段（如 `genderDisplayName`、`displayName`）。
- **转换层**：`service/convert/UserConvert` 是静态工具类，负责 entity ↔ DTO 映射，不是 Spring Bean。
- **统一响应**：所有 Controller 返回 `common/Result<T>`，结构为 `{code, message, data}`，code 是字符串（成功为 `"200"`），错误码集中定义在 `common/ResultCode`。
- **异常处理**：业务异常抛 `exception/BusinessException`（默认 code `"-1"`），由 `common/GlobalExceptionHandler` 统一转为 `Result`；参数校验异常同样在此处理。
- **持久层**：MyBatis-Plus，Mapper 接口继承 `BaseMapper<T>` 并标注 `@Mapper`，无 XML 映射文件；Application 类上没有 `@MapperScan`，依赖 `@Mapper` 注解 + MyBatis-Plus 自动配置扫描。实体用 `@TableName`/`@TableId(type = IdType.AUTO)`/`@TableField` 映射表与列。
- **安全**：Spring Security 无状态配置（`config/SecurityConfig`），公开路径只有 `/api/user/login`、`/api/user/add`，其余需认证。JWT 生成与解析在 `utils/JwtTokenUtil`（claims：userId、username、roleType），认证过滤器 `utils/JwtAuthenticationFilter` 从请求头读取 token（按 `jwt.header`，默认 `Authorization: Bearer`，兼容旧的 `token` 头）。
- **密码**：`UserServiceImpl` 内直接 `new BCryptPasswordEncoder()` 做哈希校验。

### 当前实现范围

用户域、咨询会话域、情绪日记域、知识文章域已完成端到端实现（entity/mapper/service/controller）：用户注册、登录、当前用户、退出登录；创建咨询会话、保存用户/AI 消息、查询最近消息、会话分页列表（普通用户只看自己，管理员看全部）、会话消息详情、删除会话（同时删除消息）；情绪日记新增（同一用户同一天重复提交会覆盖原记录并清空旧 `ai_emotion_analysis`）、后台分页（可按用户/评分范围筛选）、删除；知识分类（启用分类扁平列表）、文章分页/详情/新增/编辑/发布下线/删除（普通用户只能看已发布，阅读详情累加 `read_count`，且显式写 `updated_at = updated_at` 防止 MySQL 自动更新发布时间）；文章封面通过阿里云 OSS 上传；数据看板总览；会话情绪分析（最近 20 条消息交给 DeepSeek，解析为 `SessionEmotionVO`，缓存到 `consultation_session.last_emotion_analysis`，有新消息才重新分析）。AI 对话通过 Spring AI 接入 DeepSeek，以 SSE 流式返回。收藏功能仍只有 DTO 设计，没有实现。

### 接口

- `POST /api/user/login`：登录，返回 JWT + 用户详情
- `POST /api/user/add`：注册，返回用户详情
- `GET /api/user/current`：获取当前登录用户
- `POST /api/user/logout`：退出登录（JWT 无状态，仅返回成功）
- `POST /api/psychological-chat/session/start`：创建咨询会话
- `POST /api/psychological-chat/stream`：SSE 流式 AI 对话
- `GET /api/psychological-chat/sessions`：会话分页列表（兼容 `pageNum/pageSize` 与 `currentPage/size`）
- `GET /api/psychological-chat/sessions/{sessionId}/messages`：会话消息详情
- `DELETE /api/psychological-chat/sessions/{sessionId}`：删除会话及其消息
- `GET /api/psychological-chat/session/{sessionId}/emotion`：会话情绪分析（`sessionId` 传 `session_<id>`，有缓存且无新消息时直接返回缓存）
- `POST /api/emotion-diary`：新增情绪日记（同一天重复提交覆盖并清空旧 AI 分析）
- `GET /api/emotion-diary/admin/page`：后台分页查询，支持 `userId`、`minMoodScore`、`maxMoodScore`
- `DELETE /api/emotion-diary/admin/{id}`：后台删除情绪日记
- `GET /api/knowledge/category/tree`：启用的分类扁平列表
- `GET /api/knowledge/article/page`：文章分页（管理员看全部，普通用户只看已发布；支持 `title`、`categoryId`、`status`、`sortField`、`sortDirection`）
- `GET /api/knowledge/article/{id}`：文章详情（普通用户阅读时累加阅读量）
- `POST /api/knowledge/article`：新增文章（管理员，默认已发布）
- `PUT /api/knowledge/article/{id}`：编辑文章（管理员）
- `PUT /api/knowledge/article/{id}/status`：发布/下线/草稿（管理员）
- `DELETE /api/knowledge/article/{id}`：删除文章（管理员）
- `POST /api/file/upload`：上传文章封面到 OSS（管理员），返回 `/article/cover/xxx.png` 相对路径
- `GET /api/data-analytics/overview`：管理后台数据看板总览（管理员）

## 依赖约束（重要）

- **MyBatis-Plus 必须使用 `mybatis-plus-spring-boot4-starter`**（当前 3.5.18）。不要降级为 `mybatis-plus-spring-boot3-starter`：Spring Boot 4 把 `DataSourceAutoConfiguration` 移到了 `org.springframework.boot.jdbc.autoconfigure` 包，Boot3 starter 的 `@AutoConfigureAfter` 失效，会导致 `MybatisPlusAutoConfiguration` 条件不满足、Mapper 不被扫描，启动时报 `UserMapper` bean 找不到。
- 分页使用 `PaginationInnerInterceptor`（`config/MybatisPlusConfig`），必须同时引入 `mybatis-plus-jsqlparser-4.9:3.5.18`；MyBatis-Plus 3.5.9+ 已把它拆成可选依赖，不引入会找不到分页相关类。
- Spring Boot 4 使用 **Jackson 3**（`tools.jackson`），依赖 Jackson 2 的第三方库可能不兼容。
- 主要依赖：`spring-boot-starter-webmvc`（Boot 4 的新命名）、`spring-boot-starter-data-jdbc`、`spring-boot-starter-security`、`spring-boot-starter-validation`、MyBatis-Plus、`spring-ai-starter-model-openai 2.0.0`、`java-jwt 4.4.0`、`hutool-all`、`aliyun-sdk-oss 3.18.4`、Lombok。
- 配置集中在 `src/main/resources/application.yml`（数据源、端口、`jwt.*`、`spring.ai.openai.*`、`oss.*`）。当前含明文数据库口令与 JWT 密钥，改动配置时注意不要扩散。
- 文件上传走阿里云 OSS：`oss.endpoint` 默认 `oss-cn-beijing.aliyuncs.com`、`oss.bucket` 默认 `java-yzz-web`；`OSS_ACCESS_KEY_ID`、`OSS_ACCESS_KEY_SECRET` 通过环境变量注入，不要在 `application.yml` 写死。前端用 `fileBaseUrl`（`https://java-yzz-web.oss-cn-beijing.aliyuncs.com`）拼接后端返回的相对路径，bucket 需要公共读；如果是私有 bucket，需要改成签名 URL 方案。
- DeepSeek 通过 OpenAI 兼容接口接入：`base-url: https://api.deepseek.com/v1`、`model: deepseek-chat`，密钥由环境变量 `DEEPSEEK_API_KEY` 提供；`config/AiChatClientConfig` 定义 `@Bean("open-ai")` 的聊天 `ChatClient`（心理咨询 system prompt）和 `@Bean("emotion-analysis")` 的情绪分析 `ChatClient`。现在有多个 `ChatClient` Bean，按类型注入时必须用 `@Qualifier`。
- AI 对话的历史上下文由 `PsychologicalSupportService` 从 MySQL 读取（最近 20 条）并随请求传入；不要额外挂 `MessageChatMemoryAdvisor`/`ChatMemory`，否则会重复历史，且未传 `ChatMemory.CONVERSATION_ID` 时会直接抛 `conversationId cannot be null`。

## 代码约定

- 注释、异常提示、接口描述均为中文。
- 包名存在大写目录：`DTO`、`Enum`、`service/Impl`，新建类时保持与现有结构一致。
- 命名：请求对象 `*CommandDTO`，响应对象 `*VO` 或 `*ResponseDTO`。
- `Result` 的静态工厂：`Result.ok()`、`Result.ok(data)`、`Result.error()`、`Result.error(code, message, data)`。
- 枚举（`Enum/UserStatus`、`Enum/UserType`）用 `Integer code` + `String description`，并提供 `fromCode`/`isValidCode`。
