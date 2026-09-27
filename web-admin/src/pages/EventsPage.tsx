import { Box, Button, Grid } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import SectionCard from '../components/cards/SectionCard';
import DataTable from '../components/common/DataTable';
import StatusChip from '../components/common/StatusChip';
import PageHeader from '../components/common/PageHeader';
import { events } from '../data/mockData';

export default function EventsPage() {
  return (
    <Box>
      <PageHeader
        title="Eventos"
        subtitle="Calendario de reuniones, actividades y servicios"
        action={<Button variant="contained" startIcon={<AddIcon />}>Crear evento</Button>}
      />

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <SectionCard title="Eventos activos" subtitle="Este mes">
            <Box sx={{ fontSize: 32, fontWeight: 700 }}>08</Box>
          </SectionCard>
        </Grid>
        <Grid item xs={12} md={4}>
          <SectionCard title="Participantes" subtitle="Registrados">
            <Box sx={{ fontSize: 32, fontWeight: 700 }}>620</Box>
          </SectionCard>
        </Grid>
        <Grid item xs={12} md={4}>
          <SectionCard title="Capacidad" subtitle="Ocupación">
            <Box sx={{ fontSize: 32, fontWeight: 700 }}>74%</Box>
          </SectionCard>
        </Grid>
      </Grid>

      <Box mt={3}>
        <DataTable
          title="Calendario de eventos"
          rows={events}
          columns={[
            { key: 'title', label: 'Titulo' },
            { key: 'date', label: 'Fecha' },
            { key: 'location', label: 'Lugar' },
            { key: 'registered', label: 'Registrados', align: 'right' },
            { key: 'status', label: 'Estado', render: (row) => <StatusChip label={row.status} color={row.status === 'Confirmado' ? 'success' : row.status === 'Pendiente' ? 'warning' : 'primary'} /> },
          ]}
        />
      </Box>
    </Box>
  );
}
