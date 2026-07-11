import { LayoutDashboard, MapPin, Calendar, Users, Trophy, LogOut } from 'lucide-react'
import { signOut } from 'firebase/auth'
import { auth } from '../../lib/firebase'
import NavItem from '../ui/NavItem'

export default function Sidebar({ user }) {
  return (
    <aside className="w-64 bg-white dark:bg-gray-800 border-r border-border dark:border-gray-700 flex flex-col shrink-0 transition-colors">
      <div className="p-8 flex items-center space-x-3">
        <img src="/assets/logo.png" alt="Logo" className="w-8 h-8 object-contain rounded-lg" />
        <span className="font-bold text-lg tracking-tight text-dark dark:text-white">Entra a la cancha</span>
      </div>

      <nav className="flex-1 px-4 space-y-1">
        <NavItem to="/dashboard" icon={<LayoutDashboard size={18} />} label="Dashboard" />
        <NavItem to="/canchas" icon={<MapPin size={18} />} label="Canchas" />
        <NavItem to="/reservas" icon={<Calendar size={18} />} label="Reservas" />
        <NavItem to="/partidos" icon={<Trophy size={18} />} label="Partidos" />
        <NavItem to="/usuarios" icon={<Users size={18} />} label="Usuarios" />
      </nav>

      <div className="p-4 border-t border-border dark:border-gray-700">
        <div className="flex items-center space-x-3 mb-4 px-3">
          <img
            src={user?.photoURL}
            alt="avatar"
            className="w-9 h-9 rounded-full bg-gray-100 dark:bg-gray-700 object-cover shrink-0"
          />
          <div className="overflow-hidden">
            <p className="text-sm font-semibold truncate text-dark dark:text-gray-100">{user?.displayName}</p>
            <p className="text-xs text-secondary">Propietario</p>
          </div>
        </div>
        <button
          onClick={() => signOut(auth)}
          className="flex items-center space-x-3 text-secondary hover:text-danger transition-colors w-full p-3 rounded-xl hover:bg-danger/5 dark:hover:bg-danger/10"
        >
          <LogOut size={18} />
          <span className="text-sm font-medium">Cerrar Sesión</span>
        </button>
      </div>
    </aside>
  )
}
