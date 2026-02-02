package com.xuanjiao.adapter.web.usage;

import com.xuanjiao.app.usage.UsageLogService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.UsageLogDTO;
import com.xuanjiao.client.dto.log.LogGetAssetUsageLogsQry;
import com.xuanjiao.client.dto.log.LogQueryLogsQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@Api(tags = "使用日志")
@RestController
@RequestMapping("/log")
public class UsageLogController {

    @Resource
    private UsageLogService logService;

    @ApiOperation("查询日志")
    @PostMapping("/queryLogs")
    public Result<PageResult<Map<String, Object>>> list(@Valid @RequestBody LogQueryLogsQry qry) {
        return Result.success(logService.query(qry.getAction(), qry.getPageNum(), qry.getPageSize()));
    }

    @ApiOperation("查询素材使用记录")
    @PostMapping("/getAssetUsageLogs")
    public Result<PageResult<UsageLogDTO>> getAssetUsageLogs(@Valid @RequestBody LogGetAssetUsageLogsQry qry) {
        return Result.success(logService.getAssetUsageLogs(qry.getAssetId(), qry.getPageNum(), qry.getPageSize()));
    }
}
