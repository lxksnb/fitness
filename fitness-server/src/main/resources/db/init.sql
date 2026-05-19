CREATE DATABASE IF NOT EXISTS fitness DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitness;

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
    serving_weight_g DECIMAL(6,1) NOT NULL COMMENT '该单位对应的大致重量(g), PER_100G固定为100',
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
    start_time DATETIME COMMENT '训练开始时间',
    end_time DATETIME COMMENT '训练结束时间',
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
