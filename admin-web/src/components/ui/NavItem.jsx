import { NavLink } from 'react-router-dom'

export default function NavItem({ to, icon, label }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center space-x-3 p-3.5 rounded-xl transition-all w-full ${
          isActive
            ? 'bg-primary/10 dark:bg-primary/15 text-primary shadow-sm shadow-primary/5'
            : 'text-secondary hover:text-dark dark:hover:text-white hover:bg-gray-50 dark:hover:bg-gray-700/50'
        }`
      }
    >
      {({ isActive }) => (
        <>
          <span className={isActive ? 'text-primary' : 'text-secondary'}>{icon}</span>
          <span className={`text-sm ${isActive ? 'font-bold text-primary' : 'font-medium'}`}>{label}</span>
        </>
      )}
    </NavLink>
  )
}
