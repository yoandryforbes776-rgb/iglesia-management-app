import { Box, Button, Grid } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import SectionCard from '../components/cards/SectionCard';
import DataTable from '../components/common/DataTable';
import PageHeader from '../components/common/PageHeader';
import { financeRecords } from '../data/mockData';

export default function FinancePage() {
  return (
    <Box>
      <PageHeader
        title="Finanzas"
        subtitle="Seguimiento de ofrendas, diezmos y gastos"
        action={<Button variant="contained" startIcon={<AddIcon />}>Registrar ingreso</Button>}
      />

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <SectionCard title="Ingresos" subtitle="Este mes">
            <Box sx={{ fontSize: 32, fontWeight: 700 }}>$12.4K</Box>
          </SectionCard>
        </Grid>
        <Grid item xs={12} md={4}>
          <SectionCard title="Gastos" subtitle="Operativos">
            <Box sx={{ fontSize: 32, fontWeight: 700 }}>$4.9K</Box>
          </SectionCard>
        </Grid>
        <Grid item xs={12} md={4}>
          <SectionCard title="Balance" subtitle="Neto">
            <Box sx={{ fontSize: 32, fontWeight: 700 }}>$7.5K</Box>
          </SectionCard>
        </Grid>
      </Grid>

      <Box mt={3}>
        <DataTable
          title="Movimientos financieros"
          rows={financeRecords}
          columns={[
            { key: 'concept', label: 'Concepto' },
            { key: 'category', label: 'Categoría' },
            { key: 'amount', label: 'Monto', align: 'right', render: (row) => `$${row.amount.toLocaleString()}` },
            { key: 'date', label: 'Fecha' },
          ]}
        />
      </Box>
    </Box>
  );
}
