# Fútbol Nómade

## Descripción del Proyecto

Fútbol Nómade es una aplicación móvil diseñada para conectar equipos de fútbol amateur incompletos con jugadores disponibles en tiempo real, facilitando la organización de partidos y promoviendo la integración de jugadores en distintas ciudades.

## Problema

En muchas ciudades argentinas, los grupos organizados de fútbol amateur encuentran dificultades para completar sus partidos semanales debido a la ausencia de algunos jugadores. Paralelamente, existe una gran cantidad de personas (especialmente aquellas que viajan por trabajo o estudio) que desean jugar al fútbol pero no cuentan con un grupo fijo en cada ciudad que visitan.

## Solución Propuesta

Se propone una aplicación móvil que permita:
- A los organizadores de partidos publicar encuentros indicando ubicación, fecha, horario y cantidad de jugadores faltantes.
- A los usuarios visualizar partidos cercanos mediante geolocalización y postularse para participar según su disponibilidad.

Además, la aplicación incorpora el concepto de "fútbol nómade", donde los jugadores pueden integrarse temporalmente a distintos grupos en función de su ubicación actual.

## Características Principales

- Publicación y búsqueda de partidos por geolocalización.
- Perfil de usuario con posición en la cancha, nivel de juego y sistema de reputación.
- Notificaciones push para cubrir vacantes urgentes.
- Interfaz mobile-first para acceso inmediato en situaciones contextuales.
- Posible backend para gestión de usuarios, partidos y ubicaciones.
- Panel web administrativo para gestión de datos y monitoreo (opcional).

## Tecnologías Utilizadas

- **Frontend móvil**: Android (basado en .gitignore y estructura de proyecto)
- **Backend**: Por definir (por ejemplo, Node.js, Python, o Firebase)
- **Base de datos**: Por definir (por ejemplo, PostgreSQL, MongoDB, o Firebase Firestore)
- **Geolocalización**: GPS del dispositivo y servicios de mapas (por ejemplo, Google Maps API)
- **Notificaciones**: Firebase Cloud Messaging (FCM) o similar

## Instalación

1. Clonar el repositorio:
    ```bash
    git clone https://github.com/tu-usuario/tnt-futbol-nomade.git
    ```
2. Abrir el proyecto en Android Studio.
3. Configurar las variables de entorno para el backend (API keys, etc.).
4. Compilar y ejecutar en un dispositivo Android o emulador.

## Uso

- Al iniciar la aplicación, los usuarios pueden registrarse o iniciar sesión.
- Los organizadores pueden crear un nuevo partido rellenando el formulario con los detalles.
- Los jugadores pueden explorar el mapa para ver partidos cercanos y apuntarse a aquellos que les interesen.
- Los usuarios recibirán notificaciones cuando se publiquen partidos que coincidan con sus preferencias o cuando se necesiten jugadores para un partido al que se han apuntado.
