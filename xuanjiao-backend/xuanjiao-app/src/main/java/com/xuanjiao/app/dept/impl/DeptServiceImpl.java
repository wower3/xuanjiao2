package com.xuanjiao.app.dept.impl;

import com.xuanjiao.app.dept.DeptService;
import com.xuanjiao.client.dto.DeptDTO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.dept.DeptQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 * <p>实现DeptService接口，封装部门业务逻辑</p>
 * <p>核心功能：部门CRUD、树形结构生成、编码生成</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.dept.DeptService
 */
@Service
public class DeptServiceImpl implements DeptService {

    @Resource
    private DeptMapper deptMapper;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除易混淆字符
    private static final int CODE_LENGTH = 6;

    @Override
    public List<DeptDTO> list() {
        // 使用 DeptQuery 替代 LambdaQueryWrapper
        DeptQuery query = new DeptQuery();
        // XML 中默认按 level, sort 排序，无需指定 orderBy
        List<DeptDO> list = deptMapper.selectList(query);
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
