package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 标签数据对象
 * <p>对应数据库表 tag，存储素材标签的持久化数据</p>
 *
 * @author system
 * @version 1.0
 */
@Data
@TableName("tag")
public class TagDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称 */
    private String name;

    /** 标签分类 */
    private String category;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除标识 */
    @TableLogic
    private Integer deleted;
}
