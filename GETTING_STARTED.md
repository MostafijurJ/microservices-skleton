# Getting Started Guide

This quick start guide will help you get the microservices system up and running in 15 minutes.

## Prerequisites

- Docker and Docker Compose (for local development)
- Kubernetes cluster (for production deployment)
- Java 17+ (for local development)
- Postman (optional, for API testing)

## Option 1: Quick Local Development (Docker Compose)

**Note**: This repository doesn't include docker-compose.yml yet, but here's how you would set it up:

### Create docker-compose.yml

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: microservices
      MYSQL_USER: admin
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  eureka-server:
    build: eureka-server/eureka-server
    ports:
      - "8761:8761"
    environment:
      SPRING_PROFILES_ACTIVE: dev

  course-service:
    build: course-service
    depends_on:
      - mysql
      - eureka-server
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/microservices
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: password
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka

  student-service:
    build: student-service
    depends_on:
      - mysql
      - eureka-server
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/microservices
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: password
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka

  address-service:
    build: address-service
    depends_on:
      - eureka-server
    environment:
      SPRING_PROFILES_ACTIVE: dev
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka

  gateway:
    build: getway
    depends_on:
      - eureka-server
    ports:
      - "9090:9090"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka

volumes:
  mysql_data:
```

### Run with Docker Compose

```bash
# Build and start all services
docker-compose up --build -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f gateway

# Stop all services
docker-compose down
```

## Option 2: Kubernetes Deployment (Recommended)

Follow the detailed [Kubernetes Deployment Guide](KUBERNETES_DEPLOYMENT.md) for production deployment.

### Quick Kubernetes Setup

```bash
# Clone and navigate to repository
git clone https://github.com/MostafijurJ/microservices-skleton.git
cd microservices-skleton

# Deploy to Kubernetes
kubectl apply -f deploy/namespace.yaml

# Create database secret
kubectl -n microservices create secret generic mysql-credentials \
  --from-literal=username=admin \
  --from-literal=password=secretpassword

# Deploy all services
kubectl apply -f deploy/

# Create external access
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

# Wait for deployment
kubectl -n microservices wait --for=condition=ready pod --all --timeout=300s

# Get access URL
echo "Gateway available at: http://$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[0].address}'):30909"
```

## Option 3: Local Development (Individual Services)

### Start Services Manually

1. **Start MySQL Database**
   ```bash
   docker run -d --name mysql \
     -e MYSQL_ROOT_PASSWORD=rootpass \
     -e MYSQL_DATABASE=microservices \
     -e MYSQL_USER=admin \
     -e MYSQL_PASSWORD=password \
     -p 3306:3306 \
     mysql:8.0
   ```

2. **Start Eureka Server**
   ```bash
   cd eureka-server/eureka-server
   ./gradlew bootRun
   # Access at http://localhost:8761
   ```

3. **Start Course Service**
   ```bash
   cd course-service
   export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/microservices
   export SPRING_DATASOURCE_USERNAME=admin
   export SPRING_DATASOURCE_PASSWORD=password
   ./gradlew bootRun
   # Service runs on port 8883
   ```

4. **Start Student Service**
   ```bash
   cd student-service
   export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/microservices
   export SPRING_DATASOURCE_USERNAME=admin
   export SPRING_DATASOURCE_PASSWORD=password
   ./gradlew bootRun
   # Service runs on port 8882
   ```

5. **Start Address Service**
   ```bash
   cd address-service
   ./gradlew bootRun
   # Service runs on port 8881
   ```

6. **Start Gateway**
   ```bash
   cd getway
   ./gradlew bootRun
   # Gateway runs on port 9090
   ```

## Testing Your Setup

### 1. Check Service Health

```bash
# Check Eureka Dashboard
curl http://localhost:8761

# Check Gateway Health
curl http://localhost:9090/actuator/health

# Check if services are registered
curl http://localhost:8761/eureka/apps
```

### 2. Test APIs

```bash
# Test through Gateway
GATEWAY_URL="http://localhost:9090"

# Get all courses
curl $GATEWAY_URL/courses

# Create a course
curl -X POST $GATEWAY_URL/courses \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Getting Started with Microservices",
    "description": "Learn microservices fundamentals",
    "instructor": "John Doe",
    "department": "Computer Science"
  }'

# Get all students
curl $GATEWAY_URL/students

# Create a student
curl -X POST $GATEWAY_URL/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "email": "jane.smith@university.edu",
    "age": 22
  }'

# Get sample address
curl $GATEWAY_URL/address/getAddress
```

### 3. Use Postman Collection

1. Import the collection from `postman/microservices.postman_collection.json`
2. Set environment variables:
   - `gateway_base`: `http://localhost:9090` (or your gateway URL)
   - `course_service_base`: `http://localhost:8883`
   - `student_service_base`: `http://localhost:8882`
   - `correlation_id`: `test-correlation-id`
   - `user_id`: `test-user`

3. Run the collection to test all endpoints

## Common Issues and Solutions

### Issue 1: Services Not Starting

**Problem**: Services fail to start with database connection errors

**Solution**:
```bash
# Check if MySQL is running
docker ps | grep mysql

# Check MySQL logs
docker logs mysql

# Verify database credentials
mysql -h localhost -u admin -p microservices
```

### Issue 2: Services Not Registering with Eureka

**Problem**: Services start but don't appear in Eureka dashboard

**Solution**:
```bash
# Check Eureka URL in service configuration
# Verify network connectivity to Eureka
curl http://localhost:8761/eureka/apps

# Check service logs for registration errors
tail -f course-service/logs/application.log
```

### Issue 3: Gateway Routing Failures

**Problem**: Gateway returns 503 or routing errors

**Solution**:
```bash
# Check if services are registered in Eureka
curl http://localhost:8761/eureka/apps

# Check gateway routes
curl http://localhost:9090/actuator/gateway/routes

# Verify service discovery configuration
```

### Issue 4: Port Conflicts

**Problem**: "Address already in use" errors

**Solution**:
```bash
# Check what's using the ports
lsof -i :8761  # Eureka
lsof -i :9090  # Gateway
lsof -i :8883  # Course Service

# Kill processes using the ports
kill -9 $(lsof -t -i :8761)
```

## Next Steps

1. **Explore the APIs**: Use the [API Documentation](API_DOCUMENTATION.md) to understand all available endpoints

2. **Understand the Architecture**: Review the [Architecture Documentation](ARCHITECTURE.md) to see how services interact

3. **Deploy to Production**: Follow the [Kubernetes Deployment Guide](KUBERNETES_DEPLOYMENT.md) for production deployment

4. **Monitoring**: Set up monitoring and observability tools

5. **Security**: Implement authentication and authorization

6. **Performance**: Add caching and optimize database queries

## Useful Commands

```bash
# Check all running Java processes
jps -l

# Monitor service logs
tail -f */logs/application.log

# Check database content
mysql -h localhost -u admin -p -e "USE microservices; SHOW TABLES; SELECT * FROM course; SELECT * FROM student;"

# Test service connectivity
telnet localhost 8761  # Eureka
telnet localhost 9090  # Gateway
telnet localhost 8883  # Course Service
```

## Development Workflow

1. **Make Changes**: Modify service code
2. **Test Locally**: Run individual service with `./gradlew bootRun`
3. **Build**: `./gradlew build`
4. **Test Integration**: Start all services and test via Gateway
5. **Deploy**: Build Docker images and deploy to Kubernetes

## Support

- **Documentation**: Review all `.md` files in the repository
- **Issues**: Check common issues section above
- **Logs**: Always check application logs for detailed error information
- **Community**: Refer to Spring Cloud and Kubernetes documentation

Happy microservices development! 🚀