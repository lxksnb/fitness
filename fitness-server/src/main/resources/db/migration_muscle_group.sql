-- 动作库肌群拆分迁移脚本
-- 适用于已有数据库；全新初始化请直接使用 init.sql。

CREATE TABLE IF NOT EXISTS action_muscle_target (
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

INSERT IGNORE INTO sys_dict_type (dict_code, dict_name) VALUES ('muscle_group', '肌群');

INSERT IGNORE INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort) VALUES
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

-- 示例：给“杠铃卧推”补主要/辅助肌群，实际 action_id 以现场数据为准。
-- INSERT INTO action_muscle_target (action_id, muscle_code, target_role, sort_order)
-- SELECT id, 'CHEST_UPPER', 'PRIMARY', 1 FROM action_library WHERE action_name = '杠铃卧推'
-- UNION ALL SELECT id, 'CHEST_MIDDLE', 'PRIMARY', 2 FROM action_library WHERE action_name = '杠铃卧推'
-- UNION ALL SELECT id, 'CHEST_LOWER', 'PRIMARY', 3 FROM action_library WHERE action_name = '杠铃卧推'
-- UNION ALL SELECT id, 'TRICEPS', 'SECONDARY', 1 FROM action_library WHERE action_name = '杠铃卧推'
-- UNION ALL SELECT id, 'FRONT_DELTS', 'SECONDARY', 2 FROM action_library WHERE action_name = '杠铃卧推';
