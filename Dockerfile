FROM eclipse-temurin:21-jdk-alpine
LABEL authors="aiiisana"
WORKDIR /app
COPY target/tulpar-0.0.1-SNAPSHOT.jar tulpar.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tulpar.jar"]