import { useMemo, useState } from 'react'
import { Trophy, Calendar, MapPin, Trash2, Ban } from 'lucide-react'
import Card from '../components/ui/Card'
import Button from '../components/ui/Button'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import { useCollection } from '../hooks/useCollection'

const ESTADO_COLORS = {
  ABIERTO:   'bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400',
  LLENO:     'bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400',
  EN_JUEGO:  'bg-yellow-50 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400',
  FINALIZADO: 'bg-gray-50 text-gray-700 dark:bg-gray-800 dark:text-gray-400',
  CANCELADO: 'bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400',
}

export default function PartidosPage() {
  const { items: partidos, update } = useCollection('partidos')
  const [partidoToCancel, setPartidoToCancel] = useState(null)

  const handleConfirmCancel = () => {
    if (!partidoToCancel) return
    update(partidoToCancel.id, { estado: 'CANCELADO' })
    setPartidoToCancel(null)
  }

  return (
    <div className="animate-in fade-in duration-300">
      <div className="mb-10">
        <h1 className="text-xl font-bold tracking-tight text-dark dark:text-white">Gestión de Partidos</h1>
        <p className="text-xs text-secondary mt-1.5">Monitorea y gestiona los encuentros organizados por la comunidad.</p>
      </div>

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead className="bg-gray-50/80 dark:bg-gray-800/50 border-b border-border dark:border-gray-700">
            <tr>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest">Encuentro</th>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest">Cancha</th>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest">Jugadores</th>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest">Estado</th>
              <th className="px-6 py-4 text-[11px] font-bold text-secondary uppercase tracking-widest text-right">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border dark:divide-gray-700">
            {partidos.map(p => (
              <tr key={p.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/30 transition-colors group">
                <td className="px-6 py-5">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center border border-primary/20">
                      <Trophy size={18} className="text-primary" />
                    </div>
                    <div>
                      <p className="text-sm font-bold text-dark dark:text-gray-100">{p.nombreLocal} vs {p.nombreVisitante}</p>
                      <p className="text-[11px] text-secondary">
                        {new Date(p.fecha).toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })}hs
                      </p>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-5 text-sm text-secondary">
                  <div className="flex items-center gap-1.5 font-medium">
                    <MapPin size={14} className="opacity-50" />
                    {p.cancha?.nombre}
                  </div>
                </td>
                <td className="px-6 py-5 text-sm">
                  <span className="font-bold text-dark dark:text-gray-200">{p.jugadoresActuales}</span>
                  <span className="text-secondary"> / {p.jugadoresMaximos}</span>
                </td>
                <td className="px-6 py-5">
                  <span className={`text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wider ${ESTADO_COLORS[p.estado]}`}>
                    {p.estado}
                  </span>
                </td>
                <td className="px-6 py-5 text-right">
                  {p.estado !== 'CANCELADO' && (
                    <Button
                      variant="ghost"
                      className="text-red-500 hover:bg-red-50 dark:hover:bg-red-900/10 p-2"
                      onClick={() => setPartidoToCancel(p)}
                    >
                      <Trash2 size={16} />
                    </Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>

      {partidoToCancel && (
        <ConfirmDialog
          title="Cancelar partido"
          message={`¿Estás seguro de que deseas cancelar el encuentro entre ${partidoToCancel.nombreLocal} y ${partidoToCancel.nombreVisitante}? Esta acción no se puede deshacer.`}
          confirmLabel="Cancelar partido"
          onConfirm={handleConfirmCancel}
          onCancel={() => setPartidoToCancel(null)}
        />
      )}
    </div>
  )
}
