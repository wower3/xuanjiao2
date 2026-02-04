package com.xuanjiao.app.asset.impl;

import com.xuanjiao.app.asset.TagService;
import com.xuanjiao.client.dto.TagDTO;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.asset.TagQuery;
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
        TagQuery query = new TagQuery();
        query.setOrderByField("category");
        query.setOrderByDirection("ASC");
        List<TagDO> tagList = tagMapper.selectList(query);
        return tagList.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<TagDTO> listByCategory(String category) {
        TagQuery query = new TagQuery();
        if (StringUtils.hasText(category)) {
            query.setCategory(category);
        }
        query.setOrderByField("name");
        query.setOrderByDirection("ASC");
        List<TagDO> tagList = tagMapper.selectList(query);
        return tagList.stream().map(this::convert).collect(Collectors.toList());
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
