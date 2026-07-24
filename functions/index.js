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

    if (data.tipo !== "ENTER") return;

    const { userId, canchaId, manual } = data;
    let { partidoId } = data;

    try {
        // 1. Si no viene partidoId (evento automático), buscamos el partido activo en esa cancha
        if (!partidoId) {
            const ahora = admin.firestore.Timestamp.now();
            const snap = await admin.firestore()
                .collection("partidos")
                .where("cancha.id", "==", canchaId)
                .where("estado", "!=", "FINALIZADO")
                .get();

            // Tomamos el partido más próximo al momento actual
            const candidatos = snap.docs
                .map(d => ({ id: d.id, ...d.data() }))
                .filter(p => p.participantesIds?.includes(userId));

            if (candidatos.length === 0) return;
            candidatos.sort((a, b) => {
                const ta = a.fecha?.toMillis?.() ?? 0;
                const tb = b.fecha?.toMillis?.() ?? 0;
                return Math.abs(ta - ahora.toMillis()) - Math.abs(tb - ahora.toMillis());
            });
            partidoId = candidatos[0].id;
        }

        // 2. Obtener datos del usuario que llegó
        const userArrivalDoc = await admin.firestore().collection("users").doc(userId).get();
        const userArrival = userArrivalDoc.data();
        const userName = userArrival?.apodo || userArrival?.displayName || "Un jugador";

        // 3. Obtener datos del partido
        const partidoDoc = await admin.firestore().collection("partidos").doc(partidoId).get();
        const partido = partidoDoc.data();
        if (!partido) return;

        const nombreCancha = partido.cancha?.nombre || "la cancha";

        // 4. Identificar destinatarios (participantes + creador, excluyendo al que llegó)
        const recipientsIds = [...new Set([...(partido.participantesIds ?? []), partido.creatorId])]
            .filter(id => id !== userId);

        if (recipientsIds.length === 0) return;

        // 5. Obtener tokens FCM de los destinatarios
        const tokens = [];
        const userDocs = await Promise.all(
            recipientsIds.map(id => admin.firestore().collection("users").doc(id).get())
        );
        userDocs.forEach(doc => {
            const fcmToken = doc.data()?.fcmToken;
            if (fcmToken) tokens.push(fcmToken);
        });

        if (tokens.length === 0) return;

        // 6. Enviar notificación push.
        // Solo payload data (sin notification): FCM siempre llama a onMessageReceived
        // sin importar el estado de la app (foreground/background/killed), lo que
        // permite al servicio construir el Intent con deep link y navegar al partido.
        const response = await admin.messaging().sendEachForMulticast({
            tokens,
            data: {
                titulo: "¡Alguien llegó!",
                mensaje: `${userName} ya está en ${nombreCancha}.`,
                partidoId: partidoId,
                tipo: "LLEGADA_JUGADOR",
            },
            android: {
                priority: "high",
            },
        });

        console.log(`Notificaciones enviadas: ${response.successCount}. Errores: ${response.failureCount}`);

    } catch (error) {
        console.error("Error procesando evento de geocerca:", error);
    }
});

/**
 * Entrega como push FCM cualquier notificación in-app que la app escribe en
 * `notificaciones` (solicitud recibida/aprobada/rechazada, partido cercano, etc.).
 * La app solo persiste el documento; esta función lo convierte en push real
 * para que llegue con la app cerrada.
 */
exports.onNotificacionCreated = onDocumentCreated("notificaciones/{notifId}", async (event) => {
    const notif = event.data.data();
    if (!notif) return;

    const { userId, titulo, mensaje, partidoId, tipo } = notif;
    if (!userId) return;

    try {
        // 1. Obtener el token FCM del destinatario
        const userDoc = await admin.firestore().collection("users").doc(userId).get();
        const fcmToken = userDoc.data()?.fcmToken;
        if (!fcmToken) {
            console.log(`Sin fcmToken para el usuario ${userId}; se omite el push.`);
            return;
        }

        // 2. Enviar push. Solo payload data (sin notification) para que el
        // MessagingService construya el Intent con deep link al partido,
        // igual que en el flujo de geocercas.
        await admin.messaging().send({
            token: fcmToken,
            data: {
                titulo: titulo || "Fútbol TNT",
                mensaje: mensaje || "",
                partidoId: partidoId || "",
                tipo: tipo || "INFO",
            },
            android: {
                priority: "high",
            },
        });

        console.log(`Push de notificación (${tipo}) enviado a ${userId}.`);

    } catch (error) {
        console.error("Error enviando push de notificación:", error);
    }
});
