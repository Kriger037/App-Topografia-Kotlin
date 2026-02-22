# Proyecto Topografía App

Aplicación móvil nativa en Android (Kotlin) diseñada para optimizar la gestión de Puntos de Referencia (PRs) y levantamientos topográficos en terreno.

## Estado Actual del Desarrollo (Fase MVP)
- [x] **Módulo 1:** Diseño de base de datos relacional (MySQL) para Fundos, Canchas y PRs.
- [x] **Módulo 2:** Creación de API RESTful en PHP para consumo de datos.
- [x] **Módulo 3:** Configuración de cliente HTTP (Retrofit + Gson) en Android.
- [x] **Módulo 4:** Conexión exitosa entre la app móvil y el servidor local.
- [x] **Módulo 5:** Implementación de interfaz visual (RecyclerView) y adaptadores para la lista dinámica de Fundos.
- [x] **Módulo 6:** Navegación relacional (Intents) y paso de parámetros entre pantallas, incorporando Toolbars personalizadas.
- [x] **Módulo 7:** Evolución de BD con tablas dependientes (`canchas`), uso de Llaves Naturales (`codigo_fundo`) y auditoría de tiempo (`TIMESTAMP`).
- [x] **Módulo 8:** Evolución de API con peticiones GET parametrizadas e `INNER JOIN` relacional.
- [ ] **Siguiente paso:** Implementación visual (RecyclerView) para la lista de Canchas asociadas al fundo seleccionado y botón de exportación a .txt.

## 🛠️ Tecnologías Utilizadas
* **Frontend Móvil:** Kotlin, Android Studio, XML, Intents, RecyclerViews, Custom Toolbars.
* **Comunicaciones:** Retrofit2, Gson.
* **Backend:** PHP (PDO, control de Inyección SQL, respuestas JSON).
* **Base de Datos:** MySQL (Llaves Naturales, Foreign Keys en Cascada, Workbench).

## 📝 Notas de Arquitectura
El proyecto utiliza una arquitectura Cliente-Servidor mediante API REST para el MVP. Se está diseñando con la visión de una futura implementación *Offline-First* con base de datos local (Room) para zonas sin cobertura de red en cerros o faenas, además de contemplar la exportación de datos directos a archivos `.txt` para integración con Estaciones Totales o AutoCAD.