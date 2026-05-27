-- 食物库分类与营养单位重量迁移脚本
-- 适用于已有数据库；全新初始化请直接使用 init.sql。

-- 1. 表结构：食物分类、可食重量
SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'food_library'
          AND COLUMN_NAME = 'category_type'
    ),
    'SELECT 1',
    'ALTER TABLE food_library ADD COLUMN category_type VARCHAR(50) NOT NULL DEFAULT ''OTHER'' COMMENT ''食物分类: food_category字典值'' AFTER food_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'food_nutrition'
          AND COLUMN_NAME = 'edible_weight_g'
    ),
    'SELECT 1',
    'ALTER TABLE food_nutrition ADD COLUMN edible_weight_g DECIMAL(6,1) NOT NULL DEFAULT 100 COMMENT ''该单位对应的可食重量(g), PER_100G固定为100'' AFTER serving_weight_g'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 字典：食物分类
INSERT IGNORE INTO sys_dict_type (dict_code, dict_name)
VALUES ('food_category', '食物分类');

INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort)
SELECT t.id, v.dict_label, v.dict_value, v.sort
FROM sys_dict_type t
JOIN (
    SELECT '主食' dict_label, 'STAPLE' dict_value, 1 sort
    UNION ALL SELECT '肉类', 'MEAT', 2
    UNION ALL SELECT '鱼虾海鲜', 'SEAFOOD', 3
    UNION ALL SELECT '蛋奶豆制品', 'EGG_DAIRY_SOY', 4
    UNION ALL SELECT '水果', 'FRUIT', 5
    UNION ALL SELECT '蔬菜', 'VEGETABLE', 6
    UNION ALL SELECT '坚果', 'NUT', 7
    UNION ALL SELECT '饮品', 'DRINK', 8
    UNION ALL SELECT '补剂', 'SUPPLEMENT', 9
    UNION ALL SELECT '其他', 'OTHER', 99
) v ON t.dict_code = 'food_category'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_dict_data d
    WHERE d.dict_type_id = t.id
      AND d.dict_value = v.dict_value
);

-- 3. 字典：细分食物单位
INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort)
SELECT t.id, v.dict_label, v.dict_value, v.sort
FROM sys_dict_type t
JOIN (
    SELECT '每小根' dict_label, 'PER_SMALL_ROOT' dict_value, 10 sort
    UNION ALL SELECT '每根', 'PER_ROOT', 11
    UNION ALL SELECT '每大根', 'PER_LARGE_ROOT', 12
    UNION ALL SELECT '每小个', 'PER_SMALL_PIECE', 20
    UNION ALL SELECT '每个', 'PER_PIECE', 21
    UNION ALL SELECT '每大个', 'PER_LARGE_PIECE', 22
    UNION ALL SELECT '每杯', 'PER_CUP', 30
    UNION ALL SELECT '每瓶', 'PER_BOTTLE', 31
    UNION ALL SELECT '每片', 'PER_SLICE', 40
    UNION ALL SELECT '每勺', 'PER_SCOOP', 41
    UNION ALL SELECT '每串', 'PER_STICK', 42
) v ON t.dict_code = 'food_unit_type'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_dict_data d
    WHERE d.dict_type_id = t.id
      AND d.dict_value = v.dict_value
);

-- 4. 现有系统食物分类
UPDATE food_library
SET category_type = CASE
    WHEN food_name IN ('生大米', '熟米饭', '糙米饭', '炒饭', '白面条(生)', '煮面条', '炒面', '全麦面包', '白面包', '燕麦片', '红薯(生)', '蒸红薯', '玉米', '土豆(生)', '水煮土豆', '炸薯条') THEN 'STAPLE'
    WHEN food_name IN ('生牛肉', '牛排(煎)', '卤牛肉', '炒牛肉', '炖牛肉', '肥牛卷', '瘦猪肉(生)', '五花肉', '红烧肉', '猪里脊', '炒肉丝', '鸡胸肉(生)', '水煮鸡胸肉', '煎鸡胸肉', '炸鸡', '鸡腿肉', '生羊肉', '涮羊肉', '烤羊肉串') THEN 'MEAT'
    WHEN food_name IN ('三文鱼', '金枪鱼', '鳕鱼', '虾仁', '煎鱼') THEN 'SEAFOOD'
    WHEN food_name IN ('鸡蛋', '蛋清', '全脂牛奶', '低脂牛奶', '无糖酸奶', '北豆腐', '豆浆') THEN 'EGG_DAIRY_SOY'
    WHEN food_name IN ('苹果', '香蕉', '橙子', '草莓', '蓝莓', '西瓜', '牛油果') THEN 'FRUIT'
    WHEN food_name IN ('西兰花', '菠菜', '黄瓜', '番茄', '胡萝卜', '生菜') THEN 'VEGETABLE'
    WHEN food_name IN ('杏仁', '腰果', '花生') THEN 'NUT'
    WHEN food_name IN ('可乐', '无糖可乐') THEN 'DRINK'
    WHEN food_name IN ('乳清蛋白粉') THEN 'SUPPLEMENT'
    ELSE category_type
END
WHERE scope = 'SYSTEM';

-- 5. 现有营养行补齐可食重量
UPDATE food_nutrition
SET edible_weight_g = 100
WHERE unit_type = 'PER_100G';

UPDATE food_nutrition
SET edible_weight_g = serving_weight_g
WHERE unit_type <> 'PER_100G'
  AND (edible_weight_g IS NULL OR edible_weight_g = 100);

-- 6. 常用非100g单位示例：营养值按该单位可食重量折算
INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
SELECT f.id, 'PER_PIECE', 200, 170, 23.3, 0.5, 0.3, 99.6
FROM food_library f
WHERE f.food_name = '苹果' AND f.scope = 'SYSTEM'
  AND NOT EXISTS (SELECT 1 FROM food_nutrition n WHERE n.food_id = f.id AND n.unit_type = 'PER_PIECE');

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
SELECT f.id, 'PER_PIECE', 180, 140, 15.4, 1.1, 0.3, 68.6
FROM food_library f
WHERE f.food_name = '橙子' AND f.scope = 'SYSTEM'
  AND NOT EXISTS (SELECT 1 FROM food_nutrition n WHERE n.food_id = f.id AND n.unit_type = 'PER_PIECE');

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
SELECT f.id, 'PER_PIECE', 60, 50, 0.7, 6.7, 5.2, 74.6
FROM food_library f
WHERE f.food_name = '鸡蛋' AND f.scope = 'SYSTEM'
  AND NOT EXISTS (SELECT 1 FROM food_nutrition n WHERE n.food_id = f.id AND n.unit_type = 'PER_PIECE');

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
SELECT f.id, 'PER_CUP', 250, 250, 12.0, 8.0, 9.0, 161.0
FROM food_library f
WHERE f.food_name = '全脂牛奶' AND f.scope = 'SYSTEM'
  AND NOT EXISTS (SELECT 1 FROM food_nutrition n WHERE n.food_id = f.id AND n.unit_type = 'PER_CUP');

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
SELECT f.id, 'PER_BOTTLE', 500, 500, 53.0, 0.0, 0.0, 212.0
FROM food_library f
WHERE f.food_name = '可乐' AND f.scope = 'SYSTEM'
  AND NOT EXISTS (SELECT 1 FROM food_nutrition n WHERE n.food_id = f.id AND n.unit_type = 'PER_BOTTLE');

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
SELECT f.id, 'PER_SLICE', 35, 35, 15.1, 4.6, 1.5, 90.9
FROM food_library f
WHERE f.food_name = '全麦面包' AND f.scope = 'SYSTEM'
  AND NOT EXISTS (SELECT 1 FROM food_nutrition n WHERE n.food_id = f.id AND n.unit_type = 'PER_SLICE');

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
SELECT f.id, 'PER_SCOOP', 30, 30, 2.4, 23.4, 1.8, 120.6
FROM food_library f
WHERE f.food_name = '乳清蛋白粉' AND f.scope = 'SYSTEM'
  AND NOT EXISTS (SELECT 1 FROM food_nutrition n WHERE n.food_id = f.id AND n.unit_type = 'PER_SCOOP');

INSERT INTO food_nutrition
(food_id, unit_type, serving_weight_g, edible_weight_g, carb_grams, protein_grams, fat_grams, calories)
SELECT f.id, 'PER_STICK', 35, 30, 1.5, 6.0, 5.4, 82.2
FROM food_library f
WHERE f.food_name = '烤羊肉串' AND f.scope = 'SYSTEM'
  AND NOT EXISTS (SELECT 1 FROM food_nutrition n WHERE n.food_id = f.id AND n.unit_type = 'PER_STICK');
