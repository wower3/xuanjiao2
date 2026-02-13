package com.xuanjiao.adapter.web.task;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.dto.deletion.dto.AssetDeletionApplicationDTO;
import com.xuanjiao.client.dto.material.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.common.PageResult;
import com.xuanjiao.client.dto.common.Result;
import com.xuanjiao.client.dto.usage.dto.UsageApplyDTO;
import com.xuanjiao.client.dto.task.dto.DraftItemDTO;
import com.xuanjiao.client.dto.task.TaskQueryDraftsQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的任务控制器
 *
 * <p>提供当前用户发起的各类申请任务的查询和管理功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>草稿箱：查询保存草稿的素材录入/使用/删除申请</li>
 *   <li>我发起的：查询当前用户发起的所有申请</li>
 *   <li>支持按业务类型、状态筛选</li>
 *   <li>支持查看申请详情和审批进度</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "我的任务")
@RestController
@RequestMapping("/task")
public class TaskController {

    /**
     * 素材录入申请服务
     *
     * <p>处理素材录入申请的查询和管理。</p>
     */
    @Resource
    private MaterialApplicationService materialApplicationService;

    /**
     * 素材使用申请服务
     *
     * <p>处理素材使用申请的查询和管理。</p>
     */
    @Resource
    private UsageApplyService usageApplyService;

    /**
     * 素材删除申请服务
     *
     * <p>处理素材删除申请的查询和管理。</p>
     */
    @Resource
    private AssetDeletionApplicationService deletionApplicationService;

    /**
     * 查询草稿箱（支持按类型和标题筛选）
     *
     * <p>查询当前用户保存的草稿申请，包括素材录入、素材使用、素材删除三种类型。
     * 支持按草稿类型和标题进行筛选，结果按创建时间倒序排列。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件，包含分页参数、草稿类型和标题筛选条件
     * @return 分页的草稿列表，每条记录包含类型标识和业务数据
     */
    @ApiOperation("查询草稿箱（支持按类型和标题筛选）")
    @PostMapping("/queryDrafts")
    public Result<PageResult<DraftItemDTO>> queryDrafts(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody TaskQueryDraftsQry qry) {

        List<DraftItemDTO> combinedList = new ArrayList<>();

        // 查询素材录入草稿
        if (qry.getDraftType() == null || "MATERIAL_ENTRY".equals(qry.getDraftType())) {
            PageResult<MaterialApplicationDTO> materialDrafts =
                materialApplicationService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize(), qry.getTitle());
            for (MaterialApplicationDTO dto : materialDrafts.getList()) {
                DraftItemDTO item = new DraftItemDTO();
                item.setType("MATERIAL_ENTRY");
                item.setId(dto.getId());
                item.setTitle(dto.getTitle());
                item.setCreateTime(dto.getCreateTime());
                item.setUpdateTime(dto.getUpdateTime());

                DraftItemDTO.MaterialEntryDraftData data = new DraftItemDTO.MaterialEntryDraftData();
                data.setApplicantId(dto.getApplicantId());
                data.setApplicantName(dto.getApplicantName());
                data.setMaintainerId(dto.getMaintainerId());
                data.setMaintainerName(dto.getMaintainerName());
                data.setDeptId(dto.getDeptId());
                data.setDeptName(dto.getDeptName());
                data.setWorkflowId(dto.getWorkflowId());
                data.setStatus(dto.getStatus());
                data.setGuaranteeDeclaration(dto.getGuaranteeDeclaration());
                data.setAssets(dto.getAssets()); // 直接使用 DTO，避免循环依赖
                item.setMaterialEntry(data);

                combinedList.add(item);
            }
        }

        // 查询使用申请草稿
        if (qry.getDraftType() == null || "ASSET_USAGE".equals(qry.getDraftType())) {
            PageResult<UsageApplyDTO> usageDrafts =
                usageApplyService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize(), qry.getTitle());
            for (UsageApplyDTO dto : usageDrafts.getList()) {
                DraftItemDTO item = new DraftItemDTO();
                item.setType("ASSET_USAGE");
                item.setId(dto.getId());
                item.setTitle(dto.getTitle());
                item.setCreateTime(dto.getCreateTime());
                item.setUpdateTime(dto.getUpdateTime());

                DraftItemDTO.UsageDraftData data = new DraftItemDTO.UsageDraftData();
                data.setUserId(dto.getUserId());
                data.setUsername(dto.getUsername());
                data.setDeptId(dto.getDeptId());
                data.setDeptName(dto.getDeptName());
                data.setWorkflowId(dto.getWorkflowId());
                data.setStatus(dto.getStatus());
                data.setAttachmentPath(dto.getAttachmentPath());
                data.setIsSecondaryCreation(dto.getIsSecondaryCreation());
                data.setPublishChannel(dto.getPublishChannel());
                data.setDraft(dto.getDraft());
                data.setAssets(dto.getAssets()); // 直接使用 DTO，避免循环依赖
                item.setAssetUsage(data);

                combinedList.add(item);
            }
        }

        // 查询素材删除草稿
        if (qry.getDraftType() == null || "ASSET_DELETION".equals(qry.getDraftType())) {
            PageResult<AssetDeletionApplicationDTO> deletionDrafts =
                deletionApplicationService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize(), qry.getTitle());
            for (AssetDeletionApplicationDTO dto : deletionDrafts.getList()) {
                DraftItemDTO item = new DraftItemDTO();
                item.setType("ASSET_DELETION");
                item.setId(dto.getId());
                item.setTitle(dto.getTitle());
                item.setCreateTime(dto.getCreateTime());
                item.setUpdateTime(dto.getUpdateTime());

                DraftItemDTO.DeletionDraftData data = new DraftItemDTO.DeletionDraftData();
                data.setApplicantId(dto.getApplicantId());
                data.setApplicantName(dto.getApplicantName());
                data.setDeptId(dto.getDeptId());
                data.setDeptName(dto.getDeptName());
                data.setWorkflowId(dto.getWorkflowId());
                data.setStatus(dto.getStatus());
                data.setDeleteReason(dto.getDeleteReason());
                data.setAttachmentPath(dto.getAttachmentPath());
                data.setAssets(dto.getAssets()); // 直接使用 DTO，避免循环依赖
                item.setAssetDeletion(data);

                combinedList.add(item);
            }
        }

        // 按创建时间倒序排序
        combinedList.sort((a, b) -> {
            LocalDateTime timeA = a.getCreateTime();
            LocalDateTime timeB = b.getCreateTime();
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        // 分页处理
        int total = combinedList.size();
        int fromIndex = (qry.getPageNum() - 1) * qry.getPageSize();
        int toIndex = Math.min(fromIndex + qry.getPageSize(), total);
        List<DraftItemDTO> pagedList =
            fromIndex < total ? combinedList.subList(fromIndex, toIndex) : new ArrayList<>();

        return Result.success(PageResult.of(pagedList, (long) total, qry.getPageNum(), qry.getPageSize()));
    }
}
