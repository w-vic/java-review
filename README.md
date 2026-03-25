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

## Kubernetes 部署与调试流程（分布式学习版）

你当前是“`MySQL` 和 `Redis` 在本机，其他组件在 Docker”。如果要进入完整分布式学习，建议按下面流程迁移到 **K8s 全量部署**（业务服务 + 中间件都在集群内）。

项目 `k8s/` 已包含以下组件编排：

- 业务服务：`java-review-gateway`、`java-review-product`、`java-review-order`
- 中间件：`MySQL`、`Redis`、`Nacos`、`Elasticsearch`
- 流量与弹性：`Ingress`、`HPA`、`PDB`

### 1. 前置准备

```bash
# 1) K8s 集群可用（kind/minikube/docker-desktop-k8s 均可）
kubectl cluster-info

# 2) 可选：确认 metrics-server（HPA 依赖）
kubectl top nodes
```

> 如果 `kubectl top` 报错，说明 metrics-server 尚未就绪，HPA 不会生效，但不影响基础部署学习。

### 2. 打包并构建业务镜像

```bash
mvn clean package -DskipTests

docker build -t java-review-product:latest ./java-review-product
docker build -t java-review-order:latest ./java-review-order
docker build -t java-review-gateway:latest ./java-review-gateway
```

如果使用 kind，需要把本地镜像导入集群节点：

```bash
kind load docker-image java-review-product:latest
kind load docker-image java-review-order:latest
kind load docker-image java-review-gateway:latest
```

### 3. 一键部署到 K8s

```bash
kubectl apply -k k8s/overlays/local
```

### 4. 按顺序验收（非常关键）

先看基础资源是否拉起：

```bash
kubectl get pods -n java-review
kubectl get svc -n java-review
kubectl get pvc -n java-review
kubectl get ingress -n java-review
```

推荐验收顺序：

1. `MySQL` / `Redis` / `Nacos` / `Elasticsearch` 先 `Running`
2. `java-review-product`、`java-review-order` 完成注册并启动
3. `java-review-gateway` 路由可用

### 5. 联调入口（优先用端口转发）

先把网关映射到本机：

```bash
kubectl port-forward -n java-review svc/java-review-gateway 8080:8080
```

再做接口验证：

```bash
curl http://localhost:8080/api/product/1
curl http://localhost:8080/api/order/list
```

如需确认服务注册，可转发 Nacos 控制台：

```bash
kubectl port-forward -n java-review svc/nacos 8848:8848
```

浏览器访问 `http://localhost:8848/nacos/`，检查是否存在 `java-review-product` 与 `java-review-order` 实例。

### 6. 调试闭环（从现象到根因）

当请求失败或 Pod 异常时，按这个顺序排查：

```bash
# 1) 看 Pod 状态
kubectl get pods -n java-review -o wide

# 2) 看日志（网关/业务服务）
kubectl logs -n java-review deployment/java-review-gateway
kubectl logs -n java-review deployment/java-review-product
kubectl logs -n java-review deployment/java-review-order

# 3) 看事件与探针失败信息
kubectl describe pod -n java-review <pod-name>
kubectl get events -n java-review --sort-by=.metadata.creationTimestamp
```

典型故障快速定位：

- `ImagePullBackOff`：镜像未导入到集群节点（kind/minikube 常见）
- `CrashLoopBackOff`：优先看应用日志和环境变量注入（ConfigMap/Secret）
- `Pending`：资源不足或 `PVC` 绑定失败
- 网关 5xx：先看网关日志，再查 Nacos 注册是否完整

### 7. 发布、回滚与弹性验证（进阶学习）

```bash
# 滚动发布
kubectl set image deployment/java-review-product \
  java-review-product=java-review-product:latest \
  -n java-review
kubectl rollout status deployment/java-review-product -n java-review

# 回滚
kubectl rollout undo deployment/java-review-product -n java-review

# 查看 HPA/PDB
kubectl get hpa -n java-review
kubectl get pdb -n java-review
```

### 8. 清理环境

```bash
kubectl delete -k k8s/overlays/local
```

更多命令速查见 [k8s/cheatsheet.md](k8s/cheatsheet.md)，K8s 文件说明见 [k8s/README.md](k8s/README.md)。

---

## 项目结构

```text
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
