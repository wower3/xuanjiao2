package com.xuanjiao.adapter.web.approval;

import com.xuanjiao.app.approval.ApprovalService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.Result;
import com.xuanjiao.client.approval.ApprovalGetInstanceDetailQry;
import com.xuanjiao.client.approval.FlowItemDTO;
import com.xuanjiao.client.approval.MyAppliedDTO;
import com.xuanjiao.client.approval.PendingTaskDTO;
import com.xuanjiao.client.approval.ApprovalGetMyAppliedQry;
import com.xuanjiao.client.approval.ApprovalGetMyFlowItemsQry;
import com.xuanjiao.client.approval.ApprovalGetMyTasksQry;
import com.xuanjiao.client.approval.ApprovalGetTaskDetailQry;
import com.xuanjiao.client.approval.InstanceDetailDTO;
import com.xuanjiao.client.approval.TaskDetailDTO;
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
import java.util.Map;

/**
 * 审批管理控制器
 *
 * <p>提供审批任务查询、审批操作、退回等功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>待我审批：查询当前用户的待审批任务，支持按业务类型过滤</li>
 *   <li>审批操作：通过、驳回、退回审批任务</li>
 *   <li>我发起的：查询当前用户发起的审批申请</li>
 *   <li>审批进度：查询审批实例的详细进度</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "审批管理")
@RestController
@RequestMapping("/approval")
public class ApprovalController {

    /**
     * 审批服务
     *
     * <p>处理审批任务的查询和操作逻辑。</p>
     */
    @Resource
    private ApprovalService approvalService;

    /**
     * 工作流引擎服务
     *
     * <p>处理工作流实例的创建、任务完成、退回等核心流程操作。</p>
     */
    @Resource
    private WorkflowEngineService workflowEngineService;

    /**
     * 查询当前用户的待审批任务
     *
     * <p>分页查询分配给当前用户的所有待审批任务，支持按业务类型进行过滤。
     * 业务类型包括：素材录入(MATERIAL_ENTRY)、素材使用(ASSET_USAGE)、素材删除(ASSET_DELETION)。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件，包含分页参数和业务类型过滤条件
     * @return 分页的待审批任务列表
     */
    @ApiOperation("待我审批")
    @PostMapping("/getMyTasks")
    public Result<PageResult<PendingTaskDTO>> getMyTasks(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ApprovalGetMyTasksQry qry) {
        return Result.success(approvalService.getMyTasks(userId, qry.getPageNum(), qry.getPageSize(), qry.getBusinessType()));
    }

    /**
     * 获取当前用户的待办任务数量
     *
     * <p>查询当前用户状态为 PENDING 的审批任务总数，
     * 用于在导航栏显示待办数量角标。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 待办任务数量
     */
    @ApiOperation("获取待办任务数量")
    @PostMapping("/getMyTasksCount")
    public Result<Long> getMyTasksCount(@RequestAttribute("userId") Long userId) {
        return Result.success(approvalService.getMyTasksCount(userId));
    }

    /**
     * 查询当前用户发起的审批申请
     *
     * <p>分页查询当前用户发起的所有审批申请，支持多条件筛选。
     * 管理员角色可查看所有用户的申请。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件，包含分页参数、业务类型、状态等过滤条件
     * @return 分页的审批申请列表
     */
    @ApiOperation("我发起的")
    @PostMapping("/getMyApplied")
    public Result<PageResult<MyAppliedDTO>> getMyApplied(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ApprovalGetMyAppliedQry qry) {
        return Result.success(approvalService.getMyApplied(userId, qry.getPageNum(), qry.getPageSize(),
                qry.getBusinessType(), qry.getForAllUsers(), qry.getApplicantId(),
                qry.getDeptId(), qry.getRoleType(), qry.getStatus()));
    }

    /**
     * 执行审批操作
     *
     * <p>对指定的审批任务执行通过或驳回操作，同时记录审批意见。
     * 审批通过后，任务将流转到下一阶段或完成审批。</p>
     *
     * @param id 审批任务ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @param comment 审批意见
     * @param passed true 表示通过，false 表示驳回
     * @return 操作结果
     */
    @ApiOperation("审批")
    @PostMapping("/tasks/{id}/approve")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestParam String comment,
            @RequestParam boolean passed) {
        approvalService.approve(id, userId, comment, passed);
        return Result.success();
    }

    /**
     * 退回审批任务到上一级
     *
     * <p>将当前审批任务退回到上一个审批阶段，同时记录退回原因。
     * 退回后，上一阶段的审批人需要重新审批。</p>
     *
     * @param id 审批任务ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @param comment 退回原因（可选）
     * @return 操作结果
     */
    @ApiOperation("退回上一级")
    @PostMapping("/tasks/{id}/return")
    public Result<Void> returnTask(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String comment) {
        approvalService.returnTask(id, userId, comment);
        return Result.success();
    }

    /**
     * 获取审批任务详情
     *
     * <p>查询指定审批任务的详细信息，包括任务基本信息、关联的业务数据、
     * 当前审批阶段信息以及可选的审批人列表。</p>
     *
     * @param qry 查询条件，包含任务ID
     * @return 任务详情信息
     */
    @ApiOperation("获取审批任务详情")
    @PostMapping("/getTaskDetail")
    public Result<TaskDetailDTO> getTaskDetail(@Valid @RequestBody ApprovalGetTaskDetailQry qry) {
        return Result.success(approvalService.getTaskDetail(qry.getId()));
    }

    /**
     * 获取审批实例详情
     *
     * <p>查询指定审批实例的详细信息，包括实例基本信息、关联的业务数据、
     * 审批进度、各阶段状态等。</p>
     *
     * @param qry 查询条件，包含实例ID
     * @return 实例详情信息
     */
    @ApiOperation("获取审批实例详情")
    @PostMapping("/getInstanceDetail")
    public Result<InstanceDetailDTO> getInstanceDetail(@Valid @RequestBody ApprovalGetInstanceDetailQry qry) {
        return Result.success(approvalService.getInstanceDetail(qry.getId()));
    }

    /**
     * 追回工单
     *
     * <p>允许发起人追回正在审批中的工单。追回后，工单将回到草稿状态，
     * 所有未完成的审批任务将被取消。</p>
     *
     * @param id 审批实例ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @param comment 追回原因（可选）
     * @return 操作结果
     */
    @ApiOperation("追回工单（发起人追回正在审批的工单）")
    @PostMapping("/instances/{id}/withdraw")
    public Result<Void> withdrawInstance(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String comment) {
        workflowEngineService.withdrawInstance(id, userId, comment);
        return Result.success();
    }

    /**
     * 查询流经事项
     *
     * <p>查询当前用户参与过的所有审批事项（包括已审批和待审批），
     * 支持按业务类型和状态进行过滤。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件，包含分页参数、业务类型和状态过滤条件
     * @return 分页的流经事项列表
     */
    @ApiOperation("流经事项")
    @PostMapping("/getMyFlowItems")
    public Result<PageResult<FlowItemDTO>> getMyFlowItems(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ApprovalGetMyFlowItemsQry qry) {
        return Result.success(approvalService.getMyFlowItems(userId, qry.getPageNum(), qry.getPageSize(),
                qry.getBusinessType(), qry.getStatus()));
    }

    /**
     * 重新发起子流程
     *
     * <p>对于被驳回的子流程任务，允许重新选择审批人并发起新的子流程。
     * 新的子流程将独立于原流程运行。</p>
     *
     * @param id 审批任务ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @param approverIds 新选择的审批人ID列表
     * @return 操作结果
     */
    @ApiOperation("重新发起子流程")
    @PostMapping("/tasks/{id}/restart-sub-workflow")
    public Result<Void> restartSubWorkflow(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestBody List<Long> approverIds) {
        workflowEngineService.restartSubWorkflow(id, userId, approverIds);
        return Result.success();
    }
}
