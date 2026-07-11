import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { signInWithPopup, GoogleAuthProvider } from 'firebase/auth'
import { Loader2 } from 'lucide-react'
import { auth } from '../lib/firebase'
import { useAuth } from '../hooks/useAuth'

export default function LoginPage() {
  const [submitting, setSubmitting] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (user) navigate('/dashboard', { replace: true })
  }, [user, navigate])

  const handleLogin = async () => {
    try {
      setSubmitting(true)
      const provider = new GoogleAuthProvider()
      // Forzamos el selector de cuenta para evitar bucles si intentan con una cuenta no admin
      provider.setCustomParameters({ prompt: 'select_account' })
      await signInWithPopup(auth, provider)
    } catch (error) {
      console.error('Login error:', error)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="h-screen w-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 transition-colors">
      <div className="bg-white dark:bg-gray-800 p-10 rounded-2xl border border-border dark:border-gray-700 w-full max-w-md shadow-sm">
        <div className="mb-8 text-center">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-primary/5 dark:bg-primary/10 rounded-3xl mb-6 overflow-hidden">
            <img src="/assets/logo.png" alt="Entra a la cancha logo" className="w-16 h-16 object-contain" />
          </div>
          <h1 className="text-xl font-bold text-dark dark:text-white tracking-tight">Entra a la cancha</h1>
          <p className="text-xs text-secondary mt-1.5 font-medium uppercase tracking-wider">Panel Administrativo</p>
        </div>

        <button
          onClick={handleLogin}
          disabled={submitting}
          className="w-full py-4 px-4 bg-primary text-white rounded-lg font-bold text-base hover:bg-primary/90 transition-all shadow-md shadow-primary/20 disabled:opacity-60 flex items-center justify-center gap-2"
        >
          {submitting && <Loader2 className="animate-spin" size={18} />}
          <span>Continuar con Google</span>
        </button>

        <div className="mt-8 pt-6 border-t border-border dark:border-gray-700 text-center">
          <p className="text-xs text-secondary leading-relaxed">
            Al ingresar, aceptás los términos de servicio para propietarios de establecimientos deportivos.
          </p>
        </div>
      </div>
    </div>
  )
}
