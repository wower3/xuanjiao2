package com.xuanjiao.client.dto.deletion;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新删除申请命令对象
 */
@Data
public class DeletionUpdateCmd {

    @NotNull(message = "申请单ID不能为空")
    private Long id;

    private String title;

    private Long workflowId;

    private String deleteReason;

    private String attachmentPath;

    private List<Long> assetIds;
}
