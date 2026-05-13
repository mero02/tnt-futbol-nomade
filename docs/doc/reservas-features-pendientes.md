# Entra a la cancha - Reservas: Funcionalidades Pendientes

## Estado Actual

El tab **Mis Reservas** ya muestra una lista estática de reservas realizadas con:
- Nombre de la cancha y estado (Confirmada, Pendiente, etc.)
- Fecha, hora y duración.
- Precio total calculado.

Pendiente: implementar el flujo de **alquiler de cancha** desde el tab de Canchas.

---

## Feature: Alquilar Cancha (Reserva Directa)

### Flujo de Usuario
1. Usuario va al tab **Canchas**.
2. Selecciona una cancha → Abre **Detalle de Cancha**.
3. Elige una **Fecha** (Calendario).
4. Elige un **Horario** disponible (Grid de botones).
5. Selecciona **Duración** (1h, 1.5h, 2h).
6. Toca "Reservar Ahora" → Resumen de pago/confirmación.
7. Al confirmar, se crea la `Reserva` y aparece en "Mis Reservas".

### Modelo de Datos (Extensión)
```kotlin
data class Reserva(
    val id: String,
    val canchaId: String,
    val usuarioId: String, // El usuario que alquila
    val fecha: LocalDateTime,
    val duracionHoras: Int,
    val precioTotal: Double,
    val estado: EstadoReserva,
    val nombreEquipo: String? = null // Opcional
)
```

### UI - Pantalla de Detalle y Reserva
```
┌────────────────────────────────┐
│ ← Detalle de Cancha            │
│                                │
│  [ Imagen de la Cancha ]       │
│                                │
│  Quincho La Palmera            │
│  Fútbol 5 - $15.000/hr         │
│                                │
│  Seleccionar Fecha:            │
│  [ Lun 24 ] [ Mar 25 ] [ Mié 26 ]│
│                                │
│  Horarios Disponibles:         │
│  ┌──────┐ ┌──────┐ ┌──────┐  │
│  │ 18:00│ │ 19:00│ │ 21:00│  │
│  └──────┘ └──────┘ └──────┘  │
│                                │
│  Duración: [ 1h ] [ 1.5h ] [ 2h ]│
│                                │
│  Total: $22.500 (1.5h)         │
│                                │
│  ┌──────────────────────┐      │
│  │    Reservar Cancha   │      │
│  └──────────────────────┘      │
└────────────────────────────────┘
```

### Validaciones
- La cancha no puede estar ocupada en ese horario (Check contra otras reservas).
- La fecha no puede ser en el pasado.
- El usuario debe estar logueado.

---

## Integración con "Partidos" (Flujo Mejorado)

Para asegurar que cada partido tenga una cancha real garantizada, el flujo de creación se vincula directamente a la reserva:

1. **Post-Reserva**: Al finalizar el alquiler, se muestra un diálogo: *"¿Tu equipo está incompleto? Convertí esta reserva en un partido público"*.
2. **Vínculo Automático**: Al aceptar, el `Partido` se crea heredando automáticamente `canchaId`, `fecha` y `hora` de la `Reserva`.
3. **Visibilidad**: El partido aparece en el tab **Partidos** para que otros usuarios se unan.

**Cambio de UX:** Se elimina el botón genérico de "Crear Partido" del tab Partidos para evitar reservas fantasma.

---

## Tareas Pendientes

- [ ] `CanchaDetailScreen.kt`: Nueva pantalla para ver info de la cancha y elegir turnos.
- [ ] `CanchaViewModel.kt`: Manejo de disponibilidad de horarios por fecha.
- [ ] Lógica de guardado en Firestore (Colección `reservas`).
- [ ] Botón "Convertir en Partido" en el éxito de la reserva.
- [ ] Eliminar `CrearPartidoCard` de `PartidosTab.kt`.

---

## Archivos a Crear/Modificar
- `presentation/ui/screens/CanchaDetailScreen.kt` (nuevo)
- `presentation/viewmodel/CanchaViewModel.kt` (nuevo)
- `data/model/Reserva.kt` (actualizar)
- `presentation/navigation/AppNavigation.kt` (agregar ruta con argumento `canchaId`)
