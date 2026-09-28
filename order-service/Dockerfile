FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

USER appuser

EXPOSE 8081

# Set JVM flags for container-aware memory management
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]