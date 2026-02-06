package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 素材-标签关联数据对象
 * <p>对应数据库表 asset_tag，存储素材与标签的多对多关系</p>
 *
 * @author system
 * @version 1.0
 */
@Data
@TableName("asset_tag")
public class AssetTagDO {
    /** 素材ID */
    @TableField("asset_id")
    private Long assetId;

    /** 标签ID */
    @TableField("tag_id")
    private Long tagId;
}
