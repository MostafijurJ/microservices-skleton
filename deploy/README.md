Microservices Kubernetes Deployment

This folder contains Kubernetes manifests to deploy the microservices system:
- eureka-server (service discovery)
- gateway (Spring Cloud Gateway)
- course-service
- student-service
- address-service

Files
- namespace.yaml: Creates a dedicated namespace `microservices`.
- eureka-server.yaml: Deployment + ClusterIP Service on port 8761.
- gateway.yaml: Deployment + ClusterIP Service on port 9090.
- course-service.yaml: Deployment + ClusterIP Service on port 8883.
- student-service.yaml: Deployment + ClusterIP Service on port 8882.
- address-service.yaml: Deployment + ClusterIP Service on port 8881.

Notes
- Images: Replace `your-registry/<service>:latest` with the actual image references you push (e.g., ghcr.io/you/<service>:tag or your Docker Hub repo). Build with Gradle and Docker, for example:
  - ./gradlew :eureka-server:eureka-server:bootJar && docker build -t your-registry/eureka-server:latest -f eureka-server/eureka-server/Dockerfile eureka-server/eureka-server
  - ./gradlew :getway:bootJar && docker build -t your-registry/gateway:latest -f getway/Dockerfile getway
  - ./gradlew :course-service:bootJar && docker build -t your-registry/course-service:latest -f course-service/Dockerfile course-service
  - ./gradlew :student-service:bootJar && docker build -t your-registry/student-service:latest -f student-service/Dockerfile student-service
  - ./gradlew :address-service:bootJar && docker build -t your-registry/address-service:latest -f address-service/Dockerfile address-service

- Eureka URL: Manifests set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE to http://eureka-server.microservices.svc.cluster.local:8761/eureka so clients can discover via in-cluster DNS.

- Database: The course, student, and address services expect a MySQL database. The manifests reference a Service named `mysql` in the same namespace and a Secret `mysql-credentials` with keys `username` and `password`.
  - Create the secret first (example):
    kubectl -n microservices create secret generic mysql-credentials \
      --from-literal=username=user --from-literal=password=password
  - Provide or deploy a MySQL service reachable at `mysql.microservices.svc.cluster.local:3306`. You can run an external managed MySQL or deploy one in-cluster.

- Probes: We configured TCP readiness/liveness probes on the application ports to avoid requiring Spring Boot Actuator.

Deploy sequence
1) Create namespace:
   kubectl apply -f deploy/namespace.yaml
2) Start Eureka:
   kubectl apply -f deploy/eureka-server.yaml
3) Start backend services (require DB):
   kubectl apply -f deploy/course-service.yaml
   kubectl apply -f deploy/student-service.yaml
   kubectl apply -f deploy/address-service.yaml
4) Start Gateway:
   kubectl apply -f deploy/gateway.yaml

Access
- Inside the cluster, services are resolvable by DNS.
- To access the Gateway from outside, create either a NodePort/LoadBalancer service or an Ingress. Example NodePort:

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

  Apply with: kubectl apply -f - <<'EOF' ... EOF

Environment overrides
- Spring Boot supports environment variable mapping for application.yml keys. Variables set in Deployment env:
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE -> eureka.client.service-url.defaultZone
  - SPRING_DATASOURCE_URL -> spring.datasource.url
  - SPRING_DATASOURCE_USERNAME -> spring.datasource.username
  - SPRING_DATASOURCE_PASSWORD -> spring.datasource.password
  - SPRING_JPA_HIBERNATE_DDL_AUTO -> spring.jpa.hibernate.ddl-auto

Troubleshooting
- Ensure images are pushed and pullable by your cluster nodes.
- Confirm MySQL connectivity and credentials.
- Check logs: kubectl -n microservices logs deploy/<deployment-name>
- Describe pods for events: kubectl -n microservices describe pod <pod-name>
