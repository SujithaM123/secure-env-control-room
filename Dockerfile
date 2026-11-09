FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY backend/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
