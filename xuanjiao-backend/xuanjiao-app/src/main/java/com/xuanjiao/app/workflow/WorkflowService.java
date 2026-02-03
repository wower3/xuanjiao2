package com.xuanjiao.app.workflow;

import com.xuanjiao.client.dto.WorkflowDTO;
import java.util.List;

public interface WorkflowService {
    List<WorkflowDTO> list();
    WorkflowDTO getById(Long id);
    WorkflowDTO save(WorkflowDTO dto);
    void update(WorkflowDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void bindRole(Long id, Long roleId, String workflowType);
    void unbindRole(Long id);
    WorkflowDTO copy(Long id);
}
