# Skates Backend

API REST para administrar productos de una tienda de skate. El proyecto está construido con Java, Spring Boot, Spring Data JPA, Hibernate, Spring Security, JWT y H2.

## Requisitos

- JDK 17 o superior.
- No es necesario instalar Maven: el proyecto incluye Maven Wrapper.
- Puerto `8080` disponible.

Comprueba Java desde PowerShell:

```powershell
java -version
```

## Ejecutar el proyecto

Desde la raíz del repositorio (`Skates-backend`):

```powershell
./mvnw.cmd spring-boot:run
```

La aplicación quedará disponible en:

- API: `http://localhost:8080`
- Consola H2: `http://localhost:8080/h2-console/`


### Empaquetar y ejecutar el JAR

```powershell
./mvnw.cmd clean package
java -jar target/backend-skates-app-0.0.1-SNAPSHOT.jar
```

### Usar otro puerto

Si el puerto `8080` está ocupado:

```powershell
$env:SERVER_PORT="8081"
./mvnw.cmd spring-boot:run
```

En ese caso, se usa `http://localhost:8081` en todas las URLs.

## Visualizar y probar la API

Los endpoints `GET` se pueden abrir directamente en el navegador:

- `http://localhost:8080/api/skate`
- `http://localhost:8080/api/skate/modelos`
- `http://localhost:8080/api/skate/modelo/Street`
- `http://localhost:8080/api/skate/1`

También puedes probar todos los métodos con Postman usando la URL base `http://localhost:8080`.

## Autenticación

La API usa tokens JWT. El login es público y devuelve un token válido durante una hora.

### Usuarios de desarrollo

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| `cliente` | `cliente123` | `CLIENTE` |
| `admin` | `admin123` | `ADMIN` |

Solicita un token:

```powershell
$login = Invoke-RestMethod `
	-Method Post `
	-Uri "http://localhost:8080/api/auth/login" `
	-ContentType "application/json" `
	-Body '{"username":"admin","password":"admin123"}'

$token = $login.token
$token
```

En Postman, agrega el encabezado a las operaciones protegidas:

```text
Authorization: Bearer <token>
```

Los `GET` de skates son públicos. Las operaciones `POST`, `PUT` y `DELETE` requieren el rol `ADMIN`.

## Endpoints

### Autenticación

| Método | URL | Acceso | Descripción |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Público | Genera un token JWT. |

### Skates

| Método | URL | Acceso | Descripción |
| --- | --- | --- | --- |
| `GET` | `/api/skate` | Público | Lista todos los productos. |
| `GET` | `/api/skate/{id}` | Público | Obtiene un producto por ID. |
| `GET` | `/api/skate/modelos` | Público | Lista los modelos sin repetir. |
| `GET` | `/api/skate/modelo/{modelo}` | Público | Filtra por modelo, sin distinguir mayúsculas. |
| `POST` | `/api/skate` | ADMIN | Crea un producto. |
| `PUT` | `/api/skate/{id}` | ADMIN | Actualiza un producto. |
| `DELETE` | `/api/skate/{id}` | ADMIN | Elimina un producto. |

## Crear y actualizar productos

`POST /api/skate` y `PUT /api/skate/{id}` reciben JSON con esta estructura:

```json
{
	"modelo": "Street",
	"marca": "DC",
	"medida": 8.0,
	"wheelbase": null,
	"stock": 10
}
```

Reglas de validación:

- `modelo` y `marca` son obligatorios y no pueden estar vacíos.
- `medida`, cuando se informa, debe ser mayor que `0`.
- `wheelbase`, cuando se informa, debe ser mayor que `0`.
- `stock` es obligatorio y debe ser mayor o igual a `0`.
- Para productos `Street` normalmente se informa `medida`.
- Para productos `Longboard` o `Downhill` normalmente se informa `wheelbase`.

Ejemplo de creación en PowerShell:

```powershell
$headers = @{ Authorization = "Bearer $token" }
$body = @{
	modelo = "Street"
	marca = "Independent"
	medida = 8.25
	wheelbase = $null
	stock = 12
} | ConvertTo-Json

Invoke-RestMethod `
	-Method Post `
	-Uri "http://localhost:8080/api/skate" `
	-Headers $headers `
	-ContentType "application/json" `
	-Body $body
```

Ejemplo de actualización:

```powershell
$body = @{
	modelo = "Street"
	marca = "Independent"
	medida = 8.25
	wheelbase = $null
	stock = 20
} | ConvertTo-Json

Invoke-RestMethod `
	-Method Put `
	-Uri "http://localhost:8080/api/skate/1" `
	-Headers $headers `
	-ContentType "application/json" `
	-Body $body
```

Ejemplo de eliminación:

```powershell
Invoke-RestMethod `
	-Method Delete `
	-Uri "http://localhost:8080/api/skate/1" `
	-Headers $headers
```

## Base de datos H2

La aplicación usa H2 en modo archivo para desarrollo local:

- JDBC URL: `jdbc:h2:file:./data/skates;MODE=MySQL;AUTO_SERVER=TRUE`
- Usuario: `sa`
- Contraseña: vacía
- Archivos de datos: `data/skates.*`
- Consola: `http://localhost:8080/h2-console/`

En la pantalla de H2, completa los campos con esos valores y presiona **Connect**. Los cambios realizados allí permanecen después de reiniciar la aplicación porque la base está almacenada en archivos.

Hibernate usa `ddl-auto=update`, por lo que crea o actualiza la tabla sin borrar los datos existentes. Si la tabla está vacía, la aplicación carga automáticamente datos de ejemplo de los modelos `Street`, `Longboard` y `Downhill`.

No elimines los archivos de `data/` si necesitas conservar los datos locales.

## CORS

El backend permite solicitudes desde:

- `http://localhost:5173`
- `http://localhost:5174`

Los métodos permitidos son `GET`, `POST`, `PUT`, `DELETE` y `OPTIONS` para las rutas `/api/**`.

## Pruebas y calidad

Ejecutar todas las pruebas:

```powershell
./mvnw.cmd test
```

Limpiar, recompilar y probar:

```powershell
./mvnw.cmd clean test
```

Compilar sin ejecutar pruebas:

```powershell
./mvnw.cmd -q -DskipTests compile
```

Las pruebas usan una base H2 en memoria (`jdbc:h2:mem:skates`), independiente de la base persistente de `data/`.

## Respuestas de error

Los errores de validación y los productos inexistentes se devuelven como JSON con `timestamp` y `mensaje`.

- `400 Bad Request`: datos inválidos.
- `401 Unauthorized`: falta el token, el token expiró o las credenciales son incorrectas.
- `403 Forbidden`: el usuario autenticado no tiene rol `ADMIN`.
- `404 Not Found`: el producto solicitado no existe.

## Estructura principal

```text
src/main/java/cl/duoc/backendskatesapp/
├── config/       Seguridad y CORS
├── controller/   Endpoints REST y manejo de errores
├── model/        Entidad Skate
├── repository/   Acceso a datos JPA
└── service/      Reglas de negocio y datos iniciales
```

## Solución rápida de problemas

### El puerto 8080 está ocupado

Ejecuta la aplicación en otro puerto:

```powershell
$env:SERVER_PORT="8081"
./mvnw.cmd spring-boot:run
```

### H2 muestra `401` o `404`

Verifica que hayas ejecutado `./mvnw.cmd clean package` o `./mvnw.cmd spring-boot:run` después de descargar las dependencias y que estés usando `/h2-console/`. La dependencia `spring-boot-h2console` es necesaria con Spring Boot 4.

### No aparecen los datos de ejemplo

Comprueba que estás usando la URL `jdbc:h2:file:./data/skates` y que la tabla `skates` no contiene registros previos. Los datos iniciales solo se cargan cuando la tabla está vacía.
