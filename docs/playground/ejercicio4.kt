//Extension funtions
fun Int.esPar(): Boolean = this % 2 == 0

fun Int.esPrimo(): Boolean {
    if (this < 2) return false
    for (i in 2 until this) {
        if (this % i == 0) return false
    }
    return true
}

// Clase para probar apply
data class Persona(
    var nombre: String = "",
    var edad: Int = 0
)

fun main() {

    val persona = Persona().apply {
        nombre = "Mauro"
        edad = 35
    }

    println("$persona \n")

    //Collections API A
    val resultadoA = (1..20)
        .filter { it % 3 == 0 }
        .map { it * it }

    println("Cuadrados multiplos de tres: $resultadoA \n")

    //Collections API B
    data class Transaccion(
        val categoria: String,
        val monto: Double
    )

    val transacciones = listOf(
        Transaccion("Comida", 150.0),
        Transaccion("Comida", 80.0),
        Transaccion("Transporte", 120.0),
        Transaccion("Transporte", 90.0),
        Transaccion("Ocio", 200.0),
        Transaccion("Ocio", 50.0)
    )

    val reporte = transacciones
        .filter { it.monto > 100.0 }
        .groupBy { it.categoria }
        .mapValues { entry -> entry.value.map { it.monto }.average() }

    println("Reporte: $reporte \n")

    //Prueba extension functions
    println("Cuatro es par? ${4.esPar()}")
    println("Cuatro es par? ${3.esPar()}")
    
    var siete: Boolean = 7.esPrimo()
    var seis: Boolean = 6.esPrimo()   
    println("Siete es primo? $siete. \n")
    println("Seis es primo? $seis. \n")
}
