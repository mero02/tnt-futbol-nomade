# Entra a la cancha — Historias de Usuario

Tablero Trello. Miembros: Francisco Terron, Mauro G. San Pedro.


## Hechas (33)


### Sprint 1


#### HU-01 — Registro de usuario

Como jugador que quiere usar la app por primera vez, quiero registrarme, para poder acceder a las funcionalidades del sistema.

**Notas**

- No incluir redes sociales en MVP
- Validación simple (no sobre-ingeniería)

**Criterios de aceptacion:**

- [x] El sistema presenta un formulario de registro con campos básicos (email, contraseña)
- [x] El usuario debe ingresar datos válidos para completar el registro
- [x] Si los datos son incorrectos, el sistema muestra mensajes claros de error
- [x] Al registrarse correctamente, el usuario accede a la aplicación
- [x] El sistema guarda la cuenta para futuros accesos


#### HU-02 — Inicio de sesión

Como usuario registrado, quiero iniciar sesión, para acceder a mi cuenta y funcionalidades.

**Criterios de aceptacion:**

- [x] El sistema presenta campos de email y contraseña
- [x] Valida credenciales antes de permitir el acceso
- [x] Si los datos son incorrectos, muestra error claro
- [x] Permite reintentar sin perder datos
- [x] Al iniciar sesión correctamente, accede a la app


#### HU-12 — Crear partido

Como organizador, quiero crear un partido, para invitar jugadores y completar mi equipo.

**Criterios de aceptacion:**

- [x] El sistema presenta un formulario con: Ubicación
- [x] El sistema presenta un formulario con:  Fecha y hora
- [x] El sistema presenta un formulario con:  Cantidad de jugadores faltantes
- [x] Todos los campos obligatorios deben completarse
- [x] El sistema valida que los datos sean correctos
- [x] El partido queda registrado al confirmar


#### HU-13 — Definir datos del partido

Como organizador, quiero definir ubicación, fecha y hora, para organizar correctamente el partido.

**Criterios de aceptacion:**

- [x] El sistema permite ingresar ubicación
- [x] Permite seleccionar fecha y hora
- [x] Valida que la fecha sea futura
- [x] No permite campos vacíos


#### HU-14 — Definir cantidad de jugadores faltantes

Como organizador, quiero indicar cuántos jugadores faltan, para completar el equipo.

**Criterios de aceptacion:**

- [x] El sistema permite ingresar número de jugadores
- [x] El valor debe ser mayor a 0
- [x] El valor se refleja en el partido publicado
- [x] Se actualiza al aceptar jugadores


#### HU-15 — Publicar partido

Como organizador, quiero publicar un partido, para que otros jugadores puedan verlo y unirse.

**Criterios de aceptacion:**

- [x] El partido creado se vuelve visible para otros usuarios
- [x] Aparece en la lista de partidos cercanos
- [x] Se puede acceder a su detalle
- [x] El sistema permite recibir solicitudes de jugadores


### Sprint 2


#### HU-03 — Completar perfil de jugador

Como jugador, quiero completar mi perfil (posición y nivel), para que otros usuarios sepan cómo juego.

**Notas**

- No usar categorías complejas
- Mantener simple para MVP

**Criterios de aceptacion:**

- [x] El sistema solicita posición (defensor, mediocampo, delantero, etc.)
- [x] El sistema solicita nivel (bajo, medio, alto)
- [x] Ambos campos son obligatorios para continuar
- [x] El usuario puede modificar estos datos posteriormente
- [x] La información se muestra en su perfil visible


#### HU-04 — Editar perfil

Como jugador, quiero editar mi perfil, para mantener mi información actualizada.

**Criterios de aceptacion:**

- [x] El usuario puede modificar posición y nivel
- [x] Los cambios se guardan correctamente
- [x] La información actualizada se refleja en el perfil
- [x] El sistema confirma que los cambios fueron guardados


#### HU-07 — Filtrar partidos

Como jugador, quiero filtrar partidos por fecha o nivel, para encontrar opciones acordes a mi disponibilidad.

**Criterios de aceptacion:**

- [x] El sistema permite aplicar filtros (fecha, nivel)
- [x] Los resultados se actualizan según los filtros
- [x] El usuario puede limpiar los filtros
- [x] Los filtros seleccionados son visibles


#### HU-08 — Ver detalle de partido

Como jugador, quiero ver el detalle de un partido, para decidir si me conviene unirme.

**Criterios de aceptacion:**

- [x] El sistema muestra Fecha y hora
- [x] El sistema muestra Ubicación
- [x] El sistema muestra Jugadores confirmados
- [x] El sistema muestra Cupos disponibles
- [x] El usuario puede ver quiénes participan (info básica)
- [x] Se muestra un botón claro para unirse al partido
- [x] La información debe ser clara y fácil de interpretar


#### HU-09 — Solicitar unirse a un partido

Como jugador, quiero solicitar unirme a un partido, para poder participar en él.

**Notas**

- No es confirmación automática (hay organizador)

**Criterios de aceptacion:**

- [x] El usuario puede presionar “Unirme” desde el detalle del partido
- [x] El sistema registra la solicitud
- [x] El estado inicial es “pendiente”
- [x] El usuario recibe confirmación visual de que la solicitud fue enviada
- [x] No puede enviar múltiples solicitudes al mismo partido


#### HU-10 — Ver estado de solicitud

Como jugador, quiero ver el estado de mi solicitud, para saber si fui aceptado o no.

**Criterios de aceptacion:**

- [x] El sistema muestra estado: Pendiente
- [x] El sistema muestra estado: Aceptado
- [x] El sistema muestra estado: Rechazado
- [x] El estado se actualiza automáticamente o al refrescar
- [x] El usuario puede acceder desde “Mis partidos”
- [x] El estado es visible claramente


#### HU-11 — Cancelar solicitud

Como jugador, quiero cancelar mi solicitud, para liberar mi lugar si ya no puedo asistir.

**Criterios de aceptacion:**

- [x] El usuario puede cancelar solicitudes pendientes
- [x] El sistema actualiza el estado a “cancelada”
- [x] El organizador deja de ver esa solicitud
- [x] El usuario recibe confirmación del cambio


### Sprint 3


#### HU-16 — Ver solicitudes recibidas

Como organizador, quiero ver las solicitudes de jugadores, para decidir a quién aceptar.

**Criterios de aceptacion:**

- [x] El sistema muestra lista de solicitudes
- [x] Cada solicitud incluye info básica del jugador
- [x] Se puede acceder al perfil del jugador
- [x] Las solicitudes están claramente identificadas


#### HU-17 — Aceptar jugador

Como organizador, quiero aceptar jugadores, para completar el equipo de mi partido.

**Criterios de aceptacion:**

- [x] El organizador puede ver lista de solicitudes
- [x] Puede aceptar un jugador individualmente
- [x] El estado del jugador pasa a “aceptado”
- [x] Se reduce la cantidad de cupos disponibles
- [x] El jugador es notificado del cambio


#### HU-18 — Rechazar jugador

Como organizador, quiero rechazar jugadores, para mantener control sobre el equipo.

**Criterios de aceptacion:**

- [x] El organizador puede rechazar solicitudes
- [x] El estado pasa a “rechazado”
- [x] El jugador es notificado
- [x] La solicitud deja de estar activa


#### HU-19 — Ver mis partidos

Como jugador, quiero ver mis partidos, para organizar mi participación.

**Criterios de aceptacion:**

- [x] El sistema muestra lista de partidos del usuario
- [x] Incluye próximos partidos
- [x] Permite acceder al detalle
- [x] La información es clara y ordenada


#### HU-20 — Ver estado de participación

Como jugador, quiero ver si estoy confirmado o pendiente, para saber mi situación en el partido.

**Criterios de aceptacion:**

- [x] El estado se muestra claramente
- [x] Puede ser: pendiente, aceptado o rechazado
- [x] Se actualiza correctamente
- [x] Es visible desde la lista y el detalle


#### HU-21 — Calificar jugadores

Como jugador, quiero calificar a otros jugadores, para aportar a la confianza de la comunidad.

**Criterios de aceptacion:**

- [x] El sistema permite calificar después del partido
- [x] La calificación es simple (ej: puntuación)
- [x] Solo participantes pueden calificar
- [x] La calificación se guarda correctamente


#### HU-22 — Ver reputación de jugadores

Como jugador, quiero ver la reputación de otros, para decidir si me conviene jugar con ellos.

**Criterios de aceptacion:**

- [x] El perfil muestra puntuación
- [x] Se basa en calificaciones previas
- [x] Es visible antes de aceptar o unirse
- [x] Se presenta de forma clara


#### HU-23 — Ver historial de partidos

Como jugador, quiero ver mi historial, para recordar mis participaciones.

**Criterios de aceptacion:**

- [x] El sistema muestra partidos pasados
- [x] Incluye fecha y resultado básico
- [x] Permite acceder a detalles
- [x] Se diferencia de partidos futuros


#### HU-24 — Ver reputación propia

Como jugador, quiero ver mi reputación, para conocer cómo me perciben otros jugadores.

**Criterios de aceptacion:**

- [x] El sistema muestra puntuación acumulada
- [x] Se basa en evaluaciones recibidas
- [x] Es visible en el perfil
- [x] Se actualiza tras nuevas calificaciones


#### HU-28 — Cancelar partido

Como organizador, quiero cancelar un partido, para informar a los jugadores que no se realizará.

**Criterios de aceptacion:**

- [x] El organizador puede cancelar el partido
- [x] El sistema cambia el estado a “cancelado”
- [x] Los jugadores reciben una notificación
- [x] El partido deja de aparecer como disponible
- [x] El sistema evita nuevas solicitudes


#### HU-29 — Abandonar partido

Como jugador, quiero abandonar un partido, para liberar mi lugar si no puedo asistir.

**Criterios de aceptacion:**

- [x] El usuario puede salir de un partido confirmado
- [x] El sistema libera el cupo
- [x] El organizador es informado
- [x] El estado del usuario se actualiza


#### HU-32 — Comentar sobre jugadores

Como jugador, quiero dejar un comentario breve sobre otros jugadores, para dar contexto a mi calificación.

**Criterios de aceptacion:**

- [x] El sistema permite agregar un comentario junto con la calificación
- [x] El comentario es opcional
- [x] Solo el recipient puede ver el comentario
- [x] El comentario tiene límite de caracteres


### Sprint 4


#### HU-05 — Visualizar partidos cercanos

Como jugador nómade o local, quiero ver partidos cercanos a mi ubicación, para encontrar oportunidades de juego disponibles.

**Notas**

- Feature clave del producto
- Directamente ligado al valor mobile

**Criterios de aceptacion:**

- [x] El sistema obtiene la ubicación del usuario (con permiso)
- [x] Se muestran partidos ordenados por cercanía
- [x] Cada partido muestra (Fecha, Ubicación, Jugadores faltantes)
- [x] Si no hay partidos, se muestra un mensaje informativo
- [x] El usuario puede seleccionar un partido para ver más detalles


#### HU-06 — Ver partidos en mapa

Como jugador, quiero ver los partidos en un mapa, para ubicarme visualmente.

**Criterios de aceptacion:**

- [x] El sistema muestra un mapa con marcadores de partidos
- [x] Cada marcador representa un partido
- [x] Al seleccionar un marcador, se muestra información básica
- [x] Se puede acceder al detalle desde el mapa


#### HU-25 — Notificación de partidos cercanos

Como jugador, quiero recibir notificaciones de partidos cercanos, para no perder oportunidades.

**Criterios de aceptacion:**

- [x] El sistema envía notificaciones según ubicación
- [x] Incluye información básica del partido
- [x] El usuario puede desactivarlas
- [x] Se puede acceder al detalle desde la notificación

#### HU-26 — Notificación de aceptación

Como jugador, quiero recibir una notificación cuando me aceptan en un partido, para poder organizar mi tiempo.

**Criterios de aceptacion:**

- [x] El sistema envía una notificación al ser aceptado
- [x] La notificación incluye: Nombre del partido
- [x] La notificación incluye: Fecha y hora
- [x] El usuario puede acceder al detalle desde la notificación
- [x] La notificación se guarda en el historial de la app

#### HU-27 — Detección automática de ubicación

Como jugador, quiero que la app detecte mi ubicación automáticamente, para ver partidos cercanos sin configuraciones manuales.

**Criterios de aceptacion:**

- [x] El sistema solicita permiso de ubicación
- [x] Si se concede, obtiene ubicación actual
- [x] Los partidos se ordenan por cercanía
- [x] Si se rechaza el permiso, se ofrece alternativa manual

#### HU-30 — Solicitar jugadores urgentemente

Como organizador, quiero solicitar jugadores urgentemente, para cubrir vacantes cuando se acerque la hora del partido.

**Criterios de aceptacion:**

- [x] El organizador puede marcar un partido como "urgente"
- [x] El sistema envía notificaciones push a jugadores cercanos
- [x] La notificación indica claramente que es una solicitud urgente
- [x] Los jugadores reciben la notificación en tiempo real
- [x] El usuario puede desactivar este tipo de notificaciones

#### HU-31 — Acceder al panel administrativo

Como administrador, quiero acceder a un panel web, para gestionar usuarios y partidos.

**Notas**

- Feature opcional para MVP
- Puede implementarse en fase 2

**Criterios de aceptacion:**

- [x] El sistema proporciona una interfaz web segura
- [x] El admin puede visualizar todos los partidos
- [x] El admin puede visualizar usuarios y su reputación
- [x] El admin puede banear usuarios problemáticos
- [x] El admin puede cancelar partidos problemáticos

#### HU-33 — Verificar asistencia al partido

Como organizador, quiero confirmar la asistencia de los jugadores, para mejorar la reputación del sistema.

**Criterios de aceptacion:**

- [x] El organizador puede marcar qué jugadores asistieron
- [x] Los jugadores que no asistieron receive downvotes automáticos
- [x] Se registra la asistencia para estadísticas futuras
- [x] El sistema actualiza la reputación automáticamente

## Sprint Backlog (7)

### Sprint 5


#### HU-34 — Crear geocerca al guardar cancha

Como organizador, quiero que al crear/editar una cancha se genere automáticamente su geocerca, para delimitar el área sin cargarla a mano.

**Checklist:**

- [ ] La geocerca se crea automáticamente al guardar la cancha
- [ ] Usa lat/long de la cancha como centro
- [ ] Se almacena en Firestore la referencia (canchas/{id}/geofence)
- [ ] Al editar la cancha, la geocerca se actualiza
- [ ] Se maneja el caso de cancha sin coordenadas válidas


#### HU-35 — Radio configurable de geocerca

Como organizador, quiero definir el radio de la geocerca por cancha (100–150 m), para ajustar la sensibilidad según el tamaño real de la cancha.

**Checklist:**

- [ ] Campo de radio editable por cancha
- [ ] Valor por defecto dentro de 100–150 m
- [ ] Se valida un mínimo/máximo razonable
- [ ] El radio se persiste junto a la geocerca


#### HU-36 — Registrar geocercas de partidos próximos

Como sistema, quiero registrar solo las geocercas de canchas con partidos próximos, para respetar el límite de 100 geocercas activas y cuidar la batería.

**Checklist:**

- [ ] Solo se registran geocercas de canchas con partidos próximos
- [ ] No se supera el límite de 100 geocercas activas
- [ ] Se remueven geocercas de partidos ya finalizados
- [ ] Transición configurada como ENTER (y EXIT si aplica)


#### HU-37 — Solicitar permisos de ubicación

Como usuario, quiero otorgar permiso de ubicación en foreground y background, para que la app detecte mi llegada aunque esté cerrada.

**Checklist:**

- [ ] Se solicita ACCESS_FINE_LOCATION en foreground
- [ ] Se solicita ACCESS_BACKGROUND_LOCATION por separado (Android 10+)
- [ ] Se explica al usuario por qué se necesita "Permitir todo el tiempo"
- [ ] Se maneja el caso de permiso denegado (fallback a manual)


#### HU-38 — Capturar entrada a la cancha (ENTER)

Como jugador, quiero que la app detecte automáticamente cuando entro a la cancha, para quedar marcado como presente sin hacer nada.

**Checklist:**

- [ ] Se dispara el BroadcastReceiver al entrar a la geocerca
- [ ] Se identifica correctamente la cancha del evento
- [ ] Se registra el evento en base local
- [ ] Se contempla la latencia del geofence (hasta ~2-3 min en background)


#### HU-39 — Registrar evento de geocerca

Como sistema móvil, quiero escribir el evento en `geofence_events` (userId, canchaId, tipo, timestamp), para que el backend lo procese.

**Checklist:**

- [ ] Se escribe en geofence_events con userId, canchaId, tipo, timestamp
- [ ] Incluye ubicación del evento
- [ ] Manejo de errores con reintento/cola offline
- [ ] Se sincroniza cuando vuelve la conexión


#### HU-40 — Check-in manual (fallback)

Como jugador, quiero poder marcar mi asistencia manualmente si el GPS falla, para no depender exclusivamente de la geolocalización.

**Checklist:**

- [ ] Botón "Ya llegué" visible en el detalle del partido
- [ ] Escribe el mismo tipo de evento que el geofence (marcado como manual)
- [ ] Solo disponible cerca del horario del partido
- [ ] No permite doble check-in del mismo usuario

## Backlog (4)

### Sprint 6

#### HU-41 — Configurar FCM y token

Como desarrollador, quiero obtener el token FCM y guardarlo en el perfil del usuario, para poder enviarle notificaciones push.

**Checklist:**

- [ ] Se obtiene el token FCM del dispositivo
- [ ] El token se guarda en el perfil del usuario en Firestore
- [ ] Se implementa un Service extendiendo FirebaseMessagingService
- [ ] Se actualiza el token cuando cambia (refresh)


#### HU-42 — Trigger de Cloud Function

Como Cloud Function, quiero ejecutarme al crearse un documento en `geofence_events` (`onCreate`), para procesar el evento y disparar notificaciones.

**Checklist:**

- [ ] Trigger en creación de documento (onCreate sobre geofence_events)
- [ ] Se identifica el partido activo en esa cancha
- [ ] Logs estructurados para debugging
- [ ] Manejo de errores sin romper la ejecución


#### HU-43 — Notificar a organizador y jugadores

Como sistema, quiero enviar un push al organizador y a los jugadores del partido cuando alguien llega, para coordinar la asistencia en tiempo real.

**Checklist:**

- [ ] Se envía FCM al organizador ("El jugador [nombre] ingresó")
- [ ] Se notifica a los demás jugadores del mismo partido
- [ ] Se excluye al usuario que disparó el evento
- [ ] Se filtran solo participantes del partido correcto


#### HU-44 — Mostrar notificación en foreground y background

Como usuario, quiero recibir la notificación esté la app abierta o cerrada, para enterarme de las llegadas al instante.

**Checklist:**

- [ ] Notificación visible con la app en primer plano
- [ ] Data payload procesado en background (partidoId, tipoEvento)
- [ ] Tap abre la pantalla correspondiente del partido
- [ ] Sonido/vibración configurables


## Sandbox (0)

