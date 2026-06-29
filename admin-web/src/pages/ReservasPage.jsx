import { useState, useMemo } from 'react'
import { CalendarDays, Loader2, X, Users, ChevronDown } from 'lucide-react'
import Card from '../components/ui/Card'
import { useReservas } from '../hooks/useReservas'

// ─── Config de estados ───────────────────────────────────────────────────────

const ESTADOS = {
  PENDIENTE:  { label: 'Pendiente',  bg: 'bg-yellow-50 dark:bg-yellow-900/20', text: 'text-yellow-700 dark:text-yellow-400', dot: 'bg-yellow-500' },
  CONFIRMADA: { label: 'Confirmada', bg: 'bg-green-50 dark:bg-green-900/20',   text: 'text-green-700 dark:text-green-400',  dot: 'bg-green-500' },
  CANCELADA:  { label: 'Cancelada',  bg: 'bg-red-50 dark:bg-red-900/20',       text: 'text-red-600 dark:text-red-400',      dot: 'bg-red-500' },
  COMPLETADA: { label: 'Completada', bg: 'bg-blue-50 dark:bg-blue-900/20',     text: 'text-blue-700 dark:text-blue-400',    dot: 'bg-blue-400' },
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

const fmt = (date, opts) => date ? new Date(date).toLocaleString('es-AR', opts) : '—'
const fmtFecha = date => fmt(date, { day: '2-digit', month: '2-digit', year: 'numeric' })
const fmtHora  = date => fmt(date, { hour: '2-digit', minute: '2-digit' })
const fmtPesos = n => `$${Number(n ?? 0).toLocaleString('es-AR')}`

const toISODate = date => date ? new Date(date).toISOString().slice(0, 10) : ''

// ─── Subcomponentes ──────────────────────────────────────────────────────────

function CambiarEstadoDropdown({ reservaId, estadoActual, onUpdate }) {
  const [open, setOpen] = useState(false)
  const cfg = ESTADOS[estadoActual] ?? ESTADOS.PENDIENTE

  return (
    <div className="relative">
      <button
        onClick={() => setOpen(o => !o)}
        className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md text-xs font-medium transition-opacity hover:opacity-80 ${cfg.bg} ${cfg.text}`}
      >
        <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${cfg.dot}`} />
        {cfg.label}
        <ChevronDown size={11} className={`transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>

      {open && (
        <>
          <div className="fixed inset-0 z-10" onClick={() => setOpen(false)} />
          <div className="absolute left-0 mt-1 z-20 bg-white dark:bg-gray-800 border border-border dark:border-gray-700 rounded-xl shadow-lg py-1 min-w-[148px]">
            {Object.entries(ESTADOS).map(([key, c]) => (
              <button
                key={key}
                onClick={() => { onUpdate(reservaId, key); setOpen(false) }}
                className={`w-full text-left px-3 py-2 text-xs flex items-center gap-2 transition-colors
                  hover:bg-gray-50 dark:hover:bg-gray-700/50
                  ${estadoActual === key ? 'font-semibold opacity-60 cursor-default' : ''}`}
              >
                <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${c.dot}`} />
                {c.label}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  )
}

function FilterChip({ label, active, onClick }) {
  return (
    <button
      onClick={onClick}
      className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
        active
          ? 'bg-primary text-white'
          : 'bg-gray-100 dark:bg-gray-700 text-secondary hover:text-dark dark:hover:text-white'
      }`}
    >
      {label}
    </button>
  )
}

// ─── Página ──────────────────────────────────────────────────────────────────

const FILTROS_ESTADO = ['TODOS', 'PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA']

export default function ReservasPage() {
  const { reservas, loading, updateEstado } = useReservas()
  const [filtroEstado, setFiltroEstado] = useState('TODOS')
  const [filtroFecha, setFiltroFecha]   = useState('')

  const reservasFiltradas = useMemo(() => {
    return reservas.filter(r => {
      if (filtroEstado !== 'TODOS' && r.estado !== filtroEstado) return false
      if (filtroFecha && toISODate(r.fecha) !== filtroFecha) return false
      return true
    })
  }, [reservas, filtroEstado, filtroFecha])

  const contadores = useMemo(() => {
    const acc = { PENDIENTE: 0, CONFIRMADA: 0, CANCELADA: 0, COMPLETADA: 0 }
    reservas.forEach(r => { if (acc[r.estado] !== undefined) acc[r.estado]++ })
    return acc
  }, [reservas])

  return (
    <div className="animate-in fade-in duration-300">

      {/* Header */}
      <div className="mb-8">
        <h1 className="text-xl font-bold tracking-tight text-dark dark:text-white">Reservas</h1>
        <p className="text-xs text-secondary mt-1.5">Gestión de reservas de tus canchas.</p>
      </div>

      {/* Tarjetas resumen */}
      <div className="grid grid-cols-4 gap-4 mb-8">
        {Object.entries(ESTADOS).map(([key, cfg]) => (
          <Card key={key} className="p-4 space-y-1">
            <p className="text-[11px] font-semibold text-secondary uppercase tracking-wider">{cfg.label}</p>
            <p className="text-2xl font-bold text-dark dark:text-white">{contadores[key]}</p>
          </Card>
        ))}
      </div>

      {/* Filtros */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          {FILTROS_ESTADO.map(f => (
            <FilterChip
              key={f}
              label={f === 'TODOS' ? 'Todos' : ESTADOS[f].label}
              active={filtroEstado === f}
              onClick={() => setFiltroEstado(f)}
            />
          ))}
        </div>
        <div className="flex items-center gap-2">
          <CalendarDays size={15} className="text-secondary" />
          <input
            type="date"
            value={filtroFecha}
            onChange={e => setFiltroFecha(e.target.value)}
            className="text-xs px-3 py-1.5 rounded-lg border border-border dark:border-gray-600 bg-white dark:bg-gray-700/50 text-dark dark:text-gray-200 focus:outline-none focus:border-primary/60"
          />
          {filtroFecha && (
            <button onClick={() => setFiltroFecha('')} className="text-xs text-secondary hover:text-dark dark:hover:text-white">
              <X size={14} />
            </button>
          )}
        </div>
      </div>

      {/* Tabla */}
      <Card className="p-0 overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-48">
            <Loader2 className="animate-spin text-secondary" size={20} />
          </div>
        ) : reservasFiltradas.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-48 gap-2 text-secondary">
            <CalendarDays size={22} className="opacity-30" />
            <p className="text-xs">No hay reservas para los filtros seleccionados</p>
          </div>
        ) : (
          <table className="w-full text-left border-collapse">
            <thead className="bg-gray-50 dark:bg-gray-700/50 border-b border-border dark:border-gray-700">
              <tr>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Fecha</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Cancha</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Equipo</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Duración</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Pago</th>
                <th className="px-6 py-4 text-xs font-bold text-secondary uppercase tracking-wider">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border dark:divide-gray-700">
              {reservasFiltradas.map(r => (
                <tr key={r.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4">
                    <p className="text-sm font-medium text-dark dark:text-gray-200">{fmtFecha(r.fecha)}</p>
                    <p className="text-[11px] text-secondary">{fmtHora(r.fecha)}</p>
                  </td>
                  <td className="px-6 py-4">
                    <p className="text-sm text-dark dark:text-gray-200">{r.cancha?.nombre ?? '—'}</p>
                    <p className="text-[11px] text-secondary">{r.cancha?.tipo?.replace('_', ' ') ?? ''}</p>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-1.5">
                      <Users size={13} className="text-secondary shrink-0" />
                      <span className="text-sm text-dark dark:text-gray-200">
                        {r.nombreEquipo ?? <span className="text-secondary italic">Sin equipo</span>}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-secondary">
                    {r.duracionHoras === 1 ? '1 hora' : `${r.duracionHoras} hs`}
                  </td>
                  <td className="px-6 py-4">
                    <p className="text-sm font-medium text-dark dark:text-gray-200">{fmtPesos(r.montoPagado)}</p>
                    <p className="text-[11px] text-secondary">de {fmtPesos(r.precioTotal)}</p>
                  </td>
                  <td className="px-6 py-4">
                    <CambiarEstadoDropdown
                      reservaId={r.id}
                      estadoActual={r.estado}
                      onUpdate={updateEstado}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  )
}
