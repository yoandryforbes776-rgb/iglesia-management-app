import { Box, Grid, Typography } from '@mui/material';
import SectionCard from '../components/cards/SectionCard';
import PageHeader from '../components/common/PageHeader';

export default function ReportsPage() {
  return (
    <Box>
      <PageHeader title="Reportes" subtitle="Resumen ejecutivo y seguimiento de indicadores" />

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <SectionCard title="Asistencia semanal" subtitle="Comparativa">
            <Typography variant="h4" fontWeight={700}>86%</Typography>
            <Typography color="text.secondary">subió 4% respecto a la semana anterior</Typography>
          </SectionCard>
        </Grid>

        <Grid item xs={12} md={6}>
          <SectionCard title="Participación en ministerios" subtitle="Promedio actual">
            <Typography variant="h4" fontWeight={700}>72%</Typography>
            <Typography color="text.secondary">porcentaje de líderes activos</Typography>
          </SectionCard>
        </Grid>
      </Grid>
    </Box>
  );
}
