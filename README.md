# Proyecto Topografía App

Aplicación móvil nativa en Android (Kotlin) diseñada para optimizar la gestión de Puntos de Referencia (PRs) y levantamientos topográficos en terreno.

## Estado Actual del Desarrollo
- [x] **Módulo 1:** Diseño de base de datos relacional (MySQL) para Fundos, Canchas y PRs.
- [x] **Módulo 2:** Creación de API RESTful en PHP para consumo de datos.
- [x] **Módulo 3:** Configuración de cliente HTTP (Retrofit + Gson) en Android.
- [x] **Módulo 4:** Conexión exitosa entre la app móvil y el servidor local.
- [ ] **Siguiente paso:** Implementación de interfaz visual (RecyclerView) para lista de Fundos.

## 🛠️ Tecnologías Utilizadas
* **Frontend Móvil:** Kotlin, Android Studio, XML.
* **Comunicaciones:** Retrofit2, Gson.
* **Backend:** PHP (PDO).
* **Base de Datos:** MySQL (Diseño y pruebas con Workbench / phpMyAdmin).

## 📝 Notas de Arquitectura
El proyecto utiliza una arquitectura Cliente-Servidor mediante API REST, preparándose para una futura implementación *Offline-First* con base de datos local (Room) para zonas sin cobertura de red.