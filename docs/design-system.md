# Fútbol Nómade — Design System

Tokens visuales extraídos de `docs/boceto/navegacion.png`. Fuente de verdad para colores, tipografía y componentes.

## 1. Paleta de colores (fija — 7 colores)

| # | Token         | Hex       | Rol único                                                            |
|---|---------------|-----------|----------------------------------------------------------------------|
| 1 | `Verde`       | `#22C55E` | CTA primario, FAB, icono activo bottom nav, badges activos, online   |
| 2 | `VerdeOscuro` | `#0F5132` | Header de perfil (curva), contraste sobre verde claro                |
| 3 | `Blanco`      | `#FFFFFF` | Background, surface, texto sobre verde/negro                         |
| 4 | `Negro`       | `#111827` | Texto principal, bottom nav, headlines                               |
| 5 | `GrisTexto`   | `#6B7280` | Texto secundario, placeholders, iconos inactivos                     |
| 6 | `GrisBorde`   | `#E5E7EB` | Bordes, separadores, fondo de input                                  |
| 7 | `Rojo`        | `#EF4444` | Solo crítico: rechazar, urgente, login                               |

**Reglas de uso:**
- Verde domina (acción positiva, marca).
- Blanco = lienzo.
- Negro = jerarquía de texto.
- 2 grises únicos = todo lo "calmo".
- Rojo es **acento** — usar con moderación, nunca decorativo.
- Sin variantes "Soft", "Dark", "Warning". Si más adelante hace falta un estado nuevo, se agrega justificado.

## 2. Tipografía

- **Familia**: Sans-serif geométrica (Inter / Plus Jakarta Sans / Manrope).
- **Headline grande** (ej. "Login", "Tu Agenda"): 28-32sp, weight 700.
- **Section title** ("Detalle del partido", "Centro de Notificaciones"): 18-20sp, weight 600.
- **Body**: 14-16sp, weight 400-500.
- **Caption / metadata**: 12sp, weight 400, color `GrisTexto`.
- **Brand label** ("FÚTBOL NÓMADE"): 14sp, weight 700, **tracking expandido** (~2sp), uppercase.

## 3. Radios

| Token              | Valor   | Uso                                  |
|--------------------|---------|--------------------------------------|
| Material `small`   | `8dp`   | Inputs, chips chicos                 |
| Material `medium`  | `12dp`  | Cards estándar                       |
| Material `large`   | `16dp`  | Cards de partido, notificaciones     |
| Pill               | `999dp` | Botones primarios, badges, FAB       |

Elevación suave (`shadow 0 2 8 rgba(0,0,0,0.06)`) en cards sobre fondo blanco. Bottom nav sin sombra, color sólido.

## 4. Componentes

### Botón primario (CTA)
- Fondo: `Verde` (positivo) o `Rojo` (login / acción crítica).
- Texto: `Blanco`, weight 600.
- Forma: pill.
- Padding: `14dp × 24dp`.
- Altura mínima: `48dp`.

### Botón secundario (outline)
- Fondo: `Blanco`.
- Borde: `1dp` `GrisBorde`.
- Texto: `Negro`.

### Card de partido
- Fondo: `Blanco`.
- Radius: `16dp`.
- Padding: `16dp`.
- Border-left accent: tira `4dp` `Rojo` ("URGENTE") o `Verde` ("ABIERTO").
- Sombra suave.

### Badge / Chip
- Forma: pill.
- Padding: `4dp × 10dp`.
- Texto: `12sp`, weight 600, uppercase.
- Activo: fondo `Verde`, texto `Blanco`.
- Crítico: fondo `Rojo`, texto `Blanco`.
- Neutro: fondo `GrisBorde`, texto `Negro`.

### Avatar
- Circular.
- Tamaños: `40dp` (lista), `64dp` (perfil header), `96dp` (perfil propio).
- Borde `Blanco` `2dp` sobre fondos de color.
- Indicador online: círculo `12dp` `Verde` con borde `Blanco` `2dp`, esquina inferior derecha.

### Bottom navigation
- Fondo: `Negro`.
- Altura: `64dp`.
- 4-5 items, icono `24dp`.
- Icono activo: `Verde` con círculo `Verde` alrededor (spotlight).
- Icono inactivo: `GrisTexto`.

### Header de perfil (curvo)
- Fondo: `VerdeOscuro`.
- Onda/curva inferior.
- Avatar centrado superpuesto a la curva.
- Texto `Blanco` sobre verde.

### FAB
- Circular `56dp`.
- Fondo: `Verde`.
- Icono `Blanco`.

### Tabs (Próximos / Historial, Recibidas / Enviadas)
- Indicador inferior `Verde`, `2dp`.
- Texto activo: `Negro`, weight 600.
- Texto inactivo: `GrisTexto`.

## 5. Espaciado (escala 4)

`4 / 8 / 12 / 16 / 20 / 24 / 32 / 40`

Padding de pantalla horizontal: `20dp`.

## 6. Iconografía

- Lineales, weight medio (Lucide / Feather / Material Symbols Rounded).
- Color por defecto `Negro` o `GrisTexto`.
- Acento activo: `Verde`.

## 7. Mapeo a Compose

Archivos en `app/src/main/java/com/example/futbol_tnt/presentation/ui/theme/`:

| Archivo        | Contiene                                                          |
|----------------|-------------------------------------------------------------------|
| `Color.kt`     | Los 7 tokens (`Verde`, `VerdeOscuro`, `Blanco`, `Negro`, `GrisTexto`, `GrisBorde`, `Rojo`) + aliases de compatibilidad para nombres legacy (`BluePrimary`, `Gray*`, etc.) |
| `Theme.kt`     | `FutbolTNTTheme { ... }` con `primary = Verde`                    |
| `Type.kt`      | `Typography` (Material3)                                          |

**Para código nuevo**: usar los 7 tokens directamente. Los aliases existen sólo para que las pantallas existentes sigan compilando — al refactorizarlas, ir migrando a los nombres canónicos.
