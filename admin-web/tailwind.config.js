/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#10B981",
        'primary-dark': "#059669",
        dark: "#111827",
        secondary: "#6B7280",
        border: "#E5E7EB",
        danger: "#EF4444"
      },
      fontFamily: {
        sans: ['"Geist Variable"', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
