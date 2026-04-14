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
- [x] **Módulo 9:** Interfaz de Canchas con paso de parámetros (Intents) y filtrado en servidor.
- [x] **Módulo 10:** Creación de tabla `puntos_referencia` con Triggers para herencia de fechas.
- [x] **Módulo 11:** Implementación de interfaz visual tipo tabla (`activity_prs.xml` y `item_pr.xml`) para lectura ordenada de coordenadas (Norte, Este, Cota).
- [x] **Módulo 12:** Integración de Google Maps SDK para visualización satelital de los Puntos de Referencia.
- [x] **Módulo 13:** Implementación de seguridad (`local.properties` y `BuildConfig`) para inyección dinámica de credenciales y ocultamiento de IP/API Keys.
- [x] **Módulo 14:** Consolidación del sistema de Autenticación (Login) y visualización satelital en terreno con conversión estricta de coordenadas a 3 decimales y pase de parámetros.
- [ ] **Siguiente paso:** Desarrollar script en PHP para la ingesta automatizada y conversión geodésica de coordenadas (UTM a WGS84) desde archivos `.txt`, y evaluar migración a arquitectura Offline-First para visualización de mapas sin señal.

## ⚠️ Importante: Configuración del Entorno Local (Seguridad)

Para proteger la integridad del proyecto, **la dirección IP del servidor y la API Key de Google Maps no se suben a este repositorio**. Si clonas este proyecto para ejecutarlo localmente, debes configurar las variables de entorno para que el proyecto pueda compilar y conectarse a la red:

1. En la vista de proyecto de Android Studio, crea o localiza el archivo `local.properties` en el directorio raíz (este archivo está ignorado por Git).
2. Agrega las siguientes dos líneas con tus propios datos:
   ```properties
   # Reemplaza con tu clave generada en Google Cloud Console
   MAPS_API_KEY=TuClaveSecretaDeGoogleMaps
   
   # Reemplaza con la IP de tu servidor (Asegúrate de incluir "http://" y finalizar con "/")
   # Usa 10.0.2.2/ si usas el emulador local, o tu IP física (ej. 192.168.1.X/) para dispositivos físicos
   SERVER_BASE_URL=[http://10.0.2.2/](http://10.0.2.2/)
   
## Tecnologías Utilizadas
* **Frontend Móvil:** Kotlin, Android Studio, XML, Intents, RecyclerViews, Custom Toolbars, CardViews.
* **Comunicaciones:** Retrofit2, Gson.
* **Backend:** PHP (PDO, control de Inyección SQL, respuestas JSON).
* **Base de Datos:** MySQL (Llaves Naturales, Foreign Keys en Cascada, Workbench).

## Notas de Arquitectura
El proyecto utiliza una arquitectura Cliente-Servidor mediante API REST para el MVP. Se está diseñando con la visión de una futura implementación *Offline-First* con base de datos local (Room) para zonas sin cobertura de red en cerros o faenas, además de contemplar la exportación de datos directos a archivos `.txt` para integración con Estaciones Totales o AutoCAD.