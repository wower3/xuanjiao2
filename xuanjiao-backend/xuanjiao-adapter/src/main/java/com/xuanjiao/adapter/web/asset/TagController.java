package com.xuanjiao.adapter.web.asset;

import com.xuanjiao.app.asset.TagService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.TagDTO;
import com.xuanjiao.client.dto.asset.TagDeleteCmd;
import com.xuanjiao.client.dto.asset.TagGetListByCategoryQry;
import com.xuanjiao.client.dto.asset.TagGetListQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "标签管理")
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @ApiOperation("创建标签")
    @PostMapping("/create")
    public Result<TagDTO> create(
            @RequestParam String name,
            @RequestParam(required = false) String category) {
        return Result.success(tagService.create(name, category));
    }

    @ApiOperation("获取所有标签")
    @PostMapping("/getList")
    public Result<List<TagDTO>> list(@Valid @RequestBody TagGetListQry qry) {
        return Result.success(tagService.list());
    }

    @ApiOperation("根据分类获取标签")
    @PostMapping("/getListByCategory")
    public Result<List<TagDTO>> listByCategory(@Valid @RequestBody TagGetListByCategoryQry qry) {
        return Result.success(tagService.listByCategory(qry.getCategory()));
    }

    @ApiOperation("删除标签")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody TagDeleteCmd cmd) {
        tagService.delete(cmd.getId());
        return Result.success();
    }
}
