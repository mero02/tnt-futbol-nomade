import { useMemo, useState } from 'react'
import { Users, Star, ShieldAlert, ShieldCheck } from 'lucide-react'
import Card from '../components/ui/Card'
import Button from '../components/ui/Button'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import { useCollection } from '../hooks/useCollection'

export default function UsuariosPage() {
  const { items: usuarios, update } = useCollection('users')
  const [userToToggle, setUserToToggle] = useState(null)

  const handleConfirmToggle = () => {
    if (!userToToggle) return
    update(userToToggle.id, { isBanned: !userToToggle.isBanned })
    setUserToToggle(null)
  }

  return (
    <div className="animate-in fade-in duration-300">
      <div className="mb-10">
        <h1 className="text-xl font-bold tracking-tight text-dark dark:text-white">Gestión de Usuarios</h1>
        <p className="text-xs text-secondary mt-1.5">Administra los jugadores y propietarios registrados en la plataforma.</p>
      </div>

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead className="bg-gray-50/80 dark:bg-gray-800/50 border-b border-border dark:border-gray-700">
            <tr>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest">Usuario</th>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest">Reputación</th>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest">Posición / Nivel</th>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest">Estado</th>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest text-right">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border dark:divide-gray-700">
            {usuarios.map(u => (
              <tr key={u.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/30 transition-colors group">
                <td className="px-6 py-5">
                  <div className="flex items-center gap-3">
                    <img src={u.photoUrl} alt="" className="w-10 h-10 rounded-full border border-border" />
                    <div>
                      <p className="text-sm font-bold text-dark dark:text-gray-100">{u.displayName || 'Sin nombre'}</p>
                      <p className="text-[11px] text-secondary">{u.email}</p>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-5">
                  <div className="flex items-center gap-1.5">
                    <Star size={14} className="text-yellow-500 fill-yellow-500" />
                    <span className="text-sm font-bold text-dark dark:text-gray-200">{Number(u.valoracionPromedio || 0).toFixed(1)}</span>
                  </div>
                </td>
                <td className="px-6 py-5">
                   <p className="text-sm text-dark dark:text-gray-300">{u.posicion || '—'}</p>
                   <p className="text-[11px] text-secondary">{u.nivel || '—'}</p>
                </td>
                <td className="px-6 py-5">
                  {u.isBanned ? (
                    <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-700 dark:bg-red-900/20 dark:text-red-400 uppercase tracking-wider">
                      Baneado
                    </span>
                  ) : (
                    <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-green-100 text-green-700 dark:bg-green-900/20 dark:text-green-400 uppercase tracking-wider">
                      Activo
                    </span>
                  )}
                </td>
                <td className="px-6 py-5 text-right">
                  {u.role !== 'ADMIN' ? (
                    <Button
                      variant="ghost"
                      className={`${u.isBanned ? 'text-green-600' : 'text-red-500'} hover:bg-gray-100 dark:hover:bg-gray-700/50 p-2`}
                      onClick={() => setUserToToggle(u)}
                      title={u.isBanned ? 'Desbloquear' : 'Bloquear'}
                    >
                      {u.isBanned ? <ShieldCheck size={18} /> : <ShieldAlert size={18} />}
                    </Button>
                  ) : (
                    <span className="text-[10px] font-bold text-secondary uppercase px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded">
                      Inmune
                    </span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>

      {userToToggle && (
        <ConfirmDialog
          title={userToToggle.isBanned ? 'Desbloquear usuario' : 'Bloquear usuario'}
          message={`¿Estás seguro de que deseas ${userToToggle.isBanned ? 'habilitar' : 'suspender'} el acceso a ${userToToggle.displayName || userToToggle.email}?`}
          confirmLabel={userToToggle.isBanned ? 'Desbloquear' : 'Bloquear'}
          danger={!userToToggle.isBanned}
          onConfirm={handleConfirmToggle}
          onCancel={() => setUserToToggle(null)}
        />
      )}
    </div>
  )
}
