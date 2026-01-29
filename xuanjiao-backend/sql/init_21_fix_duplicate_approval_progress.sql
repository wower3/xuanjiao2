-- 修复审批进度表中的重复数据
-- 保留每个 instance_id + stage_id 组合中ID最大的记录，删除其他重复记录

USE xuanjiao_s;

-- 查看重复数据
SELECT instance_id, stage_id, COUNT(*) as count
FROM approval_progress
GROUP BY instance_id, stage_id
HAVING COUNT(*) > 1;

-- 删除重复数据，保留ID最大的记录
DELETE p1 FROM approval_progress p1
INNER JOIN (
    SELECT instance_id, stage_id, MAX(id) as max_id
    FROM approval_progress
    GROUP BY instance_id, stage_id
    HAVING COUNT(*) > 1
) p2 ON p1.instance_id = p2.instance_id AND p1.stage_id = p2.stage_id
WHERE p1.id < p2.max_id;

-- 验证修复结果
SELECT instance_id, stage_id, COUNT(*) as count
FROM approval_progress
GROUP BY instance_id, stage_id
HAVING COUNT(*) > 1;

-- 如果上面的查询返回空结果，说明重复数据已清理完毕
