-- UserMapper Refactoring Verification Script
-- Tests all query patterns used in the 14 call sites

SELECT 'Test 1: Empty query' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE deleted = 0 ORDER BY id LIMIT 3;

SELECT 'Test 2: deptId=100 AND status=1' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE dept_id = 100 AND status = 1 AND deleted = 0;

SELECT 'Test 3: roleIds IN (1, 4)' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE role_id IN (1, 4) AND status = 1 AND deleted = 0;

SELECT 'Test 4: userIds IN (1, 2, 3)' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE id IN (1, 2, 3) AND deleted = 0;

SELECT 'Test 5: deptIds IN (100, 201)' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE dept_id IN (100, 201) AND status = 1 AND deleted = 0;

SELECT 'Test 6: keyword=admin' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE (username LIKE '%admin%' OR real_name LIKE '%admin%') AND deleted = 0 LIMIT 5;

SELECT 'Test 7: deptIds IN (201) AND status=1' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE dept_id IN (201) AND status = 1 AND deleted = 0;

SELECT 'Test 8: roleId=4 AND deptIds IN (201)' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE role_id = 4 AND dept_id IN (201) AND status = 1 AND deleted = 0;

SELECT 'Test 9: selectOneByUsername for admin' as test_name;
SELECT id, username, real_name, dept_id, role_id, status 
FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1;

SELECT 'Test 10: Total count of active users' as test_name;
SELECT COUNT(*) as total_users FROM sys_user WHERE deleted = 0;
