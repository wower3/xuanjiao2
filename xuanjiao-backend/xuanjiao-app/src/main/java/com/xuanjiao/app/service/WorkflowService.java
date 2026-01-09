package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.WorkflowDTO;
import java.util.List;

public interface WorkflowService {
    List<WorkflowDTO> list();
    WorkflowDTO getById(Long id);
    void save(WorkflowDTO dto);
    void update(WorkflowDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
}
