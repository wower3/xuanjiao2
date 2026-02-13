package com.xuanjiao.client.dto.material;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新素材申请单命令对象
 *
 * <p>封装更新素材录入申请所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationUpdateCmd {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;

    /**
     * 事项标题（更新时允许为空，仅提交审批时验证）
     */
    private String title;

    /**
     * 维护人ID
     */
    private Long maintainerId;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 是否签署版权保证声明（0-否，1-是）
     */
    private Integer guaranteeDeclaration;
}
