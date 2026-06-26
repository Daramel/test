package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 用户名(手机号)
     */
    private String username;

    /**
     * 密码(Bcrypt加密)
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色类型: 1-法人 2-管理员
     */
    private Integer roleType;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志: 0-未删除, 1-已删除
     */
    @TableLogic
    private Integer deleted;

}
