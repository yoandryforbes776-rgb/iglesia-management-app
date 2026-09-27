import { useEffect, useState, type FormEvent } from 'react';
import { Avatar, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, InputAdornment, MenuItem, Pagination, Snackbar, Alert, Stack, TextField, Tooltip, Typography } from '@mui/material';
import { Add, DownloadOutlined, EditOutlined, GroupsOutlined, HowToRegOutlined, PersonSearchOutlined, Search, DeleteOutline } from '@mui/icons-material';
import { useSearchParams } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import MetricCard from '../components/common/MetricCard';
import DataTable from '../components/common/DataTable';
import StatusChip from '../components/common/StatusChip';
import ConfirmDialog from '../components/common/ConfirmDialog';
import { useChurch } from '../context/ChurchContext';
import { todayISO } from '../data/mockData';
import { downloadCSV, formatDate, initials } from '../utils/format';
import type { Member } from '../types';

const emptyMember = (): Member => ({ id: 0, name: '', email: '', phone: '', role: 'Miembro', ministry: 'Servicio', status: 'Activo', attendance: 0, joinedAt: todayISO() });
const ministries = ['Pastoral', 'Jóvenes', 'Mujeres', 'Alabanza', 'Niños', 'Servicio', 'Misiones', 'Otro'];
const PAGE_SIZE = 7;

export default function MembersPage() {
  const { members, saveMember, deleteMember } = useChurch();
  const [params, setParams] = useSearchParams();
  const [query, setQuery] = useState(params.get('q') ?? '');
  const [filter, setFilter] = useState(params.get('status') ?? 'Todos');
  const [page, setPage] = useState(1);
  const [draft, setDraft] = useState<Member | null>(null);
  const [toDelete, setToDelete] = useState<Member | null>(null);
  const [notice, setNotice] = useState('');
  useEffect(() => { setQuery(params.get('q') ?? ''); setFilter(params.get('status') ?? 'Todos'); setPage(1); }, [params]);
  const filtered = [...members].sort((a, b) => b.joinedAt.localeCompare(a.joinedAt)).filter((m) => (filter === 'Todos' || m.status === filter) && `${m.name} ${m.email} ${m.ministry} ${m.role}`.toLocaleLowerCase().includes(query.toLocaleLowerCase()));
  const pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const update = (patch: Partial<Member>) => setDraft((current) => current ? { ...current, ...patch } : current);
  const save = (e: FormEvent) => { e.preventDefault(); if (!draft) return; const editing = Boolean(draft.id); saveMember({ ...draft, name: draft.name.trim(), email: draft.email.trim(), id: draft.id || undefined }); setDraft(null); setNotice(editing ? 'Miembro actualizado correctamente' : 'Miembro agregado correctamente'); };
  const setStatus = (value: string) => { setFilter(value); setPage(1); const next = new URLSearchParams(params); if (query.trim()) next.set('q', query.trim()); else next.delete('q'); if (value === 'Todos') next.delete('status'); else next.set('status', value); setParams(next, { replace: true }); };

  return <Box>
    <PageHeader eyebrow="PERSONAS Y COMUNIDAD" title="Miembros" subtitle="Conoce, acompaña y organiza a las personas de tu congregación." action={<Button variant="contained" startIcon={<Add />} onClick={() => setDraft(emptyMember())}>Nuevo miembro</Button>} />
    <Grid container spacing={2.2} sx={{ mb: 3 }}>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Total de miembros" value={String(members.length)} detail="Personas registradas" icon={<GroupsOutlined />} /></Grid>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Miembros activos" value={String(members.filter((m) => m.status === 'Activo').length)} detail="Participan actualmente" icon={<HowToRegOutlined />} color="#5987a7" /></Grid>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Por revisar" value={String(members.filter((m) => m.status === 'En revisión').length)} detail="Necesitan seguimiento" icon={<PersonSearchOutlined />} color="#cd9559" /></Grid>
    </Grid>
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, mb: 2, flexWrap: 'wrap' }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.2} sx={{ width: { xs: '100%', sm: 'auto' } }}>
        <TextField size="small" placeholder="Buscar por nombre, correo..." aria-label="Buscar miembros" value={query} onChange={(e) => { setQuery(e.target.value); setPage(1); }} InputProps={{ startAdornment: <InputAdornment position="start"><Search sx={{ fontSize: 19, color: '#98a8a1' }} /></InputAdornment> }} sx={{ width: { xs: '100%', sm: 280 } }} />
        <TextField select size="small" label="Estado" value={filter} onChange={(e) => setStatus(e.target.value)} sx={{ width: { xs: '100%', sm: 160 } }}>{['Todos', 'Activo', 'En revisión', 'Inactivo'].map((value) => <MenuItem key={value} value={value}>{value}</MenuItem>)}</TextField>
      </Stack>
      <Button variant="outlined" color="inherit" startIcon={<DownloadOutlined />} onClick={() => { downloadCSV('miembros.csv', [['Nombre', 'Correo', 'Teléfono', 'Rol', 'Ministerio', 'Estado', 'Asistencia (%)', 'Fecha de registro'], ...filtered.map((m) => [m.name, m.email, m.phone, m.role, m.ministry, m.status, m.attendance, m.joinedAt])]); setNotice('Archivo CSV descargado'); }}>Exportar CSV</Button>
    </Box>
    <DataTable title="Directorio de miembros" subtitle={`${filtered.length} ${filtered.length === 1 ? 'persona encontrada' : 'personas encontradas'}`} rows={filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE)} emptyText="No se encontraron miembros con estos filtros." columns={[
      { key: 'name', label: 'Miembro', render: (m) => <Stack direction="row" alignItems="center" spacing={1.3}><Avatar sx={{ width: 36, height: 36, bgcolor: '#e5eee9', color: '#39745b', fontSize: 11, fontWeight: 800 }}>{initials(m.name)}</Avatar><Box><Typography sx={{ fontWeight: 700, fontSize: 12.5 }}>{m.name}</Typography><Typography color="text.secondary" sx={{ fontSize: 11 }}>{m.email || 'Sin correo'}</Typography></Box></Stack> },
      { key: 'role', label: 'Rol' }, { key: 'ministry', label: 'Ministerio' }, { key: 'status', label: 'Estado', render: (m) => <StatusChip label={m.status} /> }, { key: 'attendance', label: 'Asistencia', render: (m) => `${m.attendance}%` }, { key: 'joinedAt', label: 'Ingreso', render: (m) => formatDate(m.joinedAt, { day: 'numeric', month: 'short', year: 'numeric' }) },
      { key: 'actions', label: 'Acciones', align: 'right', render: (m) => <Stack direction="row" justifyContent="flex-end"><Tooltip title="Editar miembro"><IconButton size="small" aria-label={`Editar ${m.name}`} onClick={() => setDraft({ ...m })}><EditOutlined fontSize="small" /></IconButton></Tooltip><Tooltip title="Eliminar miembro"><IconButton size="small" color="error" aria-label={`Eliminar ${m.name}`} onClick={() => setToDelete(m)}><DeleteOutline fontSize="small" /></IconButton></Tooltip></Stack> },
    ]} />
    {pages > 1 && <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}><Pagination count={pages} page={Math.min(page, pages)} onChange={(_, value) => setPage(value)} color="primary" /></Box>}

    <Dialog open={Boolean(draft)} onClose={() => setDraft(null)} fullWidth maxWidth="sm"><Box component="form" onSubmit={save}><DialogTitle sx={{ fontWeight: 800, pt: 3 }}>{draft?.id ? 'Editar miembro' : 'Nuevo miembro'}</DialogTitle><DialogContent sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, pt: '12px !important' }}>
      <TextField label="Nombre completo" required fullWidth value={draft?.name ?? ''} onChange={(e) => update({ name: e.target.value })} sx={{ gridColumn: { sm: 'span 2' } }} inputProps={{ maxLength: 100 }} />
      <TextField label="Correo electrónico" type="email" fullWidth value={draft?.email ?? ''} onChange={(e) => update({ email: e.target.value })} />
      <TextField label="Teléfono" fullWidth value={draft?.phone ?? ''} onChange={(e) => update({ phone: e.target.value })} inputProps={{ maxLength: 30 }} />
      <TextField label="Rol" required fullWidth value={draft?.role ?? ''} onChange={(e) => update({ role: e.target.value })} inputProps={{ maxLength: 60 }} />
      <TextField select label="Ministerio" fullWidth value={draft?.ministry ?? 'Servicio'} onChange={(e) => update({ ministry: e.target.value })}>{ministries.map((item) => <MenuItem key={item} value={item}>{item}</MenuItem>)}</TextField>
      <TextField select label="Estado" fullWidth value={draft?.status ?? 'Activo'} onChange={(e) => update({ status: e.target.value as Member['status'] })}>{['Activo', 'En revisión', 'Inactivo'].map((item) => <MenuItem key={item} value={item}>{item}</MenuItem>)}</TextField>
      <TextField label="Asistencia (%)" type="number" required fullWidth value={draft?.attendance ?? 0} onChange={(e) => update({ attendance: Number(e.target.value) })} inputProps={{ min: 0, max: 100 }} />
      <TextField label="Fecha de ingreso" type="date" required fullWidth value={draft?.joinedAt ?? ''} onChange={(e) => update({ joinedAt: e.target.value })} InputLabelProps={{ shrink: true }} />
    </DialogContent><DialogActions sx={{ p: 3, pt: 1 }}><Button onClick={() => setDraft(null)} color="inherit">Cancelar</Button><Button variant="contained" type="submit">{draft?.id ? 'Guardar cambios' : 'Agregar miembro'}</Button></DialogActions></Box></Dialog>
    <ConfirmDialog open={Boolean(toDelete)} title="Eliminar miembro" message={`¿Seguro que deseas eliminar a ${toDelete?.name ?? 'este miembro'}? Esta acción no se puede deshacer.`} onClose={() => setToDelete(null)} onConfirm={() => { if (toDelete) deleteMember(toDelete.id); setToDelete(null); setNotice('Miembro eliminado'); }} />
    <Snackbar open={Boolean(notice)} autoHideDuration={3500} onClose={() => setNotice('')} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}><Alert onClose={() => setNotice('')} severity="success" variant="filled">{notice}</Alert></Snackbar>
  </Box>;
}
