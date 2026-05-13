# User Story Map — Fútbol Nómade
## 1. Actividades principales (Backbone)

Estas son las columnas del mapa (lo que hace el usuario de punta a punta):

1. Registrarse / Crear perfil
2. Explorar canchas y partidos
3. Reservar cancha (Creación del vínculo contable)
4. Gestionar Partido (La experiencia de juego)
5. Unirse a partidos
6. Gestionar participación / Reservas
7. Notificaciones y reputación

## 2. Tareas por actividad (User Stories)

Ahora bajamos cada actividad a tareas concretas (historias de usuario).

1. Registrarse / Crear perfil
- Crear cuenta
- Iniciar sesión
- Completar perfil (Posición, Nivel, etc.)

2. Explorar
- Ver partidos cercanos (geolocalización)
- Ver canchas disponibles
- Filtrar por fecha, nivel o tipo de cancha

3. Reservar cancha (Contable/Legal)
- Seleccionar fecha, hora y duración
- Realizar reserva (genera vínculo con el complejo)
- **HU-34 — Confirmación y pago**: El sistema asegura el lugar.

4. Gestionar Partido (Social/Juego)
- **HU-35 — Crear partido desde reserva (CONEXIÓN OBLIGATORIA)**: Toda reserva exitosa inicializa un "Partido" para gestionar los jugadores.
- Invitar amigos directamente al partido (vía link o búsqueda).
- Definir si el partido es Privado (solo invitados) o Público (aparece en "Explorar").
- Definir cantidad de jugadores faltantes.

5. Unirse a partidos
- Enviar solicitud para unirse (solo en partidos Públicos).
- Ver estado de solicitud.

6. Gestionar participación / Reservas
- Como organizador: Gestionar lista de convocados.
- Como jugador: Ver mis partidos confirmados y mis reservas (pagos).

7. Notificaciones y reputación
- Puntuar jugadores después del partido (basado en el Partido, no en la Reserva).
- Ver reputación acumulada.

--- 

## Historias de Usuario Detalladas

### ACTIVIDAD 3: Reservar cancha (El "Ticket")

#### HU-34 — Reservar cancha
Como jugador, quiero reservar una cancha seleccionando fecha y hora, para asegurar el lugar donde jugar.
- **Criterios:** 
    - Genera una entrada en la colección `reservas`.
    - Registra el precio total y los datos de la cancha.
    - El estado inicial es Pendiente/Confirmada.

---

### ACTIVIDAD 4: Gestionar Partido (El "Juego")

#### HU-35 — Inicializar Partido desde Reserva
Como organizador, quiero que al confirmar una reserva se cree automáticamente un espacio de "Partido", para poder gestionar quiénes van a jugar.
- **Criterios (Separación de conceptos):**
    - El **Partido** hereda fecha/hora/cancha de la **Reserva**, pero vive en su propia colección.
    - El Partido permite invitar jugadores (HU-38).
    - Permite la futura valoración de jugadores (la reserva no sabe de goles, el partido sí).
    - Si el partido es "Público", se muestra en la pestaña Explorar.

#### HU-38 — Invitar jugadores al partido
Como organizador, quiero invitar a mis amigos a unirse al partido mediante un enlace o búsqueda interna, para completar el equipo rápidamente.

---

### ACTIVIDAD 6: Gestión y Estados

#### HU-36 — Mis Reservas vs Mis Partidos
Como usuario, quiero distinguir entre mis pagos/reservas (lo contable) y mis próximos encuentros (lo social), para tener claridad de mis compromisos.

#### HU-37 — Cancelar Reserva y Partido
Como organizador, quiero que al cancelar una reserva, el partido asociado se cancele automáticamente, notificando a los jugadores unidos.

---

### ACTIVIDAD 7: Notificaciones y Reputación
*(HU-21 a HU-26 — La reputación se vincula al ID del Partido)*
