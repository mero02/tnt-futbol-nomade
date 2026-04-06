# User Story Map — Fútbol Nómade
## 1. Actividades principales (Backbone)

Estas son las columnas del mapa (lo que hace el usuario de punta a punta):

1. Registrarse / Crear perfil
2. Explorar partidos
3. Unirse a partidos
4. Crear partido
5. Gestionar participación
6. Notificaciones y reputación

## 2. Tareas por actividad (User Stories)

Ahora bajamos cada actividad a tareas concretas (historias de usuario).

1. Registrarse / Crear perfil
- Crear cuenta
- Iniciar sesión
- Completar perfil:
    - Posición
    - Nivel de juego
    - Editar perfil

2. Explorar partidos
- Ver partidos cercanos (geolocalización)
- Filtrar partidos:
    - Fecha
    - Nivel
- Ver partidos en mapa
- Seleccionar un partido
- Ver detalle del partido

3. Unirse a partidos
- Enviar solicitud para unirse
- Ver estado de solicitud
- Cancelar solicitud

4. Crear partido
- Crear partido
- Definir:
    - Ubicación
    - Fecha y hora
    - Cantidad de jugadores faltantes
- Publicar partido

5. Gestionar participación
- Como organizador:
    - Ver solicitudes recibidas
    - Aceptar jugador
    - Rechazar jugador
- Como jugador:
    - Ver partidos en los que participa
    - Ver estado (confirmado / pendiente)

6. Notificaciones y reputación
- Ver perfil de otros jugadores
- Puntuar jugadores después del partido
- Ver reputación propia

--- 

## Historias de Usuario

### ACTIVIDAD 1: Registrarse y crear perfil

#### HU-01 — Registro de usuario

Como jugador que quiere usar la app por primera vez, quiero registrarme, para poder acceder a las funcionalidades del sistema.

##### Criterios de aceptación
- El sistema presenta un formulario de registro con campos básicos (email, contraseña)
- El usuario debe ingresar datos válidos para completar el registro
- Si los datos son incorrectos, el sistema muestra mensajes claros de error
- Al registrarse correctamente, el usuario accede a la aplicación
- El sistema guarda la cuenta para futuros accesos

##### Notas
- No incluir redes sociales en MVP
- Validación simple (no sobre-ingeniería)
--- 
#### HU-02 — Inicio de sesión

Como usuario registrado, quiero iniciar sesión, para acceder a mi cuenta y funcionalidades.

##### Criterios de aceptación
- El sistema presenta campos de email y contraseña
- Valida credenciales antes de permitir el acceso
- Si los datos son incorrectos, muestra error claro
- Permite reintentar sin perder datos
- Al iniciar sesión correctamente, accede a la app
--- 
#### HU-03 — Completar perfil de jugador

Como jugador, quiero completar mi perfil (posición y nivel), para que otros usuarios sepan cómo juego.

##### Criterios de aceptación
- El sistema solicita posición (defensor, mediocampo, delantero, etc.)
- El sistema solicita nivel (bajo, medio, alto)
- Ambos campos son obligatorios para continuar
- El usuario puede modificar estos datos posteriormente
- La información se muestra en su perfil visible

##### Notas
- No usar categorías complejas
- Mantener simple para MVP
--- 
#### HU-04 — Editar perfil

Como jugador, quiero editar mi perfil, para mantener mi información actualizada.

##### Criterios de aceptación
- El usuario puede modificar posición y nivel
- Los cambios se guardan correctamente
- La información actualizada se refleja en el perfil
- El sistema confirma que los cambios fueron guardados

---
### ACTIVIDAD 2: Explorar partidos

#### HU-05 — Visualizar partidos cercanos

Como jugador nómade o local, quiero ver partidos cercanos a mi ubicación, para encontrar oportunidades de juego disponibles.

##### Criterios de aceptación
- El sistema obtiene la ubicación del usuario (con permiso)
- Se muestran partidos ordenados por cercanía
- Cada partido muestra:
    - Fecha
    - Ubicación
    - Jugadores faltantes
- Si no hay partidos, se muestra un mensaje informativo
- El usuario puede seleccionar un partido para ver más detalles

##### Notas
- Feature clave del producto
- Directamente ligado al valor mobile
---
#### HU-06 — Ver partidos en mapa

Como jugador, quiero ver los partidos en un mapa, para ubicarme visualmente.

##### Criterios de aceptación
- El sistema muestra un mapa con marcadores de partidos
- Cada marcador representa un partido
- Al seleccionar un marcador, se muestra información básica
- Se puede acceder al detalle desde el mapa
---
#### HU-07 — Filtrar partidos

Como jugador, quiero filtrar partidos por fecha o nivel, para encontrar opciones acordes a mi disponibilidad.

##### Criterios de aceptación
- El sistema permite aplicar filtros (fecha, nivel)
- Los resultados se actualizan según los filtros
- El usuario puede limpiar los filtros
- Los filtros seleccionados son visibles
---
#### HU-08 — Ver detalle de partido

Como jugador, quiero ver el detalle de un partido, para decidir si me conviene unirme.

##### Criterios de aceptación
- El sistema muestra:
    - Fecha y hora
    - Ubicación
    - Jugadores confirmados
    - Cupos disponibles
- El usuario puede ver quiénes participan (info básica)
- Se muestra un botón claro para unirse al partido
- La información debe ser clara y fácil de interpretar
---
#### HU-27 — Detección automática de ubicación

Como jugador, quiero que la app detecte mi ubicación automáticamente, para ver partidos cercanos sin configuraciones manuales.

##### Criterios de aceptación
- El sistema solicita permiso de ubicación
- Si se concede, obtiene ubicación actual
- Los partidos se ordenan por cercanía
- Si se rechaza el permiso, se ofrece alternativa manual
---
### ACTIVIDAD 3: Unirse a partidos

#### HU-09 — Solicitar unirse a un partido

Como jugador, quiero solicitar unirme a un partido, para poder participar en él.

##### Criterios de aceptación
- El usuario puede presionar “Unirme” desde el detalle del partido
- El sistema registra la solicitud
- El estado inicial es “pendiente”
- El usuario recibe confirmación visual de que la solicitud fue enviada
- No puede enviar múltiples solicitudes al mismo partido

##### Notas
- No es confirmación automática (hay organizador)
---
#### HU-10 — Ver estado de solicitud

Como jugador, quiero ver el estado de mi solicitud, para saber si fui aceptado o no.

##### Criterios de aceptación
- El sistema muestra estado:
    - Pendiente
    - Aceptado
    - Rechazado
- El estado se actualiza automáticamente o al refrescar
- El usuario puede acceder desde “Mis partidos”
- El estado es visible claramente
---
#### HU-11 — Cancelar solicitud

Como jugador, quiero cancelar mi solicitud, para liberar mi lugar si ya no puedo asistir.

##### Criterios de aceptación
- El usuario puede cancelar solicitudes pendientes
- El sistema actualiza el estado a “cancelada”
- El organizador deja de ver esa solicitud
- El usuario recibe confirmación del cambio
---
### ACTIVIDAD 4: Crear partido

#### HU-12 — Crear partido

Como organizador, quiero crear un partido, para invitar jugadores y completar mi equipo.

##### Criterios de aceptación
- El sistema presenta un formulario con:
    - Ubicación
    - Fecha y hora
    - Cantidad de jugadores faltantes
- Todos los campos obligatorios deben completarse
- El sistema valida que los datos sean correctos
- El partido queda registrado al confirmar
---
#### HU-13 — Definir datos del partido

Como organizador, quiero definir ubicación, fecha y hora, para organizar correctamente el partido.

##### Criterios de aceptación
- El sistema permite ingresar ubicación
- Permite seleccionar fecha y hora
- Valida que la fecha sea futura
- No permite campos vacíos
---
#### HU-14 — Definir cantidad de jugadores faltantes

Como organizador, quiero indicar cuántos jugadores faltan, para completar el equipo.

##### Criterios de aceptación
- El sistema permite ingresar número de jugadores
- El valor debe ser mayor a 0
- El valor se refleja en el partido publicado
- Se actualiza al aceptar jugadores
---
#### HU-15 — Publicar partido

Como organizador, quiero publicar un partido, para que otros jugadores puedan verlo y unirse.

##### Criterios de aceptación
- El partido creado se vuelve visible para otros usuarios
- Aparece en la lista de partidos cercanos
- Se puede acceder a su detalle
- El sistema permite recibir solicitudes de jugadores
---
#### HU-30 — Solicitar jugadores urgentemente

Como organizador, quiero solicitar jugadores urgentemente, para cubrir vacantes cuando se acerque la hora del partido.

##### Criterios de aceptación
- El organizador puede marcar un partido como "urgente"
- El sistema envía notificaciones push a jugadores cercanos
- La notificación indica claramente que es una solicitud urgente
- Los jugadores reciben la notificación en tiempo real
- El usuario puede desactivar este tipo de notificaciones
---
### ACTIVIDAD 5: Gestionar participación
#### HU-16 — Ver solicitudes recibidas

Como organizador, quiero ver las solicitudes de jugadores, para decidir a quién aceptar.

##### Criterios de aceptación
- El sistema muestra lista de solicitudes
- Cada solicitud incluye info básica del jugador
- Se puede acceder al perfil del jugador
- Las solicitudes están claramente identificadas
---
#### HU-17 — Aceptar jugador

Como organizador, quiero aceptar jugadores, para completar el equipo de mi partido.

##### Criterios de aceptación
- El organizador puede ver lista de solicitudes
- Puede aceptar un jugador individualmente
- El estado del jugador pasa a “aceptado”
- Se reduce la cantidad de cupos disponibles
- El jugador es notificado del cambio
---
#### HU-18 — Rechazar jugador

Como organizador, quiero rechazar jugadores, para mantener control sobre el equipo.

##### Criterios de aceptación
- El organizador puede rechazar solicitudes
- El estado pasa a “rechazado”
- El jugador es notificado
- La solicitud deja de estar activa
---
#### HU-19 — Ver mis partidos

Como jugador, quiero ver mis partidos, para organizar mi participación.

##### Criterios de aceptación
- El sistema muestra lista de partidos del usuario
- Incluye próximos partidos
- Permite acceder al detalle
- La información es clara y ordenado
---
#### HU-20 — Ver estado de participación

Como jugador, quiero ver si estoy confirmado o pendiente, para saber mi situación en el partido.

##### Criterios de aceptación
- El estado se muestra claramente
- Puede ser: pendiente, aceptado o rechazado
- Se actualiza correctamente
- Es visible desde la lista y el detalle
---
#### HU-28 — Cancelar partido

Como organizador, quiero cancelar un partido, para informar a los jugadores que no se realizará.

##### Criterios de aceptación
- El organizador puede cancelar el partido
- El sistema cambia el estado a “cancelado”
- Los jugadores reciben una notificación
- El partido deja de aparecer como disponible
- El sistema evita nuevas solicitudes
---
#### HU-29 — Abandonar partido

Como jugador, quiero abandonar un partido, para liberar mi lugar si no puedo asistir.

##### Criterios de aceptación
- El usuario puede salir de un partido confirmado
- El sistema libera el cupo
- El organizador es informado
- El estado del usuario se actualiza
---
### ACTIVIDAD 6: Notificaciones y Reputación

#### HU-21 — Calificar jugadores

Como jugador, quiero calificar a otros jugadores, para aportar a la confianza de la comunidad.

##### Criterios de aceptación
- El sistema permite calificar después del partido
- La calificación es simple (ej: puntuación)
- Solo participantes pueden calificar
- La calificación se guarda correctamente
---
#### HU-22 — Ver reputación de jugadores

Como jugador, quiero ver la reputación de otros, para decidir si me conviene jugar con ellos.

##### Criterios de aceptación
- El perfil muestra puntuación
- Se basa en calificaciones previas
- Es visible antes de aceptar o unirse
- Se presenta de forma clara
---
#### HU-23 — Ver historial de partidos

Como jugador, quiero ver mi historial, para recordar mis participaciones.

##### Criterios de aceptación
- El sistema muestra partidos pasados
- Incluye fecha y resultado básico
- Permite acceder a detalles
- Se diferencia de partidos futuros
---
#### HU-24 — Ver reputación propia

Como jugador, quiero ver mi reputación, para conocer cómo me perciben otros jugadores.

##### Criterios de aceptación
- El sistema muestra puntuación acumulada
- Se basa en evaluaciones recibidas
- Es visible en el perfil
- Se actualiza tras nuevas calificaciones
---
#### HU-25 — Notificación de partidos cercanos

Como jugador, quiero recibir notificaciones de partidos cercanos, para no perder oportunidades.

##### Criterios de aceptación
- El sistema envía notificaciones según ubicación
- Incluye información básica del partido
- El usuario puede desactivarlas
- Se puede acceder al detalle desde la notificación
---
#### HU-26 — Notificación de aceptación

Como jugador, quiero recibir una notificación cuando me aceptan en un partido, para poder organizar mi tiempo.

##### Criterios de aceptación
- El sistema envía una notificación al ser aceptado
- La notificación incluye:
    - Nombre del partido
    - Fecha y hora
- El usuario puede acceder al detalle desde la notificación
- La notificación se guarda en el historial de la app
---
#### HU-32 — Comentar sobre jugadores

Como jugador, quiero dejar un comentario breve sobre otros jugadores, para dar contexto a mi calificación.

##### Criterios de aceptación
- El sistema permite agregar un comentario junto con la calificación
- El comentario es opcional
- Solo el recipient puede ver el comentario
- El comentario tiene límite de caracteres

---

#### HU-33 — Verificar asistencia al partido

Como organizador, quiero confirmar la asistencia de los jugadores, para mejorar la reputación del sistema.

##### Criterios de aceptación
- El organizador puede marcar qué jugadores asistieron
- Los jugadores que no asistieron receive downvotes automáticos
- Se registra la asistencia para estadísticas futuras
- El sistema actualiza la reputación automáticamente
---
### ACTIVIDAD 7: Gestión Admin

#### HU-31 — Acceder al panel administrativo

Como administrador, quiero acceder a un panel web, para gestionar usuarios y partidos.

##### Criterios de aceptación
- El sistema proporciona una interfaz web segura
- El admin puede visualizar todos los partidos
- El admin puede visualizar usuarios y su reputación
- El admin puede banear usuarios problemáticos
- El admin puede cancelar partidos problemáticos

##### Notas
- Feature opcional para MVP
- Puede implementarse en fase 2