package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.DeptDTO;
import java.util.List;

public interface DeptService {
    List<DeptDTO> list();
    DeptDTO getById(Long id);
    void save(DeptDTO dto);
    void update(DeptDTO dto);
    void delete(Long id);
}
