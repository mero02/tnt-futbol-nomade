import { db } from '../lib/firebase'
import {
  collection, addDoc,
  updateDoc as fsUpdate,
  doc, onSnapshot, serverTimestamp, query, orderBy,
} from 'firebase/firestore'

export function subscribeCollection(col, callback) {
  // Quitamos el orderBy por defecto ya que no todas las colecciones tienen 'createdAt'
  // Esto evita que la consulta falle y devuelva una lista vacía si el campo no existe.
  const q = query(collection(db, col))
  return onSnapshot(q, snap =>
    callback(snap.docs.map(d => ({ id: d.id, ...d.data() })))
  )
}

export function createDoc(col, data) {
  return addDoc(collection(db, col), { ...data, active: true, createdAt: serverTimestamp() })
}

export function updateDoc(col, id, data) {
  return fsUpdate(doc(db, col, id), data)
}

export function toggleActive(col, id, current) {
  return fsUpdate(doc(db, col, id), { active: !current })
}
