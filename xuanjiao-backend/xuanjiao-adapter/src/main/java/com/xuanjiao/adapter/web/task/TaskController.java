package com.xuanjiao.adapter.web.task;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.client.dto.task.TaskQueryDraftsQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "我的任务")
@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private MaterialApplicationService materialApplicationService;

    @Resource
    private UsageApplyService usageApplyService;

    @Resource
    private AssetDeletionApplicationService deletionApplicationService;

    @ApiOperation("查询草稿箱（支持按类型和标题筛选）")
    @PostMapping("/queryDrafts")
    public Result<PageResult<Map<String, Object>>> queryDrafts(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody TaskQueryDraftsQry qry) {

        List<Map<String, Object>> combinedList = new ArrayList<>();

        // 查询素材录入草稿
        if (qry.getDraftType() == null || "MATERIAL_ENTRY".equals(qry.getDraftType())) {
            PageResult<MaterialApplicationDTO> materialDrafts =
                materialApplicationService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize(), qry.getTitle());
            for (MaterialApplicationDTO dto : materialDrafts.getList()) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "MATERIAL_ENTRY");
                map.put("data", convertMaterialApplicationAssets(dto));
                combinedList.add(map);
            }
        }

        // 查询使用申请草稿
        if (qry.getDraftType() == null || "ASSET_USAGE".equals(qry.getDraftType())) {
            PageResult<UsageApplyDTO> usageDrafts =
                usageApplyService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize(), qry.getTitle());
            for (UsageApplyDTO dto : usageDrafts.getList()) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "ASSET_USAGE");
                map.put("data", convertUsageApplyAssets(dto));
                combinedList.add(map);
            }
        }

        // 查询素材删除草稿
        if (qry.getDraftType() == null || "ASSET_DELETION".equals(qry.getDraftType())) {
            PageResult<AssetDeletionApplicationDTO> deletionDrafts =
                deletionApplicationService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize(), qry.getTitle());
            for (AssetDeletionApplicationDTO dto : deletionDrafts.getList()) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "ASSET_DELETION");
                map.put("data", convertDeletionApplicationAssets(dto));
                combinedList.add(map);
            }
        }

        // 按创建时间倒序排序
        combinedList.sort((a, b) -> {
            Object dataA = a.get("data");
            Object dataB = b.get("data");
            Comparable timeA = getCreateTime(dataA);
            Comparable timeB = getCreateTime(dataB);
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        // 分页处理
        int total = combinedList.size();
        int fromIndex = (qry.getPageNum() - 1) * qry.getPageSize();
        int toIndex = Math.min(fromIndex + qry.getPageSize(), total);
        List<Map<String, Object>> pagedList =
            fromIndex < total ? combinedList.subList(fromIndex, toIndex) : new ArrayList<>();

        return Result.success(PageResult.of(pagedList, (long) total, qry.getPageNum(), qry.getPageSize()));
    }

    /**
     * 转换 MaterialApplicationDTO 的 assets 字段
     * AssetDTO 已有 id, name, type 字段，使用 Map 包装保持一致
     */
    private Map<String, Object> convertMaterialApplicationAssets(MaterialApplicationDTO dto) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        // 复制基本字段
        resultMap.put("id", dto.getId());
        resultMap.put("title", dto.getTitle());
        resultMap.put("applicantId", dto.getApplicantId());
        resultMap.put("applicantName", dto.getApplicantName());
        resultMap.put("maintainerId", dto.getMaintainerId());
        resultMap.put("maintainerName", dto.getMaintainerName());
        resultMap.put("deptId", dto.getDeptId());
        resultMap.put("deptName", dto.getDeptName());
        resultMap.put("workflowId", dto.getWorkflowId());
        resultMap.put("status", dto.getStatus());
        resultMap.put("guaranteeDeclaration", dto.getGuaranteeDeclaration());
        resultMap.put("createTime", dto.getCreateTime());
        resultMap.put("updateTime", dto.getUpdateTime());

        // 转换 assets 为 Map 格式，添加 id, name, type 前端兼容字段
        if (dto.getAssets() != null) {
            List<Map<String, Object>> convertedAssets = dto.getAssets().stream()
                .map(asset -> {
                    Map<String, Object> assetMap = new LinkedHashMap<>();
                    assetMap.put("id", asset.getId()); // 前端兼容字段
                    assetMap.put("name", asset.getName()); // 前端兼容字段
                    assetMap.put("type", asset.getType()); // 前端兼容字段
                    assetMap.put("filePath", asset.getFilePath());
                    assetMap.put("thumbnailPath", asset.getThumbnailPath());
                    assetMap.put("fileSize", asset.getFileSize());
                    assetMap.put("md5", asset.getMd5());
                    assetMap.put("status", asset.getStatus());
                    assetMap.put("copyright", asset.getCopyright());
                    assetMap.put("uploadUserId", asset.getUploadUserId());
                    assetMap.put("uploadUserName", asset.getUploadUserName());
                    assetMap.put("description", asset.getDescription());
                    assetMap.put("tags", asset.getTags());
                    return assetMap;
                })
                .collect(Collectors.toList());
            resultMap.put("assets", convertedAssets);
        }

        return resultMap;
    }

    /**
     * 转换 UsageApplyDTO 的 assets 字段
     * AssetUsageConfigDTO 使用 assetId, assetName, assetType
     * 转换为 Map 并添加 id, name, type 前端兼容字段
     */
    private Map<String, Object> convertUsageApplyAssets(UsageApplyDTO dto) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        // 复制基本字段
        resultMap.put("id", dto.getId());
        resultMap.put("title", dto.getTitle());
        resultMap.put("userId", dto.getUserId());
        resultMap.put("userName", dto.getUserName());
        resultMap.put("deptName", dto.getDeptName());
        resultMap.put("deptId", dto.getDeptId());
        resultMap.put("workflowId", dto.getWorkflowId());
        resultMap.put("status", dto.getStatus());
        resultMap.put("attachmentPath", dto.getAttachmentPath());
        resultMap.put("isSecondaryCreation", dto.getIsSecondaryCreation());
        resultMap.put("publishChannel", dto.getPublishChannel());
        resultMap.put("draft", dto.getDraft());
        resultMap.put("createTime", dto.getCreateTime());
        resultMap.put("updateTime", dto.getUpdateTime());

        // 转换 assets 为 Map 格式，添加 id, name, type 前端兼容字段
        if (dto.getAssets() != null) {
            List<Map<String, Object>> convertedAssets = dto.getAssets().stream()
                .map(asset -> {
                    Map<String, Object> assetMap = new LinkedHashMap<>();
                    assetMap.put("id", asset.getAssetId()); // 前端兼容字段，指向 assetId
                    assetMap.put("name", asset.getAssetName()); // 前端兼容字段，指向 assetName
                    assetMap.put("type", asset.getAssetType()); // 前端兼容字段，指向 assetType
                    assetMap.put("assetId", asset.getAssetId());
                    assetMap.put("assetName", asset.getAssetName());
                    assetMap.put("assetType", asset.getAssetType());
                    assetMap.put("assetFilePath", asset.getAssetFilePath());
                    assetMap.put("assetThumbnailPath", asset.getAssetThumbnailPath());
                    assetMap.put("assetStatus", asset.getAssetStatus());
                    assetMap.put("usageDescription", asset.getUsageDescription());
                    assetMap.put("usagePublishChannel", asset.getUsagePublishChannel());
                    assetMap.put("usageIsSecondaryCreation", asset.getUsageIsSecondaryCreation());
                    assetMap.put("usageAttachmentPath", asset.getUsageAttachmentPath());
                    return assetMap;
                })
                .collect(Collectors.toList());
            resultMap.put("assets", convertedAssets);
        }

        return resultMap;
    }

    /**
     * 转换 AssetDeletionApplicationDTO 的 assets 字段
     * AssetDeletionAssetDTO 使用 assetId, assetName, assetType
     * 转换为 Map 并添加 id, name, type 前端兼容字段
     */
    private Map<String, Object> convertDeletionApplicationAssets(AssetDeletionApplicationDTO dto) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        // 复制基本字段
        resultMap.put("id", dto.getId());
        resultMap.put("title", dto.getTitle());
        resultMap.put("applicantId", dto.getApplicantId());
        resultMap.put("applicantName", dto.getApplicantName());
        resultMap.put("deptId", dto.getDeptId());
        resultMap.put("deptName", dto.getDeptName());
        resultMap.put("workflowId", dto.getWorkflowId());
        resultMap.put("status", dto.getStatus());
        resultMap.put("deleteReason", dto.getDeleteReason());
        resultMap.put("attachmentPath", dto.getAttachmentPath());
        resultMap.put("createTime", dto.getCreateTime());
        resultMap.put("updateTime", dto.getUpdateTime());

        // 转换 assets 为 Map 格式，添加 id, name, type 前端兼容字段
        if (dto.getAssets() != null) {
            List<Map<String, Object>> convertedAssets = dto.getAssets().stream()
                .map(asset -> {
                    Map<String, Object> assetMap = new LinkedHashMap<>();
                    assetMap.put("id", asset.getAssetId()); // 前端兼容字段，指向 assetId
                    assetMap.put("name", asset.getAssetName()); // 前端兼容字段，指向 assetName
                    assetMap.put("type", asset.getAssetType()); // 前端兼容字段，指向 assetType
                    assetMap.put("assetId", asset.getAssetId());
                    assetMap.put("assetName", asset.getAssetName());
                    assetMap.put("assetType", asset.getAssetType());
                    assetMap.put("filePath", asset.getFilePath());
                    assetMap.put("thumbnailPath", asset.getThumbnailPath());
                    assetMap.put("deletionApplicationId", asset.getDeletionApplicationId());
                    return assetMap;
                })
                .collect(Collectors.toList());
            resultMap.put("assets", convertedAssets);
        }

        return resultMap;
    }

    /**
     * 从DTO对象中获取创建时间
     */
    private Comparable getCreateTime(Object dto) {
        if (dto instanceof Map) {
            Object time = ((Map<?, ?>) dto).get("createTime");
            if (time instanceof Comparable) {
                return (Comparable) time;
            }
            return null;
        }
        try {
            Method method = dto.getClass().getMethod("getCreateTime");
            Object result = method.invoke(dto);
            if (result instanceof Comparable) {
                return (Comparable) result;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
