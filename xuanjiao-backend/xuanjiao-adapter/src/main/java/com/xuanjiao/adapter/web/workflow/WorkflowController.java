package com.xuanjiao.adapter.web.workflow;

import com.xuanjiao.app.workflow.WorkflowService;
import com.xuanjiao.client.dto.common.Result;
import com.xuanjiao.client.dto.workflow.dto.WorkflowDTO;
import com.xuanjiao.client.dto.workflow.dto.FirstStageApproversDTO;
import com.xuanjiao.client.dto.workflow.WorkflowBindRoleCmd;
import com.xuanjiao.client.dto.workflow.WorkflowDeleteCmd;
import com.xuanjiao.client.dto.workflow.WorkflowGetDetailQry;
import com.xuanjiao.client.dto.workflow.WorkflowGetListQry;
import com.xuanjiao.client.dto.workflow.WorkflowUnbindRoleCmd;
import com.xuanjiao.client.dto.workflow.WorkflowUpdateCmd;
import com.xuanjiao.client.dto.workflow.WorkflowUpdateStatusCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 流程管理控制器
 *
 * <p>提供审批流程定义的增删改查功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>流程列表：查询所有审批流程</li>
 *   <li>流程详情：查询单个流程的详细信息</li>
 *   <li>新增流程：创建新的审批流程</li>
 *   <li>更新流程：修改流程配置（阶段、审批人等）</li>
 *   <li>删除流程：删除指定流程</li>
 *   <li>流程类型：按业务类型查询流程</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "流程管理")
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    /**
     * 工作流服务
     *
     * <p>处理工作流的增删改查和状态管理业务逻辑。</p>
     */
    @Resource
    private WorkflowService workflowService;

    /**
     * 查询流程列表
     *
     * <p>查询系统中所有审批流程的列表信息。</p>
     *
     * @param qry 查询条件（当前无过滤参数）
     * @return 流程列表
     */
    @ApiOperation("流程列表")
    @PostMapping("/getList")
    public Result<List<WorkflowDTO>> list(@Valid @RequestBody WorkflowGetListQry qry) {
        return Result.success(workflowService.list());
    }

    /**
     * 获取流程详情
     *
     * <p>根据流程ID查询流程的详细信息，包括流程名称、描述、类型、阶段配置、审批人等。</p>
     *
     * @param qry 查询条件，包含流程ID
     * @return 流程详情信息
     */
    @ApiOperation("流程详情")
    @PostMapping("/getDetail")
    public Result<WorkflowDTO> getById(@Valid @RequestBody WorkflowGetDetailQry qry) {
        return Result.success(workflowService.getById(qry.getId()));
    }

    /**
     * 保存流程
     *
     * <p>创建新的审批流程，需要提供流程名称、描述、类型、阶段配置等信息。</p>
     *
     * @param dto 流程信息
     * @return 保存后的流程信息（包含生成的ID）
     */
    @ApiOperation("保存流程")
    @PostMapping("/create")
    public Result<WorkflowDTO> save(@RequestBody WorkflowDTO dto) {
        WorkflowDTO savedWorkflow = workflowService.save(dto);
        return Result.success(savedWorkflow);
    }

    /**
     * 更新流程
     *
     * <p>修改指定流程的配置信息，包括流程名称、描述、类型、阶段配置等。</p>
     *
     * @param cmd 更新命令，包含流程ID和要更新的字段
     * @return 操作结果
     */
    @ApiOperation("更新流程")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody WorkflowUpdateCmd cmd) {
        workflowService.update(convertToDto(cmd));
        return Result.success();
    }

    /**
     * 更新流程状态
     *
     * <p>启用或禁用指定流程。禁用的流程不能用于新建审批申请。</p>
     *
     * @param cmd 更新命令，包含流程ID和目标状态
     * @return 操作结果
     */
    @ApiOperation("更新状态")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@Valid @RequestBody WorkflowUpdateStatusCmd cmd) {
        workflowService.updateStatus(cmd.getId(), cmd.getStatus());
        return Result.success();
    }

    /**
     * 删除流程
     *
     * <p>删除指定的流程定义。如果流程正在被使用中，则需要先处理关联数据。</p>
     *
     * @param cmd 删除命令，包含要删除的流程ID
     * @return 操作结果
     */
    @ApiOperation("删除流程")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody WorkflowDeleteCmd cmd) {
        workflowService.delete(cmd.getId());
        return Result.success();
    }

    /**
     * 绑定角色
     *
     * <p>将流程绑定到指定角色，绑定后该角色的用户可以使用此流程发起申请。
     * 流程类型包括：素材录入(ASSET_UPLOAD)、素材使用(ASSET_USAGE)、素材删除(ASSET_DELETION)。</p>
     *
     * @param cmd 绑定命令，包含流程ID、角色ID和流程类型
     * @return 操作结果
     */
    @ApiOperation("绑定角色")
    @PostMapping("/bindRole")
    public Result<Void> bindRole(@Valid @RequestBody WorkflowBindRoleCmd cmd) {
        workflowService.bindRole(cmd.getId(), cmd.getRoleId(), cmd.getWorkflowType());
        return Result.success();
    }

    /**
     * 解除角色绑定
     *
     * <p>解除流程与角色的绑定关系，解除后该角色的用户不能再使用此流程发起申请。</p>
     *
     * @param cmd 解绑命令，包含流程ID
     * @return 操作结果
     */
    @ApiOperation("解除角色绑定")
    @PostMapping("/unbindRole")
    public Result<Void> unbindRole(@Valid @RequestBody WorkflowUnbindRoleCmd cmd) {
        workflowService.unbindRole(cmd.getId());
        return Result.success();
    }

    /**
     * 复制流程
     *
     * <p>复制指定流程创建一个新的流程副本，包括所有阶段和审批人配置。
     * 新流程的名称会添加"副本"后缀。</p>
     *
     * @param id 要复制的流程ID
     * @return 新创建的流程信息
     */
    @ApiOperation("复制流程")
    @PostMapping("/{id}/copy")
    public Result<WorkflowDTO> copy(@PathVariable Long id) {
        return Result.success(workflowService.copy(id));
    }

    /**
     * 将更新命令转换为DTO对象
     *
     * <p>将 WorkflowUpdateCmd 转换为 WorkflowDTO，用于服务层处理。</p>
     *
     * @param cmd 更新命令
     * @return 流程DTO对象
     */
    private WorkflowDTO convertToDto(WorkflowUpdateCmd cmd) {
        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(cmd.getId());
        dto.setName(cmd.getName());
        dto.setDescription(cmd.getDescription());
        dto.setWorkflowType(cmd.getWorkflowType());
        dto.setStages(cmd.getStages());
        return dto;
    }
}
