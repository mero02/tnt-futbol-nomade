# Entra a la cancha - Perfil de Usuario: Planificación

## Estado Actual
- El usuario puede autenticarse con Google.
- Existe `GoogleAuthClient` para manejar la sesión.
- **Problema detectado:** El usuario no se persiste en Firestore al iniciar sesión por primera vez, lo que impide guardar datos adicionales (posición, nivel, etc.).

---

## Objetivos
1.  **Persistencia automática:** Crear/actualizar un documento en la colección `users` de Firestore cada vez que un usuario se loguea.
2.  **Perfil Extendido:** Permitir al usuario definir su rol en la cancha y su nivel de habilidad autoperibido.

---

## Feature: Datos de Perfil

### Modelo de Datos (User)
```kotlin
data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val posicion: Posicion? = null,
    val nivel: NivelJuego? = null,
    val telefono: String? = null,
    val biografia: String? = null
)

enum class Posicion { PORTERO, DEFENSA, MEDIOCAMPISTA, DELANTERO, MULTIPOSICION }
enum class NivelJuego { PRINCIPIANTE, INTERMEDIO, AVANZADO, PROFESIONAL }
```

### UI - Screen de Perfil
```
┌────────────────────────────────┐
│ Perfil              [Guardar] │
│                                │
│    [ (Imagen de Usuario) ]     │
│       Cambiar foto             │
│                                │
│  Nombre: Juan Pérez            │
│  Email: juan@example.com       │
│                                │
│  Posición favorita             │
│  ┌──────────────────────┐      │
│  │ Mediocampista      ▼ │      │
│  └──────────────────────┘      │
│                                │
│  Nivel de juego                │
│  ┌──────────────────────┐      │
│  │ Intermedio         ▼ │      │
│  └──────────────────────┘      │
│                                │
│  [ Cerrar Sesión ]             │
└────────────────────────────────┘
```

---

## Feature: Persistencia en Firestore

### Lógica de Sincronización
Al completar exitosamente el login con Google:
1. Obtener datos básicos de `FirebaseUser` (uid, email, name, photo).
2. Verificar si el documento `users/{uid}` existe.
3. Si no existe: crear el documento con los datos básicos.
4. Si existe: no sobreescribir los campos personalizados (`posicion`, `nivel`).

---

## Archivos a Crear/Modificar

### Data Layer
- `data/model/User.kt` (nuevo): Definición de la entidad y los enums.
- `data/repository/UserRepository.kt` (nuevo): Métodos `syncUser()`, `getUserProfile()`, `updateUserProfile()`.

### Presentation Layer
- `presentation/viewmodel/AuthViewModel.kt`: Llamar a `userRepository.syncUser()` tras el login.
- `presentation/ui/screens/profile/ProfileScreen.kt` (nuevo): Interfaz de usuario para editar datos.
- `presentation/viewmodel/ProfileViewModel.kt` (nuevo): Lógica para cargar y guardar el perfil.

---

## Tasks Pendientes

- [ ] Crear modelos `User`, `Posicion` y `NivelJuego`.
- [ ] Implementar `UserRepository` con Firestore.
- [ ] Modificar `AuthViewModel` para persistir al usuario post-login.
- [ ] Diseñar `ProfileScreen` con Material 3.
- [ ] Integrar `ProfileScreen` en el `HomeScreen` (dentro de una de las Tabs).
- [ ] Añadir validación de campos obligatorios en el perfil.
- [ ] Configurar Security Rules en Firestore para que el usuario solo pueda editar su propio perfil (`request.auth.uid == userId`).
