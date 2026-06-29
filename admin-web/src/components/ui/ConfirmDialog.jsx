import { AlertTriangle } from 'lucide-react'
import Button from './Button'

export default function ConfirmDialog({
  title,
  message,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  danger = true,
  onConfirm,
  onCancel,
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40 dark:bg-black/60 backdrop-blur-sm" onClick={onCancel} />
      <div className="relative bg-white dark:bg-gray-800 rounded-2xl border border-border dark:border-gray-700 w-full max-w-sm mx-4 shadow-xl p-6 space-y-4">
        <div className="flex items-start gap-3">
          {danger && (
            <div className="w-8 h-8 rounded-full bg-danger/10 flex items-center justify-center shrink-0 mt-0.5">
              <AlertTriangle size={15} className="text-danger" />
            </div>
          )}
          <div className="space-y-1">
            <h2 className="text-sm font-semibold text-dark dark:text-white">{title}</h2>
            {message && <p className="text-xs text-secondary leading-relaxed">{message}</p>}
          </div>
        </div>
        <div className="flex justify-end gap-2 pt-1">
          <Button variant="outline" onClick={onCancel} className="text-xs">
            {cancelLabel}
          </Button>
          <Button variant={danger ? 'danger' : 'primary'} onClick={onConfirm} className="text-xs">
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  )
}
