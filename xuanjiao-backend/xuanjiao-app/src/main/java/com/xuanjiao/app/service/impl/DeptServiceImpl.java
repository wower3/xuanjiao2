package com.xuanjiao.app.service.impl;

import com.xuanjiao.app.service.DeptService;
import com.xuanjiao.client.dto.DeptDTO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.mapper.DeptMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl implements DeptService {

    @Resource
    private DeptMapper deptMapper;

    @Override
    public List<DeptDTO> list() {
        List<DeptDO> list = deptMapper.selectList(null);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public DeptDTO getById(Long id) {
        DeptDO dept = deptMapper.selectById(id);
        return convert(dept);
    }

    @Override
    public void save(DeptDTO dto) {
        DeptDO dept = new DeptDO();
        BeanUtils.copyProperties(dto, dept);
        deptMapper.insert(dept);
    }

    @Override
    public void update(DeptDTO dto) {
        DeptDO dept = new DeptDO();
        BeanUtils.copyProperties(dto, dept);
        deptMapper.updateById(dept);
    }

    @Override
    public void delete(Long id) {
        deptMapper.deleteById(id);
    }

    private DeptDTO convert(DeptDO entity) {
        if (entity == null) return null;
        DeptDTO dto = new DeptDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
