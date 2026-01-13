package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.DeptDTO;
import java.util.List;

public interface DeptService {
    List<DeptDTO> list();
    List<DeptDTO> getTree();  // 获取部门树形结构
    DeptDTO getById(Long id);
    void save(DeptDTO dto);
    void update(DeptDTO dto);
    void delete(Long id);
    String generateCode();  // 生成部门编号
}
