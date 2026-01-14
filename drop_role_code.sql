-- 删除 sys_role 表的 code 字段
USE xuanjiao_s;

ALTER TABLE sys_role DROP COLUMN code;

-- 验证字段已删除
SHOW COLUMNS FROM sys_role;
