# Cambios y funcionamiento del proyecto

## Resumen

El proyecto fue preparado para funcionar de forma local, sin depender de AWS, RDS, API Gateway, Cognito, ngrok ni una conexión externa.

La solución local está compuesta por:

- Backend Spring Boot.
- Base de datos H2 en modo archivo.
- Autenticación JWT local.
- Frontend de tienda React/Vite.
- Frontend de administración React/Vite.

## Arquitectura actual

```text
Tienda React :5173  --JWT--> Spring Boot :8080 -- H2 --> ./data/skates
Admin React :5174  --JWT--> Spring Boot :8080
```

El backend carga 32 productos de prueba cuando la tabla está vacía. Los datos quedan guardados en `data/skates` y sobreviven a los reinicios.

## Cambios realizados en el backend

### Base de datos

Se reemplazó MySQL/RDS por H2:

```properties
spring.datasource.url=jdbc:h2:file:./data/skates;MODE=MySQL;AUTO_SERVER=TRUE
```

Se eliminaron:

- Driver MySQL.
- Contraseñas de RDS.
- Host y puerto de RDS.
- Certificados `global-bundle.pem`.
- Referencias de conexión AWS.

### Persistencia

`Skate` ahora es una entidad JPA y `SkateRepository` administra la persistencia.

El servicio:

- Consulta productos desde H2.
- Crea, actualiza y elimina productos.
- Carga datos iniciales solo cuando la tabla está vacía.
- Evita duplicar datos en cada inicio.

### Seguridad local

Se agregó Spring Security con JWT firmado localmente.

Endpoint de autenticación:

```text
POST http://localhost:8080/api/auth/login
```

Usuarios de demostración:

```text
Cliente:
usuario: cliente
contraseña: cliente123

Administrador:
usuario: admin
contraseña: admin123
```

El token se envía en cada petición protegida:

```http
Authorization: Bearer <token>
```

### Reglas de autorización

| Operación | Permiso local |
| --- | --- |
| `POST /api/auth/login` | Público |
| `GET /api/skate/**` | Rol `CLIENTE` |
| `POST /api/skate/**` | Rol `ADMIN` |
| `PUT /api/skate/**` | Rol `ADMIN` |
| `DELETE /api/skate/**` | Rol `ADMIN` |

Sin token, el catálogo responde `401 Unauthorized`. Un cliente puede leer, pero no modificar inventario.

## Cambios realizados en la tienda

Se eliminó `aws-amplify` y toda la configuración de Cognito.

La tienda ahora:

1. Muestra un formulario de login local.
2. Solicita usuario y contraseña al backend.
3. Guarda el JWT en `localStorage`.
4. Consulta el catálogo usando el token.
5. Muestra los productos filtrables por modelo.
6. Permite cerrar sesión.

URL local:

```text
http://localhost:5173
```

## Cambios realizados en el panel admin

Se eliminó `aws-amplify`, Cognito y el archivo de entrada obsoleto que mantenía esa configuración.

El panel admin ahora:

1. Solicita una cuenta de administrador.
2. Obtiene un JWT local.
3. Consulta el inventario con autorización.
4. Usa el rol `ADMIN` para las operaciones de escritura.
5. Permite cerrar sesión.

URL local:

```text
http://localhost:5174
```

## Equivalencias con AWS

| AWS del tutorial | Implementación local |
| --- | --- |
| API Gateway HTTP API | Spring Boot en `localhost:8080` |
| Cognito clientes | Usuario local `cliente` |
| Cognito administradores | Usuario local `admin` |
| JWT de Cognito | JWT firmado por Spring Security |
| Scope `read` | Rol `CLIENTE` |
| Scope `write` | Rol `ADMIN` |
| RDS MySQL | H2 en archivo |
| EC2 | Ejecución local de Spring Boot |
| ngrok | No se utiliza |

La implementación local reproduce el flujo principal de autenticación, autorización y persistencia. No crea recursos AWS ni reemplaza un despliegue productivo.

## Ejecución

Requisitos:

- Java 17 o superior.
- Node.js y npm.

Backend:

```powershell
./mvnw.cmd spring-boot:run
```

Tienda:

```powershell
Set-Location Skates-tienda-main
npm.cmd install
npm.cmd run dev -- --port 5173
```

Admin:

```powershell
Set-Location Skates-admin-main
npm.cmd install
npm.cmd run dev -- --port 5174
```

## Prueba completa

1. Inicia el backend.
2. Abre la tienda en `http://localhost:5173`.
3. Usa `cliente / cliente123`.
4. Confirma que aparecen los 32 productos.
5. Abre el admin en `http://localhost:5174`.
6. Usa `admin / admin123`.
7. Confirma que aparece el inventario.
8. Comprueba que las operaciones de escritura requieren el usuario administrador.

Pruebas HTTP esperadas:

```text
GET /api/skate sin token -> 401
GET /api/skate con token de cliente -> 200
POST /api/skate con token de cliente -> 403
POST /api/skate con token de admin -> 201
```

## Validaciones realizadas

- Backend Maven: pruebas de contexto y datos iniciales exitosas.
- H2: conexión local y 32 registros comprobados.
- Tienda: build Vite exitoso.
- Admin: build Vite exitoso.
- Dependencias `mysql2` y `aws-sdk`: eliminadas del proyecto raíz.
- Dependencia `aws-amplify`: eliminada de los dos frontends.
- Ngrok: eliminado del proyecto y del dispositivo.

## Sobre los commits de GitHub

Para crear commits y subirlos a GitHub se necesita que la carpeta tenga un repositorio Git local y un remoto configurado. En este entorno Git no está instalado y la carpeta actual no contiene `.git`, por lo que no es posible crear commits ni hacer `push` desde aquí.

Cuando Git esté instalado y los repositorios estén clonados, los commits recomendados son:

### Backend

```text
Problemas de backend solucionados
Cambios actuales: backend local con H2 y JWT
```

### Tienda

```text
Problemas de frontend tienda solucionados
Migración de tienda a autenticación local
```

### Admin

```text
Problemas de frontend admin solucionados
Migración de admin a autenticación local
```
