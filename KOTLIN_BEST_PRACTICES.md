# Buenas Prácticas de Kotlin para FutbolTNT

Esta guía documenta las convenciones y mejores prácticas de Kotlin que seguimos en este proyecto.

## 📚 Índice

1. [Principios Generales](#principios-generales)
2. [Naming Conventions](#naming-conventions)
3. [Clases y Objetos](#clases-y-objetos)
4. [Funciones](#funciones)
5. [Nullability](#nullability)
6. [Collections](#collections)
7. [Coroutines](#coroutines)
8. [Jetpack Compose](#jetpack-compose)
9. [Testing](#testing)

---

## Principios Generales

### 1. Inmutabilidad por Defecto

```kotlin
// ✅ BIEN: Usar val por defecto
val name = "Futbol TNT"
val items = listOf(1, 2, 3)

// ❌ MAL: Usar var solo cuando sea necesario
var name = "Futbol TNT"
var items = mutableListOf(1, 2, 3)
```

### 2. Expresiones sobre Statements

```kotlin
// ✅ BIEN: Usar expresiones when
val description = when (status) {
    Status.ACTIVE -> "Activo"
    Status.INACTIVE -> "Inactivo"
    Status.PENDING -> "Pendiente"
}

// ❌ MAL: Usar if-else statement
var description = ""
if (status == Status.ACTIVE) {
    description = "Activo"
} else if (status == Status.INACTIVE) {
    description = "Inactivo"
} else {
    description = "Pendiente"
}
```

### 3. Usar Data Classes para Modelos

```kotlin
// ✅ BIEN: Data class para modelos de datos
data class User(
    val id: String,
    val name: String,
    val email: String
)

// El compilador genera automáticamente:
// - equals() y hashCode()
// - toString()
// - copy()
// - componentN() para destructuring
```

---

## Naming Conventions

### Packages

```kotlin
// ✅ BIEN: lowercase, palabras separadas por punto
package com.example.futbol_tnt.data.repository
package com.example.futbol_tnt.domain.usecase
```

### Classes y Interfaces

```kotlin
// ✅ BIEN: PascalCase
class UserRepository
interface DataSource
sealed class Result
```

### Funciones y Variables

```kotlin
// ✅ BIEN: camelCase
fun getUserById(id: String): User
val userName = "John"
var itemCount = 0
```

### Constantes

```kotlin
// ✅ BIEN: Constantes en companion object o top-level
class ApiConstants {
    companion object {
        const val BASE_URL = "https://api.example.com"
        const val TIMEOUT_SECONDS = 30
        const val MAX_RETRY_COUNT = 3
    }
}

// O a nivel de archivo
private const val DEFAULT_PAGE_SIZE = 20
```

---

## Clases y Objetos

### 1. Usar Sealed Classes para Estados

```kotlin
// ✅ BIEN: Sealed class para estados finitos
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// Uso en ViewModel
val uiState = MutableStateFlow<UiState<User>>(UiState.Loading)
```

### 2. Companion Objects para Factory Methods

```kotlin
// ✅ BIEN: Factory methods en companion object
data class User(
    val id: String,
    val name: String,
    val email: String
) {
    companion object {
        fun createEmpty() = User(
            id = "",
            name = "",
            email = ""
        )

        fun fromDto(dto: UserDto) = User(
            id = dto.id,
            name = dto.name,
            email = dto.email
        )
    }
}
```

### 3. Extension Functions sobre Utility Classes

```kotlin
// ✅ BIEN: Extension function
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Uso
val email = "test@example.com"
if (email.isValidEmail()) {
    // ...
}

// ❌ MAL: Utility class
object StringUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
```

---

## Funciones

### 1. Funciones de Una Sola Expresión

```kotlin
// ✅ BIEN: Omitir llaves y return para funciones de una expresión
fun double(x: Int): Int = x * 2

fun getGreeting(name: String): String = "Hola, $name"

// El tipo de retorno se puede inferir
fun triple(x: Int) = x * 3
```

### 2. Named Arguments para Legibilidad

```kotlin
// ✅ BIEN: Named arguments cuando hay múltiples parámetros
createUser(
    name = "John Doe",
    email = "john@example.com",
    age = 25,
    isActive = true
)

// Especialmente útil con parámetros booleanos
button.setEnabled(enabled = true)
```

### 3. Default Arguments sobre Overloads

```kotlin
// ✅ BIEN: Usar default arguments
fun fetchUsers(
    page: Int = 1,
    pageSize: Int = 20,
    sortBy: String = "name"
): List<User> {
    // ...
}

// ❌ MAL: Múltiples overloads
fun fetchUsers(): List<User> = fetchUsers(1, 20, "name")
fun fetchUsers(page: Int): List<User> = fetchUsers(page, 20, "name")
fun fetchUsers(page: Int, pageSize: Int): List<User> = fetchUsers(page, pageSize, "name")
```

---

## Nullability

### 1. Evitar Null Cuando Sea Posible

```kotlin
// ✅ BIEN: Usar tipos no-null por defecto
data class User(
    val id: String,
    val name: String,
    val email: String
)

// Solo usar nullable cuando sea realmente necesario
data class Profile(
    val userId: String,
    val bio: String?,  // Puede ser null
    val website: String?  // Puede ser null
)
```

### 2. Safe Calls y Elvis Operator

```kotlin
// ✅ BIEN: Safe call con elvis operator
val length = name?.length ?: 0

// ✅ BIEN: let para ejecutar código solo si no es null
user?.let { currentUser ->
    println("Usuario: ${currentUser.name}")
}

// ❌ MAL: Múltiples null checks
if (user != null) {
    if (user.profile != null) {
        println(user.profile.bio)
    }
}

// ✅ BIEN: Chainear safe calls
val bio = user?.profile?.bio
```

### 3. requireNotNull y checkNotNull

```kotlin
// ✅ BIEN: Para validaciones
fun processUser(user: User?) {
    val validUser = requireNotNull(user) { "User cannot be null" }
    // validUser es no-null a partir de aquí
}

// ✅ BIEN: checkNotNull con mensaje personalizado
val config = checkNotNull(getConfig()) {
    "Configuration must be initialized before use"
}
```

---

## Collections

### 1. Usar Operadores de Colecciones

```kotlin
// ✅ BIEN: Operadores funcionales
val activeUsers = users.filter { it.isActive }
val userNames = users.map { it.name }
val totalScore = scores.sum()
val averageAge = users.map { it.age }.average()

// Combinar operaciones
val topActiveUsers = users
    .filter { it.isActive }
    .sortedByDescending { it.score }
    .take(10)
```

### 2. Usar Sequences para Grandes Colecciones

```kotlin
// ✅ BIEN: Sequence para operaciones pesadas
val result = users.asSequence()
    .filter { it.isActive }
    .map { it.calculateScore() }
    .filter { it > 100 }
    .toList()

// Sequences son lazy, las operaciones se ejecutan solo cuando se necesitan
```

### 3. Inmutabilidad en Colecciones

```kotlin
// ✅ BIEN: List inmutable por defecto
val items: List<String> = listOf("a", "b", "c")

// Solo usar MutableList cuando sea necesario modificar
val mutableItems: MutableList<String> = mutableListOf()
mutableItems.add("d")

// ✅ BIEN: Exponer como inmutable
class UserRepository {
    private val _users = mutableListOf<User>()
    val users: List<User> get() = _users
}
```

---

## Coroutines

### 1. Usar viewModelScope y lifecycleScope

```kotlin
// ✅ BIEN: En ViewModel, usar viewModelScope
class UserViewModel : ViewModel() {
    fun loadUsers() {
        viewModelScope.launch {
            // La coroutine se cancela automáticamente cuando el ViewModel se destruye
            val users = userRepository.getUsers()
            _uiState.value = UiState.Success(users)
        }
    }
}

// ✅ BIEN: En Activity/Fragment, usar lifecycleScope
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            // Se cancela automáticamente con el lifecycle
            viewModel.uiState.collect { state ->
                updateUi(state)
            }
        }
    }
}
```

### 2. Manejo de Errores

```kotlin
// ✅ BIEN: Try-catch en coroutines
viewModelScope.launch {
    try {
        _uiState.value = UiState.Loading
        val users = userRepository.getUsers()
        _uiState.value = UiState.Success(users)
    } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: "Unknown error")
    }
}

// ✅ BIEN: Usar Result para operaciones que pueden fallar
suspend fun getUsers(): Result<List<User>> = runCatching {
    apiService.getUsers()
}
```

### 3. Dispatchers Apropiados

```kotlin
// ✅ BIEN: IO para operaciones de red/base de datos
suspend fun fetchUsers(): List<User> = withContext(Dispatchers.IO) {
    apiService.getUsers()
}

// ✅ BIEN: Default para operaciones pesadas de CPU
suspend fun processData(data: List<String>): List<ProcessedData> =
    withContext(Dispatchers.Default) {
        data.map { processItem(it) }
    }

// Main se usa por defecto en viewModelScope y lifecycleScope
```

---

## Jetpack Compose

### 1. State Hoisting

```kotlin
// ✅ BIEN: Hoisting del estado al composable padre
@Composable
fun ParentScreen() {
    var text by remember { mutableStateOf("") }

    ChildInput(
        text = text,
        onTextChange = { text = it }
    )
}

@Composable
fun ChildInput(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier
    )
}
```

### 2. Remember para Estado Local

```kotlin
// ✅ BIEN: remember para estado que sobrevive recomposiciones
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }

    Button(onClick = { count++ }) {
        Text("Clicks: $count")
    }
}

// ✅ BIEN: rememberSaveable para sobrevivir cambios de configuración
@Composable
fun SaveableCounter() {
    var count by rememberSaveable { mutableStateOf(0) }

    Button(onClick = { count++ }) {
        Text("Clicks: $count")
    }
}
```

### 3. Composables Stateless

```kotlin
// ✅ BIEN: Composables sin estado son más reutilizables y testeables
@Composable
fun UserCard(
    user: User,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onUserClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.name, style = MaterialTheme.typography.titleMedium)
            Text(user.email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```

### 4. Modifier como Último Parámetro con Default

```kotlin
// ✅ BIEN: Modifier siempre como último parámetro con valor por defecto
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text)
    }
}
```

---

## Testing

### 1. Naming en Tests

```kotlin
// ✅ BIEN: Nombres descriptivos que explican qué se prueba
class UserRepositoryTest {
    @Test
    fun `getUser returns user when found in database`() {
        // ...
    }

    @Test
    fun `getUser throws exception when user not found`() {
        // ...
    }

    @Test
    fun `saveUser updates existing user when id matches`() {
        // ...
    }
}
```

### 2. Given-When-Then

```kotlin
@Test
fun `login succeeds with valid credentials`() {
    // Given
    val email = "test@example.com"
    val password = "password123"
    val expectedUser = User(id = "1", name = "Test", email = email)

    // When
    val result = authRepository.login(email, password)

    // Then
    assertEquals(expectedUser, result)
}
```

### 3. Testing Coroutines

```kotlin
class UserViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadUsers updates state to Success when repository returns users`() = runTest {
        // Given
        val users = listOf(User("1", "John", "john@example.com"))
        val repository = FakeUserRepository(users)
        val viewModel = UserViewModel(repository)

        // When
        viewModel.loadUsers()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(users, (state as UiState.Success).data)
    }
}
```

---

## Recursos Adicionales

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- [Jetpack Compose Guidelines](https://developer.android.com/develop/ui/compose/api-guidelines)
- [Effective Kotlin](https://kt.academy/book/effectivekotlin)

---

## Resumen de Anti-Patrones a Evitar

❌ **NO hacer:**
- Usar `var` cuando `val` es suficiente
- Usar `!!` (null assertion operator) - indica diseño pobre
- Crear clases con `null` por defecto cuando no es necesario
- Hacer operaciones pesadas en el hilo principal
- Ignorar los warnings del compilador
- Usar `Any` o `*` en genéricos cuando el tipo es conocido
- Crear utility classes en lugar de extension functions
- No usar los operadores de colecciones de Kotlin
- Hardcodear strings en el código (usar strings.xml)
- No manejar errores en coroutines

✅ **SÍ hacer:**
- Preferir inmutabilidad (`val`, `List`, `data class`)
- Usar null safety features de Kotlin
- Aprovechar las extension functions
- Usar scope functions (`let`, `apply`, `run`, `also`, `with`) apropiadamente
- Escribir tests para la lógica de negocio
- Usar sealed classes para estados finitos
- Seguir el principio de single responsibility
- Documentar código complejo con KDoc
