package com.xuanjiao.client.deletion;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新删除申请命令对象
 *
 * <p>封装更新素材删除申请所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeletionUpdateCmd {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;

    /**
     * 申请标题
     */
    private String title;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 删除原因
     */
    private String deleteReason;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 待删除的素材ID列表
     */
    private List<Long> assetIds;
}
