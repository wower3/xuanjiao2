package com.xuanjiao.client.material;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.xuanjiao.client.asset.AssetDTO;

/**
 * 素材录入申请数据传输对象
 *
 * <p>用于在前后端之间传输素材录入申请信息，包括申请基本信息、
 * 申请人、维护人、所属部门和关联的素材列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationDTO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 事项标题
     */
    private String title;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 维护人ID
     */
    private Long maintainerId;

    /**
     * 维护人姓名
     */
    private String maintainerName;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 所属部门名称
     */
    private String deptName;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 申请状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已驳回）
     */
    private String status;

    /**
     * 是否签署版权保证声明（0-否，1-是）
     */
    private Integer guaranteeDeclaration;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 关联的素材列表
     */
    private List<AssetDTO> assets;
}
