CREATE DATABASE IF NOT EXISTS fitness DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitness;

-- ========== 系统表 ==========

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

-- ========== 身体数据表 ==========

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

-- ========== 健身计划相关表 ==========

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
    training_type VARCHAR(50) COMMENT '训练类型, TRAINING时有值',
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

-- 餐次配置表
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

-- ========== 计划模板相关表 ==========

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

-- ========== 食物相关表 ==========

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

-- ========== 动作 & 训练表 ==========

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

-- ========== 字典初始数据 ==========

INSERT INTO sys_dict_type (dict_code, dict_name) VALUES
('training_type', '训练类型'),
('meal_type', '餐次类型'),
('plan_type', '计划目标类型'),
('split_type', '分化类型'),
('day_type', '训练日/休息日'),
('food_unit_type', '食物单位类型'),
('photo_type', '照片角度'),
('difficulty', '难度等级');

-- 训练类型 (dict_type_id=1)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(1, '练胸', 'CHEST', 1), (1, '练背', 'BACK', 2), (1, '练腿', 'LEGS', 3),
(1, '练肩', 'SHOULDER', 4), (1, '练手臂', 'ARMS', 5), (1, '核心', 'CORE', 6),
(1, '有氧', 'CARDIO', 7), (1, '休息', 'REST', 8);

-- 餐次类型 (dict_type_id=2)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(2, '早餐', 'BREAKFAST', 1), (2, '午餐', 'LUNCH', 2), (2, '晚餐', 'DINNER', 3),
(2, '夜宵', 'SUPPER', 4), (2, '练前餐', 'PRE_WORKOUT', 5),
(2, '练后餐', 'POST_WORKOUT', 6), (2, '其他餐', 'OTHER', 7);

-- 计划目标类型 (dict_type_id=3)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(3, '减脂', 'CUT', 1), (3, '增肌', 'BULK', 2), (3, '维持', 'MAINTAIN', 3);

-- 分化类型 (dict_type_id=4)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(4, '全身训练', 'FULL_BODY', 1), (4, '三分化', 'THREE_DAY', 2),
(4, '四分化', 'FOUR_DAY', 3), (4, '五分化', 'FIVE_DAY', 4),
(4, '推拉腿', 'PUSH_PULL_LEGS', 5), (4, '自定义', 'CUSTOM', 6);

-- 训练日/休息日 (dict_type_id=5)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(5, '训练日', 'TRAINING', 1), (5, '休息日', 'REST', 2);

-- 食物单位类型 (dict_type_id=6)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(6, '每100g', 'PER_100G', 1), (6, '每小份', 'PER_SMALL_SERVING', 2),
(6, '每份', 'PER_SERVING', 3), (6, '每大份', 'PER_LARGE_SERVING', 4);

-- 照片角度 (dict_type_id=7)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(7, '正面', 'FRONT', 1), (7, '侧面', 'SIDE', 2), (7, '背面', 'BACK', 3);

-- 难度等级 (dict_type_id=8)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(8, '新手', 'BEGINNER', 1), (8, '中级', 'INTERMEDIATE', 2), (8, '高级', 'ADVANCED', 3);

-- 创建管理员账户 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 'ACTIVE');
