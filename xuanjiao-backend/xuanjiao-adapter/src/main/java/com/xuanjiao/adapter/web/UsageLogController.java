package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.UsageLogService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "使用日志")
@RestController
@RequestMapping("/log")
public class UsageLogController {

    @Resource
    private UsageLogService logService;

    @ApiOperation("查询日志")
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(logService.query(action, pageNum, pageSize));
    }
}
