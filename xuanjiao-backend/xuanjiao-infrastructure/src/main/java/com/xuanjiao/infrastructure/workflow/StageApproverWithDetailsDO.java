package com.xuanjiao.infrastructure.workflow;

/**
 * 审批人配置详情数据对象
 *
 * <p>用于接收审批人配置JOIN查询结果，包含审批人及其关联的用户/角色/部门/子流程信息。</p>
 * <p>这是 infrastructure 层专门用于接收多表 JOIN 查询结果的数据对象，不要与 domain 层实体混淆。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class StageApproverWithDetailsDO {

    /**
     * 审批人配置ID
     */
    private Long id;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 审批人类型：USER-指定用户、ROLE-指定角色、DEPT-指定部门
     */
    private String approverType;

    /**
     * 审批人ID（用户ID/角色ID/部门ID）
     */
    private Long approverId;

    /**
     * 是否校验二级部门：0-否、1-是
     */
    private Integer checkSecondaryDept;

    /**
     * 子流程ID
     */
    private Long subWorkflowId;

    // 关联信息（非数据库字段）

    /**
     * 用户名（USER类型）
     */
    private String username;

    /**
     * 真实姓名（USER类型）
     */
    private String realName;

    /**
     * 部门ID（USER类型）
     */
    private Long deptId;

    /**
     * 角色ID（USER类型）
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 子流程名称
     */
    private String subWorkflowName;

    // Getter and Setter methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getApproverType() {
        return approverType;
    }

    public void setApproverType(String approverType) {
        this.approverType = approverType;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public Integer getCheckSecondaryDept() {
        return checkSecondaryDept;
    }

    public void setCheckSecondaryDept(Integer checkSecondaryDept) {
        this.checkSecondaryDept = checkSecondaryDept;
    }

    public Long getSubWorkflowId() {
        return subWorkflowId;
    }

    public void setSubWorkflowId(Long subWorkflowId) {
        this.subWorkflowId = subWorkflowId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getSubWorkflowName() {
        return subWorkflowName;
    }

    public void setSubWorkflowName(String subWorkflowName) {
        this.subWorkflowName = subWorkflowName;
    }
}
