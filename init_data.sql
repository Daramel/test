-- =============================================
-- 企业征信管理平台 - 初始化数据
-- =============================================

USE credit_platform;

-- -------------------------------------------
-- 1. 初始化角色数据
-- -------------------------------------------
INSERT INTO sys_role (role_code, role_name, description, status, create_time, update_time) VALUES
('SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1, NOW(), NOW()),
('ENTERPRISE_LEGAL', '企业法人', '企业法定代表人', 1, NOW(), NOW()),
('ENTERPRISE_ADMIN', '企业管理员', '企业管理员', 1, NOW(), NOW()),
('FINANCE', '财务人员', '企业财务人员', 1, NOW(), NOW()),
('OPERATOR', '操作员', '普通操作员', 1, NOW(), NOW());

-- -------------------------------------------
-- 2. 初始化菜单数据
-- -------------------------------------------
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, create_time, update_time) VALUES
-- 一级菜单
(0, '工作台', 1, '/dashboard', NULL, NULL, 'dashboard', 0, 1, 1, NOW(), NOW()),
(0, '企业管理', 1, '/enterprise', NULL, NULL, 'enterprise', 1, 1, 1, NOW(), NOW()),
(0, '征信报告', 1, '/credit', NULL, NULL, 'credit', 2, 1, 1, NOW(), NOW()),
(0, '订单管理', 1, '/order', NULL, NULL, 'order', 3, 1, 1, NOW(), NOW()),
(0, '财务管理', 1, '/finance', NULL, NULL, 'finance', 4, 1, 1, NOW(), NOW()),
(0, '系统管理', 1, '/system', NULL, NULL, 'setting', 5, 1, 1, NOW(), NOW()),

-- 工作台二级菜单
(1, '首页', 2, '/dashboard/home', 'dashboard/home', NULL, 'home', 0, 1, 1, NOW(), NOW()),
(1, '数据统计', 2, '/dashboard/statistics', 'dashboard/statistics', NULL, 'statistics', 1, 1, 1, NOW(), NOW()),

-- 企业管理二级菜单
(2, '企业列表', 2, '/enterprise/list', 'enterprise/list', 'enterprise:list', 'list', 0, 1, 1, NOW(), NOW()),
(2, '企业认证', 2, '/enterprise/auth', 'enterprise/auth', 'enterprise:auth', 'auth', 1, 1, 1, NOW(), NOW()),
(2, '企业详情', 2, '/enterprise/detail', 'enterprise/detail', 'enterprise:detail', 'detail', 2, 0, 1, NOW(), NOW()),

-- 征信报告二级菜单
(3, '报告列表', 2, '/credit/list', 'credit/list', 'credit:list', 'list', 0, 1, 1, NOW(), NOW()),
(3, '报告购买', 2, '/credit/buy', 'credit/buy', 'credit:buy', 'buy', 1, 1, 1, NOW(), NOW()),
(3, '报告详情', 2, '/credit/detail', 'credit/detail', 'credit:detail', 'detail', 2, 0, 1, NOW(), NOW()),

-- 订单管理二级菜单
(4, '订单列表', 2, '/order/list', 'order/list', 'order:list', 'list', 0, 1, 1, NOW(), NOW()),
(4, '订单详情', 2, '/order/detail', 'order/detail', 'order:detail', 'detail', 1, 0, 1, NOW(), NOW()),
(4, '退款管理', 2, '/order/refund', 'order/refund', 'order:refund', 'refund', 2, 1, 1, NOW(), NOW()),

-- 财务管理二级菜单
(5, '账户余额', 2, '/finance/balance', 'finance/balance', 'finance:balance', 'balance', 0, 1, 1, NOW(), NOW()),
(5, '充值记录', 2, '/finance/recharge', 'finance/recharge', 'finance:recharge', 'recharge', 1, 1, 1, NOW(), NOW()),
(5, '消费记录', 2, '/finance/consume', 'finance/consume', 'finance:consume', 'consume', 2, 1, 1, NOW(), NOW()),
(5, '发票管理', 2, '/finance/invoice', 'finance/invoice', 'finance:invoice', 'invoice', 3, 1, 1, NOW(), NOW()),

-- 系统管理二级菜单
(6, '用户管理', 2, '/system/user', 'system/user', 'system:user', 'user', 0, 1, 1, NOW(), NOW()),
(6, '角色管理', 2, '/system/role', 'system/role', 'system:role', 'role', 1, 1, 1, NOW(), NOW()),
(6, '菜单管理', 2, '/system/menu', 'system/menu', 'system:menu', 'menu', 2, 1, 1, NOW(), NOW()),
(6, '操作日志', 2, '/system/log', 'system/log', 'system:log', 'log', 3, 1, 1, NOW(), NOW()),
(6, '系统配置', 2, '/system/config', 'system/config', 'system:config', 'config', 4, 1, 1, NOW(), NOW());

-- -------------------------------------------
-- 3. 初始化按钮权限（sys_menu按钮类型）
-- -------------------------------------------
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, create_time, update_time) VALUES
-- 企业管理按钮
(10, '查看', 3, NULL, NULL, 'enterprise:view', NULL, 0, 1, 1, NOW(), NOW()),
(10, '新增', 3, NULL, NULL, 'enterprise:add', NULL, 1, 1, 1, NOW(), NOW()),
(10, '编辑', 3, NULL, NULL, 'enterprise:edit', NULL, 2, 1, 1, NOW(), NOW()),
(10, '删除', 3, NULL, NULL, 'enterprise:del', NULL, 3, 1, 1, NOW(), NOW()),
(10, '审核', 3, NULL, NULL, 'enterprise:auth', NULL, 4, 1, 1, NOW(), NOW()),

-- 征信报告按钮
(13, '查看', 3, NULL, NULL, 'credit:view', NULL, 0, 1, 1, NOW(), NOW()),
(13, '下载', 3, NULL, NULL, 'credit:download', NULL, 1, 1, 1, NOW(), NOW()),
(13, '分享', 3, NULL, NULL, 'credit:share', NULL, 2, 1, 1, NOW(), NOW()),

-- 订单管理按钮
(16, '查看', 3, NULL, NULL, 'order:view', NULL, 0, 1, 1, NOW(), NOW()),
(16, '退款', 3, NULL, NULL, 'order:refund', NULL, 1, 1, 1, NOW(), NOW()),

-- 用户管理按钮
(26, '查看', 3, NULL, NULL, 'system:user:view', NULL, 0, 1, 1, NOW(), NOW()),
(26, '新增', 3, NULL, NULL, 'system:user:add', NULL, 1, 1, 1, NOW(), NOW()),
(26, '编辑', 3, NULL, NULL, 'system:user:edit', NULL, 2, 1, 1, NOW(), NOW()),
(26, '删除', 3, NULL, NULL, 'system:user:del', NULL, 3, 1, 1, NOW(), NOW()),
(26, '重置密码', 3, NULL, NULL, 'system:user:resetPwd', NULL, 4, 1, 1, NOW(), NOW()),

-- 角色管理按钮
(27, '查看', 3, NULL, NULL, 'system:role:view', NULL, 0, 1, 1, NOW(), NOW()),
(27, '新增', 3, NULL, NULL, 'system:role:add', NULL, 1, 1, 1, NOW(), NOW()),
(27, '编辑', 3, NULL, NULL, 'system:role:edit', NULL, 2, 1, 1, NOW(), NOW()),
(27, '删除', 3, NULL, NULL, 'system:role:del', NULL, 3, 1, 1, NOW(), NOW()),
(27, '分配权限', 3, NULL, NULL, 'system:role:perm', NULL, 4, 1, 1, NOW(), NOW());
