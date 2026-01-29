-- ============================================================
-- 普通用户角色和数据创建脚本
-- 密码统一为: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
-- ============================================================

-- 1. 新增"普通用户"角色
INSERT INTO sys_role (name, role_type, description, status, create_time, update_time, deleted)
VALUES ('普通用户', 'COMMON_USER', '普通用户角色，无特殊管理权限', 1, NOW(), NOW(), 0);

-- 获取新插入的角色ID（应该是14，假设是最新插入的）
SET @common_user_role_id = LAST_INSERT_ID();

-- 2. 为所有部门创建普通用户
-- 部门列表：100, 101, 102, 1, 2, 3, 4, 5, 201, 202, 203, 204, 205, 206, 207

-- 总公司(100)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_100_user', 'e10adc3949ba59abbe56e057f20f883e', '总公司普通用户', 100, @common_user_role_id, 1);

-- 总部门(101)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_101_user', 'e10adc3949ba59abbe56e057f20f883e', '总部门普通用户', 101, @common_user_role_id, 1);

-- 分部门1(102)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_102_user', 'e10adc3949ba59abbe56e057f20f883e', '分部门1普通用户', 102, @common_user_role_id, 1);

-- 总经办(1)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_1_user', 'e10adc3949ba59abbe56e057f20f883e', '总经办普通用户', 1, @common_user_role_id, 1);

-- 技术部(2)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_2_user', 'e10adc3949ba59abbe56e057f20f883e', '技术部普通用户', 2, @common_user_role_id, 1);

-- 市场部(3)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_3_user', 'e10adc3949ba59abbe56e057f20f883e', '市场部普通用户', 3, @common_user_role_id, 1);

-- 财务部(4)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_4_user', 'e10adc3949ba59abbe56e057f20f883e', '财务部普通用户', 4, @common_user_role_id, 1);

-- 人事部(5)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_5_user', 'e10adc3949ba59abbe56e057f20f883e', '人事部普通用户', 5, @common_user_role_id, 1);

-- 总消保部(201)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_201_user', 'e10adc3949ba59abbe56e057f20f883e', '总消保部普通用户', 201, @common_user_role_id, 1);

-- 分消保部(202)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_202_user', 'e10adc3949ba59abbe56e057f20f883e', '分消保部普通用户', 202, @common_user_role_id, 1);

-- 技术一部(203)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_203_user', 'e10adc3949ba59abbe56e057f20f883e', '技术一部普通用户', 203, @common_user_role_id, 1);

-- 分部门2(204)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_204_user', 'e10adc3949ba59abbe56e057f20f883e', '分部门2普通用户', 204, @common_user_role_id, 1);

-- 分消保部2(205)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_205_user', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2普通用户', 205, @common_user_role_id, 1);

-- 二分部门1(206)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_206_user', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1普通用户', 206, @common_user_role_id, 1);

-- 二分部门2(207)
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status)
VALUES ('dept_207_user', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2普通用户', 207, @common_user_role_id, 1);

-- ============================================================
-- 验证数据
-- ============================================================
-- SELECT id, username, real_name, dept_id, role_id FROM sys_user WHERE username LIKE 'dept_%_user' ORDER BY dept_id;
