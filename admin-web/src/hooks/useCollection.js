import { useState, useEffect } from 'react'
import { subscribeCollection, createDoc, updateDoc, toggleActive } from '../services/firestore'

export function useCollection(col) {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const unsub = subscribeCollection(col, data => {
      setItems(data)
      setLoading(false)
    })
    return () => unsub()
  }, [col])

  return {
    items,
    loading,
    create: data            => createDoc(col, data),
    update: (id, data)      => updateDoc(col, id, data),
    toggle: (id, current)   => toggleActive(col, id, current),
  }
}
