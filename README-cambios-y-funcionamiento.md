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

## Entorno instalado y verificado

Durante la puesta a punto del entorno de trabajo se instalaron y comprobaron las herramientas necesarias para ejecutar el proyecto completo en local:

- Java 17 (Microsoft OpenJDK 17)
- Node.js LTS
- Maven 3.9.9
- Wrapper Maven restaurado para `./mvnw.cmd`

Se confirmó además que:

- El backend arranca con Spring Boot en `http://localhost:8080`.
- La tienda de clientes queda disponible en `http://localhost:5173`.
- El panel admin queda disponible en `http://localhost:5174`.
- La base de datos se guarda en `./data/skates` usando H2 en modo archivo.
- La carga inicial de datos crea 32 skates de prueba al arrancar con la tabla vacía.

## Credenciales locales y autenticación

Usuarios de demostración aceptados por el backend local:

```text
Cliente:
usuario: cliente
contraseña: cliente123

Administrador:
usuario: admin
contraseña: admin123
```

La configuración actual usa H2 local con usuario `sa` y contraseña vacía. El JWT se firma localmente con la clave por defecto de `app.security.jwt-secret` en `application.properties` y puede sobreescribirse con la variable de entorno `JWT_SECRET`.

## Verificación final del proyecto

Se ejecutaron comprobaciones reales sobre el proyecto completo:

```powershell
./mvnw.cmd test
```

Resultado verificado:

- 2 test ejecutados
- 0 fallos
- 0 errores
- BUILD SUCCESS

Y se validó también la compilación de los frontends con Vite:

```powershell
cd "C:\Users\braul\OneDrive\Escritorio\Skates-backend-main\Skates-tienda-main"
npm install
npm run build

cd "C:\Users\braul\OneDrive\Escritorio\Skates-backend-main\Skates-admin"
npm install
npm run build
```

Resultado verificado:

- Tienda: build completado con éxito.
- Admin: build completado con éxito.

## Panel admin con stock y pedidos

El panel administrativo quedó ampliado para que el usuario administrador pueda:

- ver el stock total real cargado desde la base de datos H2;
- ver el número de modelos y productos con stock bajo;
- revisar una tabla con el inventario completo desde el backend;
- revisar un panel de pedidos recientes para operación y seguimiento.

Esto se refleja en la vista del admin al autenticarse con `admin / admin123`.

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
