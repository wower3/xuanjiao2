package com.xuanjiao.app.user;

import com.xuanjiao.client.dto.common.PageResult;
import com.xuanjiao.client.dto.user.dto.UserDTO;
import com.xuanjiao.client.dto.user.UserGetListWithFilterQry;
import com.xuanjiao.infrastructure.dataobject.RoleDO;

import java.util.List;
import java.util.Set;

/**
 * 用户服务接口
 *
 * <p>提供用户的查询、管理等功能。支持基于角色的数据权限控制。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>用户CRUD操作</li>
 *   <li>用户列表查询（支持多条件筛选）</li>
 *   <li>部门权限控制（根据角色限制可见范围）</li>
 *   <li>用户搜索</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.user.impl.UserServiceImpl
 */
public interface UserService {

    /**
     * 获取当前登录用户信息
     *
     * <p>返回当前用户的完整信息，包含角色和部门信息。</p>
     *
     * @param userId 用户ID
     * @return 用户DTO，包含角色名称、角色类型、部门名称等
     */
    UserDTO getCurrentUser(Long userId);

    /**
     * 获取所有用户列表
     *
     * <p>返回系统中所有用户的列表。不受权限限制。</p>
     *
     * @return 用户DTO列表
     */
    List<UserDTO> list();

    /**
     * 获取当前用户所在分部的用户列表
     *
     * <p>返回当前用户所属二级机构及其子部门的所有用户。
     * 用于分消保管理岗的数据权限控制。</p>
     *
     * @param currentUserId 当前用户ID
     * @return 用户DTO列表
     */
    List<UserDTO> listByBranchDept(Long currentUserId);

    /**
     * 带筛选条件的用户列表查询
     *
     * <p>根据角色和部门条件筛选用户列表，同时应用数据权限控制。
     * 权限规则：
     * <ul>
     *   <li>系统管理员/总消保管理岗：可查看所有部门</li>
     *   <li>分消保管理岗：只能查看所属二级机构及子部门</li>
     *   <li>其他用户：无查看权限</li>
     * </ul></p>
     *
     * @param userId 当前用户ID（用于权限控制）
     * @param roleIds 角色ID列表（可选），null表示不筛选
     * @param deptId 部门ID（可选），null表示不筛选
     * @param includeSubDept 是否包含子部门，true时包含deptId的所有子部门
     * @return 用户列表
     */
    List<UserDTO> listWithFilter(Long userId, List<Long> roleIds, Long deptId, Boolean includeSubDept);

    /**
     * 根据ID获取用户
     *
     * <p>返回指定用户的完整信息，包含角色和部门信息。</p>
     *
     * @param id 用户ID
     * @return 用户DTO，不存在返回null
     */
    UserDTO getById(Long id);

    /**
     * 创建用户
     *
     * <p>创建新用户。默认密码为123456（MD5加密），默认状态为启用。</p>
     *
     * @param userDTO 用户DTO
     */
    void create(UserDTO userDTO);

    /**
     * 更新用户
     *
     * <p>更新已有用户的信息。可更新姓名、电话、邮箱、部门、角色、状态等。</p>
     *
     * @param userDTO 用户DTO
     * @throws RuntimeException 如果用户不存在
     */
    void update(UserDTO userDTO);

    /**
     * 删除用户
     *
     * <p>物理删除用户。注意：删除后无法恢复。</p>
     *
     * @param id 用户ID
     */
    void delete(Long id);

    /**
     * 获取用户所属的二级机构
     *
     * <p>二级机构是指level=2的部门。向上遍历部门树直到找到level=2的部门。</p>
     *
     * @param userId 用户ID
     * @return 二级机构ID，如果找不到返回null
     */
    Long getSecondaryDeptId(Long userId);

    /**
     * 获取用户允许查询的部门ID集合
     *
     * <p>根据用户角色确定可查询的部门范围：
     * <ul>
     *   <li>系统管理员/总消保管理岗：所有部门</li>
     *   <li>分消保管理岗：所属二级机构及子部门</li>
     *   <li>其他用户：空集合（无权限）</li>
     * </ul></p>
     *
     * @param currentUser 当前用户
     * @param currentRole 当前用户角色
     * @return 可查询的部门ID集合
     */
    Set<Long> getAllowedDeptIds(UserDTO currentUser, RoleDO currentRole);

    /**
     * 搜索用户
     *
     * <p>支持角色、部门、姓名等多条件筛选的用户搜索，带分页。
     * 同时应用数据权限控制。</p>
     *
     * @param userId 当前用户ID（用于权限控制）
     * @param qry 查询条件，包含角色ID列表、部门ID、关键词、分页参数等
     * @return 分页结果，包含用户基本信息、角色信息、部门信息
     */
    PageResult<UserDTO> searchUsers(Long userId, UserGetListWithFilterQry qry);
}
