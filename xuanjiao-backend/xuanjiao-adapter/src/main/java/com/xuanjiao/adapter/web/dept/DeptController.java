package com.xuanjiao.adapter.web.dept;

import com.xuanjiao.app.dept.DeptService;
import com.xuanjiao.client.dto.dept.dto.DeptDTO;
import com.xuanjiao.client.dto.common.Result;
import com.xuanjiao.client.dto.dept.DeptDeleteCmd;
import com.xuanjiao.client.dto.dept.DeptGetDetailQry;
import com.xuanjiao.client.dto.dept.DeptGetListQry;
import com.xuanjiao.client.dto.dept.DeptGetTreeQry;
import com.xuanjiao.client.dto.dept.DeptUpdateCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 部门管理控制器
 *
 * <p>提供部门的增删改查功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>部门列表：查询部门树形结构</li>
 *   <li>部门详情：查询单个部门的详细信息</li>
 *   <li>新增部门：创建新的部门</li>
 *   <li>更新部门：修改部门信息</li>
 *   <li>删除部门：删除指定部门</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "部门管理")
@RestController
@RequestMapping("/dept")
public class DeptController {

    /**
     * 部门服务
     *
     * <p>处理部门的增删改查业务逻辑。</p>
     */
    @Resource
    private DeptService deptService;

    /**
     * 查询部门列表
     *
     * <p>查询系统中所有部门的列表信息，不包含层级关系。</p>
     *
     * @param qry 查询条件（当前无过滤参数）
     * @return 部门列表
     */
    @ApiOperation("部门列表")
    @PostMapping("/getList")
    public Result<List<DeptDTO>> list(@Valid @RequestBody DeptGetListQry qry) {
        return Result.success(deptService.list());
    }

    /**
     * 查询部门树
     *
     * <p>查询部门树形结构，以父子关系组织部门数据，
     * 用于前端展示部门树选择器。</p>
     *
     * @param qry 查询条件（当前无过滤参数）
     * @return 部门树形结构
     */
    @ApiOperation("部门树")
    @PostMapping("/getTree")
    public Result<List<DeptDTO>> tree(@Valid @RequestBody DeptGetTreeQry qry) {
        return Result.success(deptService.getTree());
    }

    /**
     * 获取部门详情
     *
     * <p>根据部门ID查询部门的详细信息，包括部门名称、编码、上级部门等。</p>
     *
     * @param qry 查询条件，包含部门ID
     * @return 部门详情信息
     */
    @ApiOperation("获取部门详情")
    @PostMapping("/getDetail")
    public Result<DeptDTO> getById(@Valid @RequestBody DeptGetDetailQry qry) {
        return Result.success(deptService.getById(qry.getId()));
    }

    /**
     * 保存部门
     *
     * <p>创建新的部门，需要提供部门名称、编码、上级部门等信息。</p>
     *
     * @param dto 部门信息
     * @return 操作结果
     */
    @ApiOperation("保存部门")
    @PostMapping("/create")
    public Result<Void> save(@RequestBody DeptDTO dto) {
        deptService.save(dto);
        return Result.success();
    }

    /**
     * 更新部门
     *
     * <p>修改指定部门的信息，包括部门名称、编码、上级部门、排序等。</p>
     *
     * @param cmd 更新命令，包含部门ID和要更新的字段
     * @return 操作结果
     */
    @ApiOperation("更新部门")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody DeptUpdateCmd cmd) {
        deptService.update(convertToDto(cmd));
        return Result.success();
    }

    /**
     * 删除部门
     *
     * <p>删除指定的部门。如果部门下有子部门或用户，则需要先处理关联数据。</p>
     *
     * @param cmd 删除命令，包含要删除的部门ID
     * @return 操作结果
     */
    @ApiOperation("删除部门")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody DeptDeleteCmd cmd) {
        deptService.delete(cmd.getId());
        return Result.success();
    }

    /**
     * 生成部门编号
     *
     * <p>自动生成唯一的部门编号，格式为系统的编号规则。</p>
     *
     * @return 生成的部门编号
     */
    @ApiOperation("生成部门编号")
    @GetMapping("/generate-code")
    public Result<String> generateCode() {
        return Result.success(deptService.generateCode());
    }

    /**
     * 将更新命令转换为DTO对象
     *
     * <p>将 DeptUpdateCmd 转换为 DeptDTO，用于服务层处理。</p>
     *
     * @param cmd 更新命令
     * @return 部门DTO对象
     */
    private DeptDTO convertToDto(DeptUpdateCmd cmd) {
        DeptDTO dto = new DeptDTO();
        dto.setId(cmd.getId());
        dto.setName(cmd.getName());
        dto.setCode(cmd.getCode());
        dto.setParentId(cmd.getParentId());
        dto.setSort(cmd.getSort());
        return dto;
    }
}
