package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 素材-标签关联数据对象
 *
 * <p>映射数据库 asset_tag 表，用于 MyBatis 数据访问。</p>
 * <p>存储素材与标签的多对多关联关系。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("asset_tag")
public class AssetTagDO {

    /**
     * 素材ID，关联 asset 表
     */
    @TableField("asset_id")
    private Long assetId;

    /**
     * 标签ID，关联 tag 表
     */
    @TableField("tag_id")
    private Long tagId;
}
