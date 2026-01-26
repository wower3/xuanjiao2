package com.xuanjiao.adapter.web.task;

import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.UsageApplyDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;

@Api(tags = "我的任务")
@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private MaterialApplicationService materialApplicationService;

    @Resource
    private UsageApplyService usageApplyService;

    @ApiOperation("查询草稿箱（支持按类型和标题筛选）")
    @GetMapping("/drafts")
    public Result<PageResult<Map<String, Object>>> queryDrafts(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String draftType,
            @RequestParam(required = false) String title) {

        List<Map<String, Object>> combinedList = new ArrayList<>();

        // 查询素材录入草稿
        if (draftType == null || "MATERIAL_ENTRY".equals(draftType)) {
            PageResult<MaterialApplicationDTO> materialDrafts =
                materialApplicationService.queryDrafts(userId, pageNum, pageSize, title);
            for (MaterialApplicationDTO dto : materialDrafts.getList()) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "MATERIAL_ENTRY");
                map.put("data", dto);
                combinedList.add(map);
            }
        }

        // 查询使用申请草稿
        if (draftType == null || "ASSET_USAGE".equals(draftType)) {
            PageResult<UsageApplyDTO> usageDrafts =
                usageApplyService.queryDrafts(userId, pageNum, pageSize, title);
            for (UsageApplyDTO dto : usageDrafts.getList()) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "ASSET_USAGE");
                map.put("data", dto);
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
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Map<String, Object>> pagedList =
            fromIndex < total ? combinedList.subList(fromIndex, toIndex) : new ArrayList<>();

        return Result.success(PageResult.of(pagedList, (long) total, pageNum, pageSize));
    }

    /**
     * 从DTO对象中获取创建时间
     */
    private Comparable getCreateTime(Object dto) {
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
