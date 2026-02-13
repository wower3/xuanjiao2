package com.xuanjiao.adapter.web.material;

import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.client.dto.material.MaterialApplicationCmd;
import com.xuanjiao.client.dto.material.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.common.Result;
import com.xuanjiao.client.dto.common.PageResult;
import com.xuanjiao.client.dto.material.MaterialApplicationDeleteCmd;
import com.xuanjiao.client.dto.material.MaterialApplicationGetDetailQry;
import com.xuanjiao.client.dto.material.MaterialApplicationGetDraftsQry;
import com.xuanjiao.client.dto.material.MaterialApplicationGetMyApplicationsQry;
import com.xuanjiao.client.dto.material.MaterialApplicationSubmitCmd;
import com.xuanjiao.client.dto.material.MaterialApplicationUpdateCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 素材录入申请控制器
 *
 * <p>提供素材录入申请的创建、修改、提交审批等功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建申请单：创建新的素材录入申请（草稿状态）</li>
 *   <li>更新申请单：修改申请单信息（仅草稿状态可修改）</li>
 *   <li>提交审批：将申请单提交审批流程</li>
 *   <li>查询详情：查询单个申请单的详细信息</li>
 *   <li>查询列表：分页查询申请单列表</li>
 *   <li>查询草稿：分页查询当前用户保存的草稿申请单</li>
 *   <li>查询我的申请：分页查询当前用户发起的申请单</li>
 *   <li>复制申请单：复制指定申请单创建一个新的草稿申请单</li>
 *   <li>删除申请单：删除指定的草稿申请单</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "素材申请单管理")
@RestController
@RequestMapping("/material-application")
public class MaterialApplicationController {
    /**
     * 素材录入申请服务
     *
     * <p>处理素材录入申请的业务逻辑。</p>
     */
    @Resource
    private MaterialApplicationService materialApplicationService;

    /**
     * 创建申请单（草稿）
     *
     * <p>创建一个新的素材录入申请单，初始状态为DRAFT。</p>
     *
     * @param cmd 申请命令，包含申请单标题，维护人、部门、保障声明等信息
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 创建后的申请单信息
     */
    @ApiOperation("创建申请单（草稿）")
    @PostMapping("/create")
    public Result<MaterialApplicationDTO> create(
            @Valid @RequestBody MaterialApplicationCmd cmd,
            @RequestAttribute("userId") Long userId) {
        MaterialApplicationDTO result = materialApplicationService.create(cmd, userId);
        return Result.success(result);
    }

    /**
     * 更新申请单
     *
     * <p>更新申请单信息，仅草稿状态的申请单可以修改。</p>
     *
     * @param id 申请单ID
     * @param cmd 申请命令，包含要更新的字段
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 更新后的申请单信息
     */
    @ApiOperation("更新申请单")
    @PostMapping("/update")
    public Result<MaterialApplicationDTO> update(
            @Valid @RequestBody MaterialApplicationUpdateCmd cmd,
            @RequestAttribute("userId") Long userId) {
        // Convert to MaterialApplicationCmd
        MaterialApplicationCmd applicationCmd = new MaterialApplicationCmd();
        applicationCmd.setTitle(cmd.getTitle());
        applicationCmd.setMaintainerId(cmd.getMaintainerId());
        applicationCmd.setDeptId(cmd.getDeptId());
        applicationCmd.setGuaranteeDeclaration(cmd.getGuaranteeDeclaration());
        return Result.success(materialApplicationService.update(cmd.getId(), applicationCmd, userId));
    }

    /**
     * 提交审批
     *
     * <p>将草稿状态的申请单提交审批流程。</p>
     *
     * @param cmd 提交命令，包含申请单ID和审批流程ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 提交结果
     */
    @ApiOperation("提交申请单")
    @PostMapping("/submit")
    public Result<Void> submit(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody MaterialApplicationSubmitCmd cmd) {
        materialApplicationService.submit(cmd.getId(), cmd.getWorkflowId(), userId);
        return Result.success();
    }

    /**
     * 查询详情
     *
     * <p>查询单个申请单的详细信息。</p>
     *
     * @param qry 查询条件，包含申请单ID
     * @return 申请单详细信息
     */
    @ApiOperation("查询申请单详情")
    @PostMapping("/getDetail")
    public Result<MaterialApplicationDTO> getDetail(@Valid @RequestBody MaterialApplicationGetDetailQry qry) {
        return Result.success(materialApplicationService.getById(qry.getId()));
    }

    /**
     * 查询草稿
     *
     * <p>分页查询当前用户保存的草稿申请单。</p>
     *
     * @param qry 查询条件，包含分页参数
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 分页的草稿申请单列表
     */
    @ApiOperation("查询草稿箱")
    @PostMapping("/getDrafts")
    public Result<PageResult<MaterialApplicationDTO>> queryDrafts(
            @Valid @RequestBody MaterialApplicationGetDraftsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(materialApplicationService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize()));
    }

    /**
     * 查询我的申请
     *
     * <p>分页查询当前用户发起的所有申请单。</p>
     *
     * @param qry 查询条件，包含分页参数
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 分页的申请单列表
     */
    @ApiOperation("查询我的申请单")
    @PostMapping("/getMyApplications")
    public Result<PageResult<MaterialApplicationDTO>> queryMyApplications(
            @Valid @RequestBody MaterialApplicationGetMyApplicationsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(materialApplicationService.queryMyApplications(userId, qry.getPageNum(), qry.getPageSize()));
    }

    /**
     * 复制申请单
     *
     * <p>复制指定申请单创建一个新的草稿申请单。</p>
     *
     * @param id 要复制的申请单ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 新创建的申请单ID
     */
    @ApiOperation("复制申请单")
    @PostMapping("/{id}/copy")
    public Result<Long> copy(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        Long newApplicationId = materialApplicationService.copyApplication(id, userId);
        return Result.success(newApplicationId);
    }

    /**
     * 删除申请单
     *
     * <p>删除指定的草稿申请单，仅草稿状态的申请单可以删除。</p>
     *
     * @param cmd 删除命令，包含要删除的申请单ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 操作结果
     */
    @ApiOperation("删除申请单")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Valid @RequestBody MaterialApplicationDeleteCmd cmd,
            @RequestAttribute("userId") Long userId) {
        materialApplicationService.delete(cmd.getId(), userId);
        return Result.success();
    }
}
