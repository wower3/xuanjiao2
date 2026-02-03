package com.xuanjiao.adapter.web.workflow;

import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.client.dto.ApproverSelectionDTO;
import com.xuanjiao.client.dto.ApprovalProgressDTO;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.WorkflowDTO;
import com.xuanjiao.client.dto.workflow.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "审批人选择API")
@RestController
@RequestMapping("/workflow")
public class ApproverSelectionController {

    @Resource
    private ApproverSelectionService approverSelectionService;

    @ApiOperation("获取审批实例进度")
    @PostMapping("/getApprovalProgress")
    public Result<List<ApprovalProgressDTO>> getApprovalProgress(@Valid @RequestBody WorkflowGetApprovalProgressQry qry) {
        List<ApprovalProgressDTO> progress = approverSelectionService.getApprovalProgress(qry.getInstanceId());
        return Result.success(progress);
    }

    @ApiOperation("获取第一层可选审批人")
    @PostMapping("/getFirstStageApprovers")
    public Result<Map<String, Object>> getFirstStageApprovers(@Valid @RequestBody WorkflowGetFirstStageApproversQry qry) {
        Map<String, Object> data = approverSelectionService.getFirstStageApprovers(
            qry.getWorkflowId(), qry.getApplicantId(), qry.getKeyword()
        );
        return Result.success(data);
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
    @PostMapping("/getSubWorkflowFirstStageApprovers")
    public Result<Map<String, Object>> getSubWorkflowFirstStageApprovers(@Valid @RequestBody WorkflowGetSubWorkflowFirstStageApproversQry qry) {
        Map<String, Object> data = approverSelectionService.getSubWorkflowFirstStageApprovers(
            qry.getSubWorkflowId(), qry.getApplicantId(), qry.getKeyword()
        );
        return Result.success(data);
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
