# Arquitectura del Proyecto - Entra a la cancha

Este documento describe la arquitectura técnica del ecosistema Futbol TNT, implementado como un **Monorepo** que integra una aplicación móvil Android y un panel administrativo Web.

---

## 1. Estructura de Repositorio (Monorepo)

El proyecto utiliza una estructura de monorepo para facilitar la consistencia de datos y la gestión de Firebase:

```text
tnt-futbol-nomade/
├── app/              # Aplicación Móvil (Android - Kotlin/Compose)
├── admin-web/        # Panel Administrativo (React + Vite)
├── docs/             # Documentación del proyecto
│   └── docs/         # Documentación técnica detallada

```

---

## 2. Arquitectura de la App Móvil (Android)

La aplicación Android sigue el patrón de arquitectura **MVVM (Model-View-ViewModel)** recomendado por Google, separando las responsabilidades en capas claras:

### A. Capa de Presentación (UI)
- **Tecnología**: Jetpack Compose (Declarativa).
- **Navegación**: Utiliza `Compose Navigation` con una estructura centralizada en `AppNavigation.kt`.
- **Componentes**: Pantallas modulares (`screens/`) y componentes reutilizables (`components/`).

### B. Capa de Lógica de Negocio (ViewModel)
- Actúa como puente entre la UI y los Datos.
- Gestiona el estado de la pantalla mediante `StateFlow`.
- Implementa `sealed classes` para el manejo de Eventos (ej: `PartidoEvento`).

### C. Capa de Datos (Repository)
- **Repositorios**: Implementan interfaces para facilitar el testeo y la abstracción (ej: `IPartidoRepository`).
- **Fuentes de Datos**: Principalmente **Firebase Firestore** y **Firebase Auth**.
- **Servicios**: `FutbolTntMessagingService` gestiona la recepción de notificaciones push en segundo plano.

### D. Capa de Dominio (Model)
- Define los objetos de datos puros (`data classes`) como `Cancha`, `Partido`, `User`, etc.

---

## 3. Arquitectura del Panel Administrativo (Web)

El panel web está diseñado para la agilidad y la gestión de datos maestros:

- **Framework**: React 18 con Vite para compilación instantánea.
- **Estilos**: Tailwind CSS (coherente con el sistema de diseño móvil).
- **Firebase SDK**: Conexión directa a la misma instancia de Firestore que la App.
- **Estructura**:
    - `src/lib/`: Utilidades de inicialización de Firebase.
    - `src/components/`: UI atómica (Cards, Botones).
    - `src/pages/`: Vistas de alto nivel (Dashboard, Gestión de Canchas).

---

## 4. Ecosistema Compartido (Firebase)

Ambos proyectos dependen de una infraestructura común en la nube:

1.  **Firebase Auth**: Gestión única de identidad (Login con Google).
2.  **Cloud Firestore**: Base de datos NoSQL en tiempo real. Actúa como la "única fuente de verdad".
3.  **Cloud Messaging (FCM)**: Canal de comunicación para enviar alertas desde el servidor (o panel admin) hacia los usuarios.
4.  **Deep Linking**: Los enlaces `https://futboltnt.app` y el esquema `futboltnt://` permiten la navegación fluida entre la web/WhatsApp y la app nativa.

---

## 5. Estrategia de Comunicación

- **Unidireccional**: La App Android no crea "establecimientos" (canchas); solo los consume.
- **Administración**: La creación de canchas y la gestión global se delega al `admin-web`.
- **Interacción**: Los usuarios (Jugadores) crean `partidos` y `reservas` que el `admin-web` monitorea en tiempo real.

---
