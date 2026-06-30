# Flujos Críticos de Usuario - Entra a la cancha

Este documento detalla los procesos clave que los usuarios realizan dentro del ecosistema de "Entra a la cancha".

---

## 1. Flujo de Autenticación y Sincronización
**Objetivo**: Asegurar que cada jugador tenga un perfil persistente vinculado a su cuenta de Google.

1.  El usuario inicia sesión con **Google Sign-In**.
2.  La app obtiene el `idToken` y lo valida con **Firebase Auth**.
3.  **Sincronización**: El `AuthViewModel` envía los datos básicos (email, nombre, foto) al `UserRepository`.
4.  Si el usuario es nuevo, se crea un documento en la colección `users`. Si ya existe, se actualizan los datos básicos manteniendo su perfil deportivo (posición, nivel, etc.).
5.  Se genera y guarda el `fcmToken` (Firebase Cloud Messaging) para habilitar notificaciones push.

---

## 2. Ciclo de Reserva y Creación de Partido
**Objetivo**: Pasar de una búsqueda de cancha a un partido organizado con cupos abiertos.

1.  **Búsqueda**: El usuario filtra canchas por ciudad o cercanía (GPS) en `CanchasTab`.
2.  **Selección**: Elige un complejo y visualiza la disponibilidad en `CanchaDetailScreen`.
3.  **Reserva**: Selecciona fecha, hora y duración. Se realiza una transacción en Firestore para evitar solapamientos.
4.  **Checkout**: El sistema registra la reserva en estado `CONFIRMADA`.
5.  **Conversión a Partido**: Automáticamente, la app redirige al usuario a `CrearPartidoScreen`.
6.  El usuario completa los nombres de los equipos y cupos. El partido nace vinculado al `reservaId`.

---

## 3. Sistema de Invitación y Unirse a Partido
**Objetivo**: Viralizar el partido y completar los cupos de forma rápida.

1.  **Generación de Link**: El organizador genera un enlace dinámico (`https://futboltnt.app/partido/{id}`) desde el detalle del partido.
2.  **Compartir**: Se envía vía WhatsApp o se muestra un **Código QR** presencialmente.
3.  **Recepción (Deep Link)**: Un invitado toca el link. Android intercepta la URL y abre la app "Entra a la cancha".
4.  **Validación**: Si el invitado no está logueado, pasa por el login y es redirigido automáticamente al partido.
5.  **Solicitud**: El invitado presiona "Quiero jugar". Se añade su UID a `solicitudesIds` del partido en Firestore.

---

## 4. Gestión de Jugadores (Organizador)
**Objetivo**: Controlar quién entra al equipo.

1.  El organizador recibe una **Notificación Push** y un alerta en su bandeja de entrada (punto rojo).
2.  Toca la notificación y salta directo a la pantalla de detalle del partido.
3.  Abre el diálogo de **"Solicitudes Pendientes"**.
4.  **Decisión**:
    *   **Aceptar**: El UID del jugador pasa de `solicitudesIds` a `participantesIds`. Se incrementa `jugadoresActuales`.
    *   **Rechazar**: Se elimina de las solicitudes.
5.  **Notificación de Cierre**: El sistema envía una notificación automática al jugador informándole el resultado de su solicitud.

---

## 5. Flujo de Notificaciones en Tiempo Real
**Objetivo**: Mantener a todos los involucrados informados sin refrescar la app.

1.  **Listener Activo**: Los ViewModels (`PartidoViewModel`, `NotificacionViewModel`) mantienen una conexión abierta (Snapshots) con Firestore.
2.  **Actualización de UI**: Cuando un organizador acepta a un jugador, la lista de participantes se actualiza instantáneamente en el celular de todos los que estén viendo ese partido.
3.  **Historial**: El usuario puede revisar en cualquier momento la pestaña de Notificaciones para ver el rastro de sus partidos confirmados o cancelados.

---
