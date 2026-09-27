# Backend — Museo Virtual

Spring Boot 4.1 con Spring Security, Spring Data JPA, Spring for GraphQL y
springdoc (Swagger). Base de datos MariaDB.

Cómo levantar todo el sistema: ver el [README de la raíz](../README.md).

## Configuración

Todo está en `src/main/resources/application.properties`. Los valores por
defecto sirven para desarrollo con el `docker-compose.yml` de la raíz; en
otro entorno se cambian con variables de entorno:

| Variable | Para qué | Por defecto |
|---|---|---|
| `DB_URL` | Conexión a MariaDB | `jdbc:mariadb://localhost:3306/museodb` |
| `DB_USUARIO` / `DB_CLAVE` | Credenciales de la base | `museo` / `museo` |
| `JWT_SECRET` | Clave para firmar los tokens (32+ caracteres) | una clave de desarrollo |
| `JWT_EXPIRACION_MS` | Duración del token | 3600000 (1 hora) |
| `FRONTEND_URL` | Origen permitido por CORS | `http://localhost:5173` |

La clave JWT por defecto está en el repositorio a propósito, para que el
proyecto arranque sin configurar nada. **No usarla fuera de desarrollo.**

## Tests

```bash
./mvnw test
```

Corren con el perfil `test` (`src/test/resources/application-test.properties`):
H2 en memoria, sin MariaDB.
