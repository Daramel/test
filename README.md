# 企业征信管理平台

基于 Spring Boot + Vue3 的企业征信管理SaaS平台。

## 项目简介

企业征信管理平台是一个提供企业信用查询、信用评估、风险预警等一站式征信服务的SaaS平台。

### 核心特性

- 企业认证与数据授权签署
- 信用评分与等级评估
- 征信报告生成与下载
- 报告授权管理（企业间互查）
- 订单支付（微信/支付宝）
- 会员体系与优惠券
- 风险预警与黑名单

## 技术栈

### 后端
- Spring Boot 3.2.5
- Spring Security + JWT
- MyBatis-Plus 3.5.6
- Redis
- RabbitMQ
- Thymeleaf + Flying Saucer (PDF生成)

### 前端
- Vue 3 + TypeScript
- Element Plus
- Pinia (状态管理)
- Vue Router
- Axios
- ECharts

### 数据库
- MySQL 8.0
- Redis 7.0

## 项目结构

```
├── credit-platform/          # 后端项目 (Spring Boot)
│   ├── src/main/java/com/credit/
│   │   ├── common/          # 通用类
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 实体类
│   │   ├── mapper/          # 数据访问层
│   │   ├── pay/             # 支付模块
│   │   ├── security/         # 安全认证
│   │   ├── service/         # 业务逻辑
│   │   └── util/            # 工具类
│   └── src/main/resources/
│       ├── templates/        # 报告模板
│       └── application.yml   # 配置文件
│
├── enterprise-credit-platform/ # 前端项目 (Vue3)
│   └── src/
│       ├── api/             # API接口
│       ├── components/       # 公共组件
│       ├── router/          # 路由配置
│       ├── store/           # 状态管理
│       ├── types/           # TypeScript类型
│       ├── utils/           # 工具函数
│       └── views/           # 页面组件
│
└── SQL/                     # 数据库脚本
    ├── init_database.sql    # 创建数据库
    ├── init_tables.sql      # 创建表结构
    └── init_data.sql        # 初始化数据
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+

### 1. 初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行SQL脚本
source /workspace/init_database.sql;
source /workspace/init_tables.sql;
source /workspace/init_data.sql;
```

### 2. 启动后端服务

```bash
cd credit-platform

# 修改配置文件中的数据库连接信息
# 编辑 src/main/resources/application.yml

# 构建项目
mvn clean package -DskipTests

# 运行
java -jar target/credit-platform.jar

# 或开发模式运行
mvn spring-boot:run
```

后端服务启动后，访问 http://localhost:8080/doc.html 查看API文档。

### 3. 启动前端服务

```bash
cd enterprise-credit-platform

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

前端服务启动后，访问 http://localhost:5173

### 4. 默认账号

| 角色 | 用户名 | 密码 |
|-----|-------|------|
| 超级管理员 | admin | admin123 |

## 功能模块

### 认证授权模块
- [x] 企业用户注册/登录
- [x] JWT Token认证
- [x] 企业认证申请/审核
- [x] 数据授权协议签署

### 企业信息模块
- [x] 企业基本信息查询
- [x] 股东信息查询
- [x] 高管信息查询
- [x] 信用评分查询

### 报告生成模块
- [x] 简版报告生成
- [x] PDF渲染与水印
- [x] 报告预览/下载
- [x] 报告列表管理

### 订单支付模块
- [x] 订单创建与管理
- [x] 微信支付（模拟）
- [x] 支付宝支付（模拟）
- [x] 余额支付
- [x] 退款申请

### 报告授权模块
- [ ] 授权其他企业查看
- [ ] 查看被授权报告
- [ ] 授权撤销/管理

### 风控预警模块
- [ ] 风险事件监控
- [ ] 预警规则配置
- [ ] 预警通知

## API接口

### 认证接口 `/api/auth`
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /register | 用户注册 |
| POST | /login | 用户登录 |
| POST | /refresh | 刷新Token |
| POST | /logout | 退出登录 |

### 授权协议接口 `/api/agreement`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /latest | 获取最新协议 |
| GET | /{version} | 获取指定版本协议 |

### 授权签署接口 `/api/sign`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /status | 检查签署状态 |
| POST | / | 签署授权协议 |
| GET | /record | 获取签署记录 |
| POST | /revoke | 撤回授权 |

### 信用评分接口 `/api/score`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /{enterpriseId} | 获取企业评分 |
| GET | /{enterpriseId}/trend | 获取评分趋势 |

### 报告接口 `/api/report`
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /create | 创建报告 |
| GET | /{id} | 获取报告详情 |
| GET | /{id}/preview | 预览报告 |
| GET | /{id}/download | 下载报告 |
| GET | /{id}/progress | 获取生成进度 |
| GET | /list | 获取报告列表 |

### 订单接口 `/api/order`
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /create | 创建订单 |
| GET | /{id} | 获取订单详情 |
| GET | /list | 获取订单列表 |
| POST | /{id}/cancel | 取消订单 |
| POST | /{id}/refund | 申请退款 |

### 支付接口 `/api/pay`
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /create | 发起支付 |
| GET | /{orderNo}/status | 查询支付状态 |
| POST | /wechat/notify | 微信回调 |
| POST | /alipay/notify | 支付宝回调 |
| POST | /balance | 余额支付 |

### 余额接口 `/api/balance`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 获取余额信息 |
| POST | /recharge | 余额充值 |
| GET | /log | 获取余额流水 |

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/credit_platform
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379

jwt:
  secret: your_jwt_secret_key
  expiration: 7200000  # 2小时

report:
  storage:
    path: /data/reports/
  watermark:
    enabled: true
    text: 仅供内部使用

pay:
  wechat:
    appid: your_appid
    mchid: your_mchid
```

### 前端配置

前端API基础路径配置在 `src/api/index.ts`:
```typescript
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 30000
});
```

## 开发说明

### 项目模块划分

按照DDD（领域驱动设计）思想，项目采用以下模块划分：

- `common` - 通用工具类、响应封装、异常处理
- `config` - Spring配置类
- `entity` - 数据实体，与数据库表对应
- `mapper` - 数据访问层
- `service` - 业务逻辑层
- `controller` - 接口层
- `dto` - 数据传输对象
- `security` - 安全认证相关
- `pay` - 支付模块（策略模式）

### 命名规范

- Entity类：大驼峰命名，与数据库表名对应
- Mapper接口：以 `Mapper` 结尾
- Service接口：以 `Service` 结尾
- Service实现：以 `ServiceImpl` 结尾
- Controller：以 `Controller` 结尾
- DTO/VO：以 `Request`/`VO` 结尾

### API响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1719388800000
}
```

## 数据库表

| 表名 | 说明 |
|-----|------|
| sys_user | 用户表 |
| sys_enterprise | 企业表 |
| auth_sign_record | 授权签署记录表 |
| credit_score | 信用评分表 |
| credit_report | 征信报告表 |
| report_template | 报告模板表 |
| report_grant | 报告授权表 |
| order_info | 订单表 |
| payment_record | 支付记录表 |
| refund_record | 退款记录表 |
| balance_log | 余额流水表 |
| coupon | 优惠券表 |
| user_coupon | 用户优惠券表 |
| sys_role | 角色表 |
| sys_menu | 菜单表 |
| sys_oper_log | 操作日志表 |

## License

MIT License
