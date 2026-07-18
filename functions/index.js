const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * HU-42 y HU-43: Trigger que se ejecuta al registrarse un evento de geocerca.
 * Notifica al organizador y jugadores cuando alguien llega a la cancha.
 */
exports.onGeofenceEventCreated = onDocumentCreated("geofence_events/{eventId}", async (event) => {
    const data = event.data.data();
    if (!data) return;

    // Solo procesamos entradas (ENTER)
    if (data.tipo !== "ENTER") return;

    const { userId, partidoId, canchaId, manual } = data;

    try {
        // 1. Obtener datos del usuario que llegó
        const userArrivalDoc = await admin.firestore().collection("users").document(userId).get();
        const userArrival = userArrivalDoc.data();
        const userName = userArrival?.apodo || userArrival?.displayName || "Un jugador";

        // 2. Obtener datos del partido
        const partidoDoc = await admin.firestore().collection("partidos").document(partidoId).get();
        const partido = partidoDoc.data();
        if (!partido) return;

        const nombreCancha = partido.cancha?.nombre || "la cancha";

        // 3. Identificar destinatarios (Participantes + Creador, excluyendo al que llegó)
        const recipientsIds = [...new Set([...partido.participantesIds, partido.creatorId])]
            .filter(id => id !== userId);

        if (recipientsIds.length === 0) return;

        // 4. Obtener tokens de los destinatarios
        const tokens = [];
        const userDocs = await Promise.all(recipientsIds.map(id =>
            admin.firestore().collection("users").document(id).get()
        ));

        userDocs.forEach(doc => {
            const userData = doc.data();
            if (userData?.fcmToken) {
                tokens.push(userData.fcmToken);
            }
        });

        if (tokens.length === 0) return;

        // 5. Enviar notificación Push
        const payload = {
            notification: {
                title: "¡Alguien llegó!",
                body: `${userName} ya está en ${nombreCancha}.`
            },
            data: {
                partidoId: partidoId,
                tipo: "LLEGADA_JUGADOR",
                click_action: "FLUTTER_NOTIFICATION_CLICK" // Para compatibilidad
            }
        };

        const response = await admin.messaging().sendEachForMulticast({
            tokens: tokens,
            notification: payload.notification,
            data: payload.data
        });

        console.log(`Notificaciones enviadas: ${response.successCount}. Errores: ${response.failureCount}`);

    } catch (error) {
        console.error("Error procesando evento de geocerca:", error);
    }
});
