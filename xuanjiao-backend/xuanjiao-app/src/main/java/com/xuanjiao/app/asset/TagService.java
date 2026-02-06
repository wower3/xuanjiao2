package com.xuanjiao.app.asset;

import com.xuanjiao.client.dto.TagDTO;

import java.util.List;

/**
 * 标签服务接口
 * <p>提供素材标签的创建、查询、删除等功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.asset.impl.TagServiceImpl
 */
public interface TagService {

    /**
     * 创建标签
     *
     * @param name 标签名称
     * @param category 标签分类
     * @return 创建的标签DTO
     */
    TagDTO create(String name, String category);

    /**
     * 获取所有标签
     *
     * @return 标签DTO列表
     */
    List<TagDTO> list();

    /**
     * 根据分类获取标签
     *
     * @param category 标签分类
     * @return 标签DTO列表
     */
    List<TagDTO> listByCategory(String category);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void delete(Long id);
}
