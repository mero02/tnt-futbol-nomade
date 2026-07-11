import { useState, useEffect } from 'react'
import { auth, db } from '../lib/firebase'
import { onAuthStateChanged, signOut } from 'firebase/auth'
import { doc, getDoc } from 'firebase/firestore'

export function useAuth() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      if (currentUser) {
        // Verificar si el usuario tiene el rol ADMIN en Firestore
        try {
          const userDoc = await getDoc(doc(db, 'users', currentUser.uid))
          if (userDoc.exists() && userDoc.data().role === 'ADMIN') {
            setUser({ ...currentUser, ...userDoc.data() })
          } else {
            // Si no es admin, cerramos la sesión automáticamente
            await signOut(auth)
            setUser(null)
            alert('Acceso denegado: Se requieren permisos de administrador.')
          }
        } catch (error) {
          console.error("Error verificando rol:", error)
          setUser(null)
        }
      } else {
        setUser(null)
      }
      setLoading(false)
    })
    return () => unsubscribe()
  }, [])

  return { user, loading }
}
