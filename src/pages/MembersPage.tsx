import { Box, Button, Grid, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import SectionCard from '../components/cards/SectionCard';
import DataTable from '../components/common/DataTable';
import StatusChip from '../components/common/StatusChip';
import PageHeader from '../components/common/PageHeader';
import { members } from '../data/mockData';

export default function MembersPage() {
  return (
    <Box>
      <PageHeader
        title="Miembros"
        subtitle="Administración del listado de congregados y líderes"
        action={<Button variant="contained" startIcon={<AddIcon />}>Nuevo miembro</Button>}
      />

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <SectionCard title="Resumen" subtitle="Estadísticas rápidas">
            <Typography variant="h3" fontWeight={700}>1,248</Typography>
            <Typography color="text.secondary">miembros activos</Typography>
          </SectionCard>
        </Grid>

        <Grid item xs={12} md={4}>
          <SectionCard title="Nuevos" subtitle="Altas este mes">
            <Typography variant="h3" fontWeight={700}>34</Typography>
            <Typography color="text.secondary">nuevos registros</Typography>
          </SectionCard>
        </Grid>

        <Grid item xs={12} md={4}>
          <SectionCard title="Necesitan seguimiento" subtitle="Completado">
            <Typography variant="h3" fontWeight={700}>12</Typography>
            <Typography color="text.secondary">casos pendientes</Typography>
          </SectionCard>
        </Grid>
      </Grid>

      <Box mt={3}>
        <DataTable
          title="Listado de miembros"
          rows={members}
          columns={[
            { key: 'name', label: 'Nombre' },
            { key: 'role', label: 'Rol' },
            { key: 'ministry', label: 'Ministerio' },
            { key: 'status', label: 'Estado', render: (row) => <StatusChip label={row.status} color={row.status === 'Activo' ? 'success' : row.status === 'En revisión' ? 'warning' : 'error'} /> },
            { key: 'attendance', label: 'Asistencia', align: 'right' },
          ]}
        />
      </Box>
    </Box>
  );
}
