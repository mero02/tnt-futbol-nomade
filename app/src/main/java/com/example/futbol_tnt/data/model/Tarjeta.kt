package com.example.futbol_tnt.data.model

data class Tarjeta(
    val id: String = "",
    val nombreTitular: String = "",
    val last4: String = "",
    val vencimiento: String = "", // MM/AA
    val marca: MarcaTarjeta = MarcaTarjeta.VISA,
    val esPredeterminada: Boolean = false
)

enum class MarcaTarjeta(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("American Express"),
    OTRA("Otra")
}
