package com.xuanjiao.infrastructure.asset;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.domain.asset.entity.Asset;
import com.xuanjiao.domain.asset.repository.AssetRepository;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 素材仓储实现类
 *
 * <p>实现素材数据的持久化操作，基于 MyBatis Mapper。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Repository
public class AssetRepositoryImpl implements AssetRepository {

    /**
     * 素材 Mapper
     */
    @Resource
    private AssetMapper assetMapper;

    /**
     * 根据ID查询素材
     *
     * @param id 素材ID
     * @return 素材实体
     */
    @Override
    public Asset findById(Long id) {
        AssetDO assetDO = assetMapper.selectById(id);
        return convert(assetDO);
    }

    /**
     * 根据MD5查询素材
     *
     * @param md5 MD5值
     * @return 素材实体
     */
    @Override
    public Asset findByMd5(String md5) {
        AssetDO assetDO = assetMapper.selectByMd5IncludeDeleted(md5);
        return convert(assetDO);
    }

    /**
     * 条件查询素材列表
     *
     * @param name 名称关键字
     * @param type 类型
     * @param status 状态
     * @param offset 偏移量
     * @param limit 限制数
     * @return 素材实体列表
     */
    @Override
    public List<Asset> findByCondition(String name, String type, String status, int offset, int limit) {
        AssetQuery query = new AssetQuery();
        query.setName(name);
        query.setType(type);
        query.setStatus(status);
        query.setOffset(offset);
        query.setLimit(limit);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");

        List<AssetDO> list = assetMapper.selectList(query);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    /**
     * 根据申请ID查询素材列表
     *
     * @param applicationId 申请ID
     * @return 素材实体列表
     */
    @Override
    public List<Asset> findByApplicationId(Long applicationId) {
        List<AssetDO> list = assetMapper.selectByApplicationId(applicationId);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    /**
     * 根据状态列表查询素材
     *
     * @param name 名称关键字
     * @param type 类型
     * @param statusList 状态列表
     * @param offset 偏移量
     * @param limit 限制数
     * @return 素材实体列表
     */
    @Override
    public List<Asset> findByStatusList(String name, String type, List<String> statusList, int offset, int limit) {
        AssetQuery query = new AssetQuery();
        query.setName(name);
        query.setType(type);
        query.setStatusList(statusList);
        query.setOffset(offset);
        query.setLimit(limit);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");

        List<AssetDO> list = assetMapper.selectList(query);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    /**
     * 统计状态列表中的素材数量
     *
     * @param name 名称关键字
     * @param type 类型
     * @param statusList 状态列表
     * @return 数量
     */
    @Override
    public long countByStatusList(String name, String type, List<String> statusList) {
        AssetQuery query = new AssetQuery();
        query.setName(name);
        query.setType(type);
        query.setStatusList(statusList);

        return assetMapper.selectCount(query);
    }

    /**
     * 条件统计素材数量
     *
     * @param name 名称关键字
     * @param type 类型
     * @param status 状态
     * @return 数量
     */
    @Override
    public long countByCondition(String name, String type, String status) {
        AssetQuery query = new AssetQuery();
        query.setName(name);
        query.setType(type);
        query.setStatus(status);

        return assetMapper.selectCount(query);
    }

    /**
     * 保存素材
     *
     * @param asset 素材实体
     */
    @Override
    public void save(Asset asset) {
        AssetDO assetDO = new AssetDO();
        BeanUtils.copyProperties(asset, assetDO);
        assetMapper.insert(assetDO);
        asset.setId(assetDO.getId());
    }

    /**
     * 更新素材
     *
     * @param asset 素材实体
     */
    @Override
    public void update(Asset asset) {
        AssetDO assetDO = new AssetDO();
        BeanUtils.copyProperties(asset, assetDO);
        assetMapper.updateById(assetDO);
    }

    /**
     * 删除素材
     *
     * @param id 素材ID
     */
    @Override
    public void deleteById(Long id) {
        assetMapper.deleteById(id);
    }

    /**
     * 将 DO 转换为实体
     *
     * @param assetDO 素材数据对象
     * @return 素材实体
     */
    private Asset convert(AssetDO assetDO) {
        if (assetDO == null) {
            return null;
        }
        Asset asset = new Asset();
        BeanUtils.copyProperties(assetDO, asset);
        return asset;
    }
}
