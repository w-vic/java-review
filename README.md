# java-review

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-POM-blue?logo=apachemaven&logoColor=white)

基于 **Spring Boot 3**、**Spring Cloud** 与 **Spring Cloud Alibaba** 的多模块微服务练手项目，覆盖网关路由、服务发现、OpenFeign、MyBatis-Plus、Redis、Elasticsearch 等常见 Java 后端能力。

---

## 目录

- [功能概览](#功能概览)
- [技术栈](#技术栈)
- [模块与端口](#模块与端口)
- [快速开始](#快速开始)
- [文档](#文档)
- [项目结构](#项目结构)

---

## 功能概览

- **商品服务**：商品 CRUD、Redis 缓存、Elasticsearch 搜索
- **订单服务**：OpenFeign 调用商品服务、Redis `SETNX` 幂等等典型写法
- **API 网关**：统一入口，路由到各微服务
- **公共能力**：统一响应 `Result`、错误码、全局异常处理
- **本地基础设施**：可通过 Docker Compose 启动 ES、Kibana、Nacos、RocketMQ、Prometheus、Grafana 等（详见 [操作指南](docs/GUIDE.md)）

---

## 技术栈

| 类别 | 选型 |
|------|------|
| 语言 / 构建 | Java 17、Maven |
| 框架 | Spring Boot 3.2、Spring Cloud 2023.0.x、Spring Cloud Alibaba 2023.0.x |
| 数据访问 | MyBatis-Plus |
| 中间件 | MySQL、Redis、Elasticsearch、Nacos；Compose 中含 RocketMQ（当前 Demo 订单/商品未接入，可按文档扩展） |
| 可观测 | Spring Boot Actuator、Prometheus、Grafana |

---

## 模块与端口

| 模块 | 说明 | 默认端口 |
|------|------|----------|
| `java-review-gateway` | Spring Cloud Gateway | **8080** |
| `java-review-product` | 商品服务 | **8081** |
| `java-review-order` | 订单服务 | **8082** |
| `java-review-common` | 公共 DTO / 异常 / 统一响应 | 无独立进程 |

网关路由示例：`/api/product/**` → 商品服务，`/api/order/**` → 订单服务（以各模块 `application.yml` 为准）。

---

## 快速开始

### 1. 环境要求

- **JDK 17**
- **Maven 3.8+**
- **MySQL 8**、**Redis**（本机安装；端口与账号需与 `application.yml` 一致）

### 2. 基础设施（可选）

```bash
cd docker
docker-compose up -d
```

> 若 `docker-compose.yml` 中 Elasticsearch 数据目录为本机绝对路径，请按你的环境修改后再启动。

### 3. 初始化数据库

```bash
mysql -uroot -p < sql/init.sql
```

（默认示例账号见 [docs/GUIDE.md](docs/GUIDE.md)。）

### 4. 编译与启动

推荐启动顺序：**商品 → 订单 → 网关**。

```bash
mvn clean package -DskipTests
```

**IDE**：分别运行

- `com.javareview.product.ProductApplication`
- `com.javareview.order.OrderApplication`
- `com.javareview.gateway.GatewayApplication`

**命令行**（示例）：

```bash
java -jar java-review-product/target/java-review-product-1.0.0-SNAPSHOT.jar
java -jar java-review-order/target/java-review-order-1.0.0-SNAPSHOT.jar
java -jar java-review-gateway/target/java-review-gateway-1.0.0-SNAPSHOT.jar
```

### 5. 健康检查

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8080/actuator/health
```

更多接口示例（curl）、Nacos / ES / Grafana 访问方式见 **[docs/GUIDE.md](docs/GUIDE.md)**。

---

## 文档

| 文档 | 说明 |
|------|------|
| [docs/GUIDE.md](docs/GUIDE.md) | 环境、Docker、启动顺序、接口测试、常见问题 |
| [docs/PRD.md](docs/PRD.md) | 产品需求说明 |

---

## 项目结构

```
java-review/
├── pom.xml                      # 父 POM（版本与模块）
├── sql/init.sql                 # MySQL 建表与测试数据
├── docker/
│   ├── docker-compose.yml       # 本地 ES / Nacos / RocketMQ / 监控 等
│   └── prometheus.yml
├── docs/                        # 指南与 PRD
├── java-review-common/          # 公共模块（Result、异常处理等）
├── java-review-product/         # 商品服务
├── java-review-order/           # 订单服务
└── java-review-gateway/         # API 网关
```

---

