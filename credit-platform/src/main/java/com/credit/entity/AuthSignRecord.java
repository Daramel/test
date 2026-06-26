package com.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权签署记录表
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("auth_sign_record")
public class AuthSignRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 签署类型: 1-授权书签署, 2-报告签署
     */
    private Integer signType;

    /**
     * 签署标题
     */
    private String title;

    /**
     * 签署文件URL
     */
    private String fileUrl;

    /**
     * 签署人ID
     */
    private Long signUserId;

    /**
     * 签署人姓名
     */
    private String signUserName;

    /**
     * 签署时间
     */
    private LocalDateTime signTime;

    /**
     * 签署状态: 0-待签署, 1-已签署, 2-已拒绝, 3-已撤回
     */
    private Integer signStatus;

    /**
     * 签署结果描述
     */
    private String resultDesc;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 设备信息
     */
    private String deviceInfo;

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
