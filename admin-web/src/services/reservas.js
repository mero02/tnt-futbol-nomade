import { db } from '../lib/firebase'
import { collection, onSnapshot, query, orderBy, updateDoc, doc } from 'firebase/firestore'

const COL = 'reservas'

export function subscribeReservas(callback) {
  const q = query(collection(db, COL), orderBy('fecha', 'desc'))
  return onSnapshot(q, snap =>
    callback(
      snap.docs.map(d => {
        const data = d.data()
        return {
          id: d.id,
          ...data,
          fecha: data.fecha?.toDate?.() ?? null,
        }
      })
    )
  )
}

export function updateEstadoReserva(id, estado) {
  return updateDoc(doc(db, COL, id), { estado })
}
