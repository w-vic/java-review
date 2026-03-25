# K8s Cheat Sheet（java-review）

仅保留实操最常用命令，默认命名空间为 `java-review`。

## 1) 部署与清理

```bash
# 部署全部资源
kubectl apply -k k8s/overlays/local

# 删除全部资源
kubectl delete -k k8s/overlays/local
```

## 2) 快速查看资源

```bash
kubectl get ns
kubectl get pods -n java-review -o wide
kubectl get svc -n java-review
kubectl get ingress -n java-review
kubectl get pvc -n java-review
kubectl get hpa -n java-review
kubectl get pdb -n java-review
```

## 3) 看日志与事件（排障第一步）

```bash
# 看网关日志
kubectl logs -n java-review deployment/java-review-gateway

# 持续跟日志
kubectl logs -f -n java-review deployment/java-review-order

# 看 Pod 详细信息（包含事件、探针失败、调度失败）
kubectl describe pod -n java-review <pod-name>

# 全局事件时间线
kubectl get events -n java-review --sort-by=.metadata.creationTimestamp
```

## 4) 进入容器排查

```bash
kubectl exec -it -n java-review <pod-name> -- sh
```

可在容器内做：

```bash
# DNS 连通性（示例）
nc -zv mysql 3306
nc -zv nacos 8848
```

## 5) 端口转发（本地联调最实用）

```bash
# 网关
kubectl port-forward -n java-review svc/java-review-gateway 8080:8080

# Nacos 控制台
kubectl port-forward -n java-review svc/nacos 8848:8848
```

联调请求：

```bash
curl http://localhost:8080/api/product/1
curl http://localhost:8080/api/order/list
```

## 6) 扩缩容与发布

```bash
# 手动扩容
kubectl scale deployment/java-review-gateway --replicas=2 -n java-review

# 查看 rollout 状态
kubectl rollout status deployment/java-review-product -n java-review

# 修改镜像（触发滚动发布）
kubectl set image deployment/java-review-product \
  java-review-product=java-review-product:latest \
  -n java-review

# 回滚到上一个版本
kubectl rollout undo deployment/java-review-product -n java-review
```

## 7) HPA 相关

```bash
kubectl get hpa -n java-review
kubectl describe hpa java-review-gateway -n java-review
kubectl top pods -n java-review
kubectl top nodes
```

## 8) 常用筛选技巧

```bash
# 只看不 Ready 的 Pod
kubectl get pods -n java-review | rg -v "Running|Completed"

# 查看某个 label 下的 Pod
kubectl get pods -n java-review -l app=java-review-order
```

## 9) 常见故障速断

- `ImagePullBackOff`：镜像不在节点上（kind/minikube 需导入镜像）。
- `CrashLoopBackOff`：先看 `kubectl logs`，再看 `kubectl describe pod`。
- `Pending`：通常是资源不足或 PVC 未绑定。
- 路由 5xx：先查网关日志，再查 Nacos 注册是否存在目标服务。
