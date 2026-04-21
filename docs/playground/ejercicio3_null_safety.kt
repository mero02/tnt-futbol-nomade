// ============================================================
// EJERCICIO 3 — Null Safety
// Sumar lista de montos donde algunos pueden ser null
// Restricción: NO usar if (x != null)
// ============================================================

// Double?  → puede ser null o un número
// Double   → NUNCA puede ser null (el compilador lo garantiza)

fun sumarMontos(montos: List<Double?>): Double {
    // sumOf recorre la lista y acumula el resultado de cada elemento.
    // Para cada elemento `monto`:
    //   - si monto es null → ?: 0.0 devuelve 0.0 (operador Elvis)
    //   - si monto tiene valor → usa ese valor directamente
    // Sin un solo if. Sin NullPointerException posible.
    return montos.sumOf { monto -> monto ?: 0.0 }
}

// Versión alternativa usando safe call (?.) + fold
// fold es como un acumulador manual: empieza en 0.0 y va sumando
fun sumarMontosConFold(montos: List<Double?>): Double {
    return montos.fold(0.0) { acumulador, monto ->
        acumulador + (monto ?: 0.0)  // si monto es null, suma 0.0
    }
}

fun main() {
    val montos = listOf(100.0, null, 250.5, null, 75.0, null, 50.0)

    val total = sumarMontos(montos)
    println("Lista: $montos")
    println("Total (ignorando nulls): $total")  // → 475.5

    val totalFold = sumarMontosConFold(montos)
    println("Total con fold: $totalFold")       // → 475.5

    // --- Mostrar cómo funcionan los operadores por separado ---

    val valorNulo: Double? = null
    val valorReal: Double? = 99.9

    // Safe call (?.) — llama el método solo si no es null, sino devuelve null
    println(valorNulo?.toString())  // null (no explota)
    println(valorReal?.toString())  // "99.9"

    // Elvis (?:) — valor por defecto cuando el lado izquierdo es null
    println(valorNulo ?: 0.0)      // 0.0
    println(valorReal ?: 0.0)      // 99.9

    // Combinados — cadena segura con fallback
    // "si valorNulo no es null, convertilo a String; sino, usá 'sin valor'"
    val texto = valorNulo?.toString() ?: "sin valor"
    println(texto)  // "sin valor"
}
