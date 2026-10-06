# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar los archivos de proyecto y descargar dependencias
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Puerto que Railway asignará dinámicamente (puedes exponer 8080 como ejemplo)
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java","-jar","app.jar"]