package com.xuanjiao.client;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 素材录入申请命令
 *
 * <p>封装创建或更新素材录入申请所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationCmd {

    /**
     * 事项标题
     */
    @NotBlank(message = "事项标题不能为空")
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
