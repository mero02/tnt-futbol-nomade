import { subscribeCollection, createDoc, updateDoc, toggleActive } from './firestore'

const COL = 'canchas'

export const subscribeCanchas  = cb             => subscribeCollection(COL, cb)
export const createCancha      = data           => createDoc(COL, data)
export const updateCancha      = (id, data)     => updateDoc(COL, id, data)
export const toggleCancha      = (id, current)  => toggleActive(COL, id, current)
