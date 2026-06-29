import { useState } from 'react'
import { Plus, MapPin, Loader2, Pencil, ToggleLeft, ToggleRight } from 'lucide-react'
import Card from '../components/ui/Card'
import Button from '../components/ui/Button'
import CanchaModal from '../components/canchas/CanchaModal'
import { useCanchas } from '../hooks/useCanchas'

const TIPO_LABELS = {
  FUTBOL_5:  'Fútbol 5',
  FUTBOL_7:  'Fútbol 7',
  FUTBOL_11: 'Fútbol 11',
}

function ActiveBadge({ active }) {
  const isActive = active !== false
  return (
    <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md text-xs font-medium
      ${isActive
        ? 'bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400'
        : 'bg-gray-100 text-gray-500 dark:bg-gray-700 dark:text-gray-400'
      }`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${isActive ? 'bg-green-500' : 'bg-gray-400'}`} />
      {isActive ? 'Activa' : 'Inactiva'}
    </span>
  )
}

export default function CanchasPage() {
  const { canchas, loading, createCancha, updateCancha, toggleCancha } = useCanchas()
  const [modal, setModal] = useState(null)

  const handleSave = async data => {
    if (modal === 'new') await createCancha(data)
    else await updateCancha(modal.id, data)
  }

  return (
    <div className="animate-in fade-in duration-300">
      <div className="flex items-center justify-between mb-10">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-dark dark:text-white">Gestión de Canchas</h1>
          <p className="text-xs text-secondary mt-1.5">Administra tus canchas, precios y ubicación.</p>
        </div>
        <Button onClick={() => setModal('new')} className="px-5 shadow-md shadow-primary/20">
          <Plus size={16} />
          <span>Añadir Cancha</span>
        </Button>
      </div>

      <Card className="p-0 overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-48">
            <Loader2 className="animate-spin text-secondary" size={20} />
          </div>
        ) : canchas.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-48 gap-2 text-secondary">
            <MapPin size={22} className="opacity-30" />
            <p className="text-xs">No hay canchas registradas aún</p>
          </div>
        ) : (
          <table className="w-full text-left border-collapse">
            <thead className="bg-gray-50 dark:bg-gray-700/50 border-b border-border dark:border-gray-700">
              <tr>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Cancha</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Ciudad</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Tipo</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Precio / Hr</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Estado</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border dark:divide-gray-700">
              {canchas.map(cancha => {
                const isActive = cancha.active !== false
                return (
                  <tr key={cancha.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-700/30 transition-colors group">
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 bg-gray-100 dark:bg-gray-700 rounded-lg flex items-center justify-center group-hover:bg-primary/10 dark:group-hover:bg-primary/20 transition-colors">
                          <MapPin size={15} className="text-secondary group-hover:text-primary transition-colors" />
                        </div>
                        <div>
                          <p className="text-sm font-semibold text-dark dark:text-gray-200">{cancha.nombre}</p>
                          <p className="text-[11px] text-secondary">{cancha.direccion}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-secondary">{cancha.ciudad}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-gray-100 dark:bg-gray-700 text-secondary dark:text-gray-400">
                        {TIPO_LABELS[cancha.tipo] ?? cancha.tipo}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm font-medium text-dark dark:text-gray-200">
                      ${cancha.precioPorHora.toLocaleString('es-AR')}
                    </td>
                    <td className="px-6 py-4">
                      <ActiveBadge active={cancha.active} />
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => setModal(cancha)}
                          className="p-2 rounded-lg text-secondary hover:text-primary hover:bg-primary/10 transition-colors"
                          title="Editar"
                        >
                          <Pencil size={15} />
                        </button>
                        <button
                          onClick={() => toggleCancha(cancha.id, isActive)}
                          className={`p-2 rounded-lg transition-colors ${
                            isActive
                              ? 'text-green-500 hover:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'
                              : 'text-gray-400 hover:text-green-500 hover:bg-green-50 dark:hover:bg-green-900/20'
                          }`}
                          title={isActive ? 'Desactivar cancha' : 'Activar cancha'}
                        >
                          {isActive
                            ? <ToggleRight size={20} />
                            : <ToggleLeft size={20} />
                          }
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </Card>

      {modal && (
        <CanchaModal
          cancha={modal === 'new' ? null : modal}
          onSave={handleSave}
          onClose={() => setModal(null)}
        />
      )}
    </div>
  )
}
