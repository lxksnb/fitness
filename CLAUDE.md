# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作提供指导。

## 项目概览

本仓库是一个健身追踪应用，拆分为两个独立项目：

- `fitness-server/`：Spring Boot 2.7 后端，使用 Java 8、Spring Security、JWT、MyBatis XML Mapper、MySQL 和 Redis。
- `fitness-web/`：Vue 3 + TypeScript + Vite 前端，使用 Element Plus、Pinia、Vue Router、Axios、ECharts 和 Sass。

前端通过 `/api/v1` 调用后端；开发环境中 Vite 会将 `/api` 代理到 `http://localhost:8080`。

## 常用命令

在 `fitness-server/` 目录运行后端命令：

```bash
mvn spring-boot:run
mvn test
mvn test -Dtest=ApiContractTest
mvn test -Dtest=JacksonContractTest#parsesFrontendTrainingDateAndTimePayload
mvn package
```

在 `fitness-web/` 目录运行前端命令：

```bash
npm install
npm run dev
npm run build
npm run preview
```

当前 `fitness-web/package.json` 没有定义前端 lint 或 test 脚本；`npm run build` 会执行 `vue-tsc -b && vite build`。

## 后端架构

`com.fitness.FitnessApplication` 启动 Spring Boot，并扫描 `com.fitness.mapper` 下的 MyBatis Mapper 接口。

后端代码采用常见的 controller/service/mapper/entity 分层：

- `controller/` 和 `controller/admin/` 分别暴露普通用户 API 和管理员 API。
- `service/` 包含业务逻辑，并协调 Mapper 调用。
- `mapper/*.java` 是 MyBatis Mapper 接口；SQL 位于 `src/main/resources/mapper/*.xml`。
- `entity/` 映射数据库表；请求和响应结构位于 `dto/` 中。
- `common/Result` 和 `ResultCode` 定义标准 API 响应包装结构，前端 Axios 封装依赖该结构。
- `exception/GlobalExceptionHandler` 统一处理异常响应。

配置位于 `src/main/resources/application.yml`，当前激活 profile 为 `dev`。`application-dev.yml` 指向本地 MySQL 数据库 `fitness`（`127.0.0.1:3306`）和 Redis（`127.0.0.1:6379`）。初始化 schema/数据位于 `src/main/resources/db/init.sql`。

安全机制基于无状态 JWT。`SecurityConfig` 放行 `/api/v1/auth/**` 和 GET `/api/v1/dict/**`，限制 `/api/v1/admin/**` 仅 `ADMIN` 可访问，其余接口都需要认证。`JwtAuthenticationFilter`、`JwtUtils` 和 `SecurityUtils` 负责 Token 解析和当前用户访问。

Jackson 在 `config/JacksonConfig` 和 `MultiDateFormatDeserializer` 中做了自定义；契约测试覆盖了与前端日期/时间载荷兼容相关的行为。

## 前端架构

`fitness-web/src/main.ts` 启动 Vue。`src/App.vue` 渲染路由。`@` 别名指向 `fitness-web/src`。

重要前端结构：

- `api/request.ts` 创建共享 Axios 实例，设置 `baseURL: '/api/v1'`，附加 `accessToken`，解包后端 `Result` 响应，并在 401 时使用 `refreshToken` 刷新 JWT。
- `api/*.ts` 按业务领域组织接口调用。
- `router/index.ts` 定义公开认证路由，以及 `components/layout/AppLayout.vue` 下的认证后路由；带 `meta.admin` 的路由要求 localStorage 中的 `role` 为 `ADMIN`。
- `stores/user.ts` 通过 Pinia 保存用户状态。
- `views/` 包含仪表盘、计划、饮食、训练、身体数据、库、个人资料和管理后台等功能页面。
- `components/layout/` 提供应用外壳；`components/common/` 包含上传、休息计时器等共享组件。
- `styles/theme.scss` 和 `styles/global.scss` 定义共享样式。

## 跨层约定

- 后端接口成功时应返回 `code === 200` 的 `Result` 对象，因为 `api/request.ts` 只在这种情况下解包 `response.data.data`。
- 认证 Token 和角色保存在 localStorage 中，键名分别为 `accessToken`、`refreshToken` 和 `role`。
- 日期/时间格式是前后端 API 边界的重要约定；修改日期、时间或 DTO 序列化行为时，要同步更新后端 Jackson 测试。
- 修改数据库字段时，需要同时更新 Java entity/DTO、Mapper 接口、对应 XML Mapper SQL 和 `db/init.sql`。
