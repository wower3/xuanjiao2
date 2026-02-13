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
     * 根据申请ID查找素材列表
     *
     * <p>获取某个素材录入申请下所有素材。</p>
     *
     * @param applicationId 申请ID
     * @return 该申请下的素材列表
     */
    List<Asset> findByApplicationId(Long applicationId);

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
