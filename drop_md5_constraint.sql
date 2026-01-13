-- 删除 asset 表的 MD5 唯一约束
ALTER TABLE asset DROP INDEX uk_md5;

-- 验证约束已删除
SHOW INDEX FROM asset WHERE Key_name = 'uk_md5';
