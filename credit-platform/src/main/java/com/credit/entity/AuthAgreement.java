package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权协议表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("auth_agreement")
public class AuthAgreement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 协议ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 协议版本
     */
    private String version;

    /**
     * 协议标题
     */
    private String title;

    /**
     * 协议内容（富文本）
     */
    private String content;

    /**
     * 状态: 0-草稿, 1-有效, 2-无效
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

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
