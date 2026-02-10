package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户数据对象
 *
 * <p>映射数据库 sys_user 表，用于 MyBatis 数据访问。</p>
 * <p>存储系统用户的基本信息，包括登录凭证、个人资料和权限关联。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("sys_user")
public class UserDO {

    /**
     * 用户ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名，登录账号，唯一标识
     */
    private String username;

    /**
     * 登录密码，加密存储
     */
    private String password;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 用户状态：1-正常、0-禁用
     */
    private Integer status;

    /**
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除、1-已删除
     */
    @TableLogic
    private Integer deleted;
}
