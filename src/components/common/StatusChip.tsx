import { Box } from '@mui/material';

export default function StatusChip({ label }: { label: string }) {
  const tone = ['Activo', 'Confirmado', 'Ingreso'].includes(label) ? { bg: '#e5f4ec', color: '#277454', dot: '#3c9a71' }
    : ['Pendiente', 'En revisión'].includes(label) ? { bg: '#fff3de', color: '#a76e20', dot: '#dfa44d' }
    : { bg: '#f9eae7', color: '#ad665d', dot: '#d98979' };
  return <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: .8, px: 1.35, py: .65, borderRadius: '7px', bgcolor: tone.bg, color: tone.color, fontSize: 12, fontWeight: 700, whiteSpace: 'nowrap' }}>
    <Box component="span" sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: tone.dot }} />{label}
  </Box>;
}
