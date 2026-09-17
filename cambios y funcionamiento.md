# Cambios y funcionamiento

## Objetivo

El proyecto funciona completamente en local, sin AWS, Cognito, API Gateway, RDS, ngrok ni servicios externos.

## Arquitectura local

```text
Tienda React :5173  ---> Spring Boot :8080 ---> H2 en ./data/skates
Admin React :5174  ---> Spring Boot :8080
```

Spring Boot expone la API y H2 mantiene los datos entre reinicios. Al arrancar con una tabla vacia, se cargan 32 datos de prueba.

## Reemplazos realizados

| Tutorial AWS | Implementacion local |
| --- | --- |
| API Gateway HTTP API | Spring Boot recibe directamente las peticiones |
| Cognito clientes | Login local `cliente` |
| Cognito administradores | Login local `admin` |
| JWT de Cognito | JWT firmado por Spring Boot |
| Scopes Cognito `read` y `write` | Roles JWT `CLIENTE` y `ADMIN` |
| RDS MySQL | H2 en modo archivo |
| ngrok | No se utiliza |

## Autenticacion local

El backend tiene un endpoint publico de login:

```text
POST http://localhost:8080/api/auth/login
```

Cuentas de demostracion:

```text
Cliente:
usuario: cliente
contraseña: cliente123

Administrador:
usuario: admin
contraseña: admin123
```

El login devuelve un JWT con una hora de duracion. Los frontends lo guardan en `localStorage` con la clave `skates-token` y lo envian como:

```text
Authorization: Bearer <token>
```

La clave de firma se configura en `src/main/resources/application.properties` mediante `JWT_SECRET`. Para una instalacion real, cambia el valor por una clave secreta de al menos 32 bytes.

## Autorizacion de endpoints

| Endpoint | Acceso |
| --- | --- |
| `POST /api/auth/login` | Publico |
| `GET /api/skate/**` | Rol `CLIENTE` |
| `POST /api/skate/**` | Rol `ADMIN` |
| `PUT /api/skate/**` | Rol `ADMIN` |
| `DELETE /api/skate/**` | Rol `ADMIN` |

Si se consulta el catalogo sin token, Spring Security responde `401 Unauthorized`. Un cliente puede consultar, pero no modificar inventario.

## Ejecucion

Requisitos:

- Java 17 o superior.
- Node.js y npm.

Backend:

```powershell
./mvnw.cmd spring-boot:run
```

Tienda:

```powershell
cd Skates-tienda-main
npm.cmd install
npm.cmd run dev -- --port 5173
```

Admin:

```powershell
cd Skates-admin-main
npm.cmd install
npm.cmd run dev -- --port 5174
```

URLs:

```text
Tienda: http://localhost:5173
Admin: http://localhost:5174
API: http://localhost:8080/api/skate
Consola H2: http://localhost:8080/h2-console
```

## Prueba manual

1. Abre `http://localhost:5173`.
2. Ingresa `cliente` y `cliente123`.
3. Comprueba que aparezcan los 32 productos.
4. Abre `http://localhost:5174`.
5. Ingresa `admin` y `admin123`.
6. Comprueba que el panel pueda consultar el inventario.
7. Las operaciones de escritura requieren el token del administrador.

## Comparacion con el tutorial AWS

El tutorial de AWS agrega una puerta de entrada externa y dos proveedores Cognito. En esta version, Spring Boot es la puerta de entrada y tambien firma y valida los JWT. Esto reproduce localmente el comportamiento principal de autenticacion y autorizacion, pero no crea recursos AWS ni representa un despliegue productivo.

Para volver a AWS en el futuro habria que reemplazar:

- El login local por Amplify/Cognito.
- El JWT local por tokens emitidos por Cognito.
- H2 por RDS.
- La entrada directa a Spring Boot por API Gateway.
- La ejecucion local por EC2, ECS o Elastic Beanstalk.

## Datos y seguridad

La base local se guarda en `data/` y esta excluida de Git. Las contraseñas de demostracion estan dentro del codigo solo para facilitar la presentacion local; no deben usarse en produccion.
