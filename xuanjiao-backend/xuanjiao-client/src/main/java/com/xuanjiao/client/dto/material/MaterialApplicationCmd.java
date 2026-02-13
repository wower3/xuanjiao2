package com.xuanjiao.client.dto.material;

import lombok.Data;
import javax.validation.constraints.NotBlank;

import java.util.List;

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
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 是否签署版权保证声明（0-否，1-是）
     */
    private Integer guaranteeDeclaration;

    /**
     * 关联的素材列表
     */
    private List<Long> assetIds;

    /**
     * 关联的素材列表（前端兼容字段名）
     */
    private List<Long> assetListId;
}
