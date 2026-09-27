import { useState, type FormEvent } from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, Pagination, Snackbar, Alert, Stack, TextField, Tooltip, Typography } from '@mui/material';
import { AccountBalanceWalletOutlined, Add, ArrowDownward, ArrowUpward, DeleteOutline, DownloadOutlined, EditOutlined } from '@mui/icons-material';
import PageHeader from '../components/common/PageHeader';
import MetricCard from '../components/common/MetricCard';
import SectionCard from '../components/cards/SectionCard';
import DataTable from '../components/common/DataTable';
import ConfirmDialog from '../components/common/ConfirmDialog';
import FinanceChart from '../components/common/FinanceChart';
import { useChurch } from '../context/ChurchContext';
import { todayISO } from '../data/mockData';
import { getMetrics } from '../utils/metrics';
import { downloadCSV, formatDate, money } from '../utils/format';
import type { FinanceRecord } from '../types';

const emptyRecord = (): FinanceRecord => ({ id: 0, concept: '', category: 'Diezmos', type: 'Ingreso', amount: 0, date: todayISO() });
const PAGE_SIZE = 8;

export default function FinancePage() {
  const { members, events, records, saveRecord, deleteRecord } = useChurch();
  const { income, expenses, balance } = getMetrics(members, events, records);
  const [filter, setFilter] = useState('Todos');
  const [page, setPage] = useState(1);
  const [draft, setDraft] = useState<FinanceRecord | null>(null);
  const [toDelete, setToDelete] = useState<FinanceRecord | null>(null);
  const [notice, setNotice] = useState('');
  const filtered = [...records].sort((a, b) => b.date.localeCompare(a.date)).filter((r) => filter === 'Todos' || r.type === filter);
  const pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const update = (patch: Partial<FinanceRecord>) => setDraft((current) => current ? { ...current, ...patch } : current);
  const save = (e: FormEvent) => { e.preventDefault(); if (!draft || draft.amount <= 0) return; const editing = Boolean(draft.id); saveRecord({ ...draft, concept: draft.concept.trim(), id: draft.id || undefined }); setDraft(null); setNotice(editing ? 'Movimiento actualizado correctamente' : 'Movimiento registrado correctamente'); };

  return <Box>
    <PageHeader eyebrow="ADMINISTRACIÓN RESPONSABLE" title="Finanzas" subtitle="Transparencia y claridad en cada recurso de tu iglesia." action={<Button variant="contained" startIcon={<Add />} onClick={() => setDraft(emptyRecord())}>Nuevo movimiento</Button>} />
    <Grid container spacing={2.2} sx={{ mb: 3 }}>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Ingresos del mes" value={money(income)} detail="Diezmos, ofrendas y donaciones" icon={<ArrowDownward />} /></Grid>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Gastos del mes" value={money(expenses)} detail="Egresos registrados" icon={<ArrowUpward />} color="#d29565" /></Grid>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Balance del mes" value={money(balance)} detail="Ingresos menos gastos" icon={<AccountBalanceWalletOutlined />} color="#5987a7" /></Grid>
    </Grid>
    <Grid container spacing={2.5} sx={{ mb: 3 }}>
      <Grid item xs={12} lg={8}><SectionCard title="Actividad financiera" subtitle="Comparativa de ingresos y gastos en los últimos 6 meses"><FinanceChart records={records} /></SectionCard></Grid>
      <Grid item xs={12} lg={4}><SectionCard title="Distribución de ingresos" subtitle="Por categoría, en el mes actual">
        {(() => { const month = todayISO().slice(0, 7); const byCategory = Object.entries(records.filter((r) => r.type === 'Ingreso' && r.date.startsWith(month)).reduce<Record<string, number>>((acc, r) => { acc[r.category] = (acc[r.category] || 0) + r.amount; return acc; }, {})).sort((a, b) => b[1] - a[1]); return byCategory.length ? <Stack spacing={2.1} sx={{ pt: .8 }}>{byCategory.map(([name, amount], i) => <Box key={name}><Stack direction="row" justifyContent="space-between" mb={.8}><Typography sx={{ fontSize: 12.5, fontWeight: 600 }}>{name}</Typography><Typography sx={{ fontSize: 12.5, fontWeight: 800 }}>{money(amount)}</Typography></Stack><Box sx={{ height: 8, borderRadius: 5, bgcolor: '#f1f5f2' }}><Box sx={{ width: `${income ? amount / income * 100 : 0}%`, height: '100%', borderRadius: 5, bgcolor: ['#4d9877', '#9ac3ad', '#d9b48a', '#9eb4c8'][i % 4] }} /></Box></Box>)}</Stack> : <Typography color="text.secondary" fontSize={13}>Aún no hay ingresos este mes.</Typography>; })()}
      </SectionCard></Grid>
    </Grid>
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 1.5, flexWrap: 'wrap', mb: 1.5 }}><TextField select size="small" label="Tipo" value={filter} onChange={(e) => { setFilter(e.target.value); setPage(1); }} sx={{ minWidth: 160 }}>{['Todos', 'Ingreso', 'Gasto'].map((v) => <MenuItem key={v} value={v}>{v}</MenuItem>)}</TextField><Button variant="outlined" color="inherit" startIcon={<DownloadOutlined />} onClick={() => { downloadCSV('finanzas.csv', [['Concepto', 'Categoría', 'Tipo', 'Monto (USD)', 'Fecha'], ...filtered.map((r) => [r.concept, r.category, r.type, r.amount, r.date])]); setNotice('Archivo CSV descargado'); }}>Exportar CSV</Button></Box>
    <DataTable title="Movimientos financieros" subtitle={`${filtered.length} registros encontrados`} rows={filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE)} emptyText="No hay movimientos con este filtro." columns={[
      { key: 'concept', label: 'Concepto', render: (r) => <Typography sx={{ fontSize: 12.5, fontWeight: 700 }}>{r.concept}</Typography> }, { key: 'category', label: 'Categoría' }, { key: 'date', label: 'Fecha', render: (r) => formatDate(r.date) }, { key: 'type', label: 'Tipo', render: (r) => <Box sx={{ display: 'inline-block', px: 1.2, py: .5, borderRadius: 1, bgcolor: r.type === 'Ingreso' ? '#e5f4ec' : '#fff0e6', color: r.type === 'Ingreso' ? '#277454' : '#b8784a', fontSize: 11, fontWeight: 800 }}>{r.type}</Box> }, { key: 'amount', label: 'Monto', align: 'right', render: (r) => <Typography sx={{ color: r.type === 'Ingreso' ? '#318265' : '#bb7954', fontWeight: 800, fontSize: 13 }}>{r.type === 'Ingreso' ? '+' : '−'} {money(r.amount)}</Typography> },
      { key: 'actions', label: 'Acciones', align: 'right', render: (r) => <Stack direction="row" justifyContent="flex-end"><Tooltip title="Editar movimiento"><IconButton size="small" aria-label={`Editar ${r.concept}`} onClick={() => setDraft({ ...r })}><EditOutlined fontSize="small" /></IconButton></Tooltip><Tooltip title="Eliminar movimiento"><IconButton size="small" color="error" aria-label={`Eliminar ${r.concept}`} onClick={() => setToDelete(r)}><DeleteOutline fontSize="small" /></IconButton></Tooltip></Stack> },
    ]} />
    {pages > 1 && <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}><Pagination count={pages} page={Math.min(page, pages)} onChange={(_, value) => setPage(value)} color="primary" /></Box>}
    <Dialog open={Boolean(draft)} onClose={() => setDraft(null)} fullWidth maxWidth="sm"><Box component="form" onSubmit={save}><DialogTitle sx={{ fontWeight: 800, pt: 3 }}>{draft?.id ? 'Editar movimiento' : 'Nuevo movimiento'}</DialogTitle><DialogContent sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, pt: '12px !important' }}>
      <TextField label="Concepto" required value={draft?.concept ?? ''} onChange={(e) => update({ concept: e.target.value })} sx={{ gridColumn: { sm: 'span 2' } }} inputProps={{ maxLength: 100 }} />
      <TextField select label="Tipo" value={draft?.type ?? 'Ingreso'} onChange={(e) => update({ type: e.target.value as FinanceRecord['type'], category: e.target.value === 'Gasto' ? 'Mantenimiento' : 'Diezmos' })}><MenuItem value="Ingreso">Ingreso</MenuItem><MenuItem value="Gasto">Gasto</MenuItem></TextField>
      <TextField select label="Categoría" value={draft?.category ?? 'Diezmos'} onChange={(e) => update({ category: e.target.value })}>{(draft?.type === 'Gasto' ? ['Mantenimiento', 'Ministerios', 'Servicios', 'Otros gastos'] : ['Diezmos', 'Ofrendas', 'Donaciones', 'Eventos', 'Otros ingresos']).concat(draft?.category && !['Mantenimiento', 'Ministerios', 'Servicios', 'Otros gastos', 'Diezmos', 'Ofrendas', 'Donaciones', 'Eventos', 'Otros ingresos'].includes(draft.category) ? [draft.category] : []).map((v) => <MenuItem key={v} value={v}>{v}</MenuItem>)}</TextField>
      <TextField label="Monto (USD)" type="number" required value={draft?.amount ?? 0} onChange={(e) => update({ amount: Number(e.target.value) })} inputProps={{ min: .01, step: .01 }} /><TextField label="Fecha" type="date" required value={draft?.date ?? ''} onChange={(e) => update({ date: e.target.value })} InputLabelProps={{ shrink: true }} />
    </DialogContent><DialogActions sx={{ p: 3, pt: 1 }}><Button color="inherit" onClick={() => setDraft(null)}>Cancelar</Button><Button type="submit" variant="contained">{draft?.id ? 'Guardar cambios' : 'Registrar movimiento'}</Button></DialogActions></Box></Dialog>
    <ConfirmDialog open={Boolean(toDelete)} title="Eliminar movimiento" message={`¿Seguro que deseas eliminar “${toDelete?.concept ?? 'este movimiento'}”? Esta acción no se puede deshacer.`} onClose={() => setToDelete(null)} onConfirm={() => { if (toDelete) deleteRecord(toDelete.id); setToDelete(null); setNotice('Movimiento eliminado'); }} />
    <Snackbar open={Boolean(notice)} autoHideDuration={3500} onClose={() => setNotice('')} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}><Alert onClose={() => setNotice('')} severity="success" variant="filled">{notice}</Alert></Snackbar>
  </Box>;
}
