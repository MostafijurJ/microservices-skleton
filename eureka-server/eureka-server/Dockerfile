FROM openjdk:17
EXPOSE 8761
ADD build/libs/eureka-server-0.0.1-SNAPSHOT.jar eureka-server-docker.jar
ENTRYPOINT ["java","-jar","/eureka-server-docker.jar"]