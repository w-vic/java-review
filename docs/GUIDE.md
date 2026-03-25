# Java Review Demo — 操作指南

## 1. 环境准备

### 1.1 本机依赖

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | 必须，项目使用 record、var、switch expression |
| Maven | 3.8+ | 构建工具 |
| MySQL | 8.0.30 | 本机安装，端口 3306 |
| Redis | 8.0.3 | 本机安装，端口 6379 |

### 1.2 Docker 容器（ES、Kibana、Nacos、RocketMQ、Dashboard、Prometheus、Grafana）

```bash
cd docker
docker-compose up -d
```

说明：**当前 demo 的订单/商品服务未连接 RocketMQ**，库存扣减在「订单付款」时由 **OpenFeign 同步调用**商品接口完成。Compose 里的 RocketMQ 与 Dashboard 仍可按需启动，便于本地练习或自行扩展异步场景。

启动后验证各组件是否正常：

```bash
# Elasticsearch
curl http://localhost:9200
# 期望返回 JSON，包含 version.number = "8.10.2"

# Kibana（与 ES 8.10.2 同版本，官方可视化：Discover / Dev Tools 等）
# 浏览器打开 http://localhost:5601 （首次启动约需 1～2 分钟）
# Dev Tools 中可执行：GET /product/_search

# Nacos 控制台
# 浏览器打开 http://localhost:8848/nacos/  用户名/密码: nacos/nacos

# RocketMQ NameServer
curl -s telnet://localhost:9876 || echo "9876 端口已监听"
# Broker 日志确认启动成功
docker logs java-review-broker 2>&1 | tail -3

# RocketMQ Dashboard（Topic、消费者、消息轨迹等）
# 浏览器打开 http://localhost:18082

# Prometheus
# 浏览器打开 http://localhost:9090

# Grafana
# 浏览器打开 http://localhost:3000  用户名/密码: admin/admin
```

### 1.3 初始化数据库

```bash
mysql -uroot -p12345678 < sql/init.sql
```

该脚本会创建 `java_review` 数据库、`product` 和 `orders` 表，并插入 3 条测试商品数据。

## 2. 启动服务

三个服务需按顺序启动：

```
1. java-review-product  (端口 8081)  — 商品服务
2. java-review-order    (端口 8082)  — 订单服务（依赖商品服务）
3. java-review-gateway  (端口 8080)  — API 网关（路由到上面两个服务）
```

### 方式一：IDE 启动（推荐开发阶段）

分别运行以下启动类的 `main` 方法：

- `com.javareview.product.ProductApplication`
- `com.javareview.order.OrderApplication`
- `com.javareview.gateway.GatewayApplication`

### 方式二：命令行启动

```bash
# 先编译整个项目
mvn clean package -DskipTests

# 启动商品服务
java -jar java-review-product/target/java-review-product-1.0.0-SNAPSHOT.jar &

# 启动订单服务
java -jar java-review-order/target/java-review-order-1.0.0-SNAPSHOT.jar &

# 启动网关
java -jar java-review-gateway/target/java-review-gateway-1.0.0-SNAPSHOT.jar &
```

### 启动验证

```bash
# 检查服务健康状态
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8080/actuator/health
# 期望返回 {"status":"UP"}
```

## 3. 接口测试

### 3.1 商品服务（直接访问 :8081 或通过网关 :8080/api）

**新增商品**
```bash
curl -X POST http://localhost:8081/product \
  -H "Content-Type: application/json" \
  -d '{"name":"Redis 设计与实现","description":"深入理解 Redis 内部机制","price":69.00,"stock":80}'
```

**查询商品（走 Redis 缓存）**
```bash
curl http://localhost:8081/product/1
```

**商品搜索（走 Elasticsearch）**
```bash
# 英文关键词可直接拼接
curl "http://localhost:8081/product/search?keyword=Java"

# 中文关键词需要 URL 编码，使用 -G + --data-urlencode
curl -G "http://localhost:8081/product/search" --data-urlencode "keyword=编程"
```

**更新商品**
```bash
curl -X PUT http://localhost:8081/product/1 \
  -H "Content-Type: application/json" \
  -d '{"price":99.00}'
```

**删除商品**
```bash
curl -X DELETE http://localhost:8081/product/4
```

### 3.2 订单服务

**查询商品库存（Feign 调用入口）**
```bash
curl http://localhost:8081/product/1/stock
# 返回: {"code":200,"message":"success","data":100}
```

**创建订单（待支付：仅 Feign 校验库存，不扣库存）**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"ORD20260322001","productId":1,"quantity":2}'
# 返回订单 JSON，statusDesc 为「待支付」；此时库存尚未扣减
```

**确认创建后库存未变**
```bash
curl http://localhost:8081/product/1/stock
# 仍为下单前的数量（例如 100）
```

**付款（Feign 同步扣减库存，并发下对订单行 SELECT ... FOR UPDATE）**
```bash
# 将 {id} 换为上一请求返回的订单 id
curl -X POST http://localhost:8082/order/{id}/pay
# 商品服务日志会出现：库存扣减成功 ...
```

**验证付款后库存已扣减**
```bash
curl http://localhost:8081/product/1/stock
# 例如购买 2 件：由 100 变为 98
```

**再次付款同一订单（幂等：已支付则直接返回）**
```bash
curl -X POST http://localhost:8082/order/{id}/pay
```

**待支付时修改数量、取消、按单号查询、删除**
```bash
curl -X PUT http://localhost:8082/order/{id} -H "Content-Type: application/json" -d '{"quantity":1}'
curl -X POST http://localhost:8082/order/{id}/cancel
curl http://localhost:8082/order/by-no/ORD20260322001
curl -X DELETE http://localhost:8082/order/{id}
# 注意：已支付订单不可删除（返回 30005）
```

**库存不足时下单（验证 Feign 库存校验）**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"ORD-BIGORDER","productId":1,"quantity":99999}'
# 返回: {"code":20002,"message":"库存不足","data":null}
```

**重复提交同一 orderNo（验证幂等拦截）**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"ORD20260322001","productId":1,"quantity":2}'
# 返回: {"code":30002,"message":"重复提交，请勿重复下单","data":null}
```

**查询订单**
```bash
curl http://localhost:8082/order/1
```

**订单列表**
```bash
curl http://localhost:8082/order/list
```

### 3.3 通过网关访问（统一入口 :8080）

```bash
# 网关会将 /api/product/** 路由到商品服务，去掉 /api 前缀
curl http://localhost:8080/api/product/1

# 网关路由到订单服务
curl http://localhost:8080/api/order/list
```

## 4. 监控配置

### 4.1 Nacos 控制台

- 地址：http://localhost:8848/nacos/
- 账号：nacos / nacos
- 服务列表 → 可以看到 `java-review-product`、`java-review-order`、`java-review-gateway` 三个服务实例

### 4.2 Prometheus

- 地址：http://localhost:9090
- 检查采集目标：打开 Status → Targets
- 三个 job（java-review-product / order / gateway）状态应显示为 **UP**（需要先启动 Spring Boot 服务）
- 示例查询：在查询框输入 `jvm_memory_used_bytes`，点击 Execute 查看 JVM 内存指标

### 4.3 Grafana 添加数据源

1. 打开 http://localhost:3000，用 admin/admin 登录
2. 左侧菜单 → Configuration → Data Sources → Add data source
3. 选择 **Prometheus**
4. URL 填写：`http://java-review-prometheus:9090`（容器间通过容器名互访）
5. 点击 Save & Test，显示 "Data source is working" 即成功

### 4.4 Grafana 导入 JVM 仪表盘

1. 左侧菜单 → Dashboards → Import
2. 在 "Import via grafana.com" 输入框填入 Dashboard ID：`4701`（JVM Micrometer Dashboard）
3. 点击 Load → 选择 Prometheus 数据源 → Import
4. 即可看到 JVM 内存、GC、线程等可视化面板

### 4.5 RocketMQ

- NameServer 端口：`localhost:9876`
- Broker 端口：`localhost:10911`
- **Dashboard**：`http://localhost:18082`（容器 `rocketmq-dashboard`，已指向 compose 内 NameServer）
- 验证 Broker 是否注册到 NameServer：

```bash
# 进入 broker 容器查看集群信息
docker exec java-review-broker sh mqadmin clusterList -n rocketmq-namesrv:9876
```

业务侧库存与订单的关系（当前实现）：
1. **创建订单**：仅校验库存是否足够，订单状态为待支付，**不扣库存**
2. **付款**：订单服务 `POST /order/{id}/pay`，在事务内对订单行加锁后，通过 Feign 调用 `POST /product/{id}/stock/deduct` 扣减 `quantity`
3. 若需练习 RocketMQ，可使用 compose 中的 Broker + Dashboard；应用代码中的异步扣减已改为上述同步链路

## 5. 常见问题

### Q: 端口被占用怎么办？

```bash
# 查看占用端口的进程
lsof -i :8081
# 杀掉进程
kill -9 <PID>
```

### Q: Nacos 启动后服务注册不上？

检查 Nacos 是否正常运行：
```bash
curl http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10
```

### Q: ES 商品搜索没有结果？

`sql/init.sql` 的测试数据是直接插入 MySQL 的，不会自动同步到 ES。只有通过 `POST /product` 接口创建的商品才会写入 ES 索引。

先通过 API 新增一条商品，然后再搜索：
```bash
curl -X POST http://localhost:8081/product \
  -H "Content-Type: application/json" \
  -d '{"name":"Redis 设计与实现","description":"深入理解 Redis 内部机制","price":69.00,"stock":80}'

curl "http://localhost:8081/product/search?keyword=Redis"
```

检查 ES 索引内的所有文档：
```bash
curl http://localhost:9200/product/_search?pretty
```

### Q: Prometheus Targets 显示 DOWN？

确认 Spring Boot 服务已启动，且 Actuator 端点已暴露。验证方式：
```bash
curl http://localhost:8081/actuator/prometheus
# 应返回大量 metrics 文本
```

## 6. Kubernetes 部署（MySQL/Redis 本机 + 其余组件在 kind 集群）

当前架构：MySQL 和 Redis 保持在宿主机运行，Nacos、Elasticsearch 和三个业务服务部署到 kind 集群内。
Pod 通过 `host.docker.internal` 访问宿主机数据库。

```
┌─────────────────────────────────────────────────────────┐
│  宿主机 macOS                                           │
│  ┌──────────┐  ┌──────────┐                             │
│  │ MySQL    │  │ Redis    │                             │
│  │ :3306    │  │ :6379    │                             │
│  └────▲─────┘  └────▲─────┘                             │
│       │ host.docker.internal                            │
│  ┌────┼──────────────┼──────────────────────────────┐   │
│  │  kind 集群 (java-review namespace)               │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────────┐  │   │
│  │  │ Nacos    │ │ ES 8.10  │ │ Gateway :8080    │  │   │
│  │  │ :8848    │ │ :9200    │ │ Product :8081    │  │   │
│  │  │          │ │          │ │ Order   :8082    │  │   │
│  │  └──────────┘ └──────────┘ └──────────────────┘  │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 6.1 前置条件

| 组件 | 说明 |
|------|------|
| Docker Desktop | 建议分配 6GB+ 内存（Settings → Resources） |
| kind | `brew install kind`，用于创建本地 K8s 集群 |
| kubectl | `brew install kubectl`，K8s 命令行工具 |
| MySQL | 宿主机已运行，端口 3306，已执行 `sql/init.sql` |
| Redis | 宿主机已运行，端口 6379 |

创建 kind 集群（如已有可跳过）：

```bash
kind create cluster --name java-review
kubectl cluster-info
```

### 6.2 停止 Docker Compose（重要）

Docker Desktop 的 VM 内存有限，kind 集群与 Docker Compose 同时运行会导致资源不足（Nacos/ES 镜像卡在 ContainerCreating）。
**部署 K8s 前必须先停止 Docker Compose：**

```bash
cd docker && docker-compose down && cd ..
```

切回 Docker Compose 模式时再 `docker-compose up -d` 即可。

### 6.3 构建镜像并导入 kind

```bash
# 1) 编译 JAR
mvn clean package -DskipTests

# 2) 构建业务服务镜像
docker build -t java-review-product:latest ./java-review-product
docker build -t java-review-order:latest ./java-review-order
docker build -t java-review-gateway:latest ./java-review-gateway

# 3) 将所有镜像导入 kind 节点（包括中间件镜像）
kind load docker-image \
  java-review-product:latest \
  java-review-order:latest \
  java-review-gateway:latest \
  nacos/nacos-server:v2.3.2-slim \
  docker.elastic.co/elasticsearch/elasticsearch:8.10.2 \
  --name java-review
```

> 如果本地没有 Nacos/ES 镜像，先手动拉取：
> `docker pull nacos/nacos-server:v2.3.2-slim`
> `docker pull docker.elastic.co/elasticsearch/elasticsearch:8.10.2`

### 6.4 一键部署

```bash
kubectl apply -k k8s/overlays/local
```

部署的资源：

| 类型 | 资源 | 说明 |
|------|------|------|
| Namespace | `java-review` | 资源隔离 |
| ConfigMap | `java-review-config` | MySQL/Redis/ES/Nacos 地址 |
| Secret | `java-review-secret` | MySQL 密码 |
| Deployment | `nacos` | Nacos 注册中心（standalone + 内嵌 Derby） |
| StatefulSet | `elasticsearch` | ES 8.10.2 单节点 + PVC |
| Deployment | `java-review-product` | 商品服务 |
| Deployment | `java-review-order` | 订单服务 |
| Deployment | `java-review-gateway` | API 网关 |
| Ingress | `java-review-gateway` | 对外入口（需 ingress-nginx） |
| HPA | `java-review-gateway` | 自动扩缩（需 metrics-server） |
| PDB | gateway / product / order | 中断预算 |

### 6.5 等待就绪

```bash
# 持续观察 Pod 状态，全部 1/1 Running 即为成功（约 60～90 秒）
kubectl get pods -n java-review -w
```

预期输出：

```
NAME                                  READY   STATUS    AGE
elasticsearch-0                       1/1     Running   88s
java-review-gateway-xxxxx             1/1     Running   88s
java-review-order-xxxxx               1/1     Running   88s
java-review-product-xxxxx             1/1     Running   88s
nacos-xxxxx                           1/1     Running   88s
```

Nacos 和 ES 启动较慢（20～30 秒），业务服务会因等待 Nacos 注册而重启 1～2 次，属正常现象。

### 6.6 验证服务联通

通过 port-forward 将网关映射到本机端口：

```bash
kubectl port-forward -n java-review svc/java-review-gateway 9080:8080
```

> 使用 9080 而非 8080 是为了避免与本机可能残留的 Java 进程冲突。

在另一个终端测试接口：

```bash
# 商品查询
curl http://localhost:9080/api/product/1

# 订单列表
curl http://localhost:9080/api/order/list

# 网关健康检查
curl http://localhost:9080/actuator/health
```

验证 Nacos 服务注册：

```bash
kubectl port-forward -n java-review svc/nacos 9848:8848
# 浏览器打开 http://localhost:9848/nacos/
# 服务列表应显示 java-review-product、java-review-order、java-review-gateway
```

验证 Elasticsearch：

```bash
kubectl port-forward -n java-review svc/elasticsearch 9200:9200
curl http://localhost:9200
# 返回 ES 版本信息 "number": "8.10.2"
```

### 6.7 K8s 目录结构

```
k8s/
├── base/                          # 基础资源清单
│   ├── kustomization.yaml         # 资源入口
│   ├── namespace.yaml             # 命名空间
│   ├── configmap.yaml             # 环境变量（MySQL/Redis/ES/Nacos 地址）
│   ├── secret.yaml                # 敏感变量（MySQL 密码）
│   ├── nacos.yaml                 # Nacos（Service + Deployment）
│   ├── elasticsearch.yaml         # ES（Service + StatefulSet + PVC）
│   ├── product.yaml               # 商品服务（Service + Deployment）
│   ├── order.yaml                 # 订单服务（Service + Deployment）
│   ├── gateway.yaml               # 网关（Service + Deployment）
│   ├── ingress.yaml               # Ingress 入口
│   ├── hpa.yaml                   # 自动扩缩容
│   ├── pdb.yaml                   # 中断预算
│   ├── mysql.yaml                 # MySQL StatefulSet（当前未启用，供后续分布式学习）
│   └── redis.yaml                 # Redis StatefulSet（当前未启用，供后续分布式学习）
└── overlays/
    └── local/
        └── kustomization.yaml     # 本地环境：引用 base + patch ConfigMap
```

### 6.8 常用 K8s 操作

**查看资源：**

```bash
kubectl get pods,svc,pvc,hpa,pdb -n java-review
kubectl get events -n java-review --sort-by=.metadata.creationTimestamp
```

**查看日志：**

```bash
kubectl logs -n java-review deployment/java-review-gateway
kubectl logs -f -n java-review deployment/java-review-order
```

**进入容器排查网络：**

```bash
kubectl exec -it -n java-review <pod-name> -- sh
# 容器内测试：
# nc -zv host.docker.internal 3306
# nc -zv nacos 8848
```

**滚动发布与回滚：**

```bash
# 重新构建镜像后触发滚动发布
kubectl set image deployment/java-review-product \
  java-review-product=java-review-product:latest \
  -n java-review
kubectl rollout status deployment/java-review-product -n java-review

# 回滚
kubectl rollout undo deployment/java-review-product -n java-review
```

**手动扩缩容：**

```bash
kubectl scale deployment/java-review-gateway --replicas=2 -n java-review
```

**HPA / PDB：**

```bash
kubectl get hpa -n java-review
kubectl describe hpa java-review-gateway -n java-review
kubectl get pdb -n java-review
# HPA 需要 metrics-server，未安装时不影响基础部署
```

### 6.9 K8s 常见问题

**Q: Pod 一直 ContainerCreating？**

1. 检查是否已停止 Docker Compose（资源争抢是最常见原因）
2. 检查镜像是否已导入 kind：`docker exec java-review-control-plane crictl images | grep nacos`
3. 查看事件详情：`kubectl describe pod -n java-review <pod-name>`

**Q: 业务服务 CrashLoopBackOff？**

通常是 Nacos 尚未就绪。Nacos 启动需要 20～30 秒，业务服务会自动重试注册。
等 Nacos Pod 变为 `1/1 Running` 后，业务服务会自行恢复。如果持续不恢复：

```bash
kubectl logs -n java-review deployment/java-review-product --tail=50
```

**Q: 应用连不上 MySQL/Redis？**

确认宿主机 MySQL 和 Redis 正在运行：

```bash
mysql -uroot -p12345678 -e "SELECT 1"
redis-cli ping
```

确认 Pod 能访问宿主机（kind 基于 Docker Desktop）：

```bash
kubectl exec -it -n java-review <pod-name> -- sh -c "nc -zv host.docker.internal 3306"
```

**Q: Namespace 删除卡在 Terminating？**

强制移除 finalizer：

```bash
kubectl get ns java-review -o json | \
  python3 -c "import sys,json; ns=json.load(sys.stdin); ns['spec']['finalizers']=[]; print(json.dumps(ns))" | \
  kubectl replace --raw "/api/v1/namespaces/java-review/finalize" -f -
```

**Q: HPA 不生效？**

需要安装 metrics-server：

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
# 验证
kubectl top pods -n java-review
```

**Q: 如何在 Docker Compose 和 K8s 之间切换？**

两种模式使用同一套 MySQL/Redis（宿主机），但 Nacos 和 ES 是各自独立的实例，数据不互通。

```bash
# 切到 K8s 模式
cd docker && docker-compose down && cd ..
kubectl apply -k k8s/overlays/local

# 切到 Docker Compose 模式
kubectl delete -k k8s/overlays/local
cd docker && docker-compose up -d && cd ..
```

### 6.10 清理 K8s 环境

```bash
kubectl delete -k k8s/overlays/local
```

如需彻底删除 kind 集群：

```bash
kind delete cluster --name java-review
```
