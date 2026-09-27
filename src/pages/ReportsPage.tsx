import { Box, Button, Grid, Stack, Typography } from '@mui/material';
import { CalendarMonthOutlined, DownloadOutlined, GroupsOutlined, HowToRegOutlined, SavingsOutlined } from '@mui/icons-material';
import PageHeader from '../components/common/PageHeader';
import MetricCard from '../components/common/MetricCard';
import SectionCard from '../components/cards/SectionCard';
import FinanceChart from '../components/common/FinanceChart';
import { useChurch } from '../context/ChurchContext';
import { getMetrics } from '../utils/metrics';
import { downloadCSV, money } from '../utils/format';

export default function ReportsPage() {
  const { members, events, records } = useChurch();
  const { active, pending, upcoming, income, expenses, attendance } = getMetrics(members, events, records);
  const ministries = Array.from(new Set(members.map((m) => m.ministry))).map((name) => ({ name, count: members.filter((m) => m.ministry === name && m.status === 'Activo').length })).filter((m) => m.count).sort((a, b) => b.count - a.count);
  const maxCount = Math.max(1, ...ministries.map((m) => m.count));
  const statuses = [{ label: 'Activos', value: active.length, color: '#4d9877' }, { label: 'En revisión', value: pending.length, color: '#e0b387' }, { label: 'Inactivos', value: members.filter((m) => m.status === 'Inactivo').length, color: '#d48d84' }];
  const download = () => downloadCSV('reporte-general.csv', [
    ['Indicador', 'Valor'], ['Total de miembros', members.length], ['Miembros activos', active.length], ['En revisión', pending.length], ['Asistencia promedio (%)', attendance], ['Próximos eventos', upcoming.length], ['Ingresos del mes (USD)', income], ['Gastos del mes (USD)', expenses], ['Balance del mes (USD)', income - expenses], [], ['Ministerio', 'Miembros activos'], ...ministries.map((m) => [m.name, m.count]), [], ['Evento', 'Fecha', 'Registrados', 'Capacidad'], ...upcoming.map((e) => [e.title, e.date, e.registered, e.capacity])
  ]);

  return <Box>
    <PageHeader eyebrow="ANÁLISIS Y CRECIMIENTO" title="Reportes" subtitle="Una mirada clara al estado de tu comunidad y sus recursos." action={<Button variant="outlined" startIcon={<DownloadOutlined />} onClick={download}>Descargar reporte CSV</Button>} />
    <Grid container spacing={2.2} sx={{ mb: 3 }}>
      <Grid item xs={12} sm={6} xl={3}><MetricCard label="Miembros" value={String(members.length)} detail="Total registrados" icon={<GroupsOutlined />} /></Grid>
      <Grid item xs={12} sm={6} xl={3}><MetricCard label="Asistencia promedio" value={`${attendance}%`} detail="Entre miembros activos" icon={<HowToRegOutlined />} color="#5987a7" /></Grid>
      <Grid item xs={12} sm={6} xl={3}><MetricCard label="Próximos eventos" value={String(upcoming.length)} detail="Programados" icon={<CalendarMonthOutlined />} color="#cd9559" /></Grid>
      <Grid item xs={12} sm={6} xl={3}><MetricCard label="Balance mensual" value={money(income - expenses)} detail="Ingresos menos gastos" icon={<SavingsOutlined />} color="#8d7baa" /></Grid>
    </Grid>
    <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
      <Grid item xs={12} lg={7}><SectionCard title="Evolución financiera" subtitle="Movimientos de los últimos seis meses"><FinanceChart records={records} /></SectionCard></Grid>
      <Grid item xs={12} lg={5}><SectionCard title="Estado de la comunidad" subtitle="Distribución actual de los miembros">
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, py: 1.5, flexWrap: 'wrap' }}><Box role="img" aria-label={`Miembros: ${statuses.map((s) => `${s.label} ${s.value}`).join(', ')}`} sx={{ width: 145, height: 145, flexShrink: 0, borderRadius: '50%', background: members.length ? `conic-gradient(${statuses[0].color} 0 ${active.length / members.length * 100}%, ${statuses[1].color} ${active.length / members.length * 100}% ${(active.length + pending.length) / members.length * 100}%, ${statuses[2].color} ${(active.length + pending.length) / members.length * 100}% 100%)` : '#edf0ee', display: 'grid', placeItems: 'center' }}><Box sx={{ width: 100, height: 100, borderRadius: '50%', bgcolor: '#fff', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}><Typography sx={{ fontFamily: 'Manrope', fontSize: 25, fontWeight: 800, lineHeight: 1 }}>{members.length}</Typography><Typography color="text.secondary" sx={{ fontSize: 10, mt: .5 }}>miembros</Typography></Box></Box><Stack spacing={1.8} sx={{ flex: 1, minWidth: 130 }}>{statuses.map((s) => <Stack key={s.label} direction="row" alignItems="center" justifyContent="space-between" spacing={1}><Stack direction="row" alignItems="center" spacing={1}><Box sx={{ width: 9, height: 9, bgcolor: s.color, borderRadius: '50%' }} /><Typography sx={{ fontSize: 12, color: 'text.secondary' }}>{s.label}</Typography></Stack><Typography sx={{ fontSize: 12, fontWeight: 800 }}>{s.value}</Typography></Stack>)}</Stack></Box>
      </SectionCard></Grid>
    </Grid>
    <Grid container spacing={2.5}>
      <Grid item xs={12} lg={7}><SectionCard title="Participación en ministerios" subtitle="Miembros activos por equipo"><Stack spacing={2.1} sx={{ mt: 1 }}>{ministries.length ? ministries.map((m, i) => <Box key={m.name}><Stack direction="row" justifyContent="space-between" mb={.7}><Typography sx={{ fontSize: 12.5, fontWeight: 600 }}>{m.name}</Typography><Typography sx={{ fontSize: 12.5, fontWeight: 800 }}>{m.count} {m.count === 1 ? 'miembro' : 'miembros'}</Typography></Stack><Box sx={{ height: 9, bgcolor: '#f2f5f2', borderRadius: 5 }}><Box sx={{ width: `${m.count / maxCount * 100}%`, height: '100%', bgcolor: ['#4d9877', '#8eb6a4', '#d6b186', '#8cabc0', '#a99dbd'][i % 5], borderRadius: 5 }} /></Box></Box>) : <Typography color="text.secondary" fontSize={13}>No hay miembros activos.</Typography>}</Stack></SectionCard></Grid>
      <Grid item xs={12} lg={5}><SectionCard title="Próximas actividades" subtitle="Ocupación de eventos programados"><Stack spacing={2.1} sx={{ mt: 1 }}>{upcoming.slice(0, 4).map((e) => <Box key={e.id}><Stack direction="row" justifyContent="space-between" mb={.7} gap={1}><Typography sx={{ fontSize: 12.5, fontWeight: 600 }}>{e.title}</Typography><Typography sx={{ fontSize: 12, fontWeight: 800, whiteSpace: 'nowrap' }}>{e.registered}/{e.capacity}</Typography></Stack><Box sx={{ height: 9, bgcolor: '#f2f5f2', borderRadius: 5 }}><Box sx={{ width: `${Math.min(e.registered / (e.capacity || 1) * 100, 100)}%`, height: '100%', bgcolor: '#68a987', borderRadius: 5 }} /></Box></Box>)}{!upcoming.length && <Typography color="text.secondary" fontSize={13}>No hay eventos próximos.</Typography>}</Stack></SectionCard></Grid>
    </Grid>
    <Typography color="text.secondary" sx={{ fontSize: 11, mt: 2.5 }}>Los indicadores se calculan a partir de los registros disponibles en esta vista local de demostración.</Typography>
  </Box>;
}
