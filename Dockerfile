# Usa el Dockerfile que ya está dentro de backend/
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia solo lo necesario para la fase de build
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn -B package -DskipTests

# Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]