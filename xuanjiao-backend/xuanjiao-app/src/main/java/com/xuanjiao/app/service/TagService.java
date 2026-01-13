package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.TagDTO;

import java.util.List;

public interface TagService {
    /**
     * 创建标签
     */
    TagDTO create(String name, String category);

    /**
     * 获取所有标签
     */
    List<TagDTO> list();

    /**
     * 根据分类获取标签
     */
    List<TagDTO> listByCategory(String category);

    /**
     * 删除标签
     */
    void delete(Long id);
}
