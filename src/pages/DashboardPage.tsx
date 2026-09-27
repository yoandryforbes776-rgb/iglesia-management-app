import { Box, Grid, Typography } from '@mui/material';
import StatCard from '../components/cards/StatCard';
import SectionCard from '../components/cards/SectionCard';
import StatusChip from '../components/common/StatusChip';
import DataTable from '../components/common/DataTable';
import { stats, members, events } from '../data/mockData';

export default function DashboardPage() {
  const memberRows = members.slice(0, 3);

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} mb={3}>
        Dashboard
      </Typography>

      <Grid container spacing={3} mb={3}>
        {stats.map((item) => (
          <Grid item xs={12} sm={6} md={3} key={item.label}>
            <StatCard label={item.label} value={item.value} delta={item.delta} tone={item.trend as any} />
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} lg={7}>
          <SectionCard title="Próximos eventos" subtitle="Cronograma semestral">
            <Box sx={{ display: 'grid', gap: 2 }}>
              {events.map((event) => (
                <Box key={event.id} sx={{ p: 2, borderRadius: 2, bgcolor: 'rgba(103,80,164,0.04)' }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="h6" fontWeight={700}>{event.title}</Typography>
                    <StatusChip label={event.status} color={event.status === 'Confirmado' ? 'success' : event.status === 'Pendiente' ? 'warning' : 'primary'} />
                  </Box>
                  <Typography variant="body2" color="text.secondary" mt={1}>
                    {event.date} • {event.location}
                  </Typography>
                  <Typography variant="body2" mt={1}>
                    {event.registered}/{event.capacity} asistentes registrados
                  </Typography>
                </Box>
              ))}
            </Box>
          </SectionCard>
        </Grid>

        <Grid item xs={12} lg={5}>
          <SectionCard title="Miembros destacados" subtitle="Líderes y equipos activos">
            <Box sx={{ display: 'grid', gap: 2 }}>
              {memberRows.map((member) => (
                <Box key={member.id} sx={{ p: 2, borderRadius: 2, bgcolor: 'rgba(0,104,116,0.04)' }}>
                  <Typography variant="subtitle1" fontWeight={700}>{member.name}</Typography>
                  <Typography variant="body2" color="text.secondary">{member.role}</Typography>
                  <Typography variant="caption" color="text.secondary">{member.ministry}</Typography>
                </Box>
              ))}
            </Box>
          </SectionCard>
        </Grid>
      </Grid>

      <Box mt={3}>
        <DataTable
          title="Miembros recientes"
          rows={members}
          columns={[
            { key: 'name', label: 'Nombre' },
            { key: 'ministry', label: 'Ministerio' },
            { key: 'status', label: 'Estado', render: (row) => <StatusChip label={row.status} color={row.status === 'Activo' ? 'success' : row.status === 'En revisión' ? 'warning' : 'error'} /> },
            { key: 'attendance', label: 'Asistencia', align: 'right' },
          ]}
        />
      </Box>
    </Box>
  );
}
