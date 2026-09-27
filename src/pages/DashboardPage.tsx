import { Avatar, Box, Button, Grid, LinearProgress, Stack, Typography } from '@mui/material';
import { ArrowForward, CalendarMonthOutlined, GroupsOutlined, HowToRegOutlined, NorthEast, SavingsOutlined } from '@mui/icons-material';
import { Link } from 'react-router-dom';
import StatCard from '../components/cards/StatCard';
import SectionCard from '../components/cards/SectionCard';
import StatusChip from '../components/common/StatusChip';
import DataTable from '../components/common/DataTable';
import { useChurch } from '../context/ChurchContext';
import { getMetrics } from '../utils/metrics';
import { formatDate, initials, money } from '../utils/format';

const colors = ['#4c947a', '#91b8a6', '#d1a574', '#789cb1', '#ae9fbc', '#b5c9a7'];

export default function DashboardPage() {
  const { members, events, records } = useChurch();
  const { active, upcoming, income, expenses, balance, attendance } = getMetrics(members, events, records);
  const ministries = Array.from(new Set(members.map((m) => m.ministry))).map((name) => {
    const group = members.filter((m) => m.ministry === name && m.status === 'Activo');
    return { name, count: group.length, average: group.length ? Math.round(group.reduce((total, m) => total + m.attendance, 0) / group.length) : 0 };
  }).filter((m) => m.count).sort((a, b) => b.average - a.average).slice(0, 5);
  const recent = [...members].sort((a, b) => b.joinedAt.localeCompare(a.joinedAt)).slice(0, 4);
  const leaders = active.filter((m) => /líder|presbítero|coordinador/i.test(m.role)).slice(0, 3);
  const now = new Date();
  const greeting = now.getHours() < 12 ? 'Buenos días' : now.getHours() < 19 ? 'Buenas tardes' : 'Buenas noches';

  return <Box>
    <Box sx={{ mb: 3.2, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', gap: 2, flexWrap: 'wrap' }}>
      <Box><Typography sx={{ color: 'primary.main', fontSize: 11, fontWeight: 800, letterSpacing: '.13em', mb: .7 }}>VISTA GENERAL</Typography><Typography variant="h4" sx={{ fontSize: { xs: 27, md: 31 } }}>{greeting}, administrador <span style={{ color: '#d7aa6f' }}>✳</span></Typography><Typography color="text.secondary" sx={{ fontSize: 14, mt: .6 }}>Esto es lo que sucede hoy en tu comunidad.</Typography></Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary', fontSize: 12, border: '1px solid #e8eeeb', borderRadius: 2, px: 1.7, py: 1.1, bgcolor: '#fff' }}><CalendarMonthOutlined sx={{ fontSize: 17, color: 'primary.main' }} />{new Intl.DateTimeFormat('es-ES', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }).format(now)}</Box>
    </Box>

    <Box sx={{ position: 'relative', overflow: 'hidden', borderRadius: 3, bgcolor: '#214f46', color: '#fff', mb: 3, px: { xs: 3, md: 4 }, py: { xs: 3, md: 3.7 }, display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
      <Box sx={{ position: 'absolute', width: 330, height: 330, borderRadius: '50%', border: '1px solid rgba(255,255,255,.13)', right: -40, top: -120, '&:after': { content: '""', position: 'absolute', width: 250, height: 250, borderRadius: '50%', border: '1px solid rgba(255,255,255,.13)', top: 39, left: 39 }, '&:before': { content: '""', position: 'absolute', width: 155, height: 155, borderRadius: '50%', bgcolor: 'rgba(171,215,178,.08)', top: 86, left: 86 } }} />
      <Box sx={{ position: 'relative', zIndex: 1 }}><Typography sx={{ color: '#cce9b4', fontSize: 11, fontWeight: 800, letterSpacing: '.16em', mb: 1 }}>TU COMUNIDAD, EN UN SOLO LUGAR</Typography><Typography sx={{ fontFamily: 'Manrope', fontWeight: 800, fontSize: { xs: 19, md: 24 }, letterSpacing: '-.04em', maxWidth: 520, lineHeight: 1.35 }}>Cada persona cuenta. Cada encuentro importa.</Typography><Typography sx={{ color: '#c2d6ce', fontSize: 13, mt: 1, maxWidth: 510 }}>Organiza tu comunidad, acompaña a tus miembros y sigue el crecimiento de tu iglesia.</Typography></Box>
      <Button component={Link} to="/members" variant="contained" endIcon={<ArrowForward sx={{ fontSize: '16px !important' }} />} sx={{ display: { xs: 'none', sm: 'inline-flex' }, position: 'relative', zIndex: 1, bgcolor: '#deedca', color: '#214f46', flexShrink: 0, '&:hover': { bgcolor: '#cce6b6' } }}>Ver miembros</Button>
    </Box>

    <Grid container spacing={2.2} sx={{ mb: 2.6 }}>
      <Grid item xs={12} sm={6} xl={3}><StatCard label="Miembros activos" value={String(active.length)} detail={`${members.length} miembros registrados`} icon={<GroupsOutlined />} tone="green" /></Grid>
      <Grid item xs={12} sm={6} xl={3}><StatCard label="Asistencia promedio" value={`${attendance}%`} detail="De miembros activos" icon={<HowToRegOutlined />} tone="blue" /></Grid>
      <Grid item xs={12} sm={6} xl={3}><StatCard label="Próximos eventos" value={String(upcoming.length).padStart(2, '0')} detail="Actividades programadas" icon={<CalendarMonthOutlined />} tone="orange" /></Grid>
      <Grid item xs={12} sm={6} xl={3}><StatCard label="Ingresos del mes" value={money(income)} detail="Registros de este mes" icon={<SavingsOutlined />} tone="purple" /></Grid>
    </Grid>

    <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
      <Grid item xs={12} lg={7}><SectionCard title="Participación por ministerio" subtitle="Asistencia promedio de miembros activos" action={<Typography sx={{ fontSize: 11, color: 'text.secondary', bgcolor: '#f5f8f6', px: 1.3, py: .7, borderRadius: 1.5 }}>Datos actuales</Typography>}>
        <Box sx={{ display: 'grid', gap: 2.25, pt: .7 }}>
          {ministries.length ? ministries.map((ministry, i) => <Box key={ministry.name} sx={{ display: 'grid', gridTemplateColumns: { xs: '90px 1fr 38px', sm: '115px 1fr 42px' }, alignItems: 'center', gap: 1.7 }}><Typography sx={{ fontSize: 12.5, fontWeight: 600 }}>{ministry.name}</Typography><Box sx={{ height: 10, bgcolor: '#f1f4f1', borderRadius: 5, overflow: 'hidden' }}><Box sx={{ width: `${ministry.average}%`, height: '100%', bgcolor: colors[i], borderRadius: 5 }} /></Box><Typography sx={{ fontSize: 12, fontWeight: 800, textAlign: 'right' }}>{ministry.average}%</Typography></Box>) : <Typography color="text.secondary" fontSize={13}>Sin miembros activos todavía.</Typography>}
        </Box>
        <Box sx={{ mt: 3.7, pt: 2, borderTop: '1px solid #edf0ee', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}><Typography color="text.secondary" sx={{ fontSize: 12 }}>Basado en la asistencia registrada de cada miembro</Typography><Button component={Link} to="/reports" size="small" endIcon={<ArrowForward />} sx={{ fontSize: 12 }}>Ver reportes</Button></Box>
      </SectionCard></Grid>
      <Grid item xs={12} lg={5}><SectionCard title="Próximos eventos" subtitle="Lo que viene en tu calendario" action={<Button component={Link} to="/events" size="small" endIcon={<ArrowForward />} sx={{ fontSize: 12, minWidth: 0, p: .4 }}>Ver todos</Button>}>
        <Stack spacing={0}>{upcoming.length ? upcoming.slice(0, 3).map((event, i) => <Box key={event.id} sx={{ display: 'flex', gap: 1.7, py: 1.45, borderBottom: i < Math.min(upcoming.length, 3) - 1 ? '1px solid #edf0ee' : 0 }}>
          <Box sx={{ width: 49, height: 52, bgcolor: i === 0 ? '#e4f1e9' : '#f4f5f2', borderRadius: 2, flexShrink: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#2c6853' }}><Typography sx={{ fontSize: 18, fontWeight: 800, lineHeight: 1 }}>{formatDate(event.date, { day: '2-digit' })}</Typography><Typography sx={{ fontSize: 10, fontWeight: 800, textTransform: 'uppercase' }}>{formatDate(event.date, { month: 'short' })}</Typography></Box>
          <Box sx={{ minWidth: 0, flex: 1 }}><Typography sx={{ fontSize: 13.5, fontWeight: 750, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{event.title}</Typography><Typography color="text.secondary" sx={{ fontSize: 11.5, mt: .3 }}>{event.time} · {event.location}</Typography><Typography sx={{ color: 'primary.main', fontWeight: 700, fontSize: 11.5, mt: .5 }}>{event.registered} registrados</Typography></Box>
        </Box>) : <Typography color="text.secondary" fontSize={13}>No hay eventos próximos.</Typography>}</Stack>
      </SectionCard></Grid>
    </Grid>

    <Grid container spacing={2.5}>
      <Grid item xs={12} lg={8}><DataTable title="Miembros recientes" subtitle="Últimas personas registradas en la comunidad" action={<Button component={Link} to="/members" size="small" endIcon={<ArrowForward />} sx={{ fontSize: 12 }}>Ver todos</Button>} rows={recent} columns={[
        { key: 'name', label: 'Nombre', render: (m) => <Stack direction="row" alignItems="center" spacing={1.3}><Avatar sx={{ width: 33, height: 33, bgcolor: '#e6efe9', color: '#39745b', fontSize: 11, fontWeight: 800 }}>{initials(m.name)}</Avatar><Box><Typography sx={{ fontSize: 12.5, fontWeight: 700 }}>{m.name}</Typography><Typography color="text.secondary" sx={{ fontSize: 11 }}>{m.email}</Typography></Box></Stack> },
        { key: 'ministry', label: 'Ministerio' }, { key: 'status', label: 'Estado', render: (m) => <StatusChip label={m.status} /> }, { key: 'joinedAt', label: 'Registro', render: (m) => formatDate(m.joinedAt, { day: 'numeric', month: 'short' }) },
      ]} /></Grid>
      <Grid item xs={12} lg={4}><SectionCard title="Balance financiero" subtitle="Resumen del mes en curso" action={<NorthEast sx={{ color: '#8a9e95', fontSize: 19 }} />}>
        <Typography color="text.secondary" sx={{ fontSize: 12, mt: .6 }}>Balance disponible</Typography><Typography sx={{ fontFamily: 'Manrope', fontSize: 31, fontWeight: 800, letterSpacing: '-.05em', mt: .4, mb: 2.7 }}>{money(balance)}</Typography>
        <Stack spacing={1.8}><Box><Stack direction="row" justifyContent="space-between" mb={.8}><Typography sx={{ fontSize: 12.5 }}>Ingresos</Typography><Typography sx={{ fontSize: 12.5, fontWeight: 800, color: '#358063' }}>{money(income)}</Typography></Stack><LinearProgress variant="determinate" value={income + expenses ? income / (income + expenses) * 100 : 0} sx={{ height: 7, borderRadius: 5, bgcolor: '#eff4ef', '& .MuiLinearProgress-bar': { bgcolor: '#55a17b', borderRadius: 5 } }} /></Box><Box><Stack direction="row" justifyContent="space-between" mb={.8}><Typography sx={{ fontSize: 12.5 }}>Gastos</Typography><Typography sx={{ fontSize: 12.5, fontWeight: 800, color: '#c7825a' }}>{money(expenses)}</Typography></Stack><LinearProgress variant="determinate" value={income + expenses ? expenses / (income + expenses) * 100 : 0} sx={{ height: 7, borderRadius: 5, bgcolor: '#f7f1eb', '& .MuiLinearProgress-bar': { bgcolor: '#dfab7c', borderRadius: 5 } }} /></Box></Stack>
        <Button component={Link} to="/finance" variant="outlined" fullWidth endIcon={<ArrowForward />} sx={{ mt: 3.1, fontSize: 12 }}>Ir a finanzas</Button>
      </SectionCard></Grid>
    </Grid>
    <Box sx={{ mt: 2.5 }}><SectionCard title="Líderes activos" subtitle="Personas que acompañan a nuestra comunidad" action={<Button component={Link} to="/members" size="small" endIcon={<ArrowForward />} sx={{ fontSize: 12 }}>Ver directorio</Button>}>
      <Grid container spacing={2}>{leaders.length ? leaders.map((leader) => <Grid item xs={12} sm={6} lg={4} key={leader.id}><Box sx={{ display: 'flex', gap: 1.4, alignItems: 'center', bgcolor: '#f7faf7', border: '1px solid #edf2ed', p: 1.7, borderRadius: 2 }}><Avatar sx={{ width: 40, height: 40, bgcolor: '#dcece2', color: '#326e53', fontSize: 12, fontWeight: 800 }}>{initials(leader.name)}</Avatar><Box><Typography sx={{ fontSize: 12.5, fontWeight: 800 }}>{leader.name}</Typography><Typography color="text.secondary" sx={{ fontSize: 11.5 }}>{leader.role} · {leader.ministry}</Typography></Box></Box></Grid>) : <Grid item xs={12}><Typography color="text.secondary" fontSize={13}>No hay líderes activos registrados.</Typography></Grid>}</Grid>
    </SectionCard></Box>
  </Box>;
}
