# syntax=docker/dockerfile:1
# Minimal runtime image for Spring Boot 3 (Java 17)
FROM eclipse-temurin:17-jre-jammy

# Set a non-root user for better security (optional but recommended)
# Using UID 1001/GID 1001 which commonly exist in K8s distros
RUN useradd -u 1001 -m appuser
USER 1001

WORKDIR /app

# Allow passing JVM options and Spring profile from the environment
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="k8s"

# Copy the fat jar built by Gradle. You can override JAR_FILE at build time.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Expose for documentation purposes (K8s uses Service/Pod spec for ports)
EXPOSE 9090

# Healthcheck (optional; K8s typically uses probes, but this helps for docker run)
HEALTHCHECK --interval=30s --timeout=3s CMD ["/bin/sh", "-c", "wget -qO- http://localhost:9090/actuator/health || exit 1"]

# Use container-aware JVM defaults
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:MinRAMPercentage=50.0 ${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]
