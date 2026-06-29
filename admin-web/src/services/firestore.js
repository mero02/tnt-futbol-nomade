import { db } from '../lib/firebase'
import {
  collection, addDoc,
  updateDoc as fsUpdate,
  doc, onSnapshot, serverTimestamp, query, orderBy,
} from 'firebase/firestore'

export function subscribeCollection(col, callback) {
  const q = query(collection(db, col), orderBy('createdAt', 'desc'))
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
