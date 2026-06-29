import { Search, Bell, Sun, Moon } from 'lucide-react'

export default function TopBar({ theme, onToggleTheme }) {
  return (
    <header className="h-16 bg-white dark:bg-gray-800 border-b border-border dark:border-gray-700 flex items-center justify-between px-8 shrink-0 transition-colors">
      <div className="relative w-96">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-secondary" size={16} />
        <input
          type="text"
          placeholder="Buscar..."
          className="w-full pl-10 pr-4 py-2 rounded-lg border border-border dark:border-gray-600 focus:outline-none focus:border-primary/60 focus:ring-2 focus:ring-primary/10 bg-gray-50/50 dark:bg-gray-700/50 dark:text-gray-200 dark:placeholder:text-gray-500 text-sm transition-all"
        />
      </div>

      <div className="flex items-center space-x-1">
        <button className="relative p-2 text-secondary hover:text-primary dark:hover:text-primary rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
          <Bell size={20} />
          <span className="absolute top-2 right-2 w-2 h-2 bg-danger rounded-full border-2 border-white dark:border-gray-800"></span>
        </button>
        <button
          onClick={onToggleTheme}
          aria-label="Cambiar tema"
          className="p-2 text-secondary hover:text-primary dark:hover:text-primary rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
        >
          {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
        </button>
      </div>
    </header>
  )
}
