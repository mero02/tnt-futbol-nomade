import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { CalendarDays, DollarSign, Clock, MapPin, Calendar, ChevronRight } from 'lucide-react'
import Card from '../components/ui/Card'
import Button from '../components/ui/Button'
import { useCanchas }  from '../hooks/useCanchas'
import { useReservas } from '../hooks/useReservas'
import { useCollection } from '../hooks/useCollection'

// ─── Helpers ─────────────────────────────────────────────────────────────────

const toISODate = date => date ? new Date(date).toISOString().slice(0, 10) : ''
const fmtPesos  = n => `$${Number(n ?? 0).toLocaleString('es-AR')}`
const fmtHora   = date => date ? new Date(date).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' }) : '—'
const today     = new Date().toISOString().slice(0, 10)

const ESTADO_COLORS = {
  PENDIENTE:  'bg-yellow-50 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400',
  CONFIRMADA: 'bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400',
  CANCELADA:  'bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400',
  COMPLETADA: 'bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400',
}
const ESTADO_LABELS = {
  PENDIENTE: 'Pendiente', CONFIRMADA: 'Confirmada',
  CANCELADA: 'Cancelada', COMPLETADA: 'Completada',
}

// ─── Subcomponentes ──────────────────────────────────────────────────────────

function StatCard({ icon: Icon, iconBg, iconColor, title, value, sub }) {
  return (
    <Card className="hover:shadow-md hover:shadow-gray-200/50 dark:hover:shadow-black/20">
      <div className="flex items-start justify-between mb-5">
        <p className="text-[11px] font-bold text-secondary uppercase tracking-widest">{title}</p>
        <div className={`p-1.5 rounded-lg ${iconBg}`}>
          <Icon size={14} className={iconColor} />
        </div>
      </div>
      <h3 className="text-2xl font-bold text-dark dark:text-white tracking-tight">{value}</h3>
      <p className="mt-2.5 text-xs text-secondary">{sub}</p>
    </Card>
  )
}

// ─── Página ──────────────────────────────────────────────────────────────────

export default function DashboardPage() {
  const navigate = useNavigate()
  const { canchas }  = useCanchas()
  const { reservas } = useReservas()
  const { items: partidos } = useCollection('partidos')
  const { items: usuarios } = useCollection('users')

  const hoy = useMemo(() => today, [])

  const stats = useMemo(() => {
    const reservasHoy = reservas.filter(r => toISODate(r.fecha) === hoy)
    const activas     = canchas.filter(c => c.active !== false)
    const pendientes  = reservas.filter(r => r.estado === 'PENDIENTE')
    const ingresosHoy = reservasHoy.reduce((acc, r) => acc + (r.montoPagado ?? 0), 0)

    return {
      reservasHoy:  reservasHoy.length,
      canchasActivas: activas.length,
      pendientes:   pendientes.length,
      ingresosHoy,
    }
  }, [reservas, canchas, hoy])

  const recientes = useMemo(() => reservas.slice(0, 5), [reservas])

  const fechaLabel = new Date().toLocaleDateString('es-AR', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  })

  return (
    <div className="animate-in fade-in duration-300">

      {/* Header */}
      <div className="mb-10">
        <h1 className="text-xl font-bold tracking-tight text-dark dark:text-white">Panel de Control</h1>
        <p className="text-xs text-secondary mt-1.5 capitalize">{fechaLabel}</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-5 mb-10">
        <StatCard
          icon={CalendarDays}
          iconBg="bg-primary/5 dark:bg-primary/10"
          iconColor="text-primary"
          title="Reservas hoy"
          value={stats.reservasHoy}
          sub="Turnos agendados para hoy"
        />
        <StatCard
          icon={DollarSign}
          iconBg="bg-green-50 dark:bg-green-900/20"
          iconColor="text-green-600 dark:text-green-400"
          title="Ingresos del día"
          value={fmtPesos(stats.ingresosHoy)}
          sub="Monto cobrado hoy"
        />
        <StatCard
          icon={Clock}
          iconBg="bg-yellow-50 dark:bg-yellow-900/20"
          iconColor="text-yellow-600 dark:text-yellow-400"
          title="Pendientes de pago"
          value={stats.pendientes}
          sub="Reservas sin confirmar"
        />
        <StatCard
          icon={MapPin}
          iconBg="bg-blue-50 dark:bg-blue-900/20"
          iconColor="text-blue-600 dark:text-blue-400"
          title="Canchas activas"
          value={stats.canchasActivas}
          sub={`de ${canchas.length} en total`}
        />
      </div>

      {/* Bottom grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Reservas recientes */}
        <Card>
          <h2 className="text-sm font-semibold mb-5 text-dark dark:text-white">Reservas Recientes</h2>
          {recientes.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-32 gap-2 text-secondary">
              <Calendar size={20} className="opacity-30" />
              <p className="text-xs">No hay reservas aún</p>
            </div>
          ) : (
            <div>
              {recientes.map(r => (
                <div key={r.id} className="flex items-center justify-between py-3 border-b border-border dark:border-gray-700 last:border-0">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 bg-gray-50 dark:bg-gray-700/50 rounded-lg flex items-center justify-center shrink-0">
                      <Calendar size={15} className="text-secondary" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-dark dark:text-gray-200">
                        {r.cancha?.nombre ?? '—'}
                      </p>
                      <p className="text-xs text-secondary">
                        {toISODate(r.fecha) === hoy ? 'Hoy' : new Date(r.fecha).toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit' })}
                        {', '}{fmtHora(r.fecha)}
                        {r.nombreEquipo ? ` · ${r.nombreEquipo}` : ''}
                      </p>
                    </div>
                  </div>
                  <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-md ${ESTADO_COLORS[r.estado] ?? ''}`}>
                    {ESTADO_LABELS[r.estado] ?? r.estado}
                  </span>
                </div>
              ))}
            </div>
          )}
          <Button variant="outline" className="w-full mt-5 text-xs" onClick={() => navigate('/reservas')}>
            Ver todas las reservas
            <ChevronRight size={14} />
          </Button>
        </Card>

        {/* Estado del sistema */}
        <Card>
          <h2 className="text-sm font-semibold mb-5 text-dark dark:text-white">Estado del Sistema</h2>
          <div className="flex items-start gap-4 p-4 bg-primary/5 dark:bg-primary/10 rounded-xl border border-primary/10 dark:border-primary/20">
            <div className="w-2.5 h-2.5 bg-primary rounded-full mt-1.5 shrink-0 animate-pulse" />
            <div>
              <p className="text-sm font-semibold text-primary dark:text-primary">Sistema Sincronizado</p>
              <p className="text-xs text-secondary mt-1 leading-relaxed">
                Firebase responde correctamente. Los cambios en precios y disponibilidad se reflejan en la app móvil en tiempo real.
              </p>
            </div>
          </div>
          <div className="mt-4 grid grid-cols-2 gap-3">
            <div className="p-3 rounded-xl bg-gray-50 dark:bg-gray-700/50 space-y-1">
              <p className="text-[10px] font-bold text-secondary uppercase tracking-wider">Canchas</p>
              <p className="text-lg font-bold text-dark dark:text-white">{canchas.length}</p>
            </div>
            <div className="p-3 rounded-xl bg-gray-50 dark:bg-gray-700/50 space-y-1">
              <p className="text-[10px] font-bold text-secondary uppercase tracking-wider">Usuarios</p>
              <p className="text-lg font-bold text-dark dark:text-white">{usuarios.length}</p>
            </div>
            <div className="p-3 rounded-xl bg-gray-50 dark:bg-gray-700/50 space-y-1">
              <p className="text-[10px] font-bold text-secondary uppercase tracking-wider">Partidos</p>
              <p className="text-lg font-bold text-dark dark:text-white">{partidos.length}</p>
            </div>
            <div className="p-3 rounded-xl bg-gray-50 dark:bg-gray-700/50 space-y-1">
              <p className="text-[10px] font-bold text-secondary uppercase tracking-wider">Reservas</p>
              <p className="text-lg font-bold text-dark dark:text-white">{reservas.length}</p>
            </div>
          </div>
        </Card>

      </div>
    </div>
  )
}
