package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Reporte
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ReporteRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val reportesCol = firestore.collection("reportes")

    suspend fun enviarReporte(reporte: Reporte) {
        reportesCol.add(reporte).await()
    }
}
