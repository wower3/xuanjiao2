package com.xuanjiao.app.asset;

import com.xuanjiao.client.dto.TagDTO;

import java.util.List;

/**
 * 标签服务接口
 *
 * <p>提供素材标签的创建、查询、删除等功能。标签用于对素材进行分类和管理。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>标签创建</li>
 *   <li>标签列表查询</li>
 *   <li>按分类查询标签</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.asset.impl.TagServiceImpl
 */
public interface TagService {

    /**
     * 创建标签
     *
     * <p>创建新的素材标签。同一分类下标签名称不能重复。</p>
     *
     * @param name 标签名称
     * @param category 标签分类（如：素材类型、素材来源等）
     * @return 创建的标签DTO
     * @throws RuntimeException 如果标签名称已存在
     */
    TagDTO create(String name, String category);

    /**
     * 获取所有标签
     *
     * <p>返回系统中所有标签的列表。</p>
     *
     * @return 标签DTO列表
     */
    List<TagDTO> list();

    /**
     * 根据分类获取标签
     *
     * <p>返回指定分类下的所有标签。</p>
     *
     * @param category 标签分类
     * @return 标签DTO列表
     */
    List<TagDTO> listByCategory(String category);

    /**
     * 删除标签
     *
     * <p>删除指定标签。删除标签不会影响已关联的素材。</p>
     *
     * @param id 标签ID
     */
    void delete(Long id);
}
