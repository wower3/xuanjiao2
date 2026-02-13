package com.xuanjiao.adapter.web.workflow;

import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.client.dto.approval.dto.ApprovalProgressDTO;
import com.xuanjiao.client.dto.common.Result;
import com.xuanjiao.client.dto.workflow.dto.FirstStageApproversDTO;
import com.xuanjiao.client.dto.workflow.WorkflowGetApprovalProgressQry;
import com.xuanjiao.client.dto.workflow.WorkflowGetFirstStageApproversQry;
import com.xuanjiao.client.dto.workflow.WorkflowGetSubWorkflowFirstStageApproversQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 审批人选择控制器
 *
 * <p>提供审批流程中审批人选择的辅助功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>获取下一阶段审批人：根据当前阶段获取下一阶段的可选审批人</li>
 *   <li>搜索审批人：按用户名/部门名搜索可选审批人</li>
 *   <li>获取子流程审批人：获取子流程的第一阶段审批人</li>
 *   <li>获取审批进度：查询审批实例的详细进度信息</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "审批人选择API")
@RestController
@RequestMapping("/workflow")
public class ApproverSelectionController {

    /**
     * 审批人选择服务
     *
     * <p>处理审批人查询、选择和子流程相关的业务逻辑。</p>
     */
    @Resource
    private ApproverSelectionService approverSelectionService;

    /**
     * 获取审批实例进度
     *
     * <p>查询指定审批实例的详细进度信息，包括各阶段的审批状态、审批人、审批意见等。
     * 支持包含子流程的进度展示。</p>
     *
     * @param qry 查询条件，包含审批实例ID
     * @return 审批进度列表
     */
    @ApiOperation("获取审批实例进度")
    @PostMapping("/getApprovalProgress")
    public Result<List<ApprovalProgressDTO>> getApprovalProgress(@Valid @RequestBody WorkflowGetApprovalProgressQry qry) {
        List<ApprovalProgressDTO> progress = approverSelectionService.getApprovalProgress(qry.getInstanceId());
        return Result.success(progress);
    }

    /**
     * 获取第一层可选审批人
     *
     * <p>根据工作流ID查询第一阶段的可选审批人列表，支持按关键字搜索。
     * 返回结果包括按部门分组的审批人列表。</p>
     *
     * @param qry 查询条件，包含工作流ID、申请人ID和搜索关键字
     * @return 首阶段审批人选择结果DTO
     */
    @ApiOperation("获取第一层可选审批人")
    @PostMapping("/getFirstStageApprovers")
    public Result<FirstStageApproversDTO> getFirstStageApprovers(@Valid @RequestBody WorkflowGetFirstStageApproversQry qry) {
        FirstStageApproversDTO data = approverSelectionService.getFirstStageApprovers(
            qry.getWorkflowId(), qry.getApplicantId(), qry.getKeyword()
        );
        return Result.success(data);
    }

    /**
     * 选择第一层审批人（包括子流程）
     *
     * <p>为审批实例选择第一阶段审批人，同时可以选择子流程的审批人。
     * 选择完成后，系统将创建对应的审批任务。</p>
     *
     * @param request 选择请求，包含审批实例ID、主流程审批人ID列表和子流程审批人映射
     * @return 操作结果
     */
    @ApiOperation("选择第一层审批人（包括子流程）")
    @PostMapping("/select-first-stage-approvers-with-subworkflows")
    public Result<Void> selectFirstStageApproversWithSubWorkflows(@RequestBody SelectFirstStageApproversWithSubWorkflowsRequest request) {
        approverSelectionService.selectFirstStageApproversWithSubWorkflows(
            request.getInstanceId(), request.getApproverIds(), request.getSubWorkflowApproverIds()
        );
        return Result.success();
    }

    /**
     * 选择下一层审批人（包括子流程）
     *
     * <p>在当前审批任务完成后，选择下一阶段的审批人，同时可以选择子流程的审批人。
     * 此操作由第一审批人执行。</p>
     *
     * @param request 选择请求，包含审批任务ID、主流程审批人ID列表和子流程审批人映射
     * @return 操作结果
     */
    @ApiOperation("选择下一层审批人（包括子流程）")
    @PostMapping("/select-next-stage-approvers-with-subworkflows")
    public Result<Void> selectNextStageApproversWithSubWorkflows(@RequestBody SelectNextStageApproversWithSubWorkflowsRequest request) {
        approverSelectionService.selectNextStageApprovers(
            request.getTaskId(), request.getApproverIds(), request.getSubWorkflowApproverIds()
        );
        return Result.success();
    }

     * <p>根据子流程ID查询子流程第一阶段的可选审批人列表，支持按关键字搜索。</p>
     *
     * @param qry 查询条件，包含子流程ID、申请人ID和搜索关键字
     * @return 首阶段审批人选择结果DTO
     */
    @ApiOperation("获取子流程第一层可选审批人")
    @PostMapping("/getSubWorkflowFirstStageApprovers")
    public Result<FirstStageApproversDTO> getSubWorkflowFirstStageApprovers(@Valid @RequestBody WorkflowGetSubWorkflowFirstStageApproversQry qry) {
        FirstStageApproversDTO data = approverSelectionService.getSubWorkflowFirstStageApprovers(
            qry.getSubWorkflowId(), qry.getApplicantId(), qry.getKeyword()
        );
        return Result.success(data);
    }

    /**
     * 选择第一层审批人请求对象
     *
     * <p>封装选择第一层审批人时需要提交的参数。</p>
     *
     * @author xuanjiao
     * @since 1.0.0
     */
    public static class SelectFirstStageApproversWithSubWorkflowsRequest {

        /**
         * 审批实例ID
         */
        private Long instanceId;

        /**
         * 主流程审批人ID列表
         */
        private List<Long> approverIds;

        /**
         * 子流程审批人映射
         *
         * <p>key 为子流程ID，value 为该子流程的审批人ID列表。</p>
         */
        private Map<Long, List<Long>> subWorkflowApproverIds;

        /**
         * 获取审批实例ID
         *
         * @return 审批实例ID
         */
        public Long getInstanceId() {
            return instanceId;
        }

        /**
         * 设置审批实例ID
         *
         * @param instanceId 审批实例ID
         */
        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
        }

        /**
         * 获取主流程审批人ID列表
         *
         * @return 审批人ID列表
         */
        public List<Long> getApproverIds() {
            return approverIds;
        }

        /**
         * 设置主流程审批人ID列表
         *
         * @param approverIds 审批人ID列表
         */
        public void setApproverIds(List<Long> approverIds) {
            this.approverIds = approverIds;
        }

        /**
         * 获取子流程审批人映射
         *
         * @return 子流程审批人映射
         */
        public Map<Long, List<Long>> getSubWorkflowApproverIds() {
            return subWorkflowApproverIds;
        }

        /**
         * 设置子流程审批人映射
         *
         * @param subWorkflowApproverIds 子流程审批人映射
         */
        public void setSubWorkflowApproverIds(Map<Long, List<Long>> subWorkflowApproverIds) {
            this.subWorkflowApproverIds = subWorkflowApproverIds;
        }
    }

    /**
     * 选择下一层审批人请求对象
     *
     * <p>封装选择下一层审批人时需要提交的参数。</p>
     *
     * @author xuanjiao
     * @since 1.0.0
     */
    public static class SelectNextStageApproversWithSubWorkflowsRequest {

        /**
         * 审批任务ID
         */
        private Long taskId;

        /**
         * 主流程审批人ID列表
         */
        private List<Long> approverIds;

        /**
         * 子流程审批人映射
         *
         * <p>key 为子流程ID，value 为该子流程的审批人ID列表。</p>
         */
        private Map<Long, List<Long>> subWorkflowApproverIds;

        /**
         * 获取审批任务ID
         *
         * @return 审批任务ID
         */
        public Long getTaskId() {
            return taskId;
        }

        /**
         * 设置审批任务ID
         *
         * @param taskId 审批任务ID
         */
        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        /**
         * 获取主流程审批人ID列表
         *
         * @return 审批人ID列表
         */
        public List<Long> getApproverIds() {
            return approverIds;
        }

        /**
         * 设置主流程审批人ID列表
         *
         * @param approverIds 审批人ID列表
         */
        public void setApproverIds(List<Long> approverIds) {
            this.approverIds = approverIds;
        }

        /**
         * 获取子流程审批人映射
         *
         * @return 子流程审批人映射
         */
        public Map<Long, List<Long>> getSubWorkflowApproverIds() {
            return subWorkflowApproverIds;
        }

        /**
         * 设置子流程审批人映射
         *
         * @param subWorkflowApproverIds 子流程审批人映射
         */
        public void setSubWorkflowApproverIds(Map<Long, List<Long>> subWorkflowApproverIds) {
            this.subWorkflowApproverIds = subWorkflowApproverIds;
        }
    }
}
