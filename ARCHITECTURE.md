# Architecture Diagrams

This document contains detailed architecture diagrams for the microservices system.

## High-Level Architecture

```mermaid
graph TB
    Client[External Clients<br/>Web/Mobile/API] --> LB[Load Balancer<br/>Kubernetes Ingress]
    LB --> GW[API Gateway<br/>:9090]
    
    GW --> |Service Discovery| ES[Eureka Server<br/>:8761]
    
    GW --> |Route: /courses/*| CS[Course Service<br/>:8883]
    GW --> |Route: /students/*| SS[Student Service<br/>:8882]
    GW --> |Route: /address/*| AS[Address Service<br/>:8881]
    
    CS --> |Register/Discover| ES
    SS --> |Register/Discover| ES
    AS --> |Register/Discover| ES
    
    CS --> DB[(MySQL Database<br/>Courses)]
    SS --> DB[(MySQL Database<br/>Students)]
    AS --> MEM[In-Memory Data<br/>Sample Addresses]
    
    subgraph "Kubernetes Cluster"
        subgraph "microservices namespace"
            GW
            ES
            CS
            SS
            AS
            DB
        end
    end
    
    style Client fill:#e1f5fe
    style GW fill:#fff3e0
    style ES fill:#f3e5f5
    style CS fill:#e8f5e8
    style SS fill:#e8f5e8
    style AS fill:#e8f5e8
    style DB fill:#fff8e1
```

## Service Communication Flow

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Eureka
    participant CourseService
    participant Database
    
    Note over Eureka: Service Discovery
    CourseService->>Eureka: Register service
    Gateway->>Eureka: Discover services
    
    Note over Client,Database: API Request Flow
    Client->>Gateway: POST /courses
    Gateway->>Gateway: Add correlation ID
    Gateway->>Gateway: Log request
    Gateway->>Eureka: Resolve course-service
    Gateway->>CourseService: Forward request
    CourseService->>Database: Insert course
    Database-->>CourseService: Course created
    CourseService-->>Gateway: Return course data
    Gateway->>Gateway: Log response
    Gateway-->>Client: Return response
```

## Kubernetes Deployment Architecture

```mermaid
graph TB
    subgraph "Kubernetes Cluster"
        subgraph "microservices Namespace"
            subgraph "Service Discovery"
                ES_POD[Eureka Server Pod<br/>eureka-server:latest]
                ES_SVC[Eureka Service<br/>ClusterIP:8761]
                ES_POD --> ES_SVC
            end
            
            subgraph "API Gateway"
                GW_POD[Gateway Pod<br/>gateway:latest]
                GW_SVC[Gateway Service<br/>ClusterIP:9090]
                GW_NODEPORT[Gateway NodePort<br/>30909]
                GW_POD --> GW_SVC
                GW_SVC --> GW_NODEPORT
            end
            
            subgraph "Business Services"
                CS_POD[Course Service Pod<br/>course-service:latest]
                CS_SVC[Course Service<br/>ClusterIP:8883]
                CS_POD --> CS_SVC
                
                SS_POD[Student Service Pod<br/>student-service:latest]
                SS_SVC[Student Service<br/>ClusterIP:8882]
                SS_POD --> SS_SVC
                
                AS_POD[Address Service Pod<br/>address-service:latest]
                AS_SVC[Address Service<br/>ClusterIP:8881]
                AS_POD --> AS_SVC
            end
            
            subgraph "Data Layer"
                DB_POD[MySQL Pod<br/>mysql:8.0]
                DB_SVC[MySQL Service<br/>ClusterIP:3306]
                DB_SECRET[mysql-credentials<br/>Secret]
                DB_POD --> DB_SVC
                DB_POD -.-> DB_SECRET
            end
        end
    end
    
    EXT[External Traffic] --> GW_NODEPORT
    
    GW_POD --> ES_SVC
    GW_POD --> CS_SVC
    GW_POD --> SS_SVC
    GW_POD --> AS_SVC
    
    CS_POD --> ES_SVC
    SS_POD --> ES_SVC
    AS_POD --> ES_SVC
    
    CS_POD --> DB_SVC
    SS_POD --> DB_SVC
    
    CS_POD -.-> DB_SECRET
    SS_POD -.-> DB_SECRET
    
    style ES_POD fill:#f3e5f5
    style GW_POD fill:#fff3e0
    style CS_POD fill:#e8f5e8
    style SS_POD fill:#e8f5e8
    style AS_POD fill:#e8f5e8
    style DB_POD fill:#fff8e1
    style DB_SECRET fill:#ffebee
```

## Data Model Relationships

```mermaid
erDiagram
    COURSE {
        long courseId PK
        string name
        string description
        string instructor
        string department
    }
    
    STUDENT {
        long studentId PK
        string name
        string email UK
        int age
    }
    
    ADDRESS {
        string street
        string city
        string state
    }
    
    COURSE ||--o{ ENROLLMENT : "has"
    STUDENT ||--o{ ENROLLMENT : "enrolls"
    STUDENT ||--|| ADDRESS : "has"
    
    ENROLLMENT {
        long courseId FK
        long studentId FK
        date enrollmentDate
        string status
    }
```

## Security Architecture

```mermaid
graph TB
    subgraph "External"
        Client[Client Applications]
        Attacker[Potential Threats]
    end
    
    subgraph "Security Layers"
        Firewall[Kubernetes Network Policies]
        Ingress[Ingress Controller<br/>TLS Termination]
        Gateway[API Gateway<br/>Rate Limiting, Logging]
    end
    
    subgraph "Internal Services"
        Services[Microservices<br/>No Direct External Access]
        Database[Database<br/>Credentials in Secrets]
    end
    
    Client --> Firewall
    Attacker -.->|Blocked| Firewall
    Firewall --> Ingress
    Ingress --> Gateway
    Gateway --> Services
    Services --> Database
    
    Gateway --> AuditLog[Audit Logs<br/>Correlation IDs]
    
    style Firewall fill:#ffebee
    style Gateway fill:#fff3e0
    style AuditLog fill:#f3e5f5
    style Database fill:#e8f5e8
```

## Monitoring and Observability

```mermaid
graph TB
    subgraph "Application Layer"
        GW[Gateway<br/>Correlation IDs]
        Services[Microservices<br/>Health Endpoints]
    end
    
    subgraph "Monitoring Stack"
        Actuator[Spring Boot Actuator<br/>Metrics & Health]
        K8sProbes[Kubernetes Probes<br/>Liveness & Readiness]
        Logs[Centralized Logging<br/>JSON Format]
    end
    
    subgraph "Future Monitoring"
        Prometheus[Prometheus<br/>Metrics Collection]
        Grafana[Grafana<br/>Dashboards]
        Jaeger[Jaeger<br/>Distributed Tracing]
    end
    
    GW --> Actuator
    Services --> Actuator
    Services --> K8sProbes
    GW --> Logs
    Services --> Logs
    
    Actuator -.->|Future| Prometheus
    Prometheus -.->|Future| Grafana
    Services -.->|Future| Jaeger
    
    style Actuator fill:#e8f5e8
    style K8sProbes fill:#fff3e0
    style Logs fill:#f3e5f5
    style Prometheus fill:#ffebee,stroke-dasharray: 5 5
    style Grafana fill:#ffebee,stroke-dasharray: 5 5
    style Jaeger fill:#ffebee,stroke-dasharray: 5 5
```

## Deployment Flow

```mermaid
graph LR
    subgraph "Development"
        Code[Source Code]
        Build[Gradle Build]
        Test[Unit Tests]
    end
    
    subgraph "CI/CD Pipeline"
        Docker[Docker Build]
        Registry[Container Registry]
        K8sManifests[K8s Manifests]
    end
    
    subgraph "Kubernetes Cluster"
        Deploy[kubectl apply]
        Pods[Running Pods]
        Services[Services & Ingress]
    end
    
    Code --> Build
    Build --> Test
    Test --> Docker
    Docker --> Registry
    K8sManifests --> Deploy
    Registry --> Deploy
    Deploy --> Pods
    Pods --> Services
    
    style Build fill:#e8f5e8
    style Docker fill:#fff3e0
    style Deploy fill:#f3e5f5
    style Services fill:#fff8e1
```

## Service Scaling Strategy

```mermaid
graph TB
    subgraph "Load Patterns"
        Peak[Peak Hours<br/>High Traffic]
        Normal[Normal Hours<br/>Medium Traffic]
        Maintenance[Maintenance<br/>Minimal Traffic]
    end
    
    subgraph "Scaling Actions"
        HPA[Horizontal Pod Autoscaler<br/>CPU/Memory Based]
        Manual[Manual Scaling<br/>kubectl scale]
        LoadTest[Load Testing<br/>Performance Validation]
    end
    
    subgraph "Resource Allocation"
        Gateway_Scale[Gateway: 2-5 replicas]
        Course_Scale[Course Service: 1-3 replicas]
        Student_Scale[Student Service: 1-3 replicas]
        Address_Scale[Address Service: 1-2 replicas]
        Eureka_Scale[Eureka: 1 replica<br/>Single Point OK]
    end
    
    Peak --> HPA
    Normal --> HPA
    Maintenance --> Manual
    
    HPA --> Gateway_Scale
    HPA --> Course_Scale
    HPA --> Student_Scale
    HPA --> Address_Scale
    Manual --> Eureka_Scale
    
    LoadTest --> HPA
    
    style HPA fill:#e8f5e8
    style LoadTest fill:#fff3e0
    style Gateway_Scale fill:#f3e5f5
```