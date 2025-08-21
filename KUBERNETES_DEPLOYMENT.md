# Kubernetes Deployment Guide

This guide provides step-by-step instructions for deploying the microservices system on Kubernetes.

## Prerequisites

- Kubernetes cluster (v1.20+ recommended)
- kubectl configured and connected to your cluster
- Docker images built and pushed to a registry
- At least 4GB RAM and 2 CPU cores available in the cluster

## Quick Start

For a rapid deployment, execute these commands in order:

```bash
# 1. Create namespace
kubectl apply -f deploy/namespace.yaml

# 2. Create database secret
kubectl -n microservices create secret generic mysql-credentials \
  --from-literal=username=admin \
  --from-literal=password=secretpassword

# 3. Deploy database
kubectl apply -f deploy/mysql.yaml

# 4. Deploy service discovery
kubectl apply -f deploy/eureka-server.yaml

# 5. Wait for Eureka to be ready
kubectl -n microservices wait --for=condition=ready pod -l app=eureka-server --timeout=300s

# 6. Deploy business services
kubectl apply -f deploy/course-service.yaml
kubectl apply -f deploy/student-service.yaml
kubectl apply -f deploy/address-service.yaml

# 7. Wait for services to be ready
kubectl -n microservices wait --for=condition=ready pod -l app=course-service --timeout=300s
kubectl -n microservices wait --for=condition=ready pod -l app=student-service --timeout=300s
kubectl -n microservices wait --for=condition=ready pod -l app=address-service --timeout=300s

# 8. Deploy gateway
kubectl apply -f deploy/gateway.yaml

# 9. Create external access
kubectl apply -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: gateway-nodeport
  namespace: microservices
spec:
  type: NodePort
  selector:
    app: gateway
  ports:
    - name: http
      port: 9090
      targetPort: 9090
      nodePort: 30909
EOF

# 10. Verify deployment
kubectl -n microservices get all
```

## Detailed Deployment Steps

### Step 1: Prepare Docker Images

Before deploying to Kubernetes, ensure all Docker images are built and available:

```bash
# Build all services
./gradlew build

# Build and tag Docker images
docker build -t your-registry/eureka-server:latest eureka-server/eureka-server/
docker build -t your-registry/gateway:latest getway/
docker build -t your-registry/course-service:latest course-service/
docker build -t your-registry/student-service:latest student-service/
docker build -t your-registry/address-service:latest address-service/

# Push to registry
docker push your-registry/eureka-server:latest
docker push your-registry/gateway:latest
docker push your-registry/course-service:latest
docker push your-registry/student-service:latest
docker push your-registry/address-service:latest
```

**Update image references in deployment files:**
```bash
# Replace placeholder images with your actual registry
find deploy/ -name "*.yaml" -exec sed -i 's|your-registry/|your-actual-registry/|g' {} \;
```

### Step 2: Create Namespace

```bash
kubectl apply -f deploy/namespace.yaml
```

**Verify:**
```bash
kubectl get namespaces | grep microservices
```

### Step 3: Setup Database

Create database credentials secret:
```bash
kubectl -n microservices create secret generic mysql-credentials \
  --from-literal=username=admin \
  --from-literal=password=your-secure-password
```

Deploy MySQL:
```bash
kubectl apply -f deploy/mysql.yaml
```

**Verify database deployment:**
```bash
kubectl -n microservices get pods -l app=mysql
kubectl -n microservices logs -l app=mysql
```

**Test database connectivity:**
```bash
kubectl -n microservices run mysql-client --image=mysql:8.0 --rm -it --restart=Never -- \
  mysql -h mysql -u admin -pyour-secure-password -e "SHOW DATABASES;"
```

### Step 4: Deploy Service Discovery

```bash
kubectl apply -f deploy/eureka-server.yaml
```

**Wait for Eureka to be ready:**
```bash
kubectl -n microservices wait --for=condition=ready pod -l app=eureka-server --timeout=300s
```

**Verify Eureka is running:**
```bash
kubectl -n microservices port-forward svc/eureka-server 8761:8761 &
curl http://localhost:8761/
# You should see the Eureka dashboard
```

### Step 5: Deploy Business Services

Deploy all business services:
```bash
kubectl apply -f deploy/course-service.yaml
kubectl apply -f deploy/student-service.yaml
kubectl apply -f deploy/address-service.yaml
```

**Monitor deployment progress:**
```bash
kubectl -n microservices get pods -w
```

**Wait for all services to be ready:**
```bash
kubectl -n microservices wait --for=condition=ready pod -l app=course-service --timeout=300s
kubectl -n microservices wait --for=condition=ready pod -l app=student-service --timeout=300s
kubectl -n microservices wait --for=condition=ready pod -l app=address-service --timeout=300s
```

**Verify service registration with Eureka:**
```bash
kubectl -n microservices port-forward svc/eureka-server 8761:8761 &
curl http://localhost:8761/eureka/apps | grep -E "(COURSE-SERVICE|STUDENT-SERVICE|ADDRESS-SERVICE)"
```

### Step 6: Deploy API Gateway

```bash
kubectl apply -f deploy/gateway.yaml
```

**Wait for gateway to be ready:**
```bash
kubectl -n microservices wait --for=condition=ready pod -l app=gateway --timeout=300s
```

### Step 7: Setup External Access

Create NodePort service for external access:
```bash
kubectl apply -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: gateway-nodeport
  namespace: microservices
spec:
  type: NodePort
  selector:
    app: gateway
  ports:
    - name: http
      port: 9090
      targetPort: 9090
      nodePort: 30909
EOF
```

**Alternative: LoadBalancer (for cloud providers)**
```bash
kubectl apply -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: gateway-loadbalancer
  namespace: microservices
spec:
  type: LoadBalancer
  selector:
    app: gateway
  ports:
    - name: http
      port: 80
      targetPort: 9090
EOF
```

**Alternative: Ingress**
```bash
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: gateway-ingress
  namespace: microservices
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: microservices.local
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: gateway
            port:
              number: 9090
EOF
```

## Verification and Testing

### Check Deployment Status

```bash
# Get all resources
kubectl -n microservices get all

# Check pod status
kubectl -n microservices get pods -o wide

# Check service endpoints
kubectl -n microservices get endpoints

# Check events for any issues
kubectl -n microservices get events --sort-by='.lastTimestamp'
```

### Test API Endpoints

Get the external IP/port:
```bash
# For NodePort
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
echo "Gateway URL: http://$NODE_IP:30909"

# For LoadBalancer
EXTERNAL_IP=$(kubectl -n microservices get svc gateway-loadbalancer -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo "Gateway URL: http://$EXTERNAL_IP"
```

Test the APIs:
```bash
# Replace <GATEWAY_URL> with your actual gateway URL
GATEWAY_URL="http://localhost:30909"

# Test health
curl $GATEWAY_URL/actuator/health

# Test course service
curl $GATEWAY_URL/courses

# Test student service
curl $GATEWAY_URL/students

# Test address service
curl $GATEWAY_URL/address/getAddress

# Create a sample course
curl -X POST $GATEWAY_URL/courses \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Kubernetes Fundamentals",
    "description": "Learn Kubernetes basics",
    "instructor": "John Doe",
    "department": "DevOps"
  }'
```

## Scaling

### Manual Scaling

Scale individual services:
```bash
# Scale gateway for high load
kubectl -n microservices scale deployment gateway --replicas=3

# Scale course service
kubectl -n microservices scale deployment course-service --replicas=2

# Scale student service
kubectl -n microservices scale deployment student-service --replicas=2
```

### Horizontal Pod Autoscaler (HPA)

Setup autoscaling based on CPU usage:
```bash
# Enable metrics server (if not already installed)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Create HPA for gateway
kubectl -n microservices autoscale deployment gateway --cpu-percent=70 --min=2 --max=5

# Create HPA for course service
kubectl -n microservices autoscale deployment course-service --cpu-percent=80 --min=1 --max=3

# Create HPA for student service
kubectl -n microservices autoscale deployment student-service --cpu-percent=80 --min=1 --max=3
```

Check HPA status:
```bash
kubectl -n microservices get hpa
kubectl -n microservices describe hpa gateway
```

## Monitoring and Logging

### Pod Logs

View logs for troubleshooting:
```bash
# Gateway logs
kubectl -n microservices logs -l app=gateway -f

# Course service logs
kubectl -n microservices logs -l app=course-service -f

# All logs with timestamps
kubectl -n microservices logs -l app=gateway --timestamps=true

# Previous container logs (if pod restarted)
kubectl -n microservices logs -l app=gateway --previous
```

### Resource Usage

Monitor resource consumption:
```bash
# Pod resource usage
kubectl -n microservices top pods

# Node resource usage
kubectl top nodes

# Describe pod for detailed info
kubectl -n microservices describe pod <pod-name>
```

### Health Checks

Check application health:
```bash
# Port forward to check health endpoints
kubectl -n microservices port-forward svc/gateway 9090:9090 &
curl http://localhost:9090/actuator/health

kubectl -n microservices port-forward svc/course-service 8883:8883 &
curl http://localhost:8883/actuator/health
```

## Backup and Recovery

### Database Backup

Create a database backup:
```bash
kubectl -n microservices exec -it deployment/mysql -- \
  mysqldump -u admin -p --all-databases > backup.sql
```

Restore from backup:
```bash
kubectl -n microservices exec -i deployment/mysql -- \
  mysql -u admin -p < backup.sql
```

### Configuration Backup

Backup Kubernetes configurations:
```bash
# Export all resources
kubectl -n microservices get all -o yaml > microservices-backup.yaml

# Export secrets (be careful with sensitive data)
kubectl -n microservices get secrets -o yaml > secrets-backup.yaml
```

## Troubleshooting

### Common Issues

**1. Pods not starting**
```bash
# Check pod events
kubectl -n microservices describe pod <pod-name>

# Check if images are pullable
kubectl -n microservices get events | grep "Failed to pull image"
```

**2. Services not registering with Eureka**
```bash
# Check Eureka logs
kubectl -n microservices logs -l app=eureka-server

# Verify network connectivity
kubectl -n microservices exec -it deployment/course-service -- nslookup eureka-server
```

**3. Database connection issues**
```bash
# Check MySQL logs
kubectl -n microservices logs -l app=mysql

# Test connectivity from services
kubectl -n microservices exec -it deployment/course-service -- telnet mysql 3306
```

**4. Gateway routing issues**
```bash
# Check gateway logs for routing errors
kubectl -n microservices logs -l app=gateway | grep ERROR

# Verify service discovery
curl http://<gateway-url>/actuator/gateway/routes
```

### Debug Commands

```bash
# Get into a pod for debugging
kubectl -n microservices exec -it deployment/course-service -- /bin/bash

# Check DNS resolution
kubectl -n microservices exec -it deployment/gateway -- nslookup course-service

# Check network policies (if any)
kubectl -n microservices get networkpolicies

# Check resource quotas
kubectl -n microservices get resourcequotas
```

## Cleanup

Remove the entire deployment:
```bash
# Delete all resources in namespace
kubectl delete namespace microservices

# Or delete individual components
kubectl -n microservices delete -f deploy/
kubectl delete namespace microservices
```

## Production Considerations

### Security
- Use secrets for all sensitive data
- Implement network policies
- Enable RBAC
- Use private container registries
- Implement service mesh for mTLS

### Performance
- Set resource limits and requests
- Use persistent volumes for database
- Implement caching strategies
- Monitor and optimize database connections

### High Availability
- Run multiple replicas of critical services
- Use multiple zones/regions
- Implement circuit breakers
- Set up proper health checks

### Monitoring
- Deploy Prometheus and Grafana
- Implement distributed tracing
- Set up alerting rules
- Monitor business metrics

## Next Steps

1. **Implement CI/CD pipeline** for automated deployments
2. **Add monitoring stack** (Prometheus, Grafana, Jaeger)
3. **Implement security** (OAuth2, JWT, RBAC)
4. **Add configuration management** (Spring Cloud Config)
5. **Implement circuit breakers** (Resilience4j)
6. **Add caching layer** (Redis)
7. **Set up service mesh** (Istio or Linkerd)