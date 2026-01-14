-- ============================================================
-- 测试用户数据脚本
-- 密码统一为: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
-- ============================================================

-- 1. 删除除admin外的所有测试用户
DELETE FROM sys_user WHERE id != 1;

-- 2. 重置自增ID
ALTER TABLE sys_user AUTO_INCREMENT = 18;

-- ============================================================
-- 总消保部(201) - 4个用户
-- ============================================================
-- 总消保审批(GENERAL_APPROV, role_id=11) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('zong_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '总消保审批1', 201, 11, 1),
('zong_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '总消保审批2', 201, 11, 1);

-- 总消保管理岗(GENERAL_MGMT, role_id=4) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('zong_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '总消保管理岗1', 201, 4, 1),
('zong_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '总消保管理岗2', 201, 4, 1);

-- ============================================================
-- 分消保部1(202) - 8个用户
-- ============================================================
-- 分消保管理岗(BRANCH_MGMT, role_id=5) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('fen1_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1管理岗1', 202, 5, 1),
('fen1_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1管理岗2', 202, 5, 1);

-- 分消保审批(BRANCH_APPROV, role_id=10) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('fen1_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1审批1', 202, 10, 1),
('fen1_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1审批2', 202, 10, 1);

-- 分消保科负责人(BRANCH_SECTION, role_id=12) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('fen1_xb_section_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1科负责人1', 202, 12, 1),
('fen1_xb_section_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1科负责人2', 202, 12, 1);

-- 分消保部负责人(BRANCH_SECRETARY, role_id=13) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('fen1_xb_secretary_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1负责人1', 202, 13, 1),
('fen1_xb_secretary_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1负责人2', 202, 13, 1);

-- ============================================================
-- 分消保部2(205) - 8个用户
-- ============================================================
-- 分消保管理岗(BRANCH_MGMT, role_id=5) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('fen2_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2管理岗1', 205, 5, 1),
('fen2_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2管理岗2', 205, 5, 1);

-- 分消保审批(BRANCH_APPROV, role_id=10) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('fen2_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2审批1', 205, 10, 1),
('fen2_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2审批2', 205, 10, 1);

-- 分消保科负责人(BRANCH_SECTION, role_id=12) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('fen2_xb_section_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2科负责人1', 205, 12, 1),
('fen2_xb_section_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2科负责人2', 205, 12, 1);

-- 分消保部负责人(BRANCH_SECRETARY, role_id=13) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('fen2_xb_secretary_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2负责人1', 205, 13, 1),
('fen2_xb_secretary_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2负责人2', 205, 13, 1);

-- ============================================================
-- 二分部门1(206) - 4个用户
-- ============================================================
-- 二分消保管理(LEAF_MGMT, role_id=8) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('erfen1_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1消保管理1', 206, 8, 1),
('erfen1_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1消保管理2', 206, 8, 1);

-- 二分消保审批(LEAF_APPROV, role_id=9) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('erfen1_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1消保审批1', 206, 9, 1),
('erfen1_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1消保审批2', 206, 9, 1);

-- ============================================================
-- 二分部门2(207) - 4个用户
-- ============================================================
-- 二分消保管理(LEAF_MGMT, role_id=8) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('erfen2_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2消保管理1', 207, 8, 1),
('erfen2_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2消保管理2', 207, 8, 1);

-- 二分消保审批(LEAF_APPROV, role_id=9) × 2
INSERT INTO sys_user (username, password, real_name, dept_id, role_id, status) VALUES
('erfen2_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2消保审批1', 207, 9, 1),
('erfen2_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2消保审批2', 207, 9, 1);

-- ============================================================
-- 验证数据
-- ============================================================
-- SELECT id, username, real_name, dept_id, role_id FROM sys_user ORDER BY id;
