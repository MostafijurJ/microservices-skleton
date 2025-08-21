# Microservices Architecture - Educational Management System

A comprehensive microservices-based educational management system built with Spring Boot and deployed on Kubernetes. This system demonstrates modern microservices patterns including service discovery, API gateway, and distributed data management.

## 🏗️ Architecture Overview

This project implements a microservices architecture with the following components:

- **Service Discovery**: Eureka Server for service registration and discovery
- **API Gateway**: Spring Cloud Gateway for routing and cross-cutting concerns
- **Business Services**: Independent microservices for domain-specific operations
- **Database**: MySQL for data persistence
- **Container Orchestration**: Kubernetes for deployment and scaling

## 📊 Service Architecture Diagram

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   External      │    │                  │    │                 │
│   Clients       │◄──►│   API Gateway    │◄──►│  Eureka Server  │
│                 │    │   (Port 9090)    │    │   (Port 8761)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        ▲
                                │                        │
                        ┌───────┼────────┬───────────────┼─────────┐
                        │       │        │               │         │
                        ▼       ▼        ▼               │         │
                ┌─────────────┐ ┌─────────────┐ ┌─────────────┐     │
                │   Course    │ │   Student   │ │   Address   │     │
                │  Service    │ │   Service   │ │   Service   │     │
                │ (Port 8883) │ │ (Port 8882) │ │ (Port 8881) │     │
                └─────────────┘ └─────────────┘ └─────────────┘     │
                        │               │               │           │
                        │               │               │           │
                        └───────────────┼───────────────┘           │
                                        │                           │
                                        ▼                           │
                                ┌─────────────┐                     │
                                │    MySQL    │                     │
                                │  Database   │                     │
                                └─────────────┘                     │
                                                                    │
                        All services register with ────────────────┘
                        Eureka for service discovery
```

## 🚀 Services Overview

### 1. Eureka Server (Service Discovery)
- **Port**: 8761
- **Purpose**: Service registration and discovery
- **Technology**: Spring Cloud Netflix Eureka
- **Endpoint**: `http://eureka-server:8761/eureka`

### 2. API Gateway
- **Port**: 9090
- **Purpose**: 
  - Route requests to appropriate microservices
  - Cross-cutting concerns (logging, security, rate limiting)
  - Load balancing
- **Technology**: Spring Cloud Gateway
- **Features**:
  - Request/Response logging with correlation IDs
  - Audit logging for security compliance
  - Route-based load balancing

### 3. Course Service
- **Port**: 8883
- **Purpose**: Manage academic courses
- **Database**: MySQL
- **API Base Path**: `/courses`

#### Course Entity
```json
{
  "courseId": "Long",
  "name": "String (required)",
  "description": "String",
  "instructor": "String",
  "department": "String"
}
```

#### API Endpoints
- `POST /courses` - Create a new course
- `GET /courses` - Get all courses
- `GET /courses/{courseId}` - Get course by ID
- `PUT /courses/{courseId}` - Update course
- `DELETE /courses/{courseId}` - Delete course

### 4. Student Service
- **Port**: 8882
- **Purpose**: Manage student information
- **Database**: MySQL
- **API Base Path**: `/students`

#### Student Entity
```json
{
  "studentId": "Long",
  "name": "String (required)",
  "email": "String (required, unique)",
  "age": "Integer"
}
```

#### API Endpoints
- `POST /students` - Create a new student
- `GET /students` - Get all students
- `GET /students/{id}` - Get student by ID
- `PUT /students/{id}` - Update student
- `DELETE /students/{id}` - Delete student

### 5. Address Service
- **Port**: 8881
- **Purpose**: Provide address information
- **API Base Path**: `/address`

#### Address DTO
```json
{
  "street": "String",
  "city": "String",
  "state": "String"
}
```

#### API Endpoints
- `GET /address/getAddress` - Get sample address data

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.x
- **Service Discovery**: Spring Cloud Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Database**: MySQL
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Java Version**: 17+

## 🐳 Kubernetes Deployment

For complete deployment instructions, see the [Kubernetes Deployment Guide](KUBERNETES_DEPLOYMENT.md).

### Quick Start Deployment

```bash
# Create namespace and secrets
kubectl apply -f deploy/namespace.yaml
kubectl -n microservices create secret generic mysql-credentials \
  --from-literal=username=admin --from-literal=password=password

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
```

Access the system at `http://<node-ip>:30909`

## 🔧 Local Development

For detailed local development instructions, see the [Getting Started Guide](GETTING_STARTED.md).

Quick build commands:
```bash
# Build all services
./gradlew build

# Build specific service
cd course-service && ./gradlew build
```

For Docker image building and local deployment steps, refer to the comprehensive guides in the documentation.

## 🔍 Service Communication

### Service Discovery Flow
1. Services register with Eureka Server on startup
2. Gateway discovers services through Eureka
3. Client requests go through Gateway
4. Gateway routes requests to appropriate services
5. Services communicate via service names (DNS resolution in K8s)

### Configuration Profiles

Each service supports multiple profiles:
- **default**: Basic configuration
- **dev**: Development environment settings
- **k8s**: Kubernetes-specific configuration

Environment variables override application properties in Kubernetes:
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` → `eureka.client.service-url.defaultZone`
- `SPRING_DATASOURCE_URL` → `spring.datasource.url`
- `SPRING_DATASOURCE_USERNAME` → `spring.datasource.username`
- `SPRING_DATASOURCE_PASSWORD` → `spring.datasource.password`

## 🧪 Testing the System

For comprehensive API testing examples, see the [API Documentation](API_DOCUMENTATION.md).

### Quick Health Checks
```bash
# Check services through Gateway (replace with your gateway URL)
GATEWAY_URL="http://localhost:30909"

curl $GATEWAY_URL/actuator/health
curl $GATEWAY_URL/courses
curl $GATEWAY_URL/students
curl $GATEWAY_URL/address/getAddress
```

### Postman Collection
Import the ready-to-use collection from `postman/microservices.postman_collection.json` for comprehensive API testing.

## 🐛 Troubleshooting

For detailed troubleshooting instructions, see:
- [Getting Started Guide](GETTING_STARTED.md#common-issues-and-solutions) for common setup issues
- [Kubernetes Deployment Guide](KUBERNETES_DEPLOYMENT.md#troubleshooting) for deployment issues

### Quick Debug Commands
```bash
# Check pod status in Kubernetes
kubectl -n microservices get pods

# View logs
kubectl -n microservices logs deployment/course-service

# Check service endpoints
kubectl -n microservices get endpoints
```

## 📈 Monitoring and Observability

### Logging
- Centralized logging with correlation IDs
- Request/response logging in Gateway
- Audit logging for security compliance

### Metrics
- Spring Boot Actuator endpoints enabled
- Health probes configured for Kubernetes
- JVM and application metrics available

## 🔒 Security Considerations

- Services communicate within Kubernetes cluster network
- Database credentials stored as Kubernetes secrets
- No direct external access to business services (only through Gateway)
- Audit logging for tracking user actions

## 🚀 Future Enhancements

- Implement authentication and authorization
- Add circuit breakers for resilience
- Implement distributed tracing
- Add configuration management (Spring Cloud Config)
- Implement event-driven communication
- Add caching layer (Redis)
- Implement comprehensive monitoring (Prometheus/Grafana)

## 📚 Documentation

This repository includes comprehensive documentation:

- **[Getting Started Guide](GETTING_STARTED.md)** - Quick setup instructions for local and Kubernetes deployment
- **[Architecture Documentation](ARCHITECTURE.md)** - Detailed diagrams and architecture explanations
- **[API Documentation](API_DOCUMENTATION.md)** - Complete API reference for all services
- **[Kubernetes Deployment Guide](KUBERNETES_DEPLOYMENT.md)** - Step-by-step Kubernetes deployment instructions
- **[Postman Collection](postman/microservices.postman_collection.json)** - Ready-to-use API testing collection

## 📚 Learn More

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html)
- [Netflix Eureka](https://github.com/Netflix/eureka)

## 📞 Support

For issues and questions, please refer to:
1. [Getting Started Guide](GETTING_STARTED.md) for common setup issues
2. [Kubernetes Deployment Guide](KUBERNETES_DEPLOYMENT.md) for deployment troubleshooting
3. The troubleshooting sections in each documentation file
4. Create an issue in the repository for additional help