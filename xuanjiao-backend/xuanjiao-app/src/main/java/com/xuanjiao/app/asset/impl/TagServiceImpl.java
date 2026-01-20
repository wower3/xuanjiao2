package com.xuanjiao.app.asset.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.asset.TagService;
import com.xuanjiao.client.dto.TagDTO;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import com.xuanjiao.infrastructure.asset.TagMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public TagDTO create(String name, String category) {
        TagDO tag = new TagDO();
        tag.setName(name);
        tag.setCategory(category);
        tag.setCreateTime(LocalDateTime.now());
        tagMapper.insert(tag);
        return convert(tag);
    }

    @Override
    public List<TagDTO> list() {
        LambdaQueryWrapper<TagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TagDO::getCategory).orderByAsc(TagDO::getName);
        List<TagDO> list = tagMapper.selectList(wrapper);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<TagDTO> listByCategory(String category) {
        LambdaQueryWrapper<TagDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(TagDO::getCategory, category);
        }
        wrapper.orderByAsc(TagDO::getName);
        List<TagDO> list = tagMapper.selectList(wrapper);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        tagMapper.deleteById(id);
    }

    private TagDTO convert(TagDO tagDO) {
        if (tagDO == null) return null;
        TagDTO dto = new TagDTO();
        BeanUtils.copyProperties(tagDO, dto);
        return dto;
    }
}
