# Museo Virtual Interactivo

TP de Web Services — Desarrollo de Software en Sistemas Distribuidos, UNLa.

Sistema de gestión para un museo virtual: catálogo de obras y reporte de
asistencia por **GraphQL**; eventos, inscripciones, filtros favoritos,
exportación a Excel y autenticación por **REST**, con JWT y permisos por rol
(visitante, curador y administrador).

## Estructura

```text
backend/     Spring Boot: API REST, API GraphQL y seguridad
frontend/    React + Vite: las pantallas
docs/        contrato de la API REST, colección de Postman y plan
docker-compose.yml   MariaDB para desarrollo
```

## Cómo levantarlo

Hace falta **Docker Desktop**, **JDK 21 o superior** y **Node 20.19+ o 22.12+**.

1. Base de datos, desde la raíz del repo:

   ```bash
   docker compose up -d
   ```

   **Sin Docker** (por ejemplo, si WSL no funciona en la PC): instalar
   MariaDB 11.4 desde [mariadb.org](https://mariadb.org/download/), o usar
   su ZIP portable, y crear la base y el usuario que espera el backend:

   ```sql
   CREATE DATABASE museodb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'museo'@'localhost' IDENTIFIED BY 'museo';
   GRANT ALL PRIVILEGES ON museodb.* TO 'museo'@'localhost';
   ```

2. Backend, en otra terminal:

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

   En Windows con cmd o PowerShell: `mvnw.cmd spring-boot:run`. Queda en
   `http://localhost:8000`. Al arrancar crea las tablas y carga los datos de
   ejemplo.

3. Frontend, en otra terminal:

   ```bash
   cd frontend
   npm ci
   npm run dev
   ```

   Queda en `http://localhost:5173`.

## Usuarios de prueba

Los crea el backend al arrancar (`backend/src/main/java/com/unla/museo/configuration/DataInitializer.java`,
donde también está la contraseña, la misma para los tres):

| Email | Rol |
|---|---|
| `visitante@test.com` | VISITANTE |
| `curador@test.com` | CURADOR |
| `admin@test.com` | ADMINISTRADOR |

Cualquiera puede registrarse desde la pantalla de login; el usuario nuevo
siempre queda como visitante.

## Documentación interactiva de las APIs

- REST (Swagger, OpenAPI 3.0): `http://localhost:8000/swagger-ui.html`.
  Para probar rutas protegidas, iniciar sesión con `POST /api/auth/login`,
  copiar el `accessToken` y pegarlo en **Authorize**.
- GraphQL (GraphiQL): `http://localhost:8000/graphiql`. El token va en la
  pestaña **Headers**: `{"Authorization": "Bearer <token>"}`.

## Tests

```bash
cd backend
./mvnw test
```

Usan una base H2 en memoria: no necesitan Docker ni MariaDB.
