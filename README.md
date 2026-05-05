# Fútbol Nómade

## Descripción del Proyecto

Fútbol Nómade es una aplicación móvil diseñada para conectar equipos de fútbol amateur incompletos con jugadores disponibles en tiempo real, facilitando la organización de partidos y promoviendo la integración de jugadores en distintas ciudades.

## Problema

En muchas ciudades argentinas, los grupos organizados de fútbol amateur encuentran dificultades para completar sus partidos semanales debido a la ausencia de algunos jugadores. Paralelamente, existe una gran cantidad de personas (especialmente aquellas que viajan por trabajo o estudio) que desean jugar al fútbol pero no cuentan con un grupo fijo en cada ciudad que visitan.

## Solución Propuesta

Se propone una aplicación móvil que permita:
- A los organizadores de partidos publicar encuentros indicando ubicación, fecha, horario y cantidad de jugadores faltantes.
- A los usuarios visualizar partidos cercanos mediante geolocalización y postularse para participar según su disponibilidad.

Además, la aplicación incorpora el concepto de "fútbol nómade", donde los jugadores pueden integrarse temporalmente a distintos grupos en función de su ubicación actual.

## Características Principales

- Publicación y búsqueda de partidos por geolocalización.
- Perfil de usuario con posición en la cancha, nivel de juego y sistema de reputación.
- Notificaciones push para cubrir vacantes urgentes.
- Interfaz mobile-first para acceso inmediato en situaciones contextuales.
- Posible backend para gestión de usuarios, partidos y ubicaciones.
- Panel web administrativo para gestión de datos y monitoreo (opcional).

## Tecnologías Utilizadas

- **Frontend móvil**: Android con Jetpack Compose + Material3
- **Autenticación**: Firebase Auth (Google Sign-In)
- **Backend**: Por definir (Firebase Firestore en roadmap)
- **Geolocalización**: Google Maps API (pendiente)
- **Notificaciones**: Firebase Cloud Messaging (pendiente)

## Pantallas

| Ruta | Composable | Descripción |
|------|-----------|-------------|
| `login` | `LoginScreen` | Login con Google via Firebase |
| `home` | `HomeScreen` | Bottom nav con 4 tabs (Canchas, Reservas, Partidos, Perfil) |
| `acerca_de` | `AcercaDe` | Info de la app y el equipo |

El diagrama completo de navegación y transiciones está en [`docs/views-flow.xml`](docs/views-flow.xml).

## Build del APK

### Prerrequisitos

| Herramienta | Versión mínima |
|-------------|---------------|
| JDK | 17 |
| Android Studio | Ladybug (2024.2) o superior |
| Android SDK | API 36 (compileSdk) |
| Dispositivo / emulador | Android 7.0+ (minSdk 24) |

### Setup local

1. Clonar el repositorio:
    ```bash
    git clone https://github.com/mero02/tnt-futbol-nomade.git
    cd tnt-futbol-nomade
    ```

2. Pedirle `google-services.json` a Franc y colocarlo en `app/`:
    ```
    app/google-services.json   ← gitignoreado por seguridad, no se sube al repo
    ```

3. Verificar que el setup está ok:
    ```bash
    ./setup.sh
    ```
    Si aparece ✓ estás listo. Si aparece ⚠️ seguí las instrucciones que imprime.

4. Verificar que `local.properties` apunta al SDK:
    ```properties
    sdk.dir=/Users/<tu-usuario>/Library/Android/sdk
    ```
    Android Studio lo genera automáticamente al abrir el proyecto.

### Build debug APK (desarrollo)

```bash
./gradlew assembleDebug
```

El APK generado queda en:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Instalar directamente en dispositivo

Con un dispositivo conectado por USB (o emulador corriendo):
```bash
./gradlew installDebug
```

### Build release APK (distribución)

1. Generar un keystore de firma (solo la primera vez):
    ```bash
    keytool -genkey -v -keystore futbol-tnt.keystore \
      -alias futbol-tnt -keyalg RSA -keysize 2048 -validity 10000
    ```

2. Agregar `signingConfigs` en `app/build.gradle.kts`:
    ```kotlin
    signingConfigs {
        create("release") {
            storeFile = file("../futbol-tnt.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "futbol-tnt"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    ```

3. Buildear:
    ```bash
    ./gradlew assembleRelease
    # Output: app/build/outputs/apk/release/app-release.apk
    ```

### Build AAB para Play Store

```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

### Troubleshooting

| Error | Solución |
|-------|---------|
| `google-services.json not found` | Colocar el archivo en `app/` (ver Setup local) |
| `Unsupported class file major version` | Verificar que Android Studio usa JDK 17: **File → Project Structure → SDK Location → JDK** |
| Build cacheado con errores | **File → Invalidate Caches → Invalidate and Restart** |
| `JAVA_HOME` apunta a JDK incorrecto | Agregar en `gradle.properties`: `org.gradle.java.home=/path/to/jdk17` |

## Uso

- Al iniciar la aplicación, los usuarios pueden registrarse o iniciar sesión.
- Los organizadores pueden crear un nuevo partido rellenando el formulario con los detalles.
- Los jugadores pueden explorar el mapa para ver partidos cercanos y apuntarse a aquellos que les interesen.
- Los usuarios recibirán notificaciones cuando se publiquen partidos que coincidan con sus preferencias o cuando se necesiten jugadores para un partido al que se han apuntado.
