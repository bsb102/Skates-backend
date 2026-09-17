# Skates Backend - API REST

> Servidor de backend desarrollado con Java y Spring Boot para la gestión de productos, inventario y operaciones de la tienda de skate.

---

## Tecnologías y Frameworks

* **Java** (JDK 17 o superior)
* **Spring Boot** (Framework principal para la creación de la API REST)
* **Maven** (Gestión de dependencias y empaquetado)
* **Spring Data JPA / Hibernate** (Conexión y persistencia con la base de datos)

---

## Endpoints Principales (`/api/skate`)

El microservicio expone los siguientes endpoints principales bajo la ruta base `/api/skate`:

* **`GET /api/skate`** - Lista todos los skates y productos disponibles.
* **`GET /api/skate/{id}`** - Obtiene los detalles de un skate específico mediante su ID.
* **`GET /api/skate/modelos`** - Lista los tipos de skate disponibles sin repetir.
* **`GET /api/skate/modelo/{modelo}`** - Lista los skates de un modelo específico, por ejemplo `Street`, `Longboard` o `Downhill`.
* **`POST /api/skate`** - Da de alta un nuevo producto/skate en el sistema.
* **`PUT /api/skate/{id}`** - Actualiza la información de un skate existente.
* **`DELETE /api/skate/{id}`** - Elimina un registro del sistema.

## Base de datos local H2

El backend usa H2 en modo archivo para desarrollo local. Los datos se guardan en `./data/skates` y permanecen disponibles entre reinicios.

Para ejecutar el backend:

```powershell
./mvnw.cmd spring-boot:run
```

Hibernate creará o actualizará la tabla `skates` automáticamente y la aplicación cargará los datos de prueba si la tabla está vacía. La consola H2 queda disponible en `http://localhost:8080/h2-console`.

---
