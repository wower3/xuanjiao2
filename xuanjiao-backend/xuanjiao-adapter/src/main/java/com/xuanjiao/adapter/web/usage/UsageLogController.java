package com.xuanjiao.adapter.web.usage;

import com.xuanjiao.app.usage.UsageLogService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.UsageLogDTO;
import com.xuanjiao.client.dto.log.LogGetAssetUsageLogsQry;
import com.xuanjiao.client.dto.log.LogQueryLogsQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * 使用日志控制器
 *
 * <p>提供素材使用日志的查询功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>日志查询：分页查询素材使用日志</li>
 *   <li>素材使用日志：查询特定素材的使用记录</li>
 *   <li>日志详情：查询单条日志的详细信息</li>
 *   <li>按时间范围查询：支持按起止时间筛选日志</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "使用日志")
@RestController
@RequestMapping("/log")
public class UsageLogController {

    /**
     * 使用日志服务
     *
     * <p>处理素材使用日志的查询和记录业务逻辑。</p>
     */
    @Resource
    private UsageLogService logService;

    /**
     * 查询日志
     *
     * <p>分页查询素材使用日志，支持按操作类型筛选。
     * 操作类型包括：下载、查看等。</p>
     *
     * @param qry 查询条件，包含分页参数和操作类型筛选条件
     * @return 分页的使用日志列表
     */
    @ApiOperation("查询日志")
    @PostMapping("/queryLogs")
    public Result<PageResult<Map<String, Object>>> list(@Valid @RequestBody LogQueryLogsQry qry) {
        return Result.success(logService.query(qry.getAction(), qry.getPageNum(), qry.getPageSize()));
    }

    /**
     * 查询素材使用记录
     *
     * <p>查询指定素材的所有使用记录，包括使用者、使用时间、使用描述、
     * 发布渠道等信息。用于追踪素材的使用情况。</p>
     *
     * @param qry 查询条件，包含素材ID和分页参数
     * @return 分页的使用记录列表
     */
    @ApiOperation("查询素材使用记录")
    @PostMapping("/getAssetUsageLogs")
    public Result<PageResult<UsageLogDTO>> getAssetUsageLogs(@Valid @RequestBody LogGetAssetUsageLogsQry qry) {
        return Result.success(logService.getAssetUsageLogs(qry.getAssetId(), qry.getPageNum(), qry.getPageSize()));
    }
}
