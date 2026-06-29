export default function Card({ children, className = "" }) {
  return (
    <div className={`bg-white dark:bg-gray-800 border border-border dark:border-gray-700 rounded-xl p-6 transition-all hover:border-primary/30 dark:hover:border-primary/40 ${className}`}>
      {children}
    </div>
  )
}
