# Java Review Demo — 操作指南

## 1. 环境准备

### 1.1 本机依赖

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | 必须，项目使用 record、var、switch expression |
| Maven | 3.8+ | 构建工具 |
| MySQL | 8.0.30 | 本机安装，端口 3306 |
| Redis | 8.0.3 | 本机安装，端口 6379 |

### 1.2 Docker 容器（ES、Nacos、Prometheus、Grafana）

```bash
cd docker
docker-compose up -d
```

启动后验证各组件是否正常：

```bash
# Elasticsearch
curl http://localhost:9200
# 期望返回 JSON，包含 version.number = "8.10.2"

# Nacos 控制台
# 浏览器打开 http://localhost:8848/nacos/  用户名/密码: nacos/nacos

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

**创建订单（带幂等键）**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"ORD20260322001","productId":1,"quantity":2}'
```

**重复提交同一 orderNo（验证幂等拦截）**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"ORD20260322001","productId":1,"quantity":2}'
# 期望返回: {"code":30002,"message":"重复提交，请勿重复下单","data":null}
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
