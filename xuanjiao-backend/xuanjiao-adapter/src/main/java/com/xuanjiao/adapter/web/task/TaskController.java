package com.xuanjiao.adapter.web.task;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.deletion.AssetDeletionApplicationDTO;
import com.xuanjiao.client.material.MaterialApplicationDTO;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.Result;
import com.xuanjiao.client.usage.UsageApplyDTO;
import com.xuanjiao.client.task.TaskQueryDraftsQry;
import com.xuanjiao.client.task.DraftItemDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
                item.setData(dto);
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
                item.setData(dto);
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
                item.setData(dto);
                combinedList.add(item);
            }
        }

        // 按创建时间倒序排序
        combinedList.sort(Comparator.comparing(
            item -> getCreateTime(item.getData()),
            Comparator.nullsLast(Comparator.reverseOrder())
        ));

        // 分页处理
        int total = combinedList.size();
        int fromIndex = (qry.getPageNum() - 1) * qry.getPageSize();
        int toIndex = Math.min(fromIndex + qry.getPageSize(), total);
        List<DraftItemDTO> pagedList =
            fromIndex < total ? combinedList.subList(fromIndex, toIndex) : new ArrayList<>();

        return Result.success(PageResult.of(pagedList, (long) total, qry.getPageNum(), qry.getPageSize()));
    }

    /**
     * 从DTO对象中获取创建时间
     *
     * <p>通过反射获取对象的 createTime 属性值。</p>
     *
     * @param dto DTO对象
     * @return 创建时间值，如果获取失败返回 null
     */
    private Comparable getCreateTime(Object dto) {
        try {
            java.lang.reflect.Method method = dto.getClass().getMethod("getCreateTime");
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
