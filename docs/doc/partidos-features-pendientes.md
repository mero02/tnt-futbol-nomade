# FutbolTNT - Partidos: Funcionalidades Pendientes

## Estado Actual

El tab **Partidos** ya muestra una lista de partidos con:
- Diseño VS (Equipo A vs Equipo B)
- Fecha, hora, cancha
- Cantidad de jugadores (actuales/máximos)
- Precio por persona
- Badge de estado (Abierto/Lleno/En juego/Finalizado)
- Botón "Unirse" (si está abierto)

Pendiente: implementar la lógica real de _unirse_ y _crear partido_.

---

## Feature: Unirse a Partido

### Flujo de Usuario
1. Usuario ve lista de partidos abiertos
2. Toca "Unirse" → Dialog de confirmación
3. Sistema valida: hay lugar disponible?
4. Si sí: suma 1 jugador, actualiza `jugadoresActuales`
5. Si no: muestra "Partido lleno"

### Modelo de Datos
```kotlin
data class Partido(
    val id: String,
    val nombreLocal: String,
    val nombreVisitante: String,
    val fecha: LocalDateTime,
    val cancha: Cancha,
    val duracionHoras: Int = 1,
    val precioPorPersona: Double,
    val jugadoresActuales: Int,
    val jugadoresMaximos: Int,
    val estado: EstadoPartido,
    val nombreOrganizador: String,
    val participantes: List<Participante> = emptyList()  // FUTURO
)
```

### UI - Dialog de Confirmación
```
┌────────────────────────────────┐
│  Unirse al partido              │
│                                │
│  Los Chetos FC vs Los Boludos   │
│  Sab 22/04 17:00 - Quincho   │
│                                │
│  Tu equipo: [Dropdown equipos] │
│  Posición: [Del/Med/Def/Por] │
│                                │
│  [Cancelar]      [Confirmar] │
└────────────────────────────────┘
```

### Validaciones
- `jugadoresActuales < jugadoresMaximos`
- No está en pasado (`fecha > now`)
- Usuario no está ya en el partido

### Archivo a Modificar
- 
- `data/repository/PartidoRepository.kt` (nuevo)

---

## Feature: Crear Partido

### Flujo de Usuario
1. Usuario toca "Crear Partido" → Screen de creación
2. Ingresa: nombre equipo local, nombre equipo visitante
3. Selecciona cancha (dropdown)
4. Selecciona fecha/hora (DatePicker + TimePicker)
5. Ingresa cantidad de jugadores max
6. Ingresa precio por persona
7. Confirma → crea `Partido` en estado `ABIERTO`

### UI - Screen CrearPartidoScreen
```
┌────────────────────────────────┐
│ ← Crear Partido              │
│                                │
│  Nombre equipo LOCAL *       │
│  ┌──────────────────────┐ │
│  │ Los Chetos FC      │ │
│  └──────────────────────┘ │
│                                │
│  Nombre equipo VISITANTE *     │
│  ┌──────────────────────┐ │
│  │ Los Boludos          │ │
│  └──────────────────────┘ │
│                                │
│  Cancha *                  │
│  ┌──────────────────────┐ │
│  │ Quincho La Palmera ▼│ │
│  └──────────────────────┘ │
│                                │
│  Fecha *        Hora *       │
│  ┌────────┐   ┌────────┐  │
│  │22/04   │   │17:00  │  │
│  └────────┘   └────────┘  │
│                                │
│  Jugadores máx: [5-22]       │
│  ┌──────────────────────┐ │
│  │ 10                  │ │
│  └──────────────────────┘ │
│                                │
│  Precio por persona:           │
│  ┌──────────────────────┐ │
│  │ $2500             │ │
│  └──────────────────────┘ │
│                                │
│  ┌──────────────────────┐ │
│  │   Crear Partido     │ │
│  └──────────────────────┘ │
└────────────────────────────────┘
```

### Validaciones
- Nombre equipo local != visitante
- Fecha >= hoy
- Hora entre 8:00 y 22:00
- Jugadores entre 5 y 22
- Precio > 0
- Cancha disponible en ese horario

### Archivos a Crear/Modificar
- `presentation/ui/screens/CrearPartidoScreen.kt` (nuevo)
- `presentation/viewmodel/PartidoViewModel.kt` (nuevo)
- `presentation/navigation/AppNavigation.kt` → agregar ruta
- `data/repository/PartidoRepository.kt` (nuevo)
- `domain/usecase/CrearPartidoUseCase.kt` (nuevo)

---

## Arquitectura Sugerida

```
presentation/
├── viewmodel/
│   └── PartidoViewModel.kt      #Estado + lógica UI
├── ui/
│   └── screens/
│       ├── HomeScreen.kt         #PartidosTab (actual)
│       └── CrearPartidoScreen.kt #Nueva screen
data/
├── model/
│   ├── Partido.kt             #Ya existe
│   └── MockData.kt           #Ya actualizado
└── repository/
    └── PartidoRepository.kt    #Futuro (Firestore)
domain/
└── usecase/
    ├── UnirseAPartidoUseCase.kt
    └── CrearPartidoUseCase.kt
```

---

## Mock vs Real

| Feature | Estado | Backend |
|---------|--------|--------|
| Ver partidos | ✅ Mock | Firestore |
| Unirse | ⚠️ UI lista, sin lógica | Firestore + Transaction |
| Crear | ⚠️ UI lista, sin lógica | Firestore + Security rules |

---

## Tasks Pendientes

- [ ] `PartidoViewModel` con estado `partidos`, `isLoading`
- [ ] `PartidoRepository` con `getPartidos()`, `joinPartido()`, `createPartido()`
- [ ] `CrearPartidoScreen`
- [ ] Navegación a `CrearPartidoScreen`
- [ ] Dialog de confirmación "Unirse"
- [ ] Firestore schema + Security rules
