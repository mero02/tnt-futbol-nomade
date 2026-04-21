// ============================================================
// EJERCICIO 2 — Data Class
// Transcripción de UserAccount de Java a Kotlin
// ============================================================

// En Java necesitábamos ~30 líneas: constructor, getters,
// toString, equals y hashCode. En Kotlin alcanza con esto:
data class UserAccount(
    val uuid: String,
    val email: String,
    val balance: Double
)

// El compilador genera automáticamente:
//   - getters (acceso directo: cuenta.uuid, cuenta.email, cuenta.balance)
//   - toString()  → "UserAccount(uuid=..., email=..., balance=...)"
//   - equals()    → compara por contenido, no por referencia de memoria
//   - hashCode()  → consistente con equals (necesario para usar en Sets/Maps)
//   - copy()      → clonar el objeto cambiando solo los campos que queremos

fun main() {
    // Instanciar — sin "new", igual que una función normal
    val cuenta = UserAccount(
        uuid = "abc-123",
        email = "pancho@futbol.com",
        balance = 1500.0
    )

    // --- toString() generado automáticamente ---
    println("toString: $cuenta")
    // → UserAccount(uuid=abc-123, email=pancho@futbol.com, balance=1500.0)

    // --- Getters: acceso directo a las propiedades ---
    println("uuid: ${cuenta.uuid}")
    println("email: ${cuenta.email}")
    println("balance: ${cuenta.balance}")

    // --- equals(): compara por contenido, no por referencia ---
    val otraCuenta = UserAccount("abc-123", "pancho@futbol.com", 1500.0)
    println("Son iguales (equals): ${cuenta == otraCuenta}")  // true
    println("Son el mismo objeto: ${cuenta === otraCuenta}")  // false — distinto objeto en memoria

    // --- copy(): crear una copia con solo algunos campos distintos ---
    // Como fotocopiar un formulario y cambiar solo el saldo — el resto queda igual
    val cuentaActualizada = cuenta.copy(balance = 2000.0)
    println("Cuenta actualizada: $cuentaActualizada")
    println("Cuenta original (no se modificó): $cuenta")

    // --- Destructuring: extraer campos directamente en variables ---
    val (uuid, email, balance) = cuenta
    println("uuid=$uuid, email=$email, balance=$balance")
}
