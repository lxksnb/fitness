# 健身记录网页 · 产品设计文档

> 版本: 1.0 | 日期: 2026-05-19 | 状态: 设计完成

---

## 一、产品概述

### 1.1 产品定位

面向全水平健身人群（新手到进阶）的免费多用户健身记录平台。先实现 Web 版，架构预留小程序/APP 扩展空间。

### 1.2 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue3 + Element Plus + ECharts + Pinia + Axios |
| 后端 | Spring Boot + MyBatis + MySQL |
| 缓存 | Redis |
| 认证 | JWT (Access Token 2h + Refresh Token 7d) |
| 部署 | Docker Compose (Nginx + 前端 + 后端 + MySQL + Redis) |
| API | RESTful, `/api/v1/` 版本化前缀 |

### 1.3 角色

| 角色 | 说明 |
|------|------|
| 普通用户 (USER) | 注册后默认角色，管理自己的全部数据 |
| 管理员 (ADMIN) | 管理公共食物库、公共动作库、计划模板市场、系统字典 |

---

## 二、核心数据模型

### 2.1 通用原则

- 所有涉及"类型"的字段统一走字典表，后续新增类型只需加数据不碰代码
- 每张业务表必备字段: `id`, `created_at`, `updated_at`（下文省略）
- 所有业务表通过 `user_id` 实现数据隔离
- 热量公式: **kcal = 碳水g×4 + 蛋白质g×4 + 脂肪g×9**，由系统计算不由用户填写

### 2.2 字典表

```
sys_dict_type
├── id, dict_code, dict_name, status

sys_dict_data
├── id, dict_type_id, dict_label, dict_value, sort, status
```

预置字典类型: `training_type`(训练类型), `meal_type`(餐次类型), `plan_type`(计划目标), `split_type`(分化类型), `day_type`(训练/休息), `food_unit_type`(食物单位类型), `photo_type`(照片角度), `difficulty`(难度)

### 2.3 用户表

```
sys_user
├── id, username, password, nickname, email, avatar
├── role: USER / ADMIN
├── status: ACTIVE / DISABLED

user_profile (1:1)
├── user_id
├── gender, birthday, height_cm
├── target_type: CUT / BULK / MAINTAIN
├── target_weight_kg, target_date
├── weekly_change_rate (如 +0.25 增肌 / -0.5 减脂)
```

---

## 三、功能模块详设

### 3.1 体重记录

```
weight_record
├── id, user_id
├── record_date (同一天可覆盖更新)
├── weight_kg (精确到0.1)
├── bmi (系统根据 体重/(身高/100)² 自动计算)
├── note
```

图表: 折线图(趋势)、散点图(分布)，支持周/月/年/自定义时间范围筛选。

---

### 3.2 身体围度记录

```
body_measurement
├── id, user_id, record_date
├── chest_cm, waist_cm, left_arm_cm, right_arm_cm
├── left_thigh_cm, right_thigh_cm, hip_cm, neck_cm (全部可选)
└── note
```

配合折线图追踪各围度随时间变化。

---

### 3.3 身体照片记录

```
body_photo
├── id, user_id
├── photo_date
├── photo_type: FRONT / SIDE / BACK (字典)
├── image_url
└── note
```

---

### 3.4 饮水记录

```
water_record
├── id, user_id
├── record_date
├── amount_ml (本次饮水量)
├── recorded_at
```

首页展示当日饮水进度条，默认目标 2000ml/天。

---

### 3.5 健身计划

#### 表结构

```
fitness_plan (计划主表)
├── id, user_id
├── plan_name
├── plan_type: CUT / BULK / MAINTAIN (字典)
├── split_type: FULL_BODY / THREE_DAY / FOUR_DAY / FIVE_DAY / PUSH_PULL_LEGS / CUSTOM (字典)
├── is_active (同时只能激活一个计划)

plan_training_day (每天安排, N:1 → plan)
├── id, plan_id
├── day_order (第1天, 第2天...)
├── day_type: TRAINING / REST (字典)
├── training_type (练胸/练背/练腿/练肩/有氧..., 仅TRAINING时有值, 字典)
├── carb_multiplier (该训练日碳水倍数)
├── protein_multiplier
├── fat_multiplier

plan_training_action (动作明细, N:1 → training_day)
├── id, training_day_id
├── action_id, action_name (冗余)
├── min_sets, max_sets
├── rest_minutes (组间休息分钟)
├── sort_order

plan_meal_config (餐次配置, N:1 → plan, 与training_day平级)
├── id, plan_id
├── day_type: TRAINING / REST
├── meal_type (早/午/晚/夜宵/练前/练后/其他, 字典)
├── carb_ratio (该餐占全天碳水百分比, 如0.2=20%)
├── protein_ratio
├── fat_ratio
├── sort_order

plan_meal_food (餐次推荐食物, N:1 → meal_config)
├── id, meal_config_id
├── food_id, food_name (冗余)
├── suggested_amount_g (建议份量)
└── sort_order
```

#### 计算示例 (65kg, 练腿日: 碳4/蛋2/脂1)

```
每日总量: 碳水=65×4=260g, 蛋白=65×2=130g, 脂肪=65×1=65g
总热量: 260×4 + 130×4 + 65×9 = 2145 kcal

早餐(碳水20%, 蛋白20%, 脂肪15%):
  碳水=52g, 蛋白=26g, 脂肪≈10g
午餐(碳水30%, 蛋白30%, 脂肪30%):
  碳水=78g, 蛋白=39g, 脂肪≈20g
...以此类推
```

---

### 3.6 计划模板市场

管理员预置通用计划模板（如"新手三分化减脂"、"进阶五分化增肌"），用户可浏览并一键导入为自己的计划，导入后完全独立可修改。

```
plan_template (计划模板)
├── id
├── template_name, description
├── plan_type, split_type
├── difficulty: BEGINNER / INTERMEDIATE / ADVANCED (字典)
├── created_by (管理员)
├── status: ACTIVE / DISABLED
```

模板的 training_day、meal_config、training_action 结构复用业务表，通过 `template_id` 关联。

---

### 3.7 食物库

```
food_library (食物库)
├── id
├── scope: SYSTEM / USER
├── user_id (scope=USER时有值, SYSTEM为null)
├── food_name
├── image_url (食物主图)
├── status: ACTIVE / DELETED
├── created_by (管理员录入时记录)

food_nutrition (食物营养表, N:1 → food)
├── id, food_id
├── unit_type (字典: PER_100G / PER_SMALL_SERVING / PER_SERVING / PER_LARGE_SERVING / ...)
├── serving_weight_g (该单位对应大致克数, PER_100G固定100)
├── carb_grams, protein_grams, fat_grams
├── calories (系统计算)
├── image_url (该单位的参考图)
```

示例数据:

```
鸡蛋:
  ├── PER_100G:          weight=100g, 碳1.1g, 蛋13.1g, 脂8.6g
  ├── PER_SMALL_SERVING: weight≈35g,  碳0.4g, 蛋4.6g,  脂3.0g
  └── PER_SERVING:       weight≈50g,  碳0.6g, 蛋6.6g,  脂4.3g
```

---

### 3.8 饮食记录

```
diet_record
├── id, user_id
├── record_date
├── meal_type (早/午/晚/夜宵/练前/练后/其他, 字典)
├── food_name (文字描述)
├── image_url (食物照片)
├── carb_grams, protein_grams, fat_grams
├── calories (系统计算)
├── recorded_at (记录时间, 默认当前时间)
```

**记录流程:**
1. 用户选择餐次类型 + 日期
2. 搜索食物库 → 选择食物 + 选择单位 → 自动填入碳蛋脂
3. 用户可手动微调数值
4. 也可不导入食物，直接手填碳蛋脂
5. 上传食物照片（可选）
6. 系统自动计算热量

---

### 3.9 当日饮食概览

在饮食记录页面顶部卡片展示:

```
目标 (练腿日, 65kg, 碳4/蛋2/脂1):
  碳水 260g | 蛋白质 130g | 脂肪 65g | 热量 2145 kcal

已吃 (今日实际):
  碳水 180g | 蛋白质 90g | 脂肪 40g | 热量 1440 kcal

还需:
  碳水 80g | 蛋白质 40g | 脂肪 25g | 热量 705 kcal

进度条: ████████░░░░░░░░ 67%
```

每餐也有目标 vs 实际对比展示。

---

### 3.10 动作库

```
action_library
├── id
├── scope: SYSTEM / USER
├── user_id (scope=USER时有值)
├── action_name
├── description
├── suitable_for (多选: CHEST/BACK/LEGS/SHOULDER/ARMS/CORE/CARDIO, 逗号分隔)
├── image_urls (JSON数组)
├── video_url
├── status: ACTIVE / DELETED

user_action_record (用户动作重量演进)
├── id, user_id, action_id
├── record_date
├── weight_kg (使用重量)
├── max_reps (该重量下的最大次数)
└── note
```

---

### 3.11 每日训练记录

```
training_record
├── id, user_id
├── record_date
├── plan_id, training_day_id (可空, 关联来源计划)
├── training_type (练胸/练背/..., 字典)
├── start_time, end_time
├── duration_minutes
├── note

training_record_detail
├── id, training_record_id
├── action_id, action_name (冗余)
├── sets (实际做了几组)
├── weight_kg (使用的重量)
├── sort_order
```

**导入功能:** 创建训练记录时如关联了计划中的训练日，自动带入当天计划动作列表作为模板，用户可增删动作、调整组数和重量。
**训练休息计时器:** 纯前端，训练记录页面内置倒计时，从计划 `rest_minutes` 读取默认值，到点提醒。

---

### 3.12 训练打卡日历

基于 `training_record.record_date` 统计，ECharts 热力图实现（类似 GitHub 贡献图），直观展示训练频率和连续性。纯查询不建表。

---

### 3.13 数据导出

支持导出为 CSV: 体重记录、饮食记录、训练记录，按时间范围筛选。后端实现，纯功能不建表。

---

## 四、首页看板

用户登录后落地页，汇总当日关键数据、快捷入口:

```
┌──────────────────────────────────────────────────────┐
│  欢迎, 张三                           目标: 增肌 →  │
│  目标体重 70kg · 预计2026-08-19 · 每周+0.25kg       │
├──────────┬──────────┬──────────┬──────────┬──────────┤
│  今日体重  │  今日热量  │  今日训练  │  连续打卡  │  饮水    │
│  65.0 kg │1440/2145│ 练胸60min│  🔥15天  │1200/2000│
│  vs昨-0.2 │  已吃67% │ ✅已完成  │          │  60%    │
├──────────┴──────────┴──────────┴──────────┴──────────┤
│  碳水    ████████░░░░░░░░  180/260g  (69%)          │
│  蛋白质  ██████░░░░░░░░░░   90/130g  (69%)          │
│  脂肪    ████████░░░░░░░░   40/65g   (62%)          │
│  饮水    ██████░░░░░░░░░░  1200/2000ml (60%)        │
├──────────────────────────────────────────────────────┤
│  体重趋势 (近30天)   📈折线图                          │
├──────────────────────┬───────────────────────────────┤
│  今日饮食              │  今日训练                     │
│  早餐: 燕麦+鸡蛋 380kcal│  暂无训练                    │
│  午餐: ...             │                              │
│  [+ 添加饮食]           │  [+ 开始训练]                │
└──────────────────────┴───────────────────────────────┘
```

---

## 五、API 设计概要

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | `POST /api/v1/auth/register` | 注册 |
| 认证 | `POST /api/v1/auth/login` | 登录, 返回 JWT |
| 认证 | `POST /api/v1/auth/refresh` | 刷新 token |
| 用户 | `GET/PUT /api/v1/user/profile` | 获取/更新个人信息 |
| 体重 | `GET/POST /api/v1/weight` | 体重记录 CRUD |
| 体重 | `GET /api/v1/weight/trend` | 体重趋势数据 |
| 围度 | `GET/POST /api/v1/measurement` | 围度记录 CRUD |
| 照片 | `GET/POST/DELETE /api/v1/body-photo` | 身体照片 CRUD |
| 饮水 | `GET/POST /api/v1/water` | 饮水记录 |
| 计划 | `GET/POST/PUT/DELETE /api/v1/plans` | 健身计划 CRUD |
| 计划 | `PUT /api/v1/plans/:id/activate` | 激活计划 |
| 计划 | `GET /api/v1/plan-templates` | 计划模板列表 |
| 计划 | `POST /api/v1/plan-templates/:id/import` | 导入模板 |
| 食物库 | `GET/POST/PUT/DELETE /api/v1/foods` | 食物 CRUD (含搜索) |
| 饮食 | `GET/POST/PUT/DELETE /api/v1/diet` | 饮食记录 CRUD |
| 饮食 | `GET /api/v1/diet/daily-summary` | 当日饮食概览 |
| 动作库 | `GET/POST/PUT/DELETE /api/v1/actions` | 动作 CRUD |
| 训练 | `GET/POST/PUT/DELETE /api/v1/training` | 训练记录 CRUD |
| 训练 | `GET /api/v1/training/calendar` | 训练打卡日历数据 |
| 导出 | `GET /api/v1/export/:type` | 导出 CSV |
| 字典 | `GET /api/v1/dict/:code` | 获取字典数据 |
| 文件 | `POST /api/v1/upload` | 文件上传 |

所有业务 API 需带 `Authorization: Bearer <token>`，后端通过 `user_id` 隔离数据。管理员接口加 `@AdminOnly` 注解。

---

## 六、架构设计

```
┌────────────────────────────────────────────────────┐
│                     Nginx (:80)                      │
│  静态资源 /static  →  前端 SPA                        │
│  API 请求 /api/*   →  后端 Spring Boot (:8080)        │
│  上传文件 /uploads  →  本地文件存储                     │
├───────────────────────┬────────────────────────────┤
│   Vue3 SPA (Nginx)    │   Spring Boot (:8080)        │
│   Element Plus        │   Spring Security + JWT       │
│   ECharts + Pinia     │   MyBatis                    │
│   Axios (拦截器自动     │   API v1 版本化               │
│     附加JWT+刷新)      │   全局异常处理 + 统一响应体      │
├───────────────────────┼────────────────────────────┤
│                       │   MySQL (:3306)              │
│   本地文件存储          │   业务数据库                   │
│   /uploads/            │                              │
│                       │   Redis (:6379)              │
│                       │   Token黑名单 + 缓存           │
└───────────────────────┴────────────────────────────┘
```

### 前端项目结构

```
src/
├── api/          # 接口请求封装
├── components/   # 公共组件 (计时器、图片上传、食物选择器等)
├── composables/  # 组合式函数
├── layouts/      # 布局组件
├── router/       # 路由 + 导航守卫
├── stores/       # Pinia 状态管理 (user, diet, training)
├── utils/        # 工具函数 (热量计算、token管理)
└── views/        # 页面
    ├── home/          # 首页看板
    ├── weight/        # 体重管理
    ├── plan/          # 健身计划
    ├── diet/          # 饮食记录 + 概览
    ├── food-library/  # 食物库
    ├── action-library/# 动作库
    ├── training/      # 训练记录
    ├── measurement/   # 身体围度
    ├── photo/         # 身体照片
    ├── water/         # 饮水记录
    ├── profile/       # 个人设置
    └── admin/         # 管理后台
```

### 后端项目结构

```
src/main/java/com/fitness/
├── config/       # Spring Security, CORS, 文件上传配置
├── controller/   # 控制器
├── service/      # 业务逻辑
├── mapper/       # MyBatis Mapper
├── entity/       # 实体类
├── dto/          # 请求/响应 DTO
├── vo/           # 视图对象
├── enums/        # 枚举
├── utils/        # JWT工具, 热量计算工具
├── interceptor/  # 拦截器 (认证, 日志)
└── exception/    # 全局异常处理
```

---

## 七、移动端扩展预留

为后续小程序/APP 预留:

1. **JWT 认证** — 天然支持移动端，无需改造
2. **API 版本化** — `/api/v1/` 前缀，移动端可平滑升级
3. **前后端分离** — 移动端直接复用现有 API
4. **文件上传接口** — 通用上传端点，移动端直接调用
5. **CORS 配置** — 后端允许跨域，移动端/不同域名直接访问

---

## 八、完整功能清单

| # | 功能模块 | 子功能 |
|---|---------|--------|
| 1 | 用户系统 | 注册、登录、JWT刷新、个人信息、身体档案 |
| 2 | 体重管理 | 记录、折线图、散点图、趋势分析、BMI自动计算 |
| 3 | 身体围度 | 胸/腰/臂/腿/臀/颈围度记录与趋势图 |
| 4 | 身体照片 | 正面/侧面/背面照片按日期对比 |
| 5 | 饮水记录 | 每日饮水量、进度条 |
| 6 | 健身计划 | 自建计划(减脂/增肌)、分化类型、训练日/休息日配置 |
| 7 | 餐次配置 | 训练日/休息日各餐碳蛋脂比例、推荐食物 |
| 8 | 计划模板 | 管理员预置、用户浏览、一键导入 |
| 9 | 食物库 | 系统公共库+用户私有库、多单位灵活配置、图片 |
| 10 | 饮食记录 | 按餐次记录、食物库导入、手填、图片、热量自动计算 |
| 11 | 饮食概览 | 当日已吃vs还需、每餐对比、进度条 |
| 12 | 动作库 | 系统+用户动作、图片、视频、重量演进记录 |
| 13 | 训练记录 | 每日训练、计划导入、组数重量记录 |
| 14 | 训练计时器 | 组间休息倒计时 |
| 15 | 训练打卡 | 热力图日历 |
| 16 | 首页看板 | 当日数据汇总、趋势图、快捷入口 |
| 17 | 数据导出 | 体重/饮食/训练 CSV导出 |
| 18 | 字典管理 | 管理员维护所有类型数据 |
| 19 | 管理后台 | 食物库管理、动作库管理、模板管理、用户管理 |

---

## 九、待定事项

以下不在当前版本范围内，后续迭代考虑:

- 社交功能 (关注、动态、排行榜)
- AI 饮食/训练建议
- 小程序版、APP版
- 国际化 (i18n)
- 消息通知 (训练提醒、饮食提醒)
