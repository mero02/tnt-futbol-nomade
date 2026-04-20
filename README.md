# FutbolTNT ⚽

Aplicación Android para gestión de fútbol desarrollada con Jetpack Compose y Kotlin.

## 🏗️ Arquitectura

Este proyecto sigue **Clean Architecture** con las siguientes capas:

```
com.example.futbol_tnt/
├── data/              # Capa de datos
│   ├── local/         # Base de datos local (Room)
│   ├── remote/        # APIs y servicios remotos
│   └── repository/    # Implementaciones de repositorios
├── domain/            # Capa de dominio (lógica de negocio)
│   ├── model/         # Modelos de dominio
│   └── usecase/       # Casos de uso
├── presentation/      # Capa de presentación
│   ├── ui/
│   │   ├── screens/   # Pantallas de la app
│   │   ├── components/# Componentes reutilizables
│   │   └── theme/     # Tema y estilos
│   └── viewmodel/     # ViewModels
├── di/                # Inyección de dependencias (Hilt)
└── core/              # Utilidades y extensiones
    ├── util/
    └── extension/
```

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose
- **Arquitectura:** Clean Architecture + MVVM
- **Compilación:** Gradle 8.13.2 con Kotlin DSL
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36

## 📋 Características de Gradle

- ✅ **Configuration Cache** habilitado para builds más rápidos
- ✅ **Build Cache** activado para reutilizar outputs
- ✅ **Parallel Execution** para construcción paralela de módulos
- ✅ **Version Catalog** (`libs.versions.toml`) para gestión centralizada de dependencias

## 🚀 Comenzar

### Prerequisitos

- Android Studio Ladybug o superior
- JDK 11 o superior
- Android SDK con API Level 36

### Compilar el proyecto

```bash
./gradlew build
```

### Ejecutar la aplicación

```bash
./gradlew installDebug
```

## 📝 Convenciones de Código

Este proyecto sigue las convenciones oficiales de Kotlin:

- **Estilo de código:** Kotlin Official Code Style
- **Indentación:** 4 espacios
- **Longitud máxima de línea:** 120 caracteres
- **Imports:** Organizados automáticamente

### Naming Conventions

- **Packages:** `lowercase` (ej: `data`, `domain`, `presentation`)
- **Classes:** `PascalCase` (ej: `MainActivity`, `UserRepository`)
- **Functions:** `camelCase` (ej: `getUserData`, `validateInput`)
- **Constants:** `UPPER_SNAKE_CASE` (ej: `MAX_RETRY_COUNT`)
- **Variables:** `camelCase` (ej: `userName`, `itemCount`)

## 🎨 Jetpack Compose

### Principios

- **Unidirectional Data Flow (UDF):** Estado fluye hacia abajo, eventos hacia arriba
- **State Hoisting:** Estado en el nivel más alto que lo necesita
- **Composables sin estado:** Preferir composables sin estado y reutilizables
- **ViewModels:** Para lógica de negocio y manejo de estado

### Estructura de Composables

```kotlin
@Composable
fun MiPantalla(
    modifier: Modifier = Modifier,
    viewModel: MiViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MiPantallaContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
private fun MiPantallaContent(
    uiState: MiUiState,
    onAction: (MiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // UI implementation
}
```

## 🔄 Git Workflow

### Commits

Seguimos [Conventional Commits](https://www.conventionalcommits.org/):

```
tipo(scope): descripción

feat: nueva funcionalidad
fix: corrección de bug
docs: documentación
style: formato, punto y coma faltante, etc
refactor: refactorización de código
test: agregar tests
chore: tareas de mantenimiento
```

### Branches

- `main`: rama principal con código estable
- `develop`: rama de desarrollo
- `feature/nombre`: nuevas funcionalidades
- `fix/nombre`: correcciones de bugs

## 📚 Recursos

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/compose)
- [Android Developers](https://developer.android.com/)
- [Gradle Best Practices](https://docs.gradle.org/current/userguide/best_practices_general.html)

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.
