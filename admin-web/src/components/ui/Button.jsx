export default function Button({ children, variant = "primary", className = "", ...props }) {
  const variants = {
    primary: "bg-primary text-white hover:bg-primary/90",
    outline: "bg-transparent border border-border dark:border-gray-600 text-dark dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700/50",
    ghost: "bg-transparent text-secondary hover:text-dark dark:hover:text-white hover:bg-gray-50 dark:hover:bg-gray-700/50",
    danger: "bg-transparent text-danger hover:bg-danger/5 dark:hover:bg-danger/10 border border-danger/20"
  }

  return (
    <button
      {...props}
      className={`px-4 py-2.5 rounded-lg font-medium transition-all flex items-center justify-center space-x-2 text-sm disabled:opacity-50 ${variants[variant]} ${className}`}
    >
      {children}
    </button>
  )
}
