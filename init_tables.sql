-- =============================================
-- 企业征信管理平台 - 表结构设计
-- =============================================

USE credit_platform;

-- -------------------------------------------
-- 1. sys_user (用户表)
-- -------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    enterprise_id       BIGINT                                  COMMENT '企业ID',
    username            VARCHAR(64)     UNIQUE NOT NULL        COMMENT '用户名(手机号)',
    password            VARCHAR(128)    NOT NULL                COMMENT '密码(Bcrypt加密)',
    real_name           VARCHAR(64)                             COMMENT '真实姓名',
    role_type           TINYINT        NOT NULL                 COMMENT '角色类型: 1-法人 2-管理员',
    status              TINYINT        NOT NULL DEFAULT 1       COMMENT '状态: 0-禁用 1-正常',
    last_login_time     DATETIME                               COMMENT '最后登录时间',
    last_login_ip       VARCHAR(64)                             COMMENT '最后登录IP',
    create_time         DATETIME       NOT NULL                 COMMENT '创建时间',
    update_time         DATETIME       NOT NULL                 COMMENT '更新时间',
    deleted             TINYINT        NOT NULL DEFAULT 0        COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_enterprise_id (enterprise_id),
    INDEX uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------
-- 2. sys_enterprise (企业表)
-- -------------------------------------------
DROP TABLE IF EXISTS sys_enterprise;
CREATE TABLE sys_enterprise (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    enterprise_name     VARCHAR(255)    NOT NULL              COMMENT '企业名称',
    credit_code         VARCHAR(64)    UNIQUE NOT NULL       COMMENT '统一社会信用代码',
    legal_person        VARCHAR(64)    NOT NULL              COMMENT '法定代表人',
    legal_id_card       VARCHAR(128)                           COMMENT '法人身份证(加密)',
    register_capital    DECIMAL(18,2)                          COMMENT '注册资本(万元)',
    establish_date      DATE                                    COMMENT '成立日期',
    industry            VARCHAR(64)                             COMMENT '行业分类',
    region              VARCHAR(64)                             COMMENT '所属地区',
    address             VARCHAR(512)                            COMMENT '注册地址',
    contact_phone       VARCHAR(32)                             COMMENT '联系电话',
    auth_status         TINYINT        NOT NULL DEFAULT 0     COMMENT '认证状态: 0-待审核 1-已认证 2-审核失败',
    auth_remark         VARCHAR(512)                            COMMENT '审核备注',
    auth_time           DATETIME                                COMMENT '认证通过时间',
    balance             DECIMAL(12,2)  NOT NULL DEFAULT 0     COMMENT '本金余额',
    gift_balance        DECIMAL(12,2)  NOT NULL DEFAULT 0     COMMENT '赠送余额',
    create_time         DATETIME       NOT NULL                COMMENT '创建时间',
    update_time         DATETIME       NOT NULL                COMMENT '更新时间',
    INDEX uk_credit_code (credit_code),
    INDEX idx_enterprise_name (enterprise_name),
    INDEX idx_auth_status (auth_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业表';

-- -------------------------------------------
-- 3. auth_sign_record (授权签署记录表)
-- -------------------------------------------
DROP TABLE IF EXISTS auth_sign_record;
CREATE TABLE auth_sign_record (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    sign_no             VARCHAR(64)    UNIQUE NOT NULL       COMMENT '签署编号',
    enterprise_id       BIGINT         NOT NULL              COMMENT '企业ID',
    user_id             BIGINT         NOT NULL              COMMENT '签署人用户ID',
    agreement_version   VARCHAR(32)    NOT NULL              COMMENT '协议版本',
    sign_type           TINYINT        NOT NULL              COMMENT '签名类型: 1-手写 2-姓名 3-公章',
    sign_image          VARCHAR(512)                            COMMENT '签名图片URL',
    sign_time           DATETIME       NOT NULL              COMMENT '签署时间',
    sign_ip             VARCHAR(64)                             COMMENT '签署IP',
    status              TINYINT        NOT NULL DEFAULT 1    COMMENT '状态: 1-有效 2-已撤回',
    create_time         DATETIME       NOT NULL              COMMENT '创建时间',
    INDEX uk_sign_no (sign_no),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='授权签署记录表';

-- -------------------------------------------
-- 4. credit_report (征信报告表)
-- -------------------------------------------
DROP TABLE IF EXISTS credit_report;
CREATE TABLE credit_report (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    report_no           VARCHAR(64)    UNIQUE NOT NULL       COMMENT '报告编号',
    enterprise_id       BIGINT         NOT NULL              COMMENT '所属企业ID',
    enterprise_name     VARCHAR(255)   NOT NULL              COMMENT '企业名称(冗余)',
    credit_code         VARCHAR(64)    NOT NULL              COMMENT '统一社会信用代码(冗余)',
    report_type         TINYINT        NOT NULL              COMMENT '报告类型: 1-简版 2-标准 3-深度 4-专项',
    total_score         INT                                    COMMENT '信用评分(冗余)',
    credit_rating       VARCHAR(16)                            COMMENT '信用等级',
    generate_status     TINYINT        NOT NULL DEFAULT 1    COMMENT '生成状态: 1-待生成 2-生成中 3-已完成 4-失败',
    fail_reason         VARCHAR(1024)                          COMMENT '失败原因',
    retry_count         INT           NOT NULL DEFAULT 0    COMMENT '重试次数',
    file_url            VARCHAR(512)                            COMMENT '报告文件URL',
    file_size           BIGINT                                 COMMENT '文件大小(字节)',
    valid_days          INT           NOT NULL DEFAULT 365 COMMENT '有效天数',
    expire_time         DATETIME                                COMMENT '过期时间',
    order_id            BIGINT                                 COMMENT '关联订单ID',
    owner_enterprise_id BIGINT         NOT NULL              COMMENT '归属企业ID(购买企业)',
    create_time         DATETIME       NOT NULL              COMMENT '创建时间',
    update_time         DATETIME       NOT NULL              COMMENT '更新时间',
    INDEX uk_report_no (report_no),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_owner_enterprise_id (owner_enterprise_id),
    INDEX idx_generate_status (generate_status),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='征信报告表';

-- -------------------------------------------
-- 5. order_info (订单表)
-- -------------------------------------------
DROP TABLE IF EXISTS order_info;
CREATE TABLE order_info (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    order_no            VARCHAR(64)    UNIQUE NOT NULL       COMMENT '订单号',
    enterprise_id       BIGINT         NOT NULL              COMMENT '下单企业ID',
    user_id             BIGINT         NOT NULL              COMMENT '下单用户ID',
    order_type          TINYINT        NOT NULL              COMMENT '订单类型: 1-单份报告 2-批量报告 3-套餐 4-会员 5-充值',
    goods_name          VARCHAR(255)   NOT NULL              COMMENT '商品名称',
    total_amount        DECIMAL(12,2)  NOT NULL              COMMENT '订单总金额',
    discount_amount     DECIMAL(12,2)  NOT NULL DEFAULT 0   COMMENT '优惠金额',
    coupon_amount       DECIMAL(12,2)  NOT NULL DEFAULT 0   COMMENT '优惠券抵扣',
    pay_amount          DECIMAL(12,2)  NOT NULL              COMMENT '实付金额',
    pay_method          TINYINT                                COMMENT '支付方式: 1-微信 2-支付宝 3-余额',
    pay_status          TINYINT        NOT NULL DEFAULT 0    COMMENT '支付状态: 0-待支付 1-已支付 2-已退款',
    order_status        TINYINT        NOT NULL DEFAULT 1   COMMENT '订单状态: 1-待支付 2-已支付 3-已完成 4-已取消',
    transaction_id      VARCHAR(128)                           COMMENT '第三方交易流水号',
    pay_time            DATETIME                                COMMENT '支付时间',
    expire_time         DATETIME       NOT NULL              COMMENT '订单过期时间',
    create_time         DATETIME       NOT NULL              COMMENT '创建时间',
    update_time         DATETIME       NOT NULL              COMMENT '更新时间',
    INDEX uk_order_no (order_no),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_order_status (order_status),
    INDEX idx_pay_status (pay_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- -------------------------------------------
-- 6. credit_score (信用评分表)
-- -------------------------------------------
DROP TABLE IF EXISTS credit_score;
CREATE TABLE credit_score (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    enterprise_id       BIGINT         NOT NULL              COMMENT '企业ID',
    total_score         INT           NOT NULL              COMMENT '综合评分(0-1000)',
    credit_rating       VARCHAR(16)    NOT NULL              COMMENT '信用等级(AAA/AA/A/BBB/BB/B/CCC/CC/C)',
    model_version       VARCHAR(32)    NOT NULL              COMMENT '评估模型版本',
    basic_score         INT           NOT NULL              COMMENT '基础素质评分(满分200)',
    operation_score     INT           NOT NULL              COMMENT '经营能力评分(满分200)',
    finance_score       INT           NOT NULL              COMMENT '财务状况评分(满分250)',
    credit_record_score INT           NOT NULL              COMMENT '信用记录评分(满分200)',
    potential_score     INT           NOT NULL              COMMENT '发展潜力评分(满分150)',
    evaluation_time     DATETIME       NOT NULL              COMMENT '评估时间',
    expire_time         DATETIME       NOT NULL              COMMENT '评分有效期',
    create_time         DATETIME       NOT NULL              COMMENT '创建时间',
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_credit_rating (credit_rating),
    INDEX idx_total_score (total_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信用评分表';

-- -------------------------------------------
-- 7. sys_role (角色表)
-- -------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    role_code           VARCHAR(64)    UNIQUE NOT NULL       COMMENT '角色编码',
    role_name           VARCHAR(64)    NOT NULL              COMMENT '角色名称',
    description         VARCHAR(255)                           COMMENT '角色描述',
    status              TINYINT        NOT NULL DEFAULT 1   COMMENT '状态: 0-禁用 1-正常',
    create_time         DATETIME       NOT NULL              COMMENT '创建时间',
    update_time         DATETIME       NOT NULL              COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -------------------------------------------
-- 8. sys_menu (菜单表)
-- -------------------------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    parent_id           BIGINT         NOT NULL DEFAULT 0   COMMENT '父级ID，0为顶级',
    menu_name           VARCHAR(64)    NOT NULL              COMMENT '菜单名称',
    menu_type           TINYINT        NOT NULL              COMMENT '类型: 1-目录 2-菜单 3-按钮',
    path                VARCHAR(255)                           COMMENT '路由路径',
    component           VARCHAR(255)                           COMMENT '组件路径',
    perms               VARCHAR(255)                           COMMENT '权限标识',
    icon                VARCHAR(64)                            COMMENT '图标',
    sort                INT           NOT NULL DEFAULT 0    COMMENT '排序',
    visible             TINYINT        NOT NULL DEFAULT 1   COMMENT '是否显示: 0-隐藏 1-显示',
    status              TINYINT        NOT NULL DEFAULT 1   COMMENT '状态: 0-禁用 1-正常',
    create_time         DATETIME       NOT NULL              COMMENT '创建时间',
    update_time         DATETIME       NOT NULL              COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- -------------------------------------------
-- 9. sys_oper_log (操作日志表)
-- -------------------------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT    COMMENT '主键',
    enterprise_id       BIGINT                                  COMMENT '企业ID',
    user_id             BIGINT         NOT NULL              COMMENT '操作人ID',
    username            VARCHAR(64)    NOT NULL              COMMENT '操作人账号',
    module              VARCHAR(64)                             COMMENT '模块名称',
    operation           VARCHAR(255)                            COMMENT '操作描述',
    method              VARCHAR(255)                            COMMENT '请求方法',
    request_params      TEXT                                    COMMENT '请求参数',
    ip                  VARCHAR(64)                             COMMENT '操作IP',
    location            VARCHAR(255)                            COMMENT '操作地点',
    status              TINYINT        NOT NULL DEFAULT 1   COMMENT '状态: 1-正常 2-异常',
    error_msg           TEXT                                    COMMENT '错误信息',
    cost_time           BIGINT                                 COMMENT '耗时(毫秒)',
    create_time         DATETIME       NOT NULL              COMMENT '操作时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
