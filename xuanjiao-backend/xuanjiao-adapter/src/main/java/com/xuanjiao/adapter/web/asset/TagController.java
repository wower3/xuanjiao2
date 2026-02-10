package com.xuanjiao.adapter.web.asset;

import com.xuanjiao.app.asset.TagService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.TagDTO;
import com.xuanjiao.client.dto.asset.TagDeleteCmd;
import com.xuanjiao.client.dto.asset.TagGetListByCategoryQry;
import com.xuanjiao.client.dto.asset.TagGetListQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 标签管理控制器
 *
 * <p>提供素材标签的增删改查功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建标签：创建新的素材标签</li>
 *   <li>标签列表：查询所有标签</li>
 *   <li>按分类查询：按标签分类查询标签列表</li>
 *   <li>删除标签：删除指定标签</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "标签管理")
@RestController
@RequestMapping("/tag")
public class TagController {

    /**
     * 标签服务
     *
     * <p>处理标签的增删改查业务逻辑。</p>
     */
    @Resource
    private TagService tagService;

    /**
     * 创建标签
     *
     * <p>创建新的素材标签，标签可以按分类进行组织。
     * 标签用于对素材进行分类和标记。</p>
     *
     * @param name 标签名称
     * @param category 标签分类（可选）
     * @return 创建后的标签信息
     */
    @ApiOperation("创建标签")
    @PostMapping("/create")
    public Result<TagDTO> create(
            @RequestParam String name,
            @RequestParam(required = false) String category) {
        return Result.success(tagService.create(name, category));
    }

    /**
     * 获取所有标签
     *
     * <p>查询系统中所有的标签列表，不分分类。</p>
     *
     * @param qry 查询条件（当前无过滤参数）
     * @return 标签列表
     */
    @ApiOperation("获取所有标签")
    @PostMapping("/getList")
    public Result<List<TagDTO>> list(@Valid @RequestBody TagGetListQry qry) {
        return Result.success(tagService.list());
    }

    /**
     * 根据分类获取标签
     *
     * <p>查询指定分类下的所有标签。
     * 分类用于对标签进行分组管理。</p>
     *
     * @param qry 查询条件，包含分类名称
     * @return 指定分类下的标签列表
     */
    @ApiOperation("根据分类获取标签")
    @PostMapping("/getListByCategory")
    public Result<List<TagDTO>> listByCategory(@Valid @RequestBody TagGetListByCategoryQry qry) {
        return Result.success(tagService.listByCategory(qry.getCategory()));
    }

    /**
     * 删除标签
     *
     * <p>删除指定的标签。删除后，与该标签关联的素材将不再显示此标签。</p>
     *
     * @param cmd 删除命令，包含要删除的标签ID
     * @return 操作结果
     */
    @ApiOperation("删除标签")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody TagDeleteCmd cmd) {
        tagService.delete(cmd.getId());
        return Result.success();
    }
}
