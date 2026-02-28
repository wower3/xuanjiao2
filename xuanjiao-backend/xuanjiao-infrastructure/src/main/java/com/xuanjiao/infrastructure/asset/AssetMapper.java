package com.xuanjiao.infrastructure.asset;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材数据访问接口
 *
 * <p>定义素材表的数据库操作方法，对应 XML Mapper 实现。</p>
 * <p>使用原生 XML Mapper 方式，不继承 BaseMapper。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface AssetMapper {

    /**
     * 根据主键查询素材
     *
     * @param id 素材ID
     * @return 素材数据对象
     */
    AssetDO selectById(@Param("id") Long id);

    /**
     * 根据MD5查询（包含已删除记录，用于去重校验）
     *
     * @param md5 MD5值
     * @return 素材数据对象
     */
    AssetDO selectByMd5IncludeDeleted(@Param("md5") String md5);

    /**
     * 根据申请ID查询素材列表
     *
     * @param applicationId 申请ID
     * @return 素材数据对象列表
     */
    List<AssetDO> selectByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 动态条件查询素材列表
     *
     * @param query 查询条件
     * @return 素材数据对象列表
     */
    List<AssetDO> selectList(AssetQuery query);

    /**
     * 查询总数
     *
     * @param query 查询条件
     * @return 总数
     */
    Long selectCount(AssetQuery query);

    /**
     * 分页查询素材
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AssetDO> selectPage(Page<AssetDO> page, @Param("query") AssetQuery query);

    /**
     * 插入素材
     *
     * @param asset 素材数据对象
     * @return 影响行数
     */
    int insert(AssetDO asset);

    /**
     * 更新素材
     *
     * @param asset 素材数据对象
     * @return 影响行数
     */
    int updateById(AssetDO asset);

    /**
     * 批量更新状态（通过申请ID更新）
     *
     * @param applicationId 申请ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatusByApplicationId(@Param("applicationId") Long applicationId, @Param("status") String status);

    /**
     * 更新素材为已删除状态（管理员彻底删除）
     *
     * <p>直接更新 deleted 字段为 1，绕过 @TableLogic 限制。</p>
     *
     * @param id 素材ID
     * @return 影响行数
     */
    int updateDeletedById(@Param("id") Long id);

    /**
     * 批量更新deleted字段（用于定时清理任务）
     *
     * <p>将状态为 DELETED 且删除审批时间早于指定时间的素材彻底软删除。</p>
     *
     * @param beforeTime 截止时间
     * @return 影响行数
     */
    int cleanupDeletedAssets(@Param("beforeTime") java.time.LocalDateTime beforeTime);

    /**
     * 删除素材（逻辑删除）
     *
     * @param id 素材ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID列表批量查询素材
     *
     * <p>用于批量获取素材信息，避免N+1查询问题。</p>
     *
     * @param ids 素材ID列表
     * @return 素材数据对象列表
     */
    List<AssetDO> selectByIds(@Param("ids") List<Long> ids);
}
