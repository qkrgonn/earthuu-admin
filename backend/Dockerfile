FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -B -q -DskipTests package && \
    find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S earthuu && adduser -S -G earthuu earthuu
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
USER earthuu
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
