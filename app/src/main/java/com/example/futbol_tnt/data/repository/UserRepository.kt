package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository(
    firestore: FirebaseFirestore = Firebase.firestore,
) : IUserRepository {

    private val usersCol = firestore.collection("users")

    override suspend fun syncUser(user: User) {
        val doc = usersCol.document(user.uid).get().await()
        if (!doc.exists()) {
            // Si no existe, lo creamos con los datos básicos de Google
            usersCol.document(user.uid).set(user.toFirestoreMap()).await()
        } else {
            // Si existe, actualizamos solo lo básico (por si cambió nombre o foto en Google)
            // pero mantenemos lo que el usuario haya personalizado (posicion, nivel, etc)
            usersCol.document(user.uid).update(
                mapOf(
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl,
                    "email" to user.email,
                    "fcmToken" to user.fcmToken
                )
            ).await()
        }
    }

    override suspend fun getUser(uid: String): User? {
        return usersCol.document(uid).get().await().toUserOrNull()
    }

    override suspend fun updateUser(user: User) {
        usersCol.document(user.uid).set(user.toFirestoreMap()).await()
    }
}

private fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
    "uid" to uid,
    "email" to email,
    "displayName" to displayName,
    "photoUrl" to photoUrl,
    "posicion" to posicion?.name,
    "nivel" to nivel?.name,
    "telefono" to telefono,
    "biografia" to biografia,
    "apodo" to apodo,
    "fechaNacimiento" to fechaNacimiento,
    "sexo" to sexo?.name,
    "piernaDominante" to piernaDominante?.name,
    "formatoPreferido" to formatoPreferido?.name,
    "equipo" to equipo,
    "valoracionPromedio" to valoracionPromedio,
    "fcmToken" to fcmToken
)

private fun com.google.firebase.firestore.DocumentSnapshot.toUserOrNull(): User? {
    return if (exists()) {
        User(
            uid = getString("uid") ?: id,
            email = getString("email"),
            displayName = getString("displayName"),
            photoUrl = getString("photoUrl"),
            posicion = getString("posicion")?.let { runCatching { Posicion.valueOf(it) }.getOrNull() },
            nivel = getString("nivel")?.let { runCatching { NivelJuego.valueOf(it) }.getOrNull() },
            telefono = getString("telefono"),
            biografia = getString("biografia"),
            apodo = getString("apodo"),
            fechaNacimiento = getString("fechaNacimiento"),
            sexo = getString("sexo")?.let { runCatching { Sexo.valueOf(it) }.getOrNull() },
            piernaDominante = getString("piernaDominante")?.let { runCatching { PiernaDominante.valueOf(it) }.getOrNull() },
            formatoPreferido = getString("formatoPreferido")?.let { runCatching { FormatoPreferido.valueOf(it) }.getOrNull() },
            equipo = getString("equipo"),
            valoracionPromedio = getDouble("valoracionPromedio") ?: 0.0,
            fcmToken = getString("fcmToken")
        )
    } else null
}
