# Cómo probar — Geocercas y Check-in de asistencia (Sprint 5)

Guía para buildear y probar la funcionalidad del **Sprint 5** (HU-34 a HU-40): detección
automática de llegada a la cancha vía geofencing + check-in manual como fallback.

> Rama: `worktree-sprint5-geofencing` · PR #1 (draft).

---

## 1. Requisitos para buildear

- **Android Studio** (o CLI con `./gradlew`) + **JDK 17** (JDK 25 aún no lo soporta el AGP).
- SDK Android con emulador **o** un teléfono Android físico (Android 10+ / API 29+).
- El proyecto ya trae `app/google-services.json` (proyecto Firebase `futbol-nomade`).

### Compilar el APK debug
```bash
./gradlew assembleDebug
# salida: app/build/outputs/apk/debug/app-debug.apk
```

### Correr los tests de la lógica (JVM, sin device)
```bash
./gradlew testDebugUnitTest --tests "com.example.futbol_tnt.core.geofence.GeofencePolicyTest"
# 11 tests: ventana de check-in, dedup/límite de canchas, coords inválidas, clamp de radio
```

---

## ⚠️ 2. Sobre el login con Google (leer antes de buildear)

El login usa **Google Sign-In (Firebase)**. Firebase valida el **SHA-1 del keystore** que
firma el APK. Hay dos caminos:

- **Usar el APK ya provisto** (`app-debug.apk` que te pasaron): está firmado con un debug
  keystore cuyo SHA-1 **ya está registrado** en el proyecto Firebase → el login funciona.
  **Recomendado para solo probar.**

- **Compilar vos mismo**: tu `~/.android/debug.keystore` tiene un **SHA-1 distinto**, que
  **no está** registrado en Firebase → el login Google fallará con `DEVELOPER_ERROR (código 10)`.
  Para que funcione hay que agregar tu SHA-1 al proyecto Firebase:
  1. `./gradlew :app:signingReport` → copiar el `SHA1` de la variante `debug`.
  2. Firebase Console → proyecto `futbol-nomade` → Project Settings → tu app Android →
     "Add fingerprint" → pegar el SHA-1 → descargar el `google-services.json` actualizado.

El emulador debe tener **Google Play Services** (usar una imagen de AVD "con Play Store")
y una **cuenta Google agregada** (Settings → Accounts).

---

## 3. Qué probar, por Historia de Usuario

### HU-35 — Radio de geocerca configurable (panel admin)
1. `cd admin-web && npm install && npm run dev`
2. Canchas → crear/editar una cancha → campo **"Radio geocerca (m) · 100-150"**.
3. Guardar → el valor se persiste en Firestore (`canchas/{id}.radioGeofence`), acotado a 100-150.

### HU-37 — Permiso de ubicación en background
1. En la app: menú ☰ → **Ajustes** → sección **"Asistencia"**.
2. Card **"Detección automática de llegada"** → botón **"Permitir ubicación"**.
3. Conceder ubicación (foreground) → el botón pasa a **"Permitir todo el tiempo"**.
4. Tocar → Android 11+ abre los ajustes del sistema → elegir **"Permitir todo el tiempo"**.
5. Volver → la card muestra **"✓ Detección automática activada"**.
   - Verificar permisos: `adb shell dumpsys package com.example.futbol_tnt | grep -i LOCATION | grep granted`

### HU-40 — Check-in manual "Ya llegué"
Requiere un partido donde el usuario sea **participante NO organizador**, dentro de la
ventana **±2 h** del horario (desde 2 h antes hasta el fin del partido).
1. Abrir el detalle de ese partido.
2. Aparece el botón verde **"Ya llegué"** → tocar → snackbar *"¡Llegada registrada!"*.
3. Tocar de nuevo → *"Ya registraste tu llegada"* (idempotencia, sin doble check-in).
4. Se crea el doc en Firestore, colección **`geofence_events`** (`manual: true`).

> Nota: si sos el **organizador** del partido, NO ves "Ya llegué" (ves "Verificar Asistencia").

### HU-34/36/38/39 — Geofence automático (necesita GPS real)
El disparo real conviene probarlo **caminando con un teléfono físico** (en emulador la
transición ENTER es poco fiable por latencia y Doze mode):
1. Conceder "Permitir todo el tiempo" (HU-37).
2. Que exista un partido próximo (≤24 h) en una cancha con coordenadas → la app registra
   su geocerca al abrir (HU-36).
3. Entrar físicamente al radio de la cancha → a los ~1-3 min llega la notificación
   *"¡Llegaste a la cancha!"* (HU-38) y se escribe un doc en `geofence_events`
   (`manual: false`, HU-39).

Simulación en emulador (parcial, orden **longitud latitud**):
```bash
adb emu geo fix <LNG_lejos> <LAT_lejos>     # empezás afuera del radio
adb emu geo fix <LNG_cancha> <LAT_cancha>   # "entrás" al radio
```

---

## 4. Alcance verificado vs pendiente

| Ítem | Estado |
|------|--------|
| Compilación (`assembleDebug`) | ✅ |
| Lógica de decisión (11 tests JVM) | ✅ |
| HU-37 permisos end-to-end (emulador) | ✅ |
| HU-40 botón "Ya llegué" | ⚠️ requiere data (partido participante ±2h) |
| Geofence ENTER real (HU-38/39) | ⚠️ requiere GPS caminando (device físico) |

## 5. Fuera de scope (Sprint 6)
El push en tiempo real al organizador/jugadores cuando alguien llega (HU-41→44) NO está:
requiere **Cloud Functions** (plan Firebase Blaze). Este sprint solo **registra** el evento.
