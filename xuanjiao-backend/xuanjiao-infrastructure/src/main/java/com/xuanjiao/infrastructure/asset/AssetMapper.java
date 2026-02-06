package com.xuanjiao.infrastructure.asset;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材数据访问接口
 * <p>定义素材的数据库操作方法，对应SQL实现</p>
 * <p>设计说明：使用原生XML Mapper方式，移除BaseMapper继承</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.asset.entity.Asset
 */
@Mapper
public interface AssetMapper {

    /**
     * 根据主键查询
     */
    AssetDO selectById(@Param("id") Long id);

    /**
     * 根据MD5查询（包含已删除记录，用于去重校验）
     */
    AssetDO selectByMd5IncludeDeleted(@Param("md5") String md5);

    /**
     * 根据申请ID查询
     */
    List<AssetDO> selectByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 动态条件查询
     */
    List<AssetDO> selectList(AssetQuery query);

    /**
     * 查询总数
     */
    Long selectCount(AssetQuery query);

    /**
     * 分页查询
     */
    IPage<AssetDO> selectPage(Page<AssetDO> page, @Param("query") AssetQuery query);

    /**
     * 插入
     */
    int insert(AssetDO asset);

    /**
     * 更新
     */
    int updateById(AssetDO asset);

    /**
     * 批量更新（通过 application_id 更新状态）
     */
    int updateStatusByApplicationId(@Param("applicationId") Long applicationId, @Param("status") String status);

    /**
     * 更新素材为已删除状态（管理员彻底删除）
     * 直接更新 deleted 字段为 1，绕过 @TableLogic 限制
     */
    int updateDeletedById(@Param("id") Long id);

    /**
     * 批量更新deleted字段（用于定时清理任务）
     * 将状态为DELETED且删除审批时间早于指定时间的素材彻底软删除
     */
    int cleanupDeletedAssets(@Param("beforeTime") java.time.LocalDateTime beforeTime);

    /**
     * 删除（逻辑删除）
     */
    int deleteById(@Param("id") Long id);
}
