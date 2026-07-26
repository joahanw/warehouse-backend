# --- Build Stage ---
FROM maven:3.9-eclipse-temurin:17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre-jammy

RUN groupadd -r spring && \
    useradd -r -g spring spring &&\
    mkdir -p /app/logs &&\
    chown -R spring:spring /app/logs

WORKDIR /app

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring

EXPOSE 9090

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]