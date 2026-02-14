package com.xuanjiao.adapter.web.task;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.AssetDeletionApplicationDTO;
import com.xuanjiao.client.MaterialApplicationDTO;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.Result;
import com.xuanjiao.client.UsageApplyDTO;
import com.xuanjiao.client.task.TaskQueryDraftsQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 转换素材录入申请的素材字段
     *
     * <p>将 MaterialApplicationDTO 转换为 Map 格式，确保 assets 字段包含
     * id, name, type 等前端兼容字段。</p>
     *
     * @param dto 素材录入申请DTO
     * @return 转换后的Map对象
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
     * 转换素材使用申请的素材字段
     *
     * <p>将 UsageApplyDTO 转换为 Map 格式，确保 assets 字段包含
     * id, name, type 等前端兼容字段。</p>
     *
     * @param dto 素材使用申请DTO
     * @return 转换后的Map对象
     */
    private Map<String, Object> convertUsageApplyAssets(UsageApplyDTO dto) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        // 复制基本字段
        resultMap.put("id", dto.getId());
        resultMap.put("title", dto.getTitle());
        resultMap.put("userId", dto.getUserId());
        resultMap.put("username", dto.getUsername());
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
     * 转换素材删除申请的素材字段
     *
     * <p>将 AssetDeletionApplicationDTO 转换为 Map 格式，确保 assets 字段包含
     * id, name, type 等前端兼容字段。</p>
     *
     * @param dto 素材删除申请DTO
     * @return 转换后的Map对象
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
     *
     * <p>通过反射获取对象的 createTime 属性值，支持 Map 和普通 Java 对象。</p>
     *
     * @param dto DTO对象
     * @return 创建时间值，如果获取失败返回 null
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
