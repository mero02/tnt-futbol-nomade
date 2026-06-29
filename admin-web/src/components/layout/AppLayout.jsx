import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import TopBar from './TopBar'
import { useTheme } from '../../hooks/useTheme'

export default function AppLayout({ user }) {
  const { theme, toggle } = useTheme()

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex font-sans antialiased text-dark dark:text-gray-100">
      <Sidebar user={user} />
      <main className="flex-1 flex flex-col h-screen overflow-hidden">
        <TopBar theme={theme} onToggleTheme={toggle} />
        <div className="flex-1 overflow-y-auto p-8">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
