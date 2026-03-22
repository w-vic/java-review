项目结构

java-review/
├── pom.xml                          # 父 POM（版本管理 + 模块声明）
├── .gitignore
├── docs/PRD.md                      # 产品需求文档
├── sql/init.sql                     # MySQL 建表 + 测试数据
├── docker/
│   ├── docker-compose.yml           # 一键拉起 MySQL、Redis、ES、Nacos、Prometheus、Grafana
│   └── prometheus.yml               # Prometheus 采集配置
│
├── java-review-common/              # 公共模块
│   └── result/Result.java           #   统一响应包装
│   └── result/ErrorCode.java        #   错误码枚举
│   └── exception/BizException.java  #   业务异常
│   └── exception/GlobalExceptionHandler.java  # 全局异常拦截
│
├── java-review-product/             # 商品服务（:8081）
│   ├── model/entity/ProductDO.java  #   DO — 数据库实体
│   ├── model/dto/ProductCreateDTO.java  #   DTO — record 入参
│   ├── model/vo/ProductVO.java      #   VO — record 出参
│   ├── mapper/ProductMapper.java    #   MyBatis-Plus CRUD
│   ├── service/impl/ProductServiceImpl.java  # Redis 缓存 + ES 搜索
│   ├── es/ProductDocument.java      #   ES 文档映射
│   ├── es/ProductSearchService.java #   ES 搜索服务
│   └── controller/ProductController.java
│
├── java-review-order/               # 订单服务（:8082）
│   ├── model/ (entity + dto + vo)   #   DO/DTO/VO 分离
│   ├── feign/ProductFeignClient.java  # OpenFeign 远程调用商品服务
│   ├── service/impl/OrderServiceImpl.java  # Redis SETNX 幂等 + Feign 调用
│   └── controller/OrderController.java
│
└── java-review-gateway/             # API 网关（:8080）
    └── application.yml              #   路由：/api/product/** → product, /api/order/** → order