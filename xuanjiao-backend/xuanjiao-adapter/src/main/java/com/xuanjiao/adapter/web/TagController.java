package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.TagService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.TagDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@Api(tags = "标签管理")
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @ApiOperation("创建标签")
    @PostMapping
    public Result<TagDTO> create(
            @RequestParam String name,
            @RequestParam(required = false) String category) {
        return Result.success(tagService.create(name, category));
    }

    @ApiOperation("获取所有标签")
    @GetMapping("/list")
    public Result<List<TagDTO>> list() {
        return Result.success(tagService.list());
    }

    @ApiOperation("根据分类获取标签")
    @GetMapping("/list/{category}")
    public Result<List<TagDTO>> listByCategory(@PathVariable String category) {
        return Result.success(tagService.listByCategory(category));
    }

    @ApiOperation("删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
