import { useState, useCallback } from 'react'
import { X } from 'lucide-react'
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIconUrl from 'leaflet/dist/images/marker-icon.png'
import markerIconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import markerShadowUrl from 'leaflet/dist/images/marker-shadow.png'
import Button from '../ui/Button'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconUrl: markerIconUrl,
  iconRetinaUrl: markerIconRetinaUrl,
  shadowUrl: markerShadowUrl,
})

const TIPOS = [
  { value: 'FUTBOL_5',  label: 'Fútbol 5' },
  { value: 'FUTBOL_7',  label: 'Fútbol 7' },
  { value: 'FUTBOL_11', label: 'Fútbol 11' },
]

const DEFAULT_CENTER = [-42.766, -65.03]

const RADIO_DEFAULT = 120
const RADIO_MIN = 100
const RADIO_MAX = 150

const EMPTY = {
  nombre: '',
  ciudad: '',
  direccion: '',
  lat: null,
  lng: null,
  tipo: 'FUTBOL_5',
  precioPorHora: '',
  radioGeofence: RADIO_DEFAULT,
}

const inputClass = "w-full px-3 py-2 rounded-lg border border-border dark:border-gray-600 bg-white dark:bg-gray-700/50 text-sm text-dark dark:text-gray-200 placeholder:text-secondary focus:outline-none focus:border-primary/60 focus:ring-2 focus:ring-primary/10 transition-all"

function Field({ label, children }) {
  return (
    <div className="space-y-1.5">
      <label className="text-[11px] font-semibold text-secondary uppercase tracking-wider">{label}</label>
      {children}
    </div>
  )
}

async function reverseGeocode(lat, lng) {
  try {
    const res = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&accept-language=es`,
      { headers: { 'Accept-Language': 'es' } }
    )
    const data = await res.json()
    const addr = data.address ?? {}
    const ciudad = addr.city ?? addr.town ?? addr.municipality ?? addr.county ?? ''
    const road = addr.road ?? ''
    const number = addr.house_number ?? ''
    const direccion = road ? `${road}${number ? ' ' + number : ''}` : (data.display_name ?? '')
    return { ciudad, direccion }
  } catch (_) {
    return { ciudad: '', direccion: '' }
  }
}

function ClickHandler({ onPick }) {
  useMapEvents({
    click: async e => {
      const { lat, lng } = e.latlng
      const { ciudad, direccion } = await reverseGeocode(lat, lng)
      onPick({ lat, lng, ciudad, direccion })
    },
  })
  return null
}

function MapPanel({ lat, lng, onPick }) {
  const center = lat != null && lng != null ? [lat, lng] : DEFAULT_CENTER
  const zoom   = lat != null ? 14 : 10

  return (
    <div className="relative h-full w-full">
      <MapContainer
        center={center}
        zoom={zoom}
        style={{ height: '100%', width: '100%' }}
        scrollWheelZoom
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        />
        {lat != null && lng != null && <Marker position={[lat, lng]} />}
        <ClickHandler onPick={onPick} />
      </MapContainer>

      <div className="absolute bottom-2 left-2 right-2 pointer-events-none z-[1000]">
        <p className="text-[10px] text-center text-secondary bg-white/85 dark:bg-gray-900/85 backdrop-blur-sm rounded-md px-2 py-1">
          {lat != null && lng != null
            ? `${lat.toFixed(5)}, ${lng.toFixed(5)} · Tocá para mover el pin`
            : 'Tocá el mapa para marcar la ubicación'}
        </p>
      </div>
    </div>
  )
}

export default function CanchaModal({ cancha, onSave, onClose }) {
  const isEdit = !!cancha
  const [form, setForm] = useState(
    isEdit
      ? { ...cancha, precioPorHora: String(cancha.precioPorHora), radioGeofence: cancha.radioGeofence ?? RADIO_DEFAULT }
      : EMPTY
  )
  const [saving, setSaving] = useState(false)
  const [error, setError]   = useState(null)

  const set = field => e => setForm(f => ({ ...f, [field]: e.target.value }))

  const handleMapPick = useCallback(({ lat, lng, ciudad, direccion }) => {
    setForm(f => ({
      ...f,
      lat,
      lng,
      ...(ciudad    && { ciudad }),
      ...(direccion && { direccion }),
    }))
  }, [])

  const handleSubmit = async e => {
    e.preventDefault()
    setError(null)
    setSaving(true)
    try {
      const radio = Math.min(RADIO_MAX, Math.max(RADIO_MIN, Number(form.radioGeofence) || RADIO_DEFAULT))
      const lat = Number(form.lat)
      const lng = Number(form.lng)
      await onSave({
        ...form,
        precioPorHora: Number(form.precioPorHora),
        lat,
        lng,
        radioGeofence: radio,
        geofence: { lat, lng, radio },
      })
      onClose()
    } catch (err) {
      console.error('Error al guardar cancha:', err)
      setError(err?.message ?? 'No se pudo guardar. Revisá los permisos de Firestore.')
    } finally {
      setSaving(false)
    }
  }

  const canSubmit = form.lat != null && form.lng != null && form.nombre && form.precioPorHora

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 dark:bg-black/60 backdrop-blur-sm" onClick={onClose} />

      <div className="relative bg-white dark:bg-gray-800 rounded-2xl border border-border dark:border-gray-700 w-full max-w-3xl shadow-xl flex flex-col" style={{ maxHeight: '90vh' }}>

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border dark:border-gray-700 shrink-0">
          <h2 className="text-sm font-semibold text-dark dark:text-white">
            {isEdit ? 'Editar Cancha' : 'Nueva Cancha'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 text-secondary hover:text-dark dark:hover:text-white rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
          >
            <X size={16} />
          </button>
        </div>

        {/* Body: mapa izquierda + formulario derecha */}
        <div className="flex flex-1 min-h-0">

          {/* Mapa */}
          <div className="w-1/2 border-r border-border dark:border-gray-700 overflow-hidden rounded-bl-2xl">
            <MapPanel lat={form.lat} lng={form.lng} onPick={handleMapPick} />
          </div>

          {/* Formulario */}
          <form
            id="cancha-form"
            onSubmit={handleSubmit}
            className="w-1/2 overflow-y-auto p-6 space-y-4"
          >
            <Field label="Nombre">
              <input
                type="text"
                required
                value={form.nombre}
                onChange={set('nombre')}
                placeholder="Ej: Golfo Fútbol"
                className={inputClass}
              />
            </Field>

            <div className="grid grid-cols-2 gap-3">
              <Field label="Tipo">
                <select value={form.tipo} onChange={set('tipo')} className={inputClass}>
                  {TIPOS.map(t => (
                    <option key={t.value} value={t.value}>{t.label}</option>
                  ))}
                </select>
              </Field>
              <Field label="Precio / hora ($)">
                <input
                  type="number"
                  required
                  min="0"
                  value={form.precioPorHora}
                  onChange={set('precioPorHora')}
                  placeholder="20000"
                  className={inputClass}
                />
              </Field>
            </div>

            <Field label={`Radio geocerca (m) · ${RADIO_MIN}-${RADIO_MAX}`}>
              <input
                type="number"
                min={RADIO_MIN}
                max={RADIO_MAX}
                step="5"
                value={form.radioGeofence}
                onChange={set('radioGeofence')}
                placeholder={String(RADIO_DEFAULT)}
                className={inputClass}
              />
            </Field>

            <Field label="Ciudad">
              <input
                type="text"
                required
                value={form.ciudad}
                onChange={set('ciudad')}
                placeholder="Ej: Puerto Madryn"
                className={inputClass}
              />
            </Field>

            <Field label="Dirección">
              <input
                type="text"
                required
                value={form.direccion}
                onChange={set('direccion')}
                placeholder="Ej: Bv. Brown 1200"
                className={inputClass}
              />
            </Field>
          </form>
        </div>

        {/* Footer */}
        <div className="flex flex-col gap-3 px-6 py-4 border-t border-border dark:border-gray-700 shrink-0">
          {error && (
            <p className="text-xs text-red-500 dark:text-red-400 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg px-3 py-2">
              {error}
            </p>
          )}
          <div className="flex items-center justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose} className="text-xs">
              Cancelar
            </Button>
            <Button form="cancha-form" type="submit" disabled={saving || !canSubmit} className="text-xs">
              {saving ? 'Guardando...' : isEdit ? 'Guardar cambios' : 'Crear cancha'}
            </Button>
          </div>
        </div>

      </div>
    </div>
  )
}
