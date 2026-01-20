package com.xuanjiao.domain.dept.repository;

import com.xuanjiao.domain.dept.entity.Dept;
import java.util.List;

public interface DeptRepository {
    Dept findById(Long id);
    List<Dept> findByParentId(Long parentId);
    List<Dept> findAll();
    void save(Dept dept);
    void update(Dept dept);
    void deleteById(Long id);
}
