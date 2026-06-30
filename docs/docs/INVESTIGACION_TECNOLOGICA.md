# Investigación Tecnológica: Geovallados y Notificaciones Push

Este documento explora dos tecnologías clave para aumentar la retención y mejorar la experiencia de usuario en **Entra a la cancha**: el Geofencing (Geovallados) y las Notificaciones Push mediante Firebase Cloud Messaging (FCM).

---

## 1. Geovallados (Geofencing)

### ¿Qué es?
El geovallado es una tecnología que permite delimitar un perímetro geográfico virtual (generalmente un círculo con un radio determinado) alrededor de un punto de interés (POI). La aplicación puede "escuchar" cuándo el dispositivo entra, sale o permanece dentro de esa área.

### ¿Cómo funciona?
Se basa en la API de **Google Play Services Geofencing**. Funciona mediante tres eventos principales:
1.  **ENTER (Entrada)**: El usuario cruza el límite hacia adentro del círculo.
2.  **EXIT (Salida)**: El usuario sale del perímetro.
3.  **DWELL (Permanencia)**: El usuario entra y se queda un tiempo determinado (ej: 5 minutos) dentro del área.

**Componentes técnicos:**
- **GeofencingClient**: La interfaz principal para añadir o quitar vallas.
- **Geofence Object**: Define el ID, las coordenadas, el radio y el tiempo de expiración.
- **PendingIntent**: Un componente que despierta a la aplicación (incluso si está cerrada) para procesar el evento a través de un `BroadcastReceiver` o un `Service`.

---

## 2. Notificaciones Push (Firebase Cloud Messaging - FCM)

### ¿Qué es?
Es un servicio de mensajería multiplataforma que permite enviar mensajes de forma gratuita y fiable. Permite notificar a la aplicación cliente que hay datos nuevos disponibles para sincronizar o mostrar mensajes importantes para re-enganchar al usuario.

### ¿Cómo funciona?
1.  **Registro**: Al iniciar, la app solicita un **FCM Token** único al servidor de Google.
2.  **Almacenamiento**: La app envía ese token a nuestra base de datos (Firestore) y lo vincula al perfil del usuario.
3.  **Envío**: Cuando ocurre un evento (ej: alguien pide unirse a un partido), nuestro servidor (o una Cloud Function) envía un mensaje al token del destinatario.
4.  **Recepción**: Google entrega el mensaje al dispositivo. Si la app está en segundo plano, el sistema muestra la notificación automáticamente. Si está en primer plano, la app decide cómo manejarlo.

**Tipos de mensajes:**
- **Notification Messages**: El sistema operativo los muestra automáticamente.
- **Data Messages**: La app procesa los datos en silencio (útil para actualizar contenido sin molestar al usuario).

---

## 3. Implementación en "Entra a la cancha"

A continuación, se proponen casos de uso específicos para nuestra aplicación:

### Caso A: El "Check-in" Automático (Geovallados)
*   **Idea**: Cuando un usuario llega al complejo deportivo para su reserva, la app le da la bienvenida y le pregunta si quiere confirmar su llegada.
*   **Implementación**: Al realizar una reserva, la app registra un Geofence temporal alrededor de la cancha. Al detectar el evento `ENTER`, dispara una notificación local: *"¡Llegaste a El Túnel! ¿Confirmamos tu asistencia?"*.

### Caso B: Alertas de Cercanía (Geovallados)
*   **Idea**: Si hay un partido buscando jugadores a menos de 1km de donde está el usuario, enviarle una invitación.
*   **Implementación**: Monitorear la ubicación del usuario y comparar contra la lista de partidos `ABIERTO` en tiempo real.

### Caso C: Gestión de Solicitudes (FCM)
*   **Idea**: Avisar al organizador en el momento exacto en que alguien quiere unirse.
*   **Implementación**: Ya tenemos la base. Cuando un jugador pulsa "Quiero jugar", se dispara un trigger que envía un mensaje FCM al `fcmToken` del organizador.

### Caso D: Recordatorios de Partidos (FCM)
*   **Idea**: 2 horas antes de un partido, enviar un mensaje push a todos los confirmados.
*   **Implementación**: Una **Firebase Cloud Function** programada (Cron Job) que revisa los partidos próximos y envía notificaciones masivas a los `participantesIds`.

---

## 4. Comparativa Técnica

| Característica | Geovallados | Notificaciones Push (FCM) |
| :--- | :--- | :--- |
| **Dependencia** | GPS / WiFi / Torres Celulares | Conexión a Internet (Datos/WiFi) |
| **Consumo Batería** | Moderado (optimizado por Google) | Mínimo |
| **Uso Principal** | Acciones basadas en ubicación física | Acciones basadas en eventos del sistema |
| **Estado de la App** | Puede despertar la app si está cerrada | Puede despertar la app si está cerrada |

---
