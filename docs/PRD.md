# Java Review Demo 项目 — PRD

## 1. 项目定位

脱产一年后的练手项目，目标是用**最精简的业务代码**跑通公司常用技术栈。不做鉴权、不做复杂业务逻辑，每个技术组件用一个可运行的 demo 来演示核心用法。

## 2. 业务场景

选择**商品管理 + 下单**作为业务载体，原因是它能自然串联所有技术组件：

- MySQL：商品表、订单表的 CRUD
- Redis：商品详情缓存、订单幂等键
- Elasticsearch：商品全文搜索
- Spring Cloud Gateway：统一入口路由
- Nacos：服务注册发现 + 配置中心
- OpenFeign：订单服务调用商品服务
- SkyWalking / Prometheus / Grafana：可观测性

## 3. 模块划分

```
java-review/
├── java-review-common      公共模块（Result 包装、全局异常、工具类）
├── java-review-product      商品服务（MySQL + Redis + ES）
├── java-review-order        订单服务（MySQL + Redis 幂等 + OpenFeign）
├── java-review-gateway      API 网关（Spring Cloud Gateway + Nacos 路由）
├── docker/                  Docker Compose 本地环境
├── deploy/                  K8s YAML（生产模拟）
├── sql/                     数据库初始化脚本
└── docs/                    文档
```

## 4. 技术栈覆盖清单

| 技术 | 版本 | 使用位置 | 演示点 |
|------|------|----------|--------|
| JDK | 17 | 全局 | record 做 DTO/VO、var、switch expression |
| Spring Boot | 3.2+ | 全局 | 自动配置、Starter 机制 |
| Spring Cloud | 2023.0.x | gateway、feign | Gateway 路由、OpenFeign 远程调用 |
| Spring Cloud Alibaba | 2023.0.x | 全局 | Nacos 注册发现 + 配置中心 |
| MySQL | 8.0 | product、order | MyBatis-Plus CRUD |
| Redis | 7 | product、order | 缓存读写、幂等键 |
| Elasticsearch | 8.x | product | 商品全文搜索 |
| Docker Compose | — | docker/ | 一键拉起 MySQL、Redis、ES、Nacos |
| K8s | — | deploy/ | Deployment + Service YAML |
| SkyWalking | 9.x | 全局 agent | 链路追踪 |
| Prometheus | — | 全局 actuator | 指标采集 |
| Grafana | — | docker/ | 仪表盘可视化 |

## 5. 核心功能列表

### 5.1 商品服务（java-review-product）

- `POST /product` — 新增商品（MySQL 写入）
- `GET /product/{id}` — 查询商品（Redis 缓存，未命中回源 MySQL）
- `GET /product/search?keyword=xxx` — 商品搜索（Elasticsearch）
- `PUT /product/{id}` — 更新商品（清除缓存 + 同步 ES）
- `DELETE /product/{id}` — 删除商品

### 5.2 订单服务（java-review-order）

- `POST /order` — 创建订单（Redis 幂等键防重复提交，OpenFeign 查商品信息）
- `GET /order/{id}` — 查询订单
- `GET /order/list` — 订单列表

### 5.3 网关（java-review-gateway）

- `/api/product/**` → 路由到 product 服务
- `/api/order/**` → 路由到 order 服务

## 6. 代码规范要点

- DTO（入参）/ DO（数据库实体）/ VO（出参）严格分离
- DTO 和 VO 使用 JDK 17 `record` 定义
- 全局异常处理器统一包装错误响应，禁止吞异常
- 关键逻辑配备 JUnit 5 + Mockito 单元测试
- 关键代码加上学习注释，说明"为什么这么做"
- 数据库操作考虑幂等性

## 7. 迭代计划

- **P0（MVP）**：项目骨架 + common 模块 + 商品 CRUD（MySQL + MyBatis-Plus）
- **P1**：Redis 缓存 + Elasticsearch 搜索
- **P2**：订单模块 + OpenFeign 调用
- **P3**：Spring Cloud Gateway + Nacos 注册发现
- **P4**：Docker Compose 一键环境 + 监控接入
- **P5**：K8s YAML + 压测验证
