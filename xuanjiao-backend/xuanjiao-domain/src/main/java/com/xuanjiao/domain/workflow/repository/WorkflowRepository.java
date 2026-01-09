package com.xuanjiao.domain.workflow.repository;

import com.xuanjiao.domain.workflow.entity.Workflow;
import java.util.List;

public interface WorkflowRepository {
    Workflow findById(Long id);
    List<Workflow> findAll();
    void save(Workflow workflow);
    void update(Workflow workflow);
    void deleteById(Long id);
}
