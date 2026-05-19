# 健身记录网页 · 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个多用户健身记录平台，支持体重追踪、饮食记录、健身计划管理、训练记录等 19 个功能模块。

**Architecture:** 前后端分离架构。后端 Spring Boot + MyBatis 提供 RESTful API，JWT 认证，Redis 缓存。前端 Vue3 + Element Plus + ECharts 构建 SPA。Docker Compose 一键部署。

**Tech Stack:** Spring Boot 2.7+, MyBatis 3, MySQL 8, Redis 7, Vue 3, Element Plus, ECharts 5, Pinia, Axios, Docker, Nginx

---

## Phase 1: 项目初始化

### Task 1: 创建 Spring Boot 后端项目

**Files:**
- Create: `fitness-server/pom.xml`
- Create: `fitness-server/src/main/java/com/fitness/FitnessApplication.java`
- Create: `fitness-server/src/main/resources/application.yml`

- [ ] **Step 1: 编写 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>
    <groupId>com.fitness</groupId>
    <artifactId>fitness-server</artifactId>
    <version>1.0.0</version>
    <name>fitness-server</name>

    <properties>
        <java.version>17</java.version>
        <mybatis-spring-boot.version>2.3.1</mybatis-spring-boot.version>
        <jjwt.version>0.11.5</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis-spring-boot.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.23</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 编写启动类**

```java
package com.fitness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fitness.mapper")
public class FitnessApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitnessApplication.class, args);
    }
}
```

- [ ] **Step 3: 编写 application.yml**

```yaml
server:
  port: 8080

spring:
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.fitness.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

jwt:
  secret: fitness-app-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256
  access-token-expiration: 7200000
  refresh-token-expiration: 604800000
```

- [ ] **Step 4: 编写 application-dev.yml**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fitness?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
```

- [ ] **Step 5: 初始化 Maven 依赖**

Run: `cd fitness-server && mvn clean compile -q`
Expected: BUILD SUCCESS

---

### Task 2: 创建 Vue3 前端项目

**Files:**
- Create: `fitness-web/package.json`
- Create: `fitness-web/vite.config.ts`
- Create: `fitness-web/index.html`
- Create: `fitness-web/src/main.ts`
- Create: `fitness-web/src/App.vue`
- Create: `fitness-web/tsconfig.json`
- Create: `fitness-web/tsconfig.node.json`

- [ ] **Step 1: 初始化 Vite + Vue3 项目**

Run:
```bash
cd "D:/Java Project/fitness"
npm create vite@latest fitness-web -- --template vue-ts
```

Expected: Scaffolded successfully.

- [ ] **Step 2: 安装依赖**

Run:
```bash
cd fitness-web
npm install
npm install element-plus @element-plus/icons-vue echarts vue-echarts pinia axios vue-router@4
npm install -D sass @types/node
```

Expected: All packages installed.

- [ ] **Step 3: 配置 vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 4: 编写 main.ts**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/global.scss'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
```

- [ ] **Step 5: 编写 App.vue**

```vue
<template>
  <router-view />
</template>
```

- [ ] **Step 6: 编写 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "preserve",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "noEmit": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 7: 创建 global.scss**

```scss
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f5f7fa;
}
```

- [ ] **Step 8: 验证前端启动**

Run: `cd fitness-web && npm run dev`
Expected: Vite dev server running on http://localhost:3000

---

### Task 3: Docker Compose 基础设施

**Files:**
- Create: `docker-compose.yml`
- Create: `fitness-server/Dockerfile`
- Create: `fitness-web/Dockerfile`
- Create: `fitness-web/nginx.conf`

- [ ] **Step 1: 编写 MySQL + Redis docker-compose**

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: fitness-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: fitness
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./fitness-server/src/main/resources/db/init.sql:/docker-entrypoint-initdb.d/init.sql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    container_name: fitness-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

volumes:
  mysql-data:
  redis-data:
```

- [ ] **Step 2: 编写后端 Dockerfile**

```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/fitness-server-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

- [ ] **Step 3: 编写前端 Dockerfile + nginx.conf**

```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

```nginx
server {
    listen 80;
    server_name localhost;

    location /api/ {
        proxy_pass http://fitness-server:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /uploads/ {
        alias /app/uploads/;
    }

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
```

- [ ] **Step 4: 启动基础设施**

Run: `docker-compose up -d mysql redis`
Expected: MySQL and Redis containers running.

---

## Phase 2: 数据库初始化

### Task 4: 编写完整建表 SQL

**Files:**
- Create: `fitness-server/src/main/resources/db/init.sql`

- [ ] **Step 1: 编写 init.sql**

```sql
CREATE DATABASE IF NOT EXISTS fitness DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitness;

-- 用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    avatar VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 用户身体档案
CREATE TABLE user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    gender VARCHAR(10) COMMENT 'MALE/FEMALE',
    birthday DATE,
    height_cm DECIMAL(5,1),
    target_type VARCHAR(20) COMMENT 'CUT/BULK/MAINTAIN',
    target_weight_kg DECIMAL(5,1),
    target_date DATE,
    weekly_change_rate DECIMAL(4,2) COMMENT '每周目标变化kg',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '用户身体档案';

-- 字典类型表
CREATE TABLE sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL UNIQUE,
    dict_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '字典类型表';

-- 字典数据表
CREATE TABLE sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL,
    dict_label VARCHAR(100) NOT NULL,
    dict_value VARCHAR(100) NOT NULL,
    sort INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dict_type_id) REFERENCES sys_dict_type(id)
) COMMENT '字典数据表';

-- 体重记录表
CREATE TABLE weight_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    weight_kg DECIMAL(4,1) NOT NULL,
    bmi DECIMAL(4,1) COMMENT '系统计算',
    note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, record_date),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '体重记录表';

-- 身体围度记录表
CREATE TABLE body_measurement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    chest_cm DECIMAL(5,1),
    waist_cm DECIMAL(5,1),
    left_arm_cm DECIMAL(5,1),
    right_arm_cm DECIMAL(5,1),
    left_thigh_cm DECIMAL(5,1),
    right_thigh_cm DECIMAL(5,1),
    hip_cm DECIMAL(5,1),
    neck_cm DECIMAL(5,1),
    note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '身体围度记录表';

-- 身体照片表
CREATE TABLE body_photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    photo_date DATE NOT NULL,
    photo_type VARCHAR(20) NOT NULL COMMENT 'FRONT/SIDE/BACK',
    image_url VARCHAR(500) NOT NULL,
    note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '身体照片表';

-- 饮水记录表
CREATE TABLE water_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    amount_ml INT NOT NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '饮水记录表';

-- 健身计划主表
CREATE TABLE fitness_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    plan_type VARCHAR(20) NOT NULL COMMENT 'CUT/BULK/MAINTAIN',
    split_type VARCHAR(50) NOT NULL COMMENT 'FULL_BODY/THREE_DAY/FOUR_DAY/FIVE_DAY/PUSH_PULL_LEGS/CUSTOM',
    is_active TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '健身计划主表';

-- 训练日表
CREATE TABLE plan_training_day (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    day_order INT NOT NULL,
    day_type VARCHAR(20) NOT NULL COMMENT 'TRAINING/REST',
    training_type VARCHAR(50) COMMENT '练胸/练背/练腿/练肩/有氧, TRAINING时有值',
    carb_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0,
    protein_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0,
    fat_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES fitness_plan(id) ON DELETE CASCADE
) COMMENT '训练日表';

-- 训练动作明细表
CREATE TABLE plan_training_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    training_day_id BIGINT NOT NULL,
    action_id BIGINT NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    min_sets INT NOT NULL DEFAULT 3,
    max_sets INT NOT NULL DEFAULT 5,
    rest_minutes INT NOT NULL DEFAULT 2,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (training_day_id) REFERENCES plan_training_day(id) ON DELETE CASCADE
) COMMENT '训练动作明细表';

-- 餐次配置表 (与training_day平级)
CREATE TABLE plan_meal_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    day_type VARCHAR(20) NOT NULL COMMENT 'TRAINING/REST',
    meal_type VARCHAR(50) NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER/SUPPER/PRE_WORKOUT/POST_WORKOUT/OTHER',
    carb_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '碳水占比',
    protein_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '蛋白质占比',
    fat_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '脂肪占比',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES fitness_plan(id) ON DELETE CASCADE
) COMMENT '餐次配置表';

-- 餐次推荐食物表
CREATE TABLE plan_meal_food (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_config_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    suggested_amount_g INT NOT NULL DEFAULT 100,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (meal_config_id) REFERENCES plan_meal_config(id) ON DELETE CASCADE
) COMMENT '餐次推荐食物表';

-- 计划模板表
CREATE TABLE plan_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    plan_type VARCHAR(20) NOT NULL,
    split_type VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20) NOT NULL COMMENT 'BEGINNER/INTERMEDIATE/ADVANCED',
    created_by BIGINT COMMENT '管理员ID',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) COMMENT '计划模板表';

-- 模板训练日
CREATE TABLE plan_template_day (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    day_order INT NOT NULL,
    day_type VARCHAR(20) NOT NULL,
    training_type VARCHAR(50),
    carb_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0,
    protein_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0,
    fat_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES plan_template(id) ON DELETE CASCADE
) COMMENT '模板训练日';

-- 模板动作明细
CREATE TABLE plan_template_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_day_id BIGINT NOT NULL,
    action_id BIGINT NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    min_sets INT NOT NULL DEFAULT 3,
    max_sets INT NOT NULL DEFAULT 5,
    rest_minutes INT NOT NULL DEFAULT 2,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_day_id) REFERENCES plan_template_day(id) ON DELETE CASCADE
) COMMENT '模板动作明细表';

-- 模板餐次配置
CREATE TABLE plan_template_meal_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    day_type VARCHAR(20) NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    carb_ratio DECIMAL(4,3) NOT NULL DEFAULT 0,
    protein_ratio DECIMAL(4,3) NOT NULL DEFAULT 0,
    fat_ratio DECIMAL(4,3) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES plan_template(id) ON DELETE CASCADE
) COMMENT '模板餐次配置表';

-- 模板餐次推荐食物
CREATE TABLE plan_template_meal_food (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_config_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    suggested_amount_g INT NOT NULL DEFAULT 100,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (meal_config_id) REFERENCES plan_template_meal_config(id) ON DELETE CASCADE
) COMMENT '模板餐次推荐食物表';

-- 食物库
CREATE TABLE food_library (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'SYSTEM/USER',
    user_id BIGINT COMMENT 'scope=USER时有值',
    food_name VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT COMMENT '管理员录入时记录',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) COMMENT '食物库';

-- 食物营养表 (多单位)
CREATE TABLE food_nutrition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_id BIGINT NOT NULL,
    unit_type VARCHAR(50) NOT NULL COMMENT 'PER_100G/PER_SMALL_SERVING/PER_SERVING/PER_LARGE_SERVING',
    serving_weight_g DECIMAL(6,1) NOT NULL COMMENT '该单位大致克数',
    carb_grams DECIMAL(6,1) NOT NULL DEFAULT 0,
    protein_grams DECIMAL(6,1) NOT NULL DEFAULT 0,
    fat_grams DECIMAL(6,1) NOT NULL DEFAULT 0,
    calories DECIMAL(7,1) COMMENT '系统计算',
    image_url VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (food_id) REFERENCES food_library(id) ON DELETE CASCADE
) COMMENT '食物营养表';

-- 饮食记录表
CREATE TABLE diet_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    meal_type VARCHAR(50) NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER/SUPPER/PRE_WORKOUT/POST_WORKOUT/OTHER',
    food_name VARCHAR(200) NOT NULL COMMENT '食物描述',
    image_url VARCHAR(500),
    carb_grams DECIMAL(6,1) NOT NULL DEFAULT 0,
    protein_grams DECIMAL(6,1) NOT NULL DEFAULT 0,
    fat_grams DECIMAL(6,1) NOT NULL DEFAULT 0,
    calories DECIMAL(7,1) COMMENT '系统计算',
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '饮食记录表';

-- 动作库
CREATE TABLE action_library (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'SYSTEM/USER',
    user_id BIGINT COMMENT 'scope=USER时有值',
    action_name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    suitable_for VARCHAR(200) COMMENT '逗号分隔: CHEST,BACK,LEGS,SHOULDER,ARMS,CORE,CARDIO',
    image_urls JSON COMMENT '["url1","url2"]',
    video_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '动作库';

-- 用户动作重量记录
CREATE TABLE user_action_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    weight_kg DECIMAL(5,1) NOT NULL,
    max_reps INT,
    note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (action_id) REFERENCES action_library(id)
) COMMENT '用户动作重量记录';

-- 训练记录主表
CREATE TABLE training_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    plan_id BIGINT,
    training_day_id BIGINT,
    training_type VARCHAR(50) NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    duration_minutes INT,
    note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '训练记录主表';

-- 训练记录动作明细
CREATE TABLE training_record_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    training_record_id BIGINT NOT NULL,
    action_id BIGINT NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    sets INT NOT NULL,
    weight_kg DECIMAL(5,1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (training_record_id) REFERENCES training_record(id) ON DELETE CASCADE
) COMMENT '训练记录动作明细';
```

- [ ] **Step 2: 初始化字典数据**

在 init.sql 末尾追加:

```sql
-- 字典类型
INSERT INTO sys_dict_type (dict_code, dict_name) VALUES
('training_type', '训练类型'),
('meal_type', '餐次类型'),
('plan_type', '计划目标类型'),
('split_type', '分化类型'),
('day_type', '训练日/休息日'),
('food_unit_type', '食物单位类型'),
('photo_type', '照片角度'),
('difficulty', '难度等级');

-- 训练类型
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(1, '练胸', 'CHEST', 1), (1, '练背', 'BACK', 2), (1, '练腿', 'LEGS', 3),
(1, '练肩', 'SHOULDER', 4), (1, '练手臂', 'ARMS', 5), (1, '核心', 'CORE', 6),
(1, '有氧', 'CARDIO', 7), (1, '休息', 'REST', 8);

-- 餐次类型
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(2, '早餐', 'BREAKFAST', 1), (2, '午餐', 'LUNCH', 2), (2, '晚餐', 'DINNER', 3),
(2, '夜宵', 'SUPPER', 4), (2, '练前餐', 'PRE_WORKOUT', 5),
(2, '练后餐', 'POST_WORKOUT', 6), (2, '其他餐', 'OTHER', 7);

-- 计划目标类型
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(3, '减脂', 'CUT', 1), (3, '增肌', 'BULK', 2), (3, '维持', 'MAINTAIN', 3);

-- 分化类型
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(4, '全身训练', 'FULL_BODY', 1), (4, '三分化', 'THREE_DAY', 2),
(4, '四分化', 'FOUR_DAY', 3), (4, '五分化', 'FIVE_DAY', 4),
(4, '推拉腿', 'PUSH_PULL_LEGS', 5), (4, '自定义', 'CUSTOM', 6);

-- 训练日/休息日
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(5, '训练日', 'TRAINING', 1), (5, '休息日', 'REST', 2);

-- 食物单位类型
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(6, '每100g', 'PER_100G', 1), (6, '每小份', 'PER_SMALL_SERVING', 2),
(6, '每份', 'PER_SERVING', 3), (6, '每大份', 'PER_LARGE_SERVING', 4);

-- 照片角度
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(7, '正面', 'FRONT', 1), (7, '侧面', 'SIDE', 2), (7, '背面', 'BACK', 3);

-- 难度等级
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(8, '新手', 'BEGINNER', 1), (8, '中级', 'INTERMEDIATE', 2), (8, '高级', 'ADVANCED', 3);
```

---

## Phase 3: 后端基础设施

### Task 5: 统一响应体 & 异常处理

**Files:**
- Create: `fitness-server/src/main/java/com/fitness/common/Result.java`
- Create: `fitness-server/src/main/java/com/fitness/common/ResultCode.java`
- Create: `fitness-server/src/main/java/com/fitness/exception/BusinessException.java`
- Create: `fitness-server/src/main/java/com/fitness/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 编写 ResultCode 枚举**

```java
package com.fitness.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "系统内部错误");

    private final int code;
    private final String message;
}
```

- [ ] **Step 2: 编写统一响应体 Result**

```java
package com.fitness.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = ResultCode.SUCCESS.getMessage();
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(ResultCode code) {
        Result<T> r = new Result<>();
        r.code = code.getCode();
        r.message = code.getMessage();
        return r;
    }

    public static <T> Result<T> fail(ResultCode code, String message) {
        Result<T> r = new Result<>();
        r.code = code.getCode();
        r.message = message;
        return r;
    }
}
```

- [ ] **Step 3: 编写 BusinessException**

```java
package com.fitness.exception;

import com.fitness.common.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ResultCode code;

    public BusinessException(ResultCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public BusinessException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **Step 4: 编写 GlobalExceptionHandler**

```java
package com.fitness.exception;

import com.fitness.common.Result;
import com.fitness.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBind(BindException e) {
        String msg = e.getBindingResult().getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("参数校验失败");
        return Result.fail(ResultCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleOther(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
```

---

### Task 6: JWT 认证 & Spring Security

**Files:**
- Create: `fitness-server/src/main/java/com/fitness/security/JwtUtils.java`
- Create: `fitness-server/src/main/java/com/fitness/security/JwtAuthenticationFilter.java`
- Create: `fitness-server/src/main/java/com/fitness/security/UserDetailsServiceImpl.java`
- Create: `fitness-server/src/main/java/com/fitness/config/SecurityConfig.java`
- Create: `fitness-server/src/main/java/com/fitness/config/CorsConfig.java`

- [ ] **Step 1: 编写 JwtUtils**

```java
package com.fitness.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.access-token-expiration}") long accessExpiration,
                    @Value("${jwt.refresh-token-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String createAccessToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject());
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: 编写 JwtAuthenticationFilter**

```java
package com.fitness.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtUtils.validate(token)) {
            Long userId = jwtUtils.getUserId(token);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 3: 编写 SecurityConfig**

```java
package com.fitness.config;

import com.fitness.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/v1/auth/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/dict/**").permitAll()
            .antMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

- [ ] **Step 4: 编写 CorsConfig**

```java
package com.fitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 5: 编写工具类获取当前用户ID**

```java
package com.fitness.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new RuntimeException("用户未登录");
    }
}
```

---

### Task 7: MyBatis 配置 & 代码生成器配置

**Files:**
- Create: `fitness-server/src/main/resources/mapper/` (directory)
- Create: `fitness-server/src/main/java/com/fitness/config/MyBatisConfig.java`

- [ ] **Step 1: 编写 MyBatisConfig**

```java
package com.fitness.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fitness.mapper")
public class MyBatisConfig {
}
```

---

### Task 8: 编写所有实体类

**Files:**
- Create: `fitness-server/src/main/java/com/fitness/entity/` — 25 个实体类

- [ ] **Step 1: 编写基础实体 SysUser**

```java
package com.fitness.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

其余 24 个实体类按照 init.sql 建表语句一一对应编写，每个实体使用 Lombok `@Data`，字段名驼峰对应数据库下划线字段名。完整实体类清单:
- `UserProfile.java`, `SysDictType.java`, `SysDictData.java`
- `WeightRecord.java`, `BodyMeasurement.java`, `BodyPhoto.java`, `WaterRecord.java`
- `FitnessPlan.java`, `PlanTrainingDay.java`, `PlanTrainingAction.java`
- `PlanMealConfig.java`, `PlanMealFood.java`
- `PlanTemplate.java`, `PlanTemplateDay.java`, `PlanTemplateAction.java`
- `PlanTemplateMealConfig.java`, `PlanTemplateMealFood.java`
- `FoodLibrary.java`, `FoodNutrition.java`, `DietRecord.java`
- `ActionLibrary.java`, `UserActionRecord.java`
- `TrainingRecord.java`, `TrainingRecordDetail.java`

---

### Task 9: 编写所有 Mapper 接口

**Files:**
- Create: `fitness-server/src/main/java/com/fitness/mapper/` — 25 个 Mapper 接口
- Create: `fitness-server/src/main/resources/mapper/` — 25 个 XML 映射文件

示例 — `SysUserMapper.java`:

```java
package com.fitness.mapper;

import com.fitness.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {
    SysUser selectById(Long id);
    SysUser selectByUsername(String username);
    int insert(SysUser user);
    int updateById(SysUser user);
    int countByUsername(String username);
}
```

示例 — `SysUserMapper.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.fitness.mapper.SysUserMapper">
    <resultMap id="BaseResultMap" type="com.fitness.entity.SysUser">
        <id column="id" property="id"/>
        <result column="username" property="username"/>
        <result column="password" property="password"/>
        <result column="nickname" property="nickname"/>
        <result column="email" property="email"/>
        <result column="avatar" property="avatar"/>
        <result column="role" property="role"/>
        <result column="status" property="status"/>
        <result column="created_at" property="createdAt"/>
        <result column="updated_at" property="updatedAt"/>
    </resultMap>

    <select id="selectById" resultMap="BaseResultMap">
        SELECT * FROM sys_user WHERE id = #{id}
    </select>

    <select id="selectByUsername" resultMap="BaseResultMap">
        SELECT * FROM sys_user WHERE username = #{username}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO sys_user (username, password, nickname, email, role, status)
        VALUES (#{username}, #{password}, #{nickname}, #{email}, #{role}, #{status})
    </insert>

    <update id="updateById">
        UPDATE sys_user
        SET nickname = #{nickname}, email = #{email}, avatar = #{avatar}
        WHERE id = #{id}
    </update>

    <select id="countByUsername" resultType="int">
        SELECT COUNT(*) FROM sys_user WHERE username = #{username}
    </select>
</mapper>
```

其余 Mapper 接口和 XML 按照相同模式编写，每个模块提供基本 CRUD + 业务查询方法。关键方法包括但不限于:

- `WeightRecordMapper`: `selectByUserAndDateRange`, `upsert`
- `DietRecordMapper`: `selectByUserAndDate`, `selectDailySummary`
- `TrainingRecordMapper`: `selectByUserAndDate`, `selectCalendarData`
- `FoodLibraryMapper`: `searchByName`, `selectByScopeAndUser`
- `FitnessPlanMapper`: `selectActiveByUser`, `deactivateByUser`

---

## Phase 4: 用户认证 & 字典

### Task 10: 用户认证 DTOs & Service

**Files:**
- Create: `fitness-server/src/main/java/com/fitness/dto/RegisterDTO.java`
- Create: `fitness-server/src/main/java/com/fitness/dto/LoginDTO.java`
- Create: `fitness-server/src/main/java/com/fitness/dto/TokenResponse.java`
- Create: `fitness-server/src/main/java/com/fitness/service/AuthService.java`
- Create: `fitness-server/src/main/java/com/fitness/service/impl/AuthServiceImpl.java`

- [ ] **Step 1: 编写 RegisterDTO**

```java
package com.fitness.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;
}
```

- [ ] **Step 2: 编写 LoginDTO & TokenResponse**

```java
package com.fitness.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

```java
package com.fitness.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private Long userId;
    private String nickname;
    private String role;
}
```

- [ ] **Step 3: 编写 AuthService**

```java
package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.RegisterDTO;
import com.fitness.dto.TokenResponse;
import com.fitness.entity.SysUser;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.SysUserMapper;
import com.fitness.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${jwt.access-token-expiration}")
    private long accessExpiration;

    public TokenResponse login(LoginDTO dto) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账户已被禁用");
        }
        return TokenResponse.builder()
                .accessToken(jwtUtils.createAccessToken(user.getId(), user.getUsername()))
                .refreshToken(jwtUtils.createRefreshToken(user.getId()))
                .expiresIn(accessExpiration / 1000)
                .userId(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void register(RegisterDTO dto) {
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        userMapper.insert(user);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtUtils.validate(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refreshToken无效或已过期");
        }
        Long userId = jwtUtils.getUserId(refreshToken);
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return TokenResponse.builder()
                .accessToken(jwtUtils.createAccessToken(user.getId(), user.getUsername()))
                .refreshToken(jwtUtils.createRefreshToken(user.getId()))
                .expiresIn(accessExpiration / 1000)
                .userId(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }
}
```

---

### Task 11: Auth & Dict Controller

**Files:**
- Create: `fitness-server/src/main/java/com/fitness/controller/AuthController.java`
- Create: `fitness-server/src/main/java/com/fitness/controller/DictController.java`
- Create: `fitness-server/src/main/java/com/fitness/service/DictService.java`

- [ ] **Step 1: 编写 AuthController**

```java
package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.RegisterDTO;
import com.fitness.dto.TokenResponse;
import com.fitness.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestParam String refreshToken) {
        return Result.ok(authService.refresh(refreshToken));
    }
}
```

- [ ] **Step 2: 编写 DictService & DictController**

```java
package com.fitness.service;

import com.fitness.entity.SysDictData;
import com.fitness.mapper.SysDictDataMapper;
import com.fitness.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DictService {
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public List<SysDictData> getByCode(String dictCode) {
        return dictDataMapper.selectByDictCode(dictCode);
    }
}
```

```java
package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.entity.SysDictData;
import com.fitness.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/{dictCode}")
    public Result<List<SysDictData>> getDict(@PathVariable String dictCode) {
        return Result.ok(dictService.getByCode(dictCode));
    }
}
```

---

## Phase 5: 核心业务模块

剩余 Tasks 遵循统一模式，每个业务模块按以下模板开发:

### 模块开发模板 (Tasks 12-26)

每个模块包含:
1. **DTO 类** (请求/响应)
2. **Service 类** (业务逻辑)
3. **Controller 类** (REST 端点)
4. **Mapper 接口 + XML**

以下是各模块的关键点:

### Task 12: 用户信息模块

**Controller:** `UserController.java` — `GET/PUT /api/v1/user/profile`
**Service:** `UserService.java` — 获取/更新用户信息 + 身体档案
**关键逻辑:** 更新体重时必须同步更新 user_profile 中的身高(用于BMI计算), 获取时连表查询 profile

### Task 13: 体重记录模块

**Controller:** `WeightController.java`
- `GET /api/v1/weight?startDate=&endDate=` — 分页列表
- `POST /api/v1/weight` — 新增/覆盖(同一天upsert)
- `GET /api/v1/weight/trend?days=30` — 返回趋势数据点数组

**Service:** `WeightService.java`
**关键逻辑:**
```java
public void save(WeightRecordDTO dto, Long userId) {
    WeightRecord record = mapper.selectByUserAndDate(userId, dto.getRecordDate());
    if (record != null) {
        record.setWeightKg(dto.getWeightKg());
        record.setNote(dto.getNote());
        record.setBmi(calcBmi(dto.getWeightKg(), getUserHeight(userId)));
        mapper.updateById(record);
    } else {
        record = new WeightRecord();
        // ... set fields
        mapper.insert(record);
    }
}
```

### Task 14: 动作库模块

**Controller:** `ActionController.java`
- `GET /api/v1/actions?keyword=&suitableFor=` — 搜索(含用户私有+系统)
- `POST/PUT/DELETE /api/v1/actions` — CRUD
- `GET /api/v1/actions/:id/records` — 该动作的重量演进记录

**Service:** 用户只能修改自己的动作，系统动作只读

### Task 15: 食物库模块

**Controller:** `FoodController.java`
- `GET /api/v1/foods?keyword=&scope=` — 搜索(含系统+用户私有)
- `POST/PUT/DELETE /api/v1/foods` — CRUD (含 nested `food_nutrition` 列表)
- `POST /api/v1/foods` 时同时插入 food_library 和 food_nutrition 列表

**Service关键逻辑:**
```java
@Transactional
public FoodLibrary createFood(FoodCreateDTO dto, Long userId) {
    FoodLibrary food = new FoodLibrary();
    food.setFoodName(dto.getFoodName());
    food.setScope("USER");
    food.setUserId(userId);
    food.setImageUrl(dto.getImageUrl());
    mapper.insert(food);

    for (NutritionDTO nDto : dto.getNutritions()) {
        FoodNutrition n = new FoodNutrition();
        n.setFoodId(food.getId());
        n.setUnitType(nDto.getUnitType());
        n.setServingWeightG(nDto.getServingWeightG());
        n.setCarbGrams(nDto.getCarbGrams());
        n.setProteinGrams(nDto.getProteinGrams());
        n.setFatGrams(nDto.getFatGrams());
        // calories = 碳×4 + 蛋×4 + 脂×9
        n.setCalories(nDto.getCarbGrams() * 4 + nDto.getProteinGrams() * 4 + nDto.getFatGrams() * 9);
        n.setImageUrl(nDto.getImageUrl());
        nutritionMapper.insert(n);
    }
    return food;
}
```

### Task 16: 健身计划模块

**Controller:** `PlanController.java`
- `GET /api/v1/plans` — 用户计划列表
- `POST /api/v1/plans` — 创建完整计划(plan + days + actions + meal_configs + meal_foods)
- `PUT /api/v1/plans/:id` — 更新计划
- `PUT /api/v1/plans/:id/activate` — 激活计划(先停用所有)
- `DELETE /api/v1/plans/:id`

**创建计划 DTO 结构:**
```java
@Data
public class PlanCreateDTO {
    private String planName;
    private String planType;
    private String splitType;
    private List<TrainingDayDTO> trainingDays;
    private List<MealConfigDTO> mealConfigs;
}

@Data
public class TrainingDayDTO {
    private Integer dayOrder;
    private String dayType;      // TRAINING or REST
    private String trainingType; // CHEST/BACK/... only if TRAINING
    private Double carbMultiplier;
    private Double proteinMultiplier;
    private Double fatMultiplier;
    private List<TrainingActionDTO> actions;
}

@Data
public class MealConfigDTO {
    private String dayType;      // TRAINING or REST
    private String mealType;
    private Double carbRatio;
    private Double proteinRatio;
    private Double fatRatio;
    private List<MealFoodDTO> foods;
}
```

**Service 层在创建时必须用 @Transactional，一次性插入所有关联表。**

### Task 17: 饮食记录模块

**Controller:** `DietController.java`
- `GET /api/v1/diet?date=` — 某日饮食记录列表(按餐次分组)
- `POST/PUT/DELETE /api/v1/diet` — CRUD
- `GET /api/v1/diet/daily-summary?date=` — 当日概览

**当日概览 关键逻辑:**
```java
public DailySummaryVO getDailySummary(Long userId, LocalDate date) {
    // 1. 获取用户激活的计划
    FitnessPlan activePlan = planMapper.selectActiveByUser(userId);
    if (activePlan == null) return emptySummary();

    // 2. 确定当天是训练日还是休息日(根据计划轮转)
    PlanTrainingDay todayDay = determineTodayDay(activePlan);

    // 3. 获取最新体重
    WeightRecord latestWeight = weightMapper.selectByUserAndDate(userId, date);

    // 4. 计算目标
    double targetCarb = latestWeight.getWeightKg() * todayDay.getCarbMultiplier();
    double targetProtein = latestWeight.getWeightKg() * todayDay.getProteinMultiplier();
    double targetFat = latestWeight.getWeightKg() * todayDay.getFatMultiplier();

    // 5. 汇总今日已吃
    DietSummary actual = dietMapper.selectDailySummary(userId, date);

    // 6. 组装返回
    return DailySummaryVO.builder()
        .targetCarb(targetCarb).targetProtein(targetProtein).targetFat(targetFat)
        .targetCalories(targetCarb*4 + targetProtein*4 + targetFat*9)
        .actualCarb(actual.getCarb()).actualProtein(actual.getProtein()).actualFat(actual.getFat())
        .actualCalories(actual.getCalories())
        .build();
}
```

### Task 18: 训练记录模块

**Controller:** `TrainingController.java`
- `GET /api/v1/training?startDate=&endDate=` — 列表
- `POST /api/v1/training` — 创建(含 details)
- `PUT/DELETE /api/v1/training/:id`
- `GET /api/v1/training/calendar?year=&month=` — 打卡日历数据

**创建时支持从计划导入:**
```java
@Transactional
public TrainingRecord createFromPlan(TrainingRecordDTO dto, Long userId) {
    TrainingRecord record = new TrainingRecord();
    // ... set fields
    mapper.insert(record);

    List<TrainingRecordDetail> details;
    if (dto.getPlanId() != null && dto.getTrainingDayId() != null) {
        // 从计划导入动作列表
        List<PlanTrainingAction> planActions = planActionMapper.selectByDayId(dto.getTrainingDayId());
        details = planActions.stream().map(pa -> {
            TrainingRecordDetail d = new TrainingRecordDetail();
            d.setTrainingRecordId(record.getId());
            d.setActionId(pa.getActionId());
            d.setActionName(pa.getActionName());
            d.setSets(pa.getMinSets());
            d.setWeightKg(0.0);
            d.setSortOrder(pa.getSortOrder());
            return d;
        }).collect(Collectors.toList());
    } else {
        details = dto.getDetails().stream().map(d -> { /* map from DTO */ }).toList();
    }

    detailMapper.batchInsert(details);
    return record;
}
```

### Task 19: 身体围度 & 照片 & 饮水

三个简单 CRUD 模块，各自 controller + service + mapper。

- `MeasurementController`: `GET/POST /api/v1/measurement`
- `BodyPhotoController`: `GET/POST/DELETE /api/v1/body-photo`
- `WaterController`: `GET/POST /api/v1/water` (首页汇总 `GET /api/v1/water/today`)

### Task 20: 文件上传

**Controller:** `UploadController.java`

```java
@PostMapping("/api/v1/upload")
public Result<String> upload(@RequestParam("file") MultipartFile file) {
    String originalName = file.getOriginalFilename();
    String ext = originalName.substring(originalName.lastIndexOf("."));
    String fileName = UUID.randomUUID().toString() + ext;
    String uploadDir = "uploads/" + LocalDate.now().toString().replace("-", "/");
    File dir = new File(uploadDir);
    if (!dir.exists()) dir.mkdirs();
    File dest = new File(uploadDir, fileName);
    file.transferTo(dest);
    return Result.ok("/" + uploadDir + "/" + fileName);
}
```

### Task 21: 首页看板 API

**Controller:** `DashboardController.java` — `GET /api/v1/dashboard`

聚合查询: 今日体重、今日饮食汇总、今日训练、连续打卡天数、饮水总量、体重趋势(近30天)

### Task 22: 数据导出 API

**Controller:** `ExportController.java` — `GET /api/v1/export/:type?startDate=&endDate=`

使用 Hutool 的 CsvWriter，流式输出 CSV 文件。

### Task 23: 管理后台 API

**AdminFoodController**: 管理员管理公共食物库 (scope=SYSTEM)
**AdminActionController**: 管理员管理公共动作库
**AdminTemplateController**: 计划模板 CRUD
**AdminUserController**: 用户管理 (列表, 禁用/启用)

所有 Admin Controller 路径为 `/api/v1/admin/**`，SecurityConfig 已配置仅 ADMIN 角色可访问。

---

## Phase 6: 前端实现

### Task 24: 前端基础设施

**Files:**
- `src/router/index.ts` — 路由配置 + 导航守卫
- `src/api/request.ts` — Axios 实例 + JWT 拦截器
- `src/stores/user.ts` — 用户状态管理
- `src/utils/index.ts` — 热量计算等工具函数
- `src/components/layout/AppLayout.vue` — 主布局
- `src/components/layout/Sidebar.vue` — 侧边栏导航
- `src/components/layout/HeaderBar.vue` — 顶部栏

- [ ] **Step 1: Axios 实例 (src/api/request.ts)**

```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) return data
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const res = await axios.post('/api/v1/auth/refresh', null, { params: { refreshToken } })
          const { accessToken, refreshToken: newRefresh } = res.data.data
          localStorage.setItem('accessToken', accessToken)
          localStorage.setItem('refreshToken', newRefresh)
          error.config.headers.Authorization = `Bearer ${accessToken}`
          return request(error.config)
        } catch {
          localStorage.clear()
          router.push('/login')
        }
      }
    }
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **Step 2: 路由配置 (src/router/index.ts)**

```typescript
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/auth/Login.vue'), meta: { noAuth: true } },
    { path: '/register', name: 'Register', component: () => import('@/views/auth/Register.vue'), meta: { noAuth: true } },
    {
      path: '/',
      component: () => import('@/components/layout/AppLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/home/Dashboard.vue') },
        { path: 'weight', name: 'Weight', component: () => import('@/views/weight/WeightPage.vue') },
        { path: 'plans', name: 'Plans', component: () => import('@/views/plan/PlanList.vue') },
        { path: 'plans/create', name: 'PlanCreate', component: () => import('@/views/plan/PlanEditor.vue') },
        { path: 'plans/:id/edit', name: 'PlanEdit', component: () => import('@/views/plan/PlanEditor.vue') },
        { path: 'plans/templates', name: 'PlanTemplates', component: () => import('@/views/plan/PlanTemplate.vue') },
        { path: 'diet', name: 'Diet', component: () => import('@/views/diet/DietPage.vue') },
        { path: 'diet/overview', name: 'DietOverview', component: () => import('@/views/diet/DietOverview.vue') },
        { path: 'foods', name: 'Foods', component: () => import('@/views/food-library/FoodList.vue') },
        { path: 'actions', name: 'Actions', component: () => import('@/views/action-library/ActionList.vue') },
        { path: 'training', name: 'Training', component: () => import('@/views/training/TrainingPage.vue') },
        { path: 'measurement', name: 'Measurement', component: () => import('@/views/measurement/MeasurementPage.vue') },
        { path: 'photos', name: 'Photos', component: () => import('@/views/photo/BodyPhotoPage.vue') },
        { path: 'water', name: 'Water', component: () => import('@/views/water/WaterPage.vue') },
        { path: 'profile', name: 'Profile', component: () => import('@/views/profile/ProfilePage.vue') },
        { path: 'admin/foods', name: 'AdminFoods', component: () => import('@/views/admin/AdminFood.vue'), meta: { admin: true } },
        { path: 'admin/actions', name: 'AdminActions', component: () => import('@/views/admin/AdminAction.vue'), meta: { admin: true } },
        { path: 'admin/templates', name: 'AdminTemplates', component: () => import('@/views/admin/AdminTemplate.vue'), meta: { admin: true } },
        { path: 'admin/users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUsers.vue'), meta: { admin: true } }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  if (!to.meta.noAuth && !token) {
    next('/login')
  } else if (to.meta.admin) {
    const role = localStorage.getItem('role')
    if (role !== 'ADMIN') next('/dashboard')
    else next()
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 3: 状态管理 (src/stores/user.ts)**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const nickname = ref(localStorage.getItem('nickname') || '')
  const role = ref(localStorage.getItem('role') || '')
  const userId = ref(Number(localStorage.getItem('userId')) || 0)

  function setUser(data: { nickname: string; role: string; userId: number }) {
    nickname.value = data.nickname
    role.value = data.role
    userId.value = data.userId
    localStorage.setItem('nickname', data.nickname)
    localStorage.setItem('role', data.role)
    localStorage.setItem('userId', String(data.userId))
  }

  function logout() {
    localStorage.clear()
    nickname.value = ''
    role.value = ''
    userId.value = 0
  }

  return { nickname, role, userId, setUser, logout }
})
```

- [ ] **Step 4: 主布局 AppLayout**

```vue
<template>
  <el-container>
    <el-aside width="220px">
      <Sidebar />
    </el-aside>
    <el-container>
      <el-header><HeaderBar /></el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>
```

Sidebar 使用 `el-menu`，根据 `userStore.role === 'ADMIN'` 显示管理菜单组。

---

### Task 25: 登录注册页面

**Files:**
- `src/views/auth/Login.vue`
- `src/views/auth/Register.vue`
- `src/api/auth.ts`

使用 Element Plus `el-form` + `el-input` + 表单校验，登录成功后存储 token 并跳转首页。

### Task 26: 首页看板 Dashboard

**Files:**
- `src/views/home/Dashboard.vue`
- `src/api/dashboard.ts`

核心实现:
- 顶部 5 个统计卡片(`el-statistic`/ 自定义卡片)
- 营养进度条(`el-progress` 展示碳水/蛋白/脂肪/饮水)
- 体重趋势图(ECharts 折线图，近30天)
- 今日饮食/训练快捷列表
- 使用 `vue-echarts` 的 `<VChart>` 组件

### Task 27-36: 其余前端页面

按照设计文档中的页面结构逐一实现，每个页面包含:
- 列表/表格 + 分页 (`el-table` + `el-pagination`)
- 搜索/筛选表单 (`el-form` inline)
- 新增/编辑弹窗 (`el-dialog` + `el-form`)
- 图表展示 (体重、围度、饮食概览使用 ECharts)

**关键公共组件:**
1. **ImageUpload.vue** — 使用 `el-upload`，调用上传接口，返回URL
2. **FoodSelector.vue** — 弹窗搜索食物库，选择单位和数量，返回碳蛋脂数据
3. **RestTimer.vue** — 倒计时器组件，训练记录页面使用
4. **NutritionProgress.vue** — 营养进度条组件，首页和饮食页复用

### Task 37: 计划模板导入前端

用户浏览模板列表 → 点击导入 → 调用 `POST /api/v1/plan-templates/:id/import` → 跳转到计划编辑页面。

---

## Phase 7: 部署

### Task 38: 完整的 docker-compose

**Files:**
- Modify: `docker-compose.yml` (添加 server 和 web 服务)

在 Phase 1 创建的 docker-compose.yml 中追加:

```yaml
  server:
    build: ./fitness-server
    container_name: fitness-server
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod
    volumes:
      - uploads-data:/app/uploads

  web:
    build: ./fitness-web
    container_name: fitness-web
    ports:
      - "80:80"
    depends_on:
      - server
    volumes:
      - uploads-data:/app/uploads

volumes:
  mysql-data:
  redis-data:
  uploads-data:
```

---

## 实现顺序建议

按 Task 编号顺序执行，依赖关系如下:

```
Phase 1 (Tasks 1-3)     → 基础设施，可并行
Phase 2 (Task 4)        → 数据库
Phase 3 (Tasks 5-9)     → 后端基础设施，串行
Phase 4 (Tasks 10-11)   → 认证系统
Phase 5 (Tasks 12-23)   → 业务模块，部分可并行(12/13/14/15互不依赖)
Phase 6 (Tasks 24-37)   → 前端，依赖Phase 5的API
Phase 7 (Task 38)       → 部署验证
```

---

**总 Task 数量: 38**  
**预估总工时: 按每个 Task 5-30 分钟，约 20-30 小时完成全部实现**
