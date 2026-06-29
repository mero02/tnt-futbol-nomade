import { useState, useEffect } from 'react'
import { subscribeReservas, updateEstadoReserva } from '../services/reservas'

export function useReservas() {
  const [reservas, setReservas] = useState([])
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    const unsub = subscribeReservas(data => {
      setReservas(data)
      setLoading(false)
    })
    return () => unsub()
  }, [])

  return { reservas, loading, updateEstado: updateEstadoReserva }
}
