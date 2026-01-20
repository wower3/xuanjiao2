package com.xuanjiao.adapter.web.workflow;

import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.client.dto.ApproverSelectionDTO;
import com.xuanjiao.client.dto.ApprovalProgressDTO;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.WorkflowDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "审批人选择API")
@RestController
@RequestMapping("/workflow")
public class ApproverSelectionController {

    @Resource
    private ApproverSelectionService approverSelectionService;

    @ApiOperation("获取下一层可选审批人")
    @GetMapping("/next-stage-approvers")
    public Result<List<ApproverSelectionDTO>> getNextStageApprovers(
            @RequestParam Long stageId,
            @RequestParam Long instanceId,
            @RequestParam Long applicantId,
            @RequestParam(required = false) String keyword
    ) {
        List<ApproverSelectionDTO> approvers = approverSelectionService.getNextStageApprovers(
            stageId, instanceId, applicantId, keyword
        );
        return Result.success(approvers);
    }

    @ApiOperation("选择下一层审批人")
    @PostMapping("/select-next-stage-approvers")
    public Result<Void> selectNextStageApprovers(@RequestBody SelectNextStageApproversRequest request) {
        approverSelectionService.selectNextStageApprovers(request.getTaskId(), request.getApproverIds());
        return Result.success();
    }

    @ApiOperation("获取审批实例进度")
    @GetMapping("/progress/{instanceId}")
    public Result<List<ApprovalProgressDTO>> getApprovalProgress(@PathVariable Long instanceId) {
        List<ApprovalProgressDTO> progress = approverSelectionService.getApprovalProgress(instanceId);
        return Result.success(progress);
    }

    @ApiOperation("根据角色获取绑定的审批流程")
    @GetMapping("/by-role")
    public Result<WorkflowDTO> getWorkflowByRole(
            @RequestParam Long roleId,
            @RequestParam String workflowType
    ) {
        WorkflowDTO workflow = approverSelectionService.getWorkflowByRole(roleId, workflowType);
        if (workflow == null) {
            return Result.error("该角色未绑定的审批流程");
        }
        return Result.success(workflow);
    }

    @ApiOperation("获取第一层可选审批人")
    @GetMapping("/first-stage-approvers")
    public Result<Map<String, Object>> getFirstStageApprovers(
            @RequestParam Long workflowId,
            @RequestParam Long applicantId,
            @RequestParam(required = false) String keyword
    ) {
        Map<String, Object> data = approverSelectionService.getFirstStageApprovers(
            workflowId, applicantId, keyword
        );
        return Result.success(data);
    }

    @ApiOperation("选择第一层审批人")
    @PostMapping("/select-first-stage-approvers")
    public Result<Void> selectFirstStageApprovers(@RequestBody SelectFirstStageApproversRequest request) {
        approverSelectionService.selectFirstStageApprovers(request.getInstanceId(), request.getApproverIds());
        return Result.success();
    }

    @ApiOperation("选择第一层审批人（包括子流程）")
    @PostMapping("/select-first-stage-approvers-with-subworkflows")
    public Result<Void> selectFirstStageApproversWithSubWorkflows(@RequestBody SelectFirstStageApproversWithSubWorkflowsRequest request) {
        approverSelectionService.selectFirstStageApproversWithSubWorkflows(
            request.getInstanceId(), request.getApproverIds(), request.getSubWorkflowApproverIds()
        );
        return Result.success();
    }

    @ApiOperation("选择下一层审批人（包括子流程）")
    @PostMapping("/select-next-stage-approvers-with-subworkflows")
    public Result<Void> selectNextStageApproversWithSubWorkflows(@RequestBody SelectNextStageApproversWithSubWorkflowsRequest request) {
        approverSelectionService.selectNextStageApprovers(
            request.getTaskId(), request.getApproverIds(), request.getSubWorkflowApproverIds()
        );
        return Result.success();
    }

    @ApiOperation("获取子流程第一层可选审批人")
    @GetMapping("/sub-workflow-approvers")
    public Result<Map<String, Object>> getSubWorkflowFirstStageApprovers(
            @RequestParam Long subWorkflowId,
            @RequestParam Long applicantId,
            @RequestParam(required = false) String keyword
    ) {
        Map<String, Object> data = approverSelectionService.getSubWorkflowFirstStageApprovers(
            subWorkflowId, applicantId, keyword
        );
        return Result.success(data);
    }

    @ApiOperation("选择子流程第一层审批人")
    @PostMapping("/select-sub-workflow-approvers")
    public Result<Void> selectSubWorkflowFirstStageApprovers(@RequestBody SelectSubWorkflowFirstStageApproversRequest request) {
        approverSelectionService.selectSubWorkflowFirstStageApprovers(request.getSubInstanceId(), request.getApproverIds());
        return Result.success();
    }

    public static class SelectNextStageApproversRequest {
        private Long taskId;
        private List<Long> approverIds;

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public List<Long> getApproverIds() {
            return approverIds;
        }

        public void setApproverIds(List<Long> approverIds) {
            this.approverIds = approverIds;
        }
    }

    public static class SelectFirstStageApproversRequest {
        private Long instanceId;
        private List<Long> approverIds;

        public Long getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
        }

        public List<Long> getApproverIds() {
            return approverIds;
        }

        public void setApproverIds(List<Long> approverIds) {
            this.approverIds = approverIds;
        }
    }

    public static class SelectSubWorkflowFirstStageApproversRequest {
        private Long subInstanceId;
        private List<Long> approverIds;

        public Long getSubInstanceId() {
            return subInstanceId;
        }

        public void setSubInstanceId(Long subInstanceId) {
            this.subInstanceId = subInstanceId;
        }

        public List<Long> getApproverIds() {
            return approverIds;
        }

        public void setApproverIds(List<Long> approverIds) {
            this.approverIds = approverIds;
        }
    }

    public static class SelectFirstStageApproversWithSubWorkflowsRequest {
        private Long instanceId;
        private List<Long> approverIds;
        private Map<Long, List<Long>> subWorkflowApproverIds;

        public Long getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
        }

        public List<Long> getApproverIds() {
            return approverIds;
        }

        public void setApproverIds(List<Long> approverIds) {
            this.approverIds = approverIds;
        }

        public Map<Long, List<Long>> getSubWorkflowApproverIds() {
            return subWorkflowApproverIds;
        }

        public void setSubWorkflowApproverIds(Map<Long, List<Long>> subWorkflowApproverIds) {
            this.subWorkflowApproverIds = subWorkflowApproverIds;
        }
    }

    public static class SelectNextStageApproversWithSubWorkflowsRequest {
        private Long taskId;
        private List<Long> approverIds;
        private Map<Long, List<Long>> subWorkflowApproverIds;

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public List<Long> getApproverIds() {
            return approverIds;
        }

        public void setApproverIds(List<Long> approverIds) {
            this.approverIds = approverIds;
        }

        public Map<Long, List<Long>> getSubWorkflowApproverIds() {
            return subWorkflowApproverIds;
        }

        public void setSubWorkflowApproverIds(Map<Long, List<Long>> subWorkflowApproverIds) {
            this.subWorkflowApproverIds = subWorkflowApproverIds;
        }
    }
}
