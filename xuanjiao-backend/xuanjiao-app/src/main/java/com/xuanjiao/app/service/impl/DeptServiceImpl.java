package com.xuanjiao.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.service.DeptService;
import com.xuanjiao.client.dto.DeptDTO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.mapper.DeptMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl implements DeptService {

    @Resource
    private DeptMapper deptMapper;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除易混淆字符
    private static final int CODE_LENGTH = 6;

    @Override
    public List<DeptDTO> list() {
        List<DeptDO> list = deptMapper.selectList(new LambdaQueryWrapper<DeptDO>()
                .eq(DeptDO::getDeleted, 0)
                .orderByAsc(DeptDO::getLevel, DeptDO::getSort));
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<DeptDTO> getTree() {
        List<DeptDO> all = deptMapper.selectAll();
        return buildTree(all, 0L);
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

        // 生成部门编号
        if (dept.getCode() == null || dept.getCode().isEmpty()) {
            dept.setCode(generateCode());
        }

        // 设置层级和完整编号
        if (dept.getParentId() != null && dept.getParentId() > 0) {
            DeptDO parent = deptMapper.selectById(dept.getParentId());
            if (parent != null) {
                dept.setLevel(parent.getLevel() + 1);
                dept.setFullCode(parent.getFullCode() + "-" + dept.getCode());
            }
        } else {
            dept.setLevel(1);
            dept.setFullCode(dept.getCode());
        }

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

    @Override
    public String generateCode() {
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (deptMapper.selectByCode(code) != null);
        return code;
    }

    private List<DeptDTO> buildTree(List<DeptDO> all, Long parentId) {
        List<DeptDTO> result = new ArrayList<>();
        for (DeptDO dept : all) {
            if (parentId.equals(dept.getParentId())) {
                DeptDTO dto = convert(dept);
                dto.setChildren(buildTree(all, dept.getId()));
                result.add(dto);
            }
        }
        return result;
    }

    private DeptDTO convert(DeptDO entity) {
        if (entity == null) return null;
        DeptDTO dto = new DeptDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
