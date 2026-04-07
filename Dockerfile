FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
LABEL authors="aiiisana"
WORKDIR /app
COPY --from=build /app/target/tulpar-0.0.1-SNAPSHOT.jar tulpar.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tulpar.jar"]