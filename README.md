# Skates Backend - API REST

> Servidor de backend desarrollado con Java y Spring Boot para la gestión de productos, inventario y operaciones de la tienda de skate.

---

## Tecnologías y Frameworks

* **Java** (JDK 17 o superior)
* **Spring Boot** (Framework principal para la creación de la API REST)
* **Maven** (Gestión de dependencias y empaquetado)
* **Spring Data JPA / Hibernate** (Conexión y persistencia con la base de datos)

---

## 📡 Endpoints Principales (`/api/skate`)

El microservicio expone los siguientes endpoints principales bajo la ruta base `/api/skate`:

* **`GET /api/skate`** - Lista todos los skates y productos disponibles.
* **`GET /api/skate/{id}`** - Obtiene los detalles de un skate específico mediante su ID.
* **`POST /api/skate`** - Da de alta un nuevo producto/skate en el sistema.
* **`PUT /api/skate/{id}`** - Actualiza la información de un skate existente.
* **`DELETE /api/skate/{id}`** - Elimina un registro del sistema.

---
