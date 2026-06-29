# Futbol TNT - Panel Administrativo

Este es el panel web para dueños de complejos deportivos. Desde aquí podrán gestionar sus canchas, horarios y ver el estado de las reservas en tiempo real.

## Tecnologías
- **React** (Vite)
- **Tailwind CSS**
- **Lucide React** (Iconos)
- **Firebase** (Auth y Firestore)

## Requisitos
- Node.js instalado.

## Instalación y Ejecución

1. Entra a la carpeta:
   ```bash
   cd admin-web
   ```

2. Instala las dependencias:
   ```bash
   npm install
   ```

3. Ejecuta el proyecto en modo desarrollo:
   ```bash
   npm run dev
   ```

4. Abre el navegador en `http://localhost:5173`.

## Estructura de Carpetas
- `/src/components`: Componentes reutilizables (Botones, Inputs, Cards).
- `/src/pages`: Vistas principales (Dashboard, Canchas, Login).
- `/src/hooks`: Lógica de conexión con Firebase.
