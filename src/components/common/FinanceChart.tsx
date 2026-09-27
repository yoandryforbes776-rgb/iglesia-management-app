import { Box, Stack, Typography } from '@mui/material';
import type { FinanceRecord } from '../../types';
import { money, monthKey } from '../../utils/format';

export default function FinanceChart({ records }: { records: FinanceRecord[] }) {
  const months = Array.from({ length: 6 }, (_, i) => { const d = new Date(); d.setDate(1); d.setMonth(d.getMonth() - (5 - i)); const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`; const rows = records.filter((r) => monthKey(r.date) === key); return { key, label: new Intl.DateTimeFormat('es-ES', { month: 'short' }).format(d).replace('.', ''), income: rows.filter((r) => r.type === 'Ingreso').reduce((sum, r) => sum + r.amount, 0), expenses: rows.filter((r) => r.type === 'Gasto').reduce((sum, r) => sum + r.amount, 0) }; });
  const max = Math.max(1, ...months.flatMap((m) => [m.income, m.expenses]));
  return <Box>
    <Stack direction="row" spacing={2.5} sx={{ mb: 2.5 }}><Stack direction="row" alignItems="center" spacing={.8}><Box sx={{ width: 9, height: 9, borderRadius: 1, bgcolor: '#4d9877' }} /><Typography color="text.secondary" sx={{ fontSize: 11 }}>Ingresos</Typography></Stack><Stack direction="row" alignItems="center" spacing={.8}><Box sx={{ width: 9, height: 9, borderRadius: 1, bgcolor: '#e0b387' }} /><Typography color="text.secondary" sx={{ fontSize: 11 }}>Gastos</Typography></Stack></Stack>
    <Box sx={{ height: 190, display: 'flex', position: 'relative' }}>
      <Box sx={{ position: 'absolute', inset: '0 0 22px 0', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', pointerEvents: 'none' }}>{[1, .5, 0].map((v) => <Box key={v} sx={{ borderTop: '1px dashed #e9eeea', width: '100%', height: 1 }} />)}</Box>
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', width: '100%', gap: { xs: .7, sm: 2 }, position: 'relative' }}>{months.map((m) => <Box key={m.key} sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'flex-end', minWidth: 0 }}><Box sx={{ height: 168, display: 'flex', justifyContent: 'center', alignItems: 'flex-end', gap: { xs: 3/8, sm: 1 }, px: .5 }}><Box title={`Ingresos ${m.label}: ${money(m.income)}`} sx={{ width: { xs: 11, sm: 22 }, maxWidth: '40%', height: `${Math.max(m.income / max * 100, m.income ? 3 : 0)}%`, bgcolor: '#4d9877', borderRadius: '5px 5px 0 0', transition: 'height .3s' }} /><Box title={`Gastos ${m.label}: ${money(m.expenses)}`} sx={{ width: { xs: 11, sm: 22 }, maxWidth: '40%', height: `${Math.max(m.expenses / max * 100, m.expenses ? 3 : 0)}%`, bgcolor: '#e0b387', borderRadius: '5px 5px 0 0', transition: 'height .3s' }} /></Box><Typography sx={{ textAlign: 'center', color: 'text.secondary', fontSize: 11, textTransform: 'capitalize', mt: .7 }}>{m.label}</Typography></Box>)}</Box>
    </Box>
  </Box>;
}
