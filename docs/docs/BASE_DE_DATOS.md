# Esquema de Base de Datos - Entra a la cancha (Cloud Firestore)

Este documento describe la estructura de las colecciones y documentos en Firebase Firestore para el proyecto Entra a la cancha.

---

## 1. Colección: `users`
Almacena la información de perfil de los jugadores y organizadores.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `uid` | String | ID único de Firebase Auth. |
| `email` | String | Correo electrónico del usuario. |
| `displayName` | String | Nombre para mostrar (desde Google). |
| `photoUrl` | String | URL de la foto de perfil. |
| `apodo` | String | Sobrenombre elegido por el usuario. |
| `posicion` | String | Enum: `ARQUERO`, `DEFENSA`, `MEDIOCAMPISTA`, `DELANTERO`, `MULTIPOSICION`. |
| `nivel` | String | Enum: `PRINCIPIANTE`, `INTERMEDIO`, `AVANZADO`, `PROFESIONAL`. |
| `telefono` | String | Número de contacto. |
| `biografia` | String | Breve descripción del jugador. |
| `valoracionPromedio` | Double | Promedio de estrellas recibidas (0.0 a 5.0). |
| `fcmToken` | String | Token para notificaciones push (Firebase Cloud Messaging). |
| `sexo` | String | Enum: `HOMBRE`, `MUJER`, `OTRO`. |
| `piernaDominante` | String | Enum: `DIESTRO`, `ZURDO`, `AMBIDIESTRO`. |

---

## 2. Colección: `canchas`
Establecimientos deportivos disponibles para reserva.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `nombre` | String | Nombre del complejo deportivo. |
| `direccion` | String | Calle y número. |
| `ciudad` | String | Localidad (Trelew, Madryn, etc.). |
| `lat` / `lng` | Double | Coordenadas geográficas para el mapa. |
| `precioPorHora` | Double | Valor base del turno. |
| `tipo` | String | Enum: `FUTBOL_5`, `F7`, `F11`, `PADDEL`. |
| `imagenUrl` | String | Foto del complejo. |

---

## 3. Colección: `partidos`
Encuentros organizados a los que los usuarios pueden unirse.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `nombreLocal` | String | Nombre del equipo que organiza o "Local". |
| `nombreVisitante` | String | Nombre del equipo rival o "Invitados". |
| `fecha` | Timestamp | Fecha y hora de inicio del partido. |
| `cancha` | Map | Objeto embebido con datos de la cancha (denormalizado para velocidad). |
| `jugadoresMaximos` | Int | Cupo total (ej: 10 para F5). |
| `jugadoresActuales` | Int | Cantidad de personas ya confirmadas. |
| `precioPorPersona` | Double | Costo que debe abonar cada jugador. |
| `estado` | String | Enum: `ABIERTO`, `LLENO`, `EN_JUEGO`, `FINALIZADO`. |
| `creatorId` | String | UID del usuario que creó el partido. |
| `participantesIds` | List<String> | Array de UIDs de usuarios confirmados. |
| `solicitudesIds` | List<String> | Array de UIDs de usuarios esperando aprobación. |
| `reservaId` | String | (Opcional) Referencia al documento en la colección `reservas`. |

---

## 4. Colección: `reservas`
Bloqueos de turnos realizados en un complejo.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `usuarioId` | String | UID del usuario que realizó la reserva. |
| `fecha` | Timestamp | Fecha y hora exacta del turno. |
| `duracionHoras` | Double | Tiempo de juego. |
| `precioTotal` | Double | Costo total del turno. |
| `montoPagado` | Double | Cantidad ya abonada (seña o total). |
| `estado` | String | Enum: `PENDIENTE`, `CONFIRMADA`, `COMPLETADA`. |
| `cancha` | Map | Objeto embebido con datos de la cancha. |

---

## 5. Colección: `notificaciones`
Alertas enviadas a los usuarios.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `userId` | String | UID del destinatario. |
| `titulo` | String | Título breve (ej: "Nueva Solicitud"). |
| `mensaje` | String | Cuerpo de la notificación. |
| `tipo` | String | Enum: `SOLICITUD_RECIBIDA`, `SOLICITUD_APROBADA`, `CANCELACION`, `INFO`. |
| `leido` | Boolean | Estado de lectura (falso por defecto). |
| `partidoId` | String | (Opcional) Link al partido relacionado para navegación directa. |
| `fecha` | Timestamp | Cuándo se generó la alerta. |

---

## Consideraciones de Denormalización
En Entra a la cancha utilizamos **denormalización estratégica** (copiar datos de `canchas` dentro de `partidos` y `reservas`). 
- **Razón**: Permite mostrar la lista de partidos sin realizar múltiples consultas a la colección de canchas, optimizando el rendimiento y reduciendo costos de lectura en Firebase.


---
