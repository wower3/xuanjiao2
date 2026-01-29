package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("asset_tag")
public class AssetTagDO {
    @TableField("asset_id")
    private Long assetId;
    @TableField("tag_id")
    private Long tagId;
}
