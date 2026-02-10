package com.xuanjiao.domain.asset.repository;

import com.xuanjiao.domain.asset.entity.Asset;
import java.util.List;

/**
 * 素材仓储接口
 *
 * <p>定义素材的持久化操作，包括素材的查询、保存、更新和删除。</p>
 * <p>素材是系统中的核心媒体资产，包括视频、图片、文档等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface AssetRepository {

    /**
     * 根据ID查找素材
     *
     * @param id 素材ID
     * @return 素材实体，如果不存在返回 null
     */
    Asset findById(Long id);

    /**
     * 根据MD5查找素材
     *
     * <p>用于文件去重，相同MD5的文件不重复存储。</p>
     *
     * @param md5 文件MD5值
     * @return 素材实体，如果不存在返回 null
     */
    Asset findByMd5(String md5);

    /**
     * 根据条件查询素材列表
     *
     * @param name 素材名称（模糊匹配）
     * @param type 素材类型
     * @param status 素材状态
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 匹配的素材列表
     */
    List<Asset> findByCondition(String name, String type, String status, int offset, int limit);

    /**
     * 根据状态列表查询素材
     *
     * <p>支持多状态筛选。</p>
     *
     * @param name 素材名称（模糊匹配）
     * @param type 素材类型
     * @param statusList 状态列表
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 匹配的素材列表
     */
    List<Asset> findByStatusList(String name, String type, List<String> statusList, int offset, int limit);

    /**
     * 根据申请ID查找素材列表
     *
     * <p>获取某个素材录入申请下所有素材。</p>
     *
     * @param applicationId 申请ID
     * @return 该申请下的素材列表
     */
    List<Asset> findByApplicationId(Long applicationId);

    /**
     * 根据条件统计素材数量
     *
     * @param name 素材名称（模糊匹配）
     * @param type 素材类型
     * @param status 素材状态
     * @return 匹配的素材数量
     */
    long countByCondition(String name, String type, String status);

    /**
     * 根据状态列表统计素材数量
     *
     * @param name 素材名称（模糊匹配）
     * @param type 素材类型
     * @param statusList 状态列表
     * @return 匹配的素材数量
     */
    long countByStatusList(String name, String type, List<String> statusList);

    /**
     * 保存素材
     *
     * @param asset 素材实体
     */
    void save(Asset asset);

    /**
     * 更新素材
     *
     * @param asset 素材实体
     */
    void update(Asset asset);

    /**
     * 根据ID删除素材
     *
     * @param id 素材ID
     */
    void deleteById(Long id);
}
