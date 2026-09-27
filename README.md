# IglesiaFlow

Panel de gestión eclesiástica en español, construido con React, TypeScript, Vite, Material UI y React Router. Incluye un diseño adaptable inspirado en Material Design 3.

## Empezar

Requiere Node.js 20.19+ o 22.12+.

```bash
npm install
npm run dev
```

Abre la URL que indique Vite (por defecto `http://localhost:5173`). Para verificar la compilación: `npm run build`. Para servir la compilación: `npm run preview`.

## Qué incluye

- **Resumen:** indicadores calculados desde los registros, participación por ministerio, próximos eventos, miembros recientes y balance mensual.
- **Miembros:** búsqueda, filtro, paginación, alta, edición, eliminación con confirmación y exportación CSV.
- **Eventos:** calendario navegable, filtro por fecha y estado, alta, edición y eliminación. Valida que los inscritos no superen la capacidad.
- **Finanzas:** ingresos y gastos, evolución mensual, categorías, filtro, alta, edición, eliminación y exportación CSV.
- **Reportes:** indicadores en tiempo real de los datos disponibles, distribución de miembros, ministerios, ocupación y exportación CSV.
- **Navegación adaptable:** barra lateral, menú móvil, búsqueda de miembros y avisos de pendientes/próximos eventos.

## Datos y límites de esta versión

Esta es una **demo local**, no un sistema de producción. Al abrirla por primera vez se cargan registros de ejemplo en `src/data/mockData.ts`. Las modificaciones se guardan únicamente en el `localStorage` del navegador actual; no se comparten entre dispositivos y pueden perderse al limpiar los datos del navegador. Los importes de ejemplo se muestran en USD y las fechas se calculan con la fecha local del dispositivo. **No hay autenticación, permisos, backend ni base de datos remota. No introduzcas datos personales o financieros reales en esta demo.**

Para producción, el siguiente paso es conectar un backend (por ejemplo, Supabase o Firebase), incorporar autenticación y roles, validar operaciones en el servidor y establecer copias de seguridad, auditoría y políticas de privacidad.

## Estructura

- `src/context/ChurchContext.tsx` — estado compartido y persistencia local.
- `src/data/mockData.ts` — registros iniciales de ejemplo.
- `src/components` — layout, tarjetas, tablas y componentes de uso común.
- `src/pages` — resumen, miembros, eventos, finanzas y reportes.
- `src/utils` — formatos, exportación CSV y métricas derivadas.
- `src/theme.ts` — tema visual de Material UI.
