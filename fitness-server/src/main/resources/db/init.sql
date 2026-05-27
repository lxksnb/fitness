CREATE DATABASE IF NOT EXISTS fitness DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitness;
SET NAMES utf8mb4;

-- ========== 系统表 ==========

-- 用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像URL',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER普通用户 / ADMIN管理员',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE正常 / DISABLED禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '用户表';

-- 用户身体档案
CREATE TABLE user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    gender VARCHAR(10) COMMENT '性别: MALE男 / FEMALE女',
    birthday DATE COMMENT '出生日期',
    height_cm DECIMAL(5,1) COMMENT '身高(cm)',
    target_type VARCHAR(20) COMMENT '目标类型: CUT减脂 / BULK增肌 / MAINTAIN维持',
    target_weight_kg DECIMAL(5,1) COMMENT '目标体重(kg)',
    target_date DATE COMMENT '预计达成日期',
    weekly_change_rate DECIMAL(4,2) COMMENT '每周目标变化量(kg), 增肌为正数如+0.25, 减脂为负数如-0.5',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '用户身体档案';

-- 字典类型表
CREATE TABLE sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dict_code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典编码, 如training_type',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称, 如训练类型',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE启用 / DISABLED停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '字典类型表';

-- 字典数据表
CREATE TABLE sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dict_type_id BIGINT NOT NULL COMMENT '字典类型ID, 关联sys_dict_type',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典显示名, 如练胸',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值, 如CHEST',
    sort INT DEFAULT 0 COMMENT '排序号, 越小越靠前',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE启用 / DISABLED停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (dict_type_id) REFERENCES sys_dict_type(id)
) COMMENT '字典数据表';

-- ========== 身体数据表 ==========

-- 体重记录表
CREATE TABLE weight_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    weight_kg DECIMAL(4,1) NOT NULL COMMENT '体重(kg), 精确到0.1',
    bmi DECIMAL(4,1) COMMENT 'BMI指数, 系统根据体重/(身高/100)²自动计算',
    note VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_id, record_date),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '体重记录表, 同一用户同一天只保留一条记录(覆盖更新)';

-- 身体围度记录表
CREATE TABLE body_measurement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    chest_cm DECIMAL(5,1) COMMENT '胸围(cm)',
    waist_cm DECIMAL(5,1) COMMENT '腰围(cm)',
    left_arm_cm DECIMAL(5,1) COMMENT '左臂围(cm)',
    right_arm_cm DECIMAL(5,1) COMMENT '右臂围(cm)',
    left_thigh_cm DECIMAL(5,1) COMMENT '左大腿围(cm)',
    right_thigh_cm DECIMAL(5,1) COMMENT '右大腿围(cm)',
    hip_cm DECIMAL(5,1) COMMENT '臀围(cm)',
    neck_cm DECIMAL(5,1) COMMENT '颈围(cm)',
    note VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '身体围度记录表';

-- 身体照片表
CREATE TABLE body_photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    photo_date DATE NOT NULL COMMENT '拍照日期',
    photo_type VARCHAR(20) NOT NULL COMMENT '照片角度: FRONT正面 / SIDE侧面 / BACK背面',
    image_url VARCHAR(500) NOT NULL COMMENT '照片存储URL',
    note VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '身体照片表, 用于记录不同时期不同角度的身材变化';

-- 饮水记录表
CREATE TABLE water_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    amount_ml INT NOT NULL COMMENT '本次饮水量(ml)',
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '饮水时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '饮水记录表, 每日默认目标2000ml';

-- ========== 健身计划相关表 ==========

-- 健身计划主表
CREATE TABLE fitness_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    plan_name VARCHAR(100) NOT NULL COMMENT '计划名称',
    plan_type VARCHAR(20) NOT NULL COMMENT '计划目标: CUT减脂 / BULK增肌 / MAINTAIN维持',
    split_type VARCHAR(50) NOT NULL COMMENT '分化类型: FULL_BODY全身 / THREE_DAY三分化 / FOUR_DAY四分化 / FIVE_DAY五分化 / PUSH_PULL_LEGS推拉腿 / CUSTOM自定义',
    is_active TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否激活: 1激活 / 0未激活, 同一用户同时只能激活一个计划',
    activated_at DATETIME COMMENT '计划激活时间, 用于计算训练日轮换',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '健身计划主表';

-- 训练日表
CREATE TABLE plan_training_day (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    plan_id BIGINT NOT NULL COMMENT '所属计划ID',
    day_order INT NOT NULL COMMENT '天序号: 第1天/第2天/第3天...',
    day_type VARCHAR(20) NOT NULL COMMENT '日类型: TRAINING训练日 / REST休息日',
    training_type VARCHAR(50) COMMENT '训练类型: CHEST练胸/BACK练背/LEGS练腿/SHOULDER练肩/ARMS练手臂/CORE核心/CARDIO有氧, 仅TRAINING时有值',
    carb_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '碳水倍数, 每日碳水克数 = 体重(kg) × 此倍数',
    protein_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '蛋白质倍数, 每日蛋白质克数 = 体重(kg) × 此倍数',
    fat_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '脂肪倍数, 每日脂肪克数 = 体重(kg) × 此倍数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (plan_id) REFERENCES fitness_plan(id) ON DELETE CASCADE
) COMMENT '训练日表, 记录计划中每一天的安排(训练内容+营养倍数)';

-- 训练动作明细表
CREATE TABLE plan_training_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    training_day_id BIGINT NOT NULL COMMENT '所属训练日ID',
    action_id BIGINT NOT NULL COMMENT '动作ID, 关联action_library',
    action_name VARCHAR(100) NOT NULL COMMENT '动作名称(冗余字段, 便于展示)',
    min_sets INT NOT NULL DEFAULT 3 COMMENT '最少组数',
    max_sets INT NOT NULL DEFAULT 5 COMMENT '最多组数',
    rest_minutes INT NOT NULL DEFAULT 2 COMMENT '组间休息时间(分钟)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号, 越小越靠前',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (training_day_id) REFERENCES plan_training_day(id) ON DELETE CASCADE
) COMMENT '训练动作明细表, 记录每个训练日包含的动作及其推荐组数范围';

-- 餐次配置表 (与training_day平级, 按day_type区分)
CREATE TABLE plan_meal_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    plan_id BIGINT NOT NULL COMMENT '所属计划ID',
    day_type VARCHAR(20) NOT NULL COMMENT '适用日类型: TRAINING训练日餐次配置 / REST休息日餐次配置',
    meal_type VARCHAR(50) NOT NULL COMMENT '餐次类型: BREAKFAST早餐/LUNCH午餐/DINNER晚餐/SUPPER夜宵/PRE_WORKOUT练前餐/POST_WORKOUT练后餐/OTHER其他餐',
    carb_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '碳水占比(0~1), 如0.20=20%',
    protein_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '蛋白质占比(0~1), 如0.20=20%',
    fat_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '脂肪占比(0~1), 如0.15=15%',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号, 按用餐时间排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (plan_id) REFERENCES fitness_plan(id) ON DELETE CASCADE
) COMMENT '餐次配置表, 训练日和休息日各自一套餐次营养分配方案';

-- 餐次推荐食物表
CREATE TABLE plan_meal_food (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    meal_config_id BIGINT NOT NULL COMMENT '所属餐次配置ID',
    food_id BIGINT NOT NULL COMMENT '食物ID, 关联food_library',
    food_name VARCHAR(100) NOT NULL COMMENT '食物名称(冗余字段, 便于展示)',
    suggested_amount_g INT NOT NULL DEFAULT 100 COMMENT '建议食用份量(g)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (meal_config_id) REFERENCES plan_meal_config(id) ON DELETE CASCADE
) COMMENT '餐次推荐食物表, 每餐可配置多个推荐食物';

-- ========== 计划模板相关表 ==========

-- 计划模板表(管理员创建, 用户可浏览导入)
CREATE TABLE plan_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    description VARCHAR(500) COMMENT '模板描述/简介',
    plan_type VARCHAR(20) NOT NULL COMMENT '计划目标: CUT减脂 / BULK增肌 / MAINTAIN维持',
    split_type VARCHAR(50) NOT NULL COMMENT '分化类型',
    difficulty VARCHAR(20) NOT NULL COMMENT '难度等级: BEGINNER新手 / INTERMEDIATE中级 / ADVANCED高级',
    created_by BIGINT COMMENT '创建者(管理员)ID',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE启用 / DISABLED停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) COMMENT '计划模板表, 管理员预置通用训练计划供用户一键导入';

-- 模板训练日
CREATE TABLE plan_template_day (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '所属模板ID',
    day_order INT NOT NULL COMMENT '天序号',
    day_type VARCHAR(20) NOT NULL COMMENT '日类型: TRAINING训练日 / REST休息日',
    training_type VARCHAR(50) COMMENT '训练类型, 仅TRAINING时有值',
    carb_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '碳水倍数',
    protein_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '蛋白质倍数',
    fat_multiplier DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '脂肪倍数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (template_id) REFERENCES plan_template(id) ON DELETE CASCADE
) COMMENT '模板训练日表';

-- 模板动作明细
CREATE TABLE plan_template_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    template_day_id BIGINT NOT NULL COMMENT '所属模板训练日ID',
    action_id BIGINT NOT NULL COMMENT '动作ID',
    action_name VARCHAR(100) NOT NULL COMMENT '动作名称(冗余)',
    min_sets INT NOT NULL DEFAULT 3 COMMENT '最少组数',
    max_sets INT NOT NULL DEFAULT 5 COMMENT '最多组数',
    rest_minutes INT NOT NULL DEFAULT 2 COMMENT '组间休息(分钟)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (template_day_id) REFERENCES plan_template_day(id) ON DELETE CASCADE
) COMMENT '模板动作明细表';

-- 模板餐次配置
CREATE TABLE plan_template_meal_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '所属模板ID',
    day_type VARCHAR(20) NOT NULL COMMENT '适用日类型',
    meal_type VARCHAR(50) NOT NULL COMMENT '餐次类型',
    carb_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '碳水占比(0~1)',
    protein_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '蛋白质占比(0~1)',
    fat_ratio DECIMAL(4,3) NOT NULL DEFAULT 0 COMMENT '脂肪占比(0~1)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (template_id) REFERENCES plan_template(id) ON DELETE CASCADE
) COMMENT '模板餐次配置表';

-- 模板餐次推荐食物
CREATE TABLE plan_template_meal_food (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    meal_config_id BIGINT NOT NULL COMMENT '所属模板餐次配置ID',
    food_id BIGINT NOT NULL COMMENT '食物ID',
    food_name VARCHAR(100) NOT NULL COMMENT '食物名称(冗余)',
    suggested_amount_g INT NOT NULL DEFAULT 100 COMMENT '建议份量(g)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (meal_config_id) REFERENCES plan_template_meal_config(id) ON DELETE CASCADE
) COMMENT '模板餐次推荐食物表';

-- ========== 食物相关表 ==========

-- 食物库
CREATE TABLE food_library (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    scope VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '范围: SYSTEM系统公共食物(所有用户可见) / USER用户私有食物(仅自己可见)',
    user_id BIGINT COMMENT '所属用户ID, scope=USER时有值',
    food_name VARCHAR(100) NOT NULL COMMENT '食物名称',
    category_type VARCHAR(50) NOT NULL DEFAULT 'OTHER' COMMENT '食物分类: food_category字典值',
    image_url VARCHAR(500) COMMENT '食物图片URL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE正常 / DELETED已删除(软删除)',
    created_by BIGINT COMMENT '创建者ID, 管理员录入系统食物时记录',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) COMMENT '食物库, 系统食物+用户自定义食物';

-- 食物营养表 (一个食物可有多种单位的营养数据)
CREATE TABLE food_nutrition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    food_id BIGINT NOT NULL COMMENT '所属食物ID',
    unit_type VARCHAR(50) NOT NULL COMMENT '单位类型: PER_100G每100g / PER_SMALL_SERVING每小份 / PER_SERVING每份 / PER_LARGE_SERVING每大份, 可通过字典扩展',
    serving_weight_g DECIMAL(6,1) NOT NULL COMMENT '该单位对应的整体重量(g), PER_100G固定为100',
    edible_weight_g DECIMAL(6,1) NOT NULL COMMENT '该单位对应的可食重量(g), PER_100G固定为100',
    carb_grams DECIMAL(6,1) NOT NULL DEFAULT 0 COMMENT '碳水化合物含量(g), 该单位下的数值',
    protein_grams DECIMAL(6,1) NOT NULL DEFAULT 0 COMMENT '蛋白质含量(g), 该单位下的数值',
    fat_grams DECIMAL(6,1) NOT NULL DEFAULT 0 COMMENT '脂肪含量(g), 该单位下的数值',
    calories DECIMAL(7,1) COMMENT '热量(kcal), 系统按 碳×4+蛋×4+脂×9 自动计算',
    image_url VARCHAR(500) COMMENT '该单位对应的参考图片URL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (food_id) REFERENCES food_library(id) ON DELETE CASCADE
) COMMENT '食物营养表, 一个食物可为多种单位(每100g/每份/每大份等)分别配置营养数值';

-- 饮食记录表
CREATE TABLE diet_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '饮食日期',
    meal_type VARCHAR(50) NOT NULL COMMENT '餐次类型: BREAKFAST/LUNCH/DINNER/SUPPER/PRE_WORKOUT/POST_WORKOUT/OTHER',
    food_name VARCHAR(200) NOT NULL COMMENT '食物描述, 用户手动输入或从食物库导入时自动填充',
    image_url VARCHAR(500) COMMENT '食物照片URL',
    carb_grams DECIMAL(6,1) NOT NULL DEFAULT 0 COMMENT '碳水化合物摄入量(g)',
    protein_grams DECIMAL(6,1) NOT NULL DEFAULT 0 COMMENT '蛋白质摄入量(g)',
    fat_grams DECIMAL(6,1) NOT NULL DEFAULT 0 COMMENT '脂肪摄入量(g)',
    calories DECIMAL(7,1) COMMENT '热量(kcal), 系统按 碳×4+蛋×4+脂×9 自动计算',
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '饮食记录表, 记录每餐具体摄入的食物及营养数据';

-- ========== 动作 & 训练表 ==========

-- 动作库
CREATE TABLE action_library (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    scope VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '范围: SYSTEM系统公共动作 / USER用户自定义动作',
    user_id BIGINT COMMENT '所属用户ID, scope=USER时有值',
    action_name VARCHAR(100) NOT NULL COMMENT '动作名称',
    description VARCHAR(1000) COMMENT '动作描述/要点说明',
    suitable_for VARCHAR(200) COMMENT '适用部位, 逗号分隔: CHEST练胸,BACK练背,LEGS练腿,SHOULDER练肩,ARMS练手臂,CORE核心,CARDIO有氧',
    image_urls JSON COMMENT '动作示范图片URL数组, JSON格式: ["url1","url2"]',
    video_url VARCHAR(500) COMMENT '动作示范视频URL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE正常 / DELETED已删除(软删除)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '动作库, 系统预置+用户自定义训练动作';

-- 动作刺激肌群关系
CREATE TABLE action_muscle_target (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    action_id BIGINT NOT NULL COMMENT '动作ID',
    muscle_code VARCHAR(50) NOT NULL COMMENT '肌群编码, 取muscle_group字典',
    target_role VARCHAR(20) NOT NULL COMMENT '刺激角色: PRIMARY主要 / SECONDARY辅助',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_action_muscle_role (action_id, muscle_code, target_role),
    INDEX idx_action_muscle_code (muscle_code),
    FOREIGN KEY (action_id) REFERENCES action_library(id) ON DELETE CASCADE
) COMMENT '动作与主要/辅助刺激肌群关系表';

-- 用户动作重量记录 (追踪每个动作的重量进步)
CREATE TABLE user_action_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    action_id BIGINT NOT NULL COMMENT '动作ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    weight_kg DECIMAL(5,1) NOT NULL COMMENT '本次使用的重量(kg)',
    max_reps INT COMMENT '该重量下的最大完成次数',
    note VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (action_id) REFERENCES action_library(id)
) COMMENT '用户动作重量记录表, 追踪每个动作的重量演进历史';

-- 训练记录主表
CREATE TABLE training_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '训练日期',
    plan_id BIGINT COMMENT '关联的健身计划ID, 可为空',
    training_day_id BIGINT COMMENT '关联的计划训练日ID, 可为空, 用于从计划导入动作列表',
    training_type VARCHAR(50) NOT NULL COMMENT '训练类型: CHEST练胸/BACK练背/LEGS练腿/SHOULDER练肩/ARMS练手臂/CORE核心/CARDIO有氧',
    start_time TIME COMMENT '训练开始时间',
    end_time TIME COMMENT '训练结束时间',
    duration_minutes INT COMMENT '训练时长(分钟), 可由开始/结束时间自动计算或手动填写',
    note VARCHAR(500) COMMENT '训练备注/心得',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '训练记录主表, 记录每次训练的整体信息';

-- 训练记录动作明细
CREATE TABLE training_record_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    training_record_id BIGINT NOT NULL COMMENT '所属训练记录ID',
    action_id BIGINT NOT NULL COMMENT '动作ID',
    action_name VARCHAR(100) NOT NULL COMMENT '动作名称(冗余)',
    sets INT NOT NULL COMMENT '实际完成的组数',
    weight_kg DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '使用的重量(kg)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号, 按训练顺序排列',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (training_record_id) REFERENCES training_record(id) ON DELETE CASCADE
) COMMENT '训练记录动作明细表, 记录本次训练每个动作实际做了几组、多大重量';

-- ========== 字典初始数据 ==========

INSERT INTO sys_dict_type (dict_code, dict_name) VALUES
('training_type', '训练类型'),
('meal_type', '餐次类型'),
('plan_type', '计划目标类型'),
('split_type', '分化类型'),
('day_type', '训练日/休息日'),
('food_unit_type', '食物单位类型'),
('photo_type', '照片角度'),
('difficulty', '难度等级'),
('muscle_group', '肌群'),
('food_category', '食物分类');

-- 训练类型 (dict_type_id=1)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(1, '练胸', 'CHEST', 1), (1, '练背', 'BACK', 2), (1, '练腿', 'LEGS', 3),
(1, '练肩', 'SHOULDER', 4), (1, '练手臂', 'ARMS', 5), (1, '腹部', 'CORE', 6),
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
(6, '每100g', 'PER_100G', 1),
(6, '每小份', 'PER_SMALL_SERVING', 2), (6, '每份', 'PER_SERVING', 3), (6, '每大份', 'PER_LARGE_SERVING', 4),
(6, '每小根', 'PER_SMALL_ROOT', 10), (6, '每根', 'PER_ROOT', 11), (6, '每大根', 'PER_LARGE_ROOT', 12),
(6, '每小个', 'PER_SMALL_PIECE', 20), (6, '每个', 'PER_PIECE', 21), (6, '每大个', 'PER_LARGE_PIECE', 22),
(6, '每杯', 'PER_CUP', 30), (6, '每瓶', 'PER_BOTTLE', 31),
(6, '每片', 'PER_SLICE', 40), (6, '每勺', 'PER_SCOOP', 41), (6, '每串', 'PER_STICK', 42);

-- 照片角度 (dict_type_id=7)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(7, '正面', 'FRONT', 1), (7, '侧面', 'SIDE', 2), (7, '背面', 'BACK', 3);

-- 难度等级 (dict_type_id=8)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
(8, '新手', 'BEGINNER', 1), (8, '中级', 'INTERMEDIATE', 2), (8, '高级', 'ADVANCED', 3);

-- 肌群 (muscle_group)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '上胸', 'CHEST_UPPER', 1),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '中胸', 'CHEST_MIDDLE', 2),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '下胸', 'CHEST_LOWER', 3),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '背阔肌', 'LATS', 4),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '上背', 'UPPER_BACK', 5),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '斜方肌', 'TRAPS', 6),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '肩前束', 'FRONT_DELTS', 7),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '肩中束', 'SIDE_DELTS', 8),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '肩后束', 'REAR_DELTS', 9),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '肱二头肌', 'BICEPS', 10),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '肱三头肌', 'TRICEPS', 11),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '前臂', 'FOREARMS', 12),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '股四头肌', 'QUADS', 13),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '腘绳肌', 'HAMSTRINGS', 14),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '臀肌', 'GLUTES', 15),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '小腿', 'CALVES', 16),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '腹直肌', 'ABS', 17),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '腹斜肌', 'OBLIQUES', 18),
((SELECT id FROM sys_dict_type WHERE dict_code = 'muscle_group'), '下背', 'LOWER_BACK', 19);

-- 食物分类 (food_category)
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '主食', 'STAPLE', 1),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '肉类', 'MEAT', 2),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '鱼虾海鲜', 'SEAFOOD', 3),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '蛋奶豆制品', 'EGG_DAIRY_SOY', 4),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '水果', 'FRUIT', 5),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '蔬菜', 'VEGETABLE', 6),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '坚果', 'NUT', 7),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '饮品', 'DRINK', 8),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '补剂', 'SUPPLEMENT', 9),
((SELECT id FROM sys_dict_type WHERE dict_code = 'food_category'), '其他', 'OTHER', 99);

-- 确保root可从任意主机连接(宿主机开发连接Docker MySQL)
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- 创建管理员账户 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$EJhza/5WlNJTO9YN/XlEsu5B7DDi2MxQm2SJwr1OORQm//RRu38gW', '系统管理员', 'ADMIN', 'ACTIVE');


-- =========================
-- action_library 动作库初始化
-- 主流力量训练 + 健身房常见动作
-- 默认 scope=SYSTEM
-- =========================

INSERT INTO action_library
(scope, user_id, action_name, description, suitable_for, image_urls, video_url)
VALUES

-- ================= 胸部 =================
('SYSTEM', NULL, '杠铃卧推',
 '经典胸部复合动作，主要刺激中胸，同时训练肱三头和肩前束。注意肩胛后缩下沉。',
 'CHEST,ARMS,SHOULDER',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '上斜杠铃卧推',
 '主要刺激上胸，凳子角度建议30~45度。',
 'CHEST,SHOULDER,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '下斜杠铃卧推',
 '强化下胸区域，同时减少肩部参与。',
 'CHEST,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '哑铃卧推',
 '比杠铃拥有更大活动范围，可改善左右肌力不平衡。',
 'CHEST,ARMS,SHOULDER',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '上斜哑铃卧推',
 '重点刺激上胸和肩前束。',
 'CHEST,SHOULDER',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '双杠臂屈伸',
 '身体前倾时重点刺激下胸，同时训练肱三头。',
 'CHEST,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '绳索夹胸',
 '孤立胸肌，顶峰收缩明显。',
 'CHEST',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '蝴蝶机夹胸',
 '器械孤立胸部动作，适合新手建立胸肌发力感。',
 'CHEST',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '俯卧撑',
 '经典自重推类动作，适合作为热身和耐力训练。',
 'CHEST,ARMS,CORE',
 JSON_ARRAY(),
 NULL),

-- ================= 背部 =================
('SYSTEM', NULL, '引体向上',
 '经典背部自重动作，主要刺激背阔肌。',
 'BACK,ARMS,CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '高位下拉',
 '适合替代引体向上，主要刺激背阔肌。',
 'BACK,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '杠铃划船',
 '经典背部厚度训练动作，强化上背与背阔。',
 'BACK,ARMS,CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '哑铃单臂划船',
 '改善左右背部发力差异，强化背阔肌。',
 'BACK,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '坐姿划船',
 '稳定性高，适合中下背训练。',
 'BACK,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, 'T杠划船',
 '强化背部厚度和斜方肌。',
 'BACK,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '硬拉',
 '经典全身复合动作，强化后链。',
 'BACK,LEGS,CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '罗马尼亚硬拉',
 '重点刺激腘绳肌和臀部。',
 'LEGS,BACK,GLUTES',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '面拉',
 '强化肩后束与上背，改善圆肩。',
 'BACK,SHOULDER',
 JSON_ARRAY(),
 NULL),

-- ================= 肩部 =================
('SYSTEM', NULL, '杠铃肩推',
 '肩部经典推举动作，主要刺激肩前束。',
 'SHOULDER,ARMS,CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '哑铃肩推',
 '提升肩部稳定性与活动范围。',
 'SHOULDER,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '阿诺德推举',
 '旋转轨迹增加肩部刺激。',
 'SHOULDER,ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '哑铃侧平举',
 '经典肩中束孤立动作。',
 'SHOULDER',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '俯身飞鸟',
 '主要刺激肩后束。',
 'SHOULDER,BACK',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '杠铃耸肩',
 '强化斜方肌。',
 'SHOULDER,BACK',
 JSON_ARRAY(),
 NULL),

-- ================= 手臂 =================
('SYSTEM', NULL, '杠铃弯举',
 '经典肱二头训练动作。',
 'ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '哑铃弯举',
 '增强肱二头峰值收缩。',
 'ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '锤式弯举',
 '重点刺激肱肌与前臂。',
 'ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '绳索下压',
 '经典肱三头孤立动作。',
 'ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '仰卧臂屈伸',
 '重点刺激肱三头长头。',
 'ARMS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '窄距卧推',
 '复合型肱三头训练动作。',
 'ARMS,CHEST',
 JSON_ARRAY(),
 NULL),

-- ================= 腿部 =================
('SYSTEM', NULL, '杠铃深蹲',
 '下肢训练之王，强化股四头、臀部和核心。',
 'LEGS,CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '前蹲',
 '更偏向股四头刺激。',
 'LEGS,CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '保加利亚分腿蹲',
 '单腿训练，提高稳定性和平衡。',
 'LEGS,GLUTES,CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '腿举',
 '器械腿部复合动作。',
 'LEGS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '腿屈伸',
 '孤立股四头肌。',
 'LEGS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '腿弯举',
 '孤立腘绳肌。',
 'LEGS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '臀桥',
 '重点刺激臀大肌。',
 'GLUTES,CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '坐姿提踵',
 '孤立小腿训练。',
 'LEGS',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '站姿提踵',
 '强化腓肠肌。',
 'LEGS',
 JSON_ARRAY(),
 NULL),

-- ================= 核心 =================
('SYSTEM', NULL, '卷腹',
 '经典腹直肌训练动作。',
 'CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '平板支撑',
 '核心稳定训练动作。',
 'CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '俄罗斯转体',
 '强化腹斜肌。',
 'CORE',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '悬垂举腿',
 '强化下腹与髋屈肌。',
 'CORE',
 JSON_ARRAY(),
 NULL),

-- ================= 有氧 =================
('SYSTEM', NULL, '跑步机跑步',
 '经典有氧训练。',
 'CARDIO',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '椭圆机',
 '低冲击有氧训练。',
 'CARDIO',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '划船机',
 '结合有氧与背部发力。',
 'CARDIO,BACK',
 JSON_ARRAY(),
 NULL),

('SYSTEM', NULL, '波比跳',
 '高强度全身有氧动作。',
 'CARDIO,CORE,LEGS',
 JSON_ARRAY(),
 NULL);


INSERT INTO action_muscle_target
(action_id, muscle_code, target_role, sort_order)
VALUES

-- ================= 胸部 =================

-- 杠铃卧推
((SELECT id FROM action_library WHERE action_name='杠铃卧推' AND scope='SYSTEM'), 'CHEST_MIDDLE', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='杠铃卧推' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='杠铃卧推' AND scope='SYSTEM'), 'FRONT_DELTS', 'SECONDARY', 3),

-- 上斜杠铃卧推
((SELECT id FROM action_library WHERE action_name='上斜杠铃卧推' AND scope='SYSTEM'), 'CHEST_UPPER', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='上斜杠铃卧推' AND scope='SYSTEM'), 'FRONT_DELTS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='上斜杠铃卧推' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 3),

-- 下斜杠铃卧推
((SELECT id FROM action_library WHERE action_name='下斜杠铃卧推' AND scope='SYSTEM'), 'CHEST_LOWER', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='下斜杠铃卧推' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 2),

-- 哑铃卧推
((SELECT id FROM action_library WHERE action_name='哑铃卧推' AND scope='SYSTEM'), 'CHEST_MIDDLE', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='哑铃卧推' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='哑铃卧推' AND scope='SYSTEM'), 'FRONT_DELTS', 'SECONDARY', 3),

-- 上斜哑铃卧推
((SELECT id FROM action_library WHERE action_name='上斜哑铃卧推' AND scope='SYSTEM'), 'CHEST_UPPER', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='上斜哑铃卧推' AND scope='SYSTEM'), 'FRONT_DELTS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='上斜哑铃卧推' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 3),

-- 双杠臂屈伸
((SELECT id FROM action_library WHERE action_name='双杠臂屈伸' AND scope='SYSTEM'), 'CHEST_LOWER', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='双杠臂屈伸' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='双杠臂屈伸' AND scope='SYSTEM'), 'FRONT_DELTS', 'SECONDARY', 3),

-- 绳索夹胸
((SELECT id FROM action_library WHERE action_name='绳索夹胸' AND scope='SYSTEM'), 'CHEST_MIDDLE', 'PRIMARY', 1),

-- 蝴蝶机夹胸
((SELECT id FROM action_library WHERE action_name='蝴蝶机夹胸' AND scope='SYSTEM'), 'CHEST_MIDDLE', 'PRIMARY', 1),

-- 俯卧撑
((SELECT id FROM action_library WHERE action_name='俯卧撑' AND scope='SYSTEM'), 'CHEST_MIDDLE', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='俯卧撑' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='俯卧撑' AND scope='SYSTEM'), 'FRONT_DELTS', 'SECONDARY', 3),
((SELECT id FROM action_library WHERE action_name='俯卧撑' AND scope='SYSTEM'), 'ABS', 'SECONDARY', 4),

-- ================= 背部 =================

-- 引体向上
((SELECT id FROM action_library WHERE action_name='引体向上' AND scope='SYSTEM'), 'LATS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='引体向上' AND scope='SYSTEM'), 'BICEPS', 'SECONDARY', 2),

-- 高位下拉
((SELECT id FROM action_library WHERE action_name='高位下拉' AND scope='SYSTEM'), 'LATS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='高位下拉' AND scope='SYSTEM'), 'BICEPS', 'SECONDARY', 2),

-- 杠铃划船
((SELECT id FROM action_library WHERE action_name='杠铃划船' AND scope='SYSTEM'), 'UPPER_BACK', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='杠铃划船' AND scope='SYSTEM'), 'LATS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='杠铃划船' AND scope='SYSTEM'), 'BICEPS', 'SECONDARY', 3),

-- 哑铃单臂划船
((SELECT id FROM action_library WHERE action_name='哑铃单臂划船' AND scope='SYSTEM'), 'LATS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='哑铃单臂划船' AND scope='SYSTEM'), 'UPPER_BACK', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='哑铃单臂划船' AND scope='SYSTEM'), 'BICEPS', 'SECONDARY', 3),

-- 坐姿划船
((SELECT id FROM action_library WHERE action_name='坐姿划船' AND scope='SYSTEM'), 'UPPER_BACK', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='坐姿划船' AND scope='SYSTEM'), 'LATS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='坐姿划船' AND scope='SYSTEM'), 'BICEPS', 'SECONDARY', 3),

-- T杠划船
((SELECT id FROM action_library WHERE action_name='T杠划船' AND scope='SYSTEM'), 'UPPER_BACK', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='T杠划船' AND scope='SYSTEM'), 'LATS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='T杠划船' AND scope='SYSTEM'), 'TRAPS', 'SECONDARY', 3),
((SELECT id FROM action_library WHERE action_name='T杠划船' AND scope='SYSTEM'), 'BICEPS', 'SECONDARY', 4),

-- 硬拉
((SELECT id FROM action_library WHERE action_name='硬拉' AND scope='SYSTEM'), 'LOWER_BACK', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='硬拉' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='硬拉' AND scope='SYSTEM'), 'HAMSTRINGS', 'SECONDARY', 3),
((SELECT id FROM action_library WHERE action_name='硬拉' AND scope='SYSTEM'), 'TRAPS', 'SECONDARY', 4),

-- 罗马尼亚硬拉
((SELECT id FROM action_library WHERE action_name='罗马尼亚硬拉' AND scope='SYSTEM'), 'HAMSTRINGS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='罗马尼亚硬拉' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='罗马尼亚硬拉' AND scope='SYSTEM'), 'LOWER_BACK', 'SECONDARY', 3),

-- 面拉
((SELECT id FROM action_library WHERE action_name='面拉' AND scope='SYSTEM'), 'REAR_DELTS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='面拉' AND scope='SYSTEM'), 'TRAPS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='面拉' AND scope='SYSTEM'), 'UPPER_BACK', 'SECONDARY', 3),

-- ================= 肩部 =================

-- 杠铃肩推
((SELECT id FROM action_library WHERE action_name='杠铃肩推' AND scope='SYSTEM'), 'FRONT_DELTS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='杠铃肩推' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 2),

-- 哑铃肩推
((SELECT id FROM action_library WHERE action_name='哑铃肩推' AND scope='SYSTEM'), 'FRONT_DELTS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='哑铃肩推' AND scope='SYSTEM'), 'SIDE_DELTS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='哑铃肩推' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 3),

-- 阿诺德推举
((SELECT id FROM action_library WHERE action_name='阿诺德推举' AND scope='SYSTEM'), 'FRONT_DELTS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='阿诺德推举' AND scope='SYSTEM'), 'SIDE_DELTS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='阿诺德推举' AND scope='SYSTEM'), 'TRICEPS', 'SECONDARY', 3),

-- 哑铃侧平举
((SELECT id FROM action_library WHERE action_name='哑铃侧平举' AND scope='SYSTEM'), 'SIDE_DELTS', 'PRIMARY', 1),

-- 俯身飞鸟
((SELECT id FROM action_library WHERE action_name='俯身飞鸟' AND scope='SYSTEM'), 'REAR_DELTS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='俯身飞鸟' AND scope='SYSTEM'), 'UPPER_BACK', 'SECONDARY', 2),

-- 杠铃耸肩
((SELECT id FROM action_library WHERE action_name='杠铃耸肩' AND scope='SYSTEM'), 'TRAPS', 'PRIMARY', 1),

-- ================= 手臂 =================

-- 杠铃弯举
((SELECT id FROM action_library WHERE action_name='杠铃弯举' AND scope='SYSTEM'), 'BICEPS', 'PRIMARY', 1),

-- 哑铃弯举
((SELECT id FROM action_library WHERE action_name='哑铃弯举' AND scope='SYSTEM'), 'BICEPS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='哑铃弯举' AND scope='SYSTEM'), 'FOREARMS', 'SECONDARY', 2),

-- 锤式弯举
((SELECT id FROM action_library WHERE action_name='锤式弯举' AND scope='SYSTEM'), 'BICEPS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='锤式弯举' AND scope='SYSTEM'), 'FOREARMS', 'SECONDARY', 2),

-- 绳索下压
((SELECT id FROM action_library WHERE action_name='绳索下压' AND scope='SYSTEM'), 'TRICEPS', 'PRIMARY', 1),

-- 仰卧臂屈伸
((SELECT id FROM action_library WHERE action_name='仰卧臂屈伸' AND scope='SYSTEM'), 'TRICEPS', 'PRIMARY', 1),

-- 窄距卧推
((SELECT id FROM action_library WHERE action_name='窄距卧推' AND scope='SYSTEM'), 'TRICEPS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='窄距卧推' AND scope='SYSTEM'), 'CHEST_MIDDLE', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='窄距卧推' AND scope='SYSTEM'), 'FRONT_DELTS', 'SECONDARY', 3),

-- ================= 腿部 =================

-- 杠铃深蹲
((SELECT id FROM action_library WHERE action_name='杠铃深蹲' AND scope='SYSTEM'), 'QUADS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='杠铃深蹲' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='杠铃深蹲' AND scope='SYSTEM'), 'LOWER_BACK', 'SECONDARY', 3),

-- 前蹲
((SELECT id FROM action_library WHERE action_name='前蹲' AND scope='SYSTEM'), 'QUADS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='前蹲' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='前蹲' AND scope='SYSTEM'), 'ABS', 'SECONDARY', 3),

-- 保加利亚分腿蹲
((SELECT id FROM action_library WHERE action_name='保加利亚分腿蹲' AND scope='SYSTEM'), 'QUADS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='保加利亚分腿蹲' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 2),

-- 腿举
((SELECT id FROM action_library WHERE action_name='腿举' AND scope='SYSTEM'), 'QUADS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='腿举' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='腿举' AND scope='SYSTEM'), 'HAMSTRINGS', 'SECONDARY', 3),

-- 腿屈伸
((SELECT id FROM action_library WHERE action_name='腿屈伸' AND scope='SYSTEM'), 'QUADS', 'PRIMARY', 1),

-- 腿弯举
((SELECT id FROM action_library WHERE action_name='腿弯举' AND scope='SYSTEM'), 'HAMSTRINGS', 'PRIMARY', 1),

-- 臀桥
((SELECT id FROM action_library WHERE action_name='臀桥' AND scope='SYSTEM'), 'GLUTES', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='臀桥' AND scope='SYSTEM'), 'HAMSTRINGS', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='臀桥' AND scope='SYSTEM'), 'LOWER_BACK', 'SECONDARY', 3),

-- 坐姿提踵
((SELECT id FROM action_library WHERE action_name='坐姿提踵' AND scope='SYSTEM'), 'CALVES', 'PRIMARY', 1),

-- 站姿提踵
((SELECT id FROM action_library WHERE action_name='站姿提踵' AND scope='SYSTEM'), 'CALVES', 'PRIMARY', 1),

-- ================= 核心 =================

-- 卷腹
((SELECT id FROM action_library WHERE action_name='卷腹' AND scope='SYSTEM'), 'ABS', 'PRIMARY', 1),

-- 平板支撑
((SELECT id FROM action_library WHERE action_name='平板支撑' AND scope='SYSTEM'), 'ABS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='平板支撑' AND scope='SYSTEM'), 'LOWER_BACK', 'SECONDARY', 2),

-- 俄罗斯转体
((SELECT id FROM action_library WHERE action_name='俄罗斯转体' AND scope='SYSTEM'), 'OBLIQUES', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='俄罗斯转体' AND scope='SYSTEM'), 'ABS', 'SECONDARY', 2),

-- 悬垂举腿
((SELECT id FROM action_library WHERE action_name='悬垂举腿' AND scope='SYSTEM'), 'ABS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='悬垂举腿' AND scope='SYSTEM'), 'OBLIQUES', 'SECONDARY', 2),

-- ================= 有氧 =================

-- 跑步机跑步
((SELECT id FROM action_library WHERE action_name='跑步机跑步' AND scope='SYSTEM'), 'QUADS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='跑步机跑步' AND scope='SYSTEM'), 'CALVES', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='跑步机跑步' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 3),

-- 椭圆机
((SELECT id FROM action_library WHERE action_name='椭圆机' AND scope='SYSTEM'), 'QUADS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='椭圆机' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='椭圆机' AND scope='SYSTEM'), 'CALVES', 'SECONDARY', 3),

-- 划船机
((SELECT id FROM action_library WHERE action_name='划船机' AND scope='SYSTEM'), 'LATS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='划船机' AND scope='SYSTEM'), 'UPPER_BACK', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='划船机' AND scope='SYSTEM'), 'QUADS', 'SECONDARY', 3),
((SELECT id FROM action_library WHERE action_name='划船机' AND scope='SYSTEM'), 'BICEPS', 'SECONDARY', 4),

-- 波比跳
((SELECT id FROM action_library WHERE action_name='波比跳' AND scope='SYSTEM'), 'QUADS', 'PRIMARY', 1),
((SELECT id FROM action_library WHERE action_name='波比跳' AND scope='SYSTEM'), 'GLUTES', 'SECONDARY', 2),
((SELECT id FROM action_library WHERE action_name='波比跳' AND scope='SYSTEM'), 'CHEST_MIDDLE', 'SECONDARY', 3),
((SELECT id FROM action_library WHERE action_name='波比跳' AND scope='SYSTEM'), 'ABS', 'SECONDARY', 4),
((SELECT id FROM action_library WHERE action_name='波比跳' AND scope='SYSTEM'), 'FRONT_DELTS', 'SECONDARY', 5);

-- =========================
-- food_library 初始化数据
-- 仅 SYSTEM 公共食物
-- =========================

INSERT INTO food_library
(scope, user_id, food_name, category_type, image_url, status, created_by)
VALUES

-- 主食
('SYSTEM', NULL, '生大米', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '熟米饭', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '糙米饭', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '炒饭', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '白面条(生)', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '煮面条', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '炒面', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '全麦面包', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '白面包', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '燕麦片', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '红薯(生)', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '蒸红薯', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '玉米', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '土豆(生)', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '水煮土豆', 'STAPLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '炸薯条', 'STAPLE', NULL, 'ACTIVE', NULL),

-- 牛肉
('SYSTEM', NULL, '生牛肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '牛排(煎)', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '卤牛肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '炒牛肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '炖牛肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '肥牛卷', 'MEAT', NULL, 'ACTIVE', NULL),

-- 猪肉
('SYSTEM', NULL, '瘦猪肉(生)', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '五花肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '红烧肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '猪里脊', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '炒肉丝', 'MEAT', NULL, 'ACTIVE', NULL),

-- 鸡肉
('SYSTEM', NULL, '鸡胸肉(生)', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '水煮鸡胸肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '煎鸡胸肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '炸鸡', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '鸡腿肉', 'MEAT', NULL, 'ACTIVE', NULL),

-- 羊肉
('SYSTEM', NULL, '生羊肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '涮羊肉', 'MEAT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '烤羊肉串', 'MEAT', NULL, 'ACTIVE', NULL),

-- 鱼虾海鲜
('SYSTEM', NULL, '三文鱼', 'SEAFOOD', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '金枪鱼', 'SEAFOOD', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '鳕鱼', 'SEAFOOD', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '虾仁', 'SEAFOOD', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '煎鱼', 'SEAFOOD', NULL, 'ACTIVE', NULL),

-- 蛋奶豆制品
('SYSTEM', NULL, '鸡蛋', 'EGG_DAIRY_SOY', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '蛋清', 'EGG_DAIRY_SOY', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '全脂牛奶', 'EGG_DAIRY_SOY', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '低脂牛奶', 'EGG_DAIRY_SOY', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '无糖酸奶', 'EGG_DAIRY_SOY', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '北豆腐', 'EGG_DAIRY_SOY', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '豆浆', 'EGG_DAIRY_SOY', NULL, 'ACTIVE', NULL),

-- 水果
('SYSTEM', NULL, '苹果', 'FRUIT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '香蕉', 'FRUIT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '橙子', 'FRUIT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '草莓', 'FRUIT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '蓝莓', 'FRUIT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '西瓜', 'FRUIT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '牛油果', 'FRUIT', NULL, 'ACTIVE', NULL),

-- 蔬菜
('SYSTEM', NULL, '西兰花', 'VEGETABLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '菠菜', 'VEGETABLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '黄瓜', 'VEGETABLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '番茄', 'VEGETABLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '胡萝卜', 'VEGETABLE', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '生菜', 'VEGETABLE', NULL, 'ACTIVE', NULL),

-- 坚果
('SYSTEM', NULL, '杏仁', 'NUT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '腰果', 'NUT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '花生', 'NUT', NULL, 'ACTIVE', NULL),

-- 饮品/其他
('SYSTEM', NULL, '乳清蛋白粉', 'NUT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '可乐', 'NUT', NULL, 'ACTIVE', NULL),
('SYSTEM', NULL, '无糖可乐', 'NUT', NULL, 'ACTIVE', NULL);

-- =========================
-- food_nutrition 初始化数据
-- 当前仅生成 PER_100G
-- 热量 calories = 碳水*4 + 蛋白*4 + 脂肪*9
-- =========================

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
VALUES

-- ================= 主食 =================

((SELECT id FROM food_library WHERE food_name='生大米' AND scope='SYSTEM'), 'PER_100G', 100, 100, 77.0, 7.4, 0.6, 344.6),
((SELECT id FROM food_library WHERE food_name='熟米饭' AND scope='SYSTEM'), 'PER_100G', 100, 100, 25.9, 2.6, 0.3, 117.5),
((SELECT id FROM food_library WHERE food_name='糙米饭' AND scope='SYSTEM'), 'PER_100G', 100, 100, 23.0, 2.7, 1.0, 110.8),
((SELECT id FROM food_library WHERE food_name='炒饭' AND scope='SYSTEM'), 'PER_100G', 100, 100, 31.0, 5.8, 8.5, 225.7),

((SELECT id FROM food_library WHERE food_name='白面条(生)' AND scope='SYSTEM'), 'PER_100G', 100, 100, 72.0, 11.0, 1.5, 347.5),
((SELECT id FROM food_library WHERE food_name='煮面条' AND scope='SYSTEM'), 'PER_100G', 100, 100, 25.0, 5.0, 1.1, 129.9),
((SELECT id FROM food_library WHERE food_name='炒面' AND scope='SYSTEM'), 'PER_100G', 100, 100, 28.0, 6.0, 9.0, 221.0),

((SELECT id FROM food_library WHERE food_name='全麦面包' AND scope='SYSTEM'), 'PER_100G', 100, 100, 43.0, 13.0, 4.2, 259.8),
((SELECT id FROM food_library WHERE food_name='白面包' AND scope='SYSTEM'), 'PER_100G', 100, 100, 49.0, 8.0, 3.2, 255.8),

((SELECT id FROM food_library WHERE food_name='燕麦片' AND scope='SYSTEM'), 'PER_100G', 100, 100, 66.0, 16.9, 6.9, 394.7),

((SELECT id FROM food_library WHERE food_name='红薯(生)' AND scope='SYSTEM'), 'PER_100G', 100, 100, 20.1, 1.6, 0.1, 87.3),
((SELECT id FROM food_library WHERE food_name='蒸红薯' AND scope='SYSTEM'), 'PER_100G', 100, 100, 21.0, 1.4, 0.2, 90.2),

((SELECT id FROM food_library WHERE food_name='玉米' AND scope='SYSTEM'), 'PER_100G', 100, 100, 21.0, 4.0, 1.5, 111.5),

((SELECT id FROM food_library WHERE food_name='土豆(生)' AND scope='SYSTEM'), 'PER_100G', 100, 100, 17.0, 2.0, 0.1, 76.9),
((SELECT id FROM food_library WHERE food_name='水煮土豆' AND scope='SYSTEM'), 'PER_100G', 100, 100, 15.0, 1.9, 0.1, 68.5),
((SELECT id FROM food_library WHERE food_name='炸薯条' AND scope='SYSTEM'), 'PER_100G', 100, 100, 41.0, 3.4, 15.0, 315.6),

-- ================= 牛肉 =================

((SELECT id FROM food_library WHERE food_name='生牛肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 20.4, 10.2, 173.4),
((SELECT id FROM food_library WHERE food_name='牛排(煎)' AND scope='SYSTEM'), 'PER_100G', 100, 100, 1.0, 27.0, 15.0, 247.0),
((SELECT id FROM food_library WHERE food_name='卤牛肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 3.0, 27.0, 8.0, 188.0),
((SELECT id FROM food_library WHERE food_name='炒牛肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 4.0, 22.0, 12.0, 212.0),
((SELECT id FROM food_library WHERE food_name='炖牛肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 2.0, 18.0, 9.0, 161.0),
((SELECT id FROM food_library WHERE food_name='肥牛卷' AND scope='SYSTEM'), 'PER_100G', 100, 100, 1.0, 13.0, 28.0, 304.0),

-- ================= 猪肉 =================

((SELECT id FROM food_library WHERE food_name='瘦猪肉(生)' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 20.3, 6.2, 139.0),
((SELECT id FROM food_library WHERE food_name='五花肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 1.0, 9.3, 53.0, 532.2),
((SELECT id FROM food_library WHERE food_name='红烧肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 6.0, 13.0, 35.0, 403.0),
((SELECT id FROM food_library WHERE food_name='猪里脊' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 22.0, 4.0, 124.0),
((SELECT id FROM food_library WHERE food_name='炒肉丝' AND scope='SYSTEM'), 'PER_100G', 100, 100, 5.0, 18.0, 14.0, 222.0),

-- ================= 鸡肉 =================

((SELECT id FROM food_library WHERE food_name='鸡胸肉(生)' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 23.0, 1.9, 109.1),
((SELECT id FROM food_library WHERE food_name='水煮鸡胸肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 29.0, 3.0, 143.0),
((SELECT id FROM food_library WHERE food_name='煎鸡胸肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 1.0, 27.0, 8.0, 188.0),
((SELECT id FROM food_library WHERE food_name='炸鸡' AND scope='SYSTEM'), 'PER_100G', 100, 100, 15.0, 20.0, 18.0, 302.0),
((SELECT id FROM food_library WHERE food_name='鸡腿肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 18.0, 12.0, 180.0),

-- ================= 羊肉 =================

((SELECT id FROM food_library WHERE food_name='生羊肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 18.0, 14.0, 198.0),
((SELECT id FROM food_library WHERE food_name='涮羊肉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 1.0, 20.0, 13.0, 201.0),
((SELECT id FROM food_library WHERE food_name='烤羊肉串' AND scope='SYSTEM'), 'PER_100G', 100, 100, 5.0, 20.0, 18.0, 274.0);

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
VALUES

-- ================= 鱼虾海鲜 =================

((SELECT id FROM food_library WHERE food_name='三文鱼' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 20.4, 13.4, 197.2),
((SELECT id FROM food_library WHERE food_name='金枪鱼' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 24.0, 1.0, 105.0),
((SELECT id FROM food_library WHERE food_name='鳕鱼' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 18.0, 0.7, 78.3),
((SELECT id FROM food_library WHERE food_name='虾仁' AND scope='SYSTEM'), 'PER_100G', 100, 100, 1.0, 20.0, 1.0, 93.0),
((SELECT id FROM food_library WHERE food_name='煎鱼' AND scope='SYSTEM'), 'PER_100G', 100, 100, 3.0, 22.0, 10.0, 190.0),

-- ================= 蛋奶豆制品 =================

((SELECT id FROM food_library WHERE food_name='鸡蛋' AND scope='SYSTEM'), 'PER_100G', 100, 100, 1.3, 13.3, 10.3, 149.1),
((SELECT id FROM food_library WHERE food_name='蛋清' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.7, 11.0, 0.2, 49.0),

((SELECT id FROM food_library WHERE food_name='全脂牛奶' AND scope='SYSTEM'), 'PER_100G', 100, 100, 4.8, 3.2, 3.6, 64.4),
((SELECT id FROM food_library WHERE food_name='低脂牛奶' AND scope='SYSTEM'), 'PER_100G', 100, 100, 5.0, 3.4, 1.5, 47.1),

((SELECT id FROM food_library WHERE food_name='无糖酸奶' AND scope='SYSTEM'), 'PER_100G', 100, 100, 5.0, 4.0, 3.0, 63.0),

((SELECT id FROM food_library WHERE food_name='北豆腐' AND scope='SYSTEM'), 'PER_100G', 100, 100, 2.0, 12.0, 7.0, 119.0),

((SELECT id FROM food_library WHERE food_name='豆浆' AND scope='SYSTEM'), 'PER_100G', 100, 100, 1.8, 3.0, 1.6, 33.6),

-- ================= 水果 =================

((SELECT id FROM food_library WHERE food_name='苹果' AND scope='SYSTEM'), 'PER_100G', 100, 100, 13.7, 0.3, 0.2, 58.6),

((SELECT id FROM food_library WHERE food_name='香蕉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 22.8, 1.1, 0.3, 98.3),

((SELECT id FROM food_library WHERE food_name='橙子' AND scope='SYSTEM'), 'PER_100G', 100, 100, 11.0, 0.8, 0.2, 49.0),

((SELECT id FROM food_library WHERE food_name='草莓' AND scope='SYSTEM'), 'PER_100G', 100, 100, 7.7, 0.8, 0.3, 37.5),

((SELECT id FROM food_library WHERE food_name='蓝莓' AND scope='SYSTEM'), 'PER_100G', 100, 100, 14.5, 0.7, 0.3, 63.5),

((SELECT id FROM food_library WHERE food_name='西瓜' AND scope='SYSTEM'), 'PER_100G', 100, 100, 7.5, 0.6, 0.2, 34.2),

((SELECT id FROM food_library WHERE food_name='牛油果' AND scope='SYSTEM'), 'PER_100G', 100, 100, 8.5, 2.0, 15.0, 175.0),

-- ================= 蔬菜 =================

((SELECT id FROM food_library WHERE food_name='西兰花' AND scope='SYSTEM'), 'PER_100G', 100, 100, 6.6, 2.8, 0.4, 41.2),

((SELECT id FROM food_library WHERE food_name='菠菜' AND scope='SYSTEM'), 'PER_100G', 100, 100, 3.6, 2.9, 0.4, 29.6),

((SELECT id FROM food_library WHERE food_name='黄瓜' AND scope='SYSTEM'), 'PER_100G', 100, 100, 3.6, 0.7, 0.1, 17.7),

((SELECT id FROM food_library WHERE food_name='番茄' AND scope='SYSTEM'), 'PER_100G', 100, 100, 3.9, 0.9, 0.2, 21.8),

((SELECT id FROM food_library WHERE food_name='胡萝卜' AND scope='SYSTEM'), 'PER_100G', 100, 100, 9.6, 0.9, 0.2, 44.2),

((SELECT id FROM food_library WHERE food_name='生菜' AND scope='SYSTEM'), 'PER_100G', 100, 100, 2.9, 1.4, 0.2, 19.4),

-- ================= 坚果 =================

((SELECT id FROM food_library WHERE food_name='杏仁' AND scope='SYSTEM'), 'PER_100G', 100, 100, 21.6, 21.2, 49.9, 613.1),

((SELECT id FROM food_library WHERE food_name='腰果' AND scope='SYSTEM'), 'PER_100G', 100, 100, 30.2, 18.2, 43.8, 587.8),

((SELECT id FROM food_library WHERE food_name='花生' AND scope='SYSTEM'), 'PER_100G', 100, 100, 16.1, 25.8, 49.2, 613.2),

-- ================= 饮品/补剂 =================

((SELECT id FROM food_library WHERE food_name='乳清蛋白粉' AND scope='SYSTEM'), 'PER_100G', 100, 100, 8.0, 78.0, 6.0, 402.0),

((SELECT id FROM food_library WHERE food_name='可乐' AND scope='SYSTEM'), 'PER_100G', 100, 100, 10.6, 0.0, 0.0, 42.4),

((SELECT id FROM food_library WHERE food_name='无糖可乐' AND scope='SYSTEM'), 'PER_100G', 100, 100, 0.0, 0.0, 0.0, 0.0);

-- 常用非100g单位示例：营养值按该单位可食重量折算
INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
VALUES
((SELECT id FROM food_library WHERE food_name='香蕉' AND scope='SYSTEM'), 'PER_SMALL_ROOT', 110, 75, 17.1, 0.8, 0.2, 73.7),
((SELECT id FROM food_library WHERE food_name='香蕉' AND scope='SYSTEM'), 'PER_ROOT', 150, 100, 22.8, 1.1, 0.3, 98.3),
((SELECT id FROM food_library WHERE food_name='香蕉' AND scope='SYSTEM'), 'PER_LARGE_ROOT', 190, 130, 29.6, 1.4, 0.4, 127.8),
((SELECT id FROM food_library WHERE food_name='苹果' AND scope='SYSTEM'), 'PER_PIECE', 200, 170, 23.3, 0.5, 0.3, 99.6),
((SELECT id FROM food_library WHERE food_name='橙子' AND scope='SYSTEM'), 'PER_PIECE', 180, 140, 15.4, 1.1, 0.3, 68.6),
((SELECT id FROM food_library WHERE food_name='鸡蛋' AND scope='SYSTEM'), 'PER_PIECE', 60, 50, 0.7, 6.7, 5.2, 74.6),
((SELECT id FROM food_library WHERE food_name='全脂牛奶' AND scope='SYSTEM'), 'PER_CUP', 250, 250, 12.0, 8.0, 9.0, 161.0),
((SELECT id FROM food_library WHERE food_name='可乐' AND scope='SYSTEM'), 'PER_BOTTLE', 500, 500, 53.0, 0.0, 0.0, 212.0),
((SELECT id FROM food_library WHERE food_name='全麦面包' AND scope='SYSTEM'), 'PER_SLICE', 35, 35, 15.1, 4.6, 1.5, 90.9),
((SELECT id FROM food_library WHERE food_name='乳清蛋白粉' AND scope='SYSTEM'), 'PER_SCOOP', 30, 30, 2.4, 23.4, 1.8, 120.6),
((SELECT id FROM food_library WHERE food_name='烤羊肉串' AND scope='SYSTEM'), 'PER_STICK', 35, 30, 1.5, 6.0, 5.4, 82.2);