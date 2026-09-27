import { useState, type FormEvent } from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, Snackbar, Alert, Stack, TextField, Tooltip, Typography } from '@mui/material';
import { Add, CalendarMonthOutlined, ChevronLeft, ChevronRight, DeleteOutline, EditOutlined, GroupsOutlined, PlaceOutlined } from '@mui/icons-material';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/cards/SectionCard';
import MetricCard from '../components/common/MetricCard';
import DataTable from '../components/common/DataTable';
import StatusChip from '../components/common/StatusChip';
import ConfirmDialog from '../components/common/ConfirmDialog';
import { useChurch } from '../context/ChurchContext';
import { todayISO } from '../data/mockData';
import { formatDate } from '../utils/format';
import type { EventItem } from '../types';

const emptyEvent = (): EventItem => ({ id: 0, title: '', date: todayISO(), time: '10:00', location: '', category: 'Servicio', capacity: 100, registered: 0, status: 'Confirmado' });
const toISO = (year: number, month: number, day: number) => `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

export default function EventsPage() {
  const { events, saveEvent, deleteEvent } = useChurch();
  const [viewMonth, setViewMonth] = useState(() => { const d = new Date(); return new Date(d.getFullYear(), d.getMonth(), 1); });
  const [selectedDate, setSelectedDate] = useState('');
  const [filter, setFilter] = useState('Todos');
  const [draft, setDraft] = useState<EventItem | null>(null);
  const [toDelete, setToDelete] = useState<EventItem | null>(null);
  const [notice, setNotice] = useState('');
  const upcoming = events.filter((e) => e.date >= todayISO() && e.status !== 'Cerrado');
  const filtered = [...events].sort((a, b) => a.date.localeCompare(b.date)).filter((e) => (filter === 'Todos' || e.status === filter) && (!selectedDate || e.date === selectedDate));
  const year = viewMonth.getFullYear(); const month = viewMonth.getMonth();
  const firstDay = (new Date(year, month, 1).getDay() + 6) % 7;
  const days = new Date(year, month + 1, 0).getDate();
  const cells = Array.from({ length: Math.ceil((firstDay + days) / 7) * 7 }, (_, i) => i - firstDay + 1);
  const changeMonth = (offset: number) => { setViewMonth(new Date(year, month + offset, 1)); setSelectedDate(''); };
  const update = (patch: Partial<EventItem>) => setDraft((current) => current ? { ...current, ...patch } : current);
  const save = (e: FormEvent) => { e.preventDefault(); if (!draft) return; if (draft.registered > draft.capacity) return; const editing = Boolean(draft.id); saveEvent({ ...draft, title: draft.title.trim(), location: draft.location.trim(), id: draft.id || undefined }); setDraft(null); setNotice(editing ? 'Evento actualizado correctamente' : 'Evento creado correctamente'); };

  return <Box>
    <PageHeader eyebrow="AGENDA DE LA IGLESIA" title="Eventos" subtitle="Planifica encuentros y mantén a tu comunidad conectada." action={<Button variant="contained" startIcon={<Add />} onClick={() => setDraft(emptyEvent())}>Crear evento</Button>} />
    <Grid container spacing={2.2} sx={{ mb: 3 }}>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Próximos eventos" value={String(upcoming.length)} detail="En agenda" icon={<CalendarMonthOutlined />} /></Grid>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Participantes" value={String(upcoming.reduce((s, e) => s + e.registered, 0))} detail="Registrados en próximos eventos" icon={<GroupsOutlined />} color="#5987a7" /></Grid>
      <Grid item xs={12} sm={6} lg={4}><MetricCard label="Espacios disponibles" value={String(upcoming.reduce((s, e) => s + Math.max(0, e.capacity - e.registered), 0))} detail="Cupos en eventos próximos" icon={<PlaceOutlined />} color="#cd9559" /></Grid>
    </Grid>
    <Grid container spacing={2.5} sx={{ mb: 3 }}>
      <Grid item xs={12} lg={8}><SectionCard title="Calendario de actividades" subtitle="Selecciona un día para filtrar los eventos">
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}><Typography sx={{ fontWeight: 800, fontSize: 15, textTransform: 'capitalize' }}>{new Intl.DateTimeFormat('es-ES', { month: 'long', year: 'numeric' }).format(viewMonth)}</Typography><Stack direction="row" alignItems="center" spacing={.5}><Button size="small" onClick={() => { const d = new Date(); setViewMonth(new Date(d.getFullYear(), d.getMonth(), 1)); setSelectedDate(''); }} sx={{ fontSize: 11 }}>Hoy</Button><IconButton size="small" aria-label="Mes anterior" onClick={() => changeMonth(-1)}><ChevronLeft /></IconButton><IconButton size="small" aria-label="Mes siguiente" onClick={() => changeMonth(1)}><ChevronRight /></IconButton></Stack></Stack>
        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', borderTop: '1px solid #edf0ee', borderLeft: '1px solid #edf0ee' }}>{['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'].map((day) => <Box key={day} sx={{ textAlign: 'center', py: 1.2, fontSize: 11, fontWeight: 800, color: 'text.secondary', borderRight: '1px solid #edf0ee', borderBottom: '1px solid #edf0ee', bgcolor: '#f9fbf9' }}>{day}</Box>)}
          {cells.map((day, i) => { const valid = day > 0 && day <= days; const date = valid ? toISO(year, month, day) : ''; const dayEvents = events.filter((e) => e.date === date); return <Box component="button" type="button" key={i} disabled={!valid} onClick={() => setSelectedDate(selectedDate === date ? '' : date)} aria-label={valid ? `${day} de ${new Intl.DateTimeFormat('es-ES', { month: 'long' }).format(viewMonth)}, ${dayEvents.length} eventos` : undefined} sx={{ border: 0, borderRight: '1px solid #edf0ee', borderBottom: '1px solid #edf0ee', bgcolor: selectedDate === date && valid ? '#e5f2e9' : '#fff', minHeight: { xs: 48, sm: 68 }, textAlign: 'left', p: { xs: .5, sm: 1 }, cursor: valid ? 'pointer' : 'default', '&:hover': valid ? { bgcolor: '#f0f7f1' } : {}, color: date === todayISO() ? '#25805a' : '#253c35' }}><Box sx={{ width: 23, height: 23, borderRadius: '50%', bgcolor: date === todayISO() ? '#285e57' : 'transparent', color: date === todayISO() ? '#fff' : 'inherit', display: 'grid', placeItems: 'center', fontSize: 11, fontWeight: dayEvents.length ? 800 : 500 }}>{valid ? day : ''}</Box>{dayEvents.length > 0 && <Box sx={{ display: 'flex', gap: .35, mt: .8, ml: .4 }}>{dayEvents.slice(0, 3).map((event) => <Box key={event.id} sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: event.status === 'Confirmado' ? '#4c9b75' : '#e5aa67' }} />)}</Box>}</Box>; })}</Box>
        {selectedDate && <Button size="small" onClick={() => setSelectedDate('')} sx={{ mt: 1.5, fontSize: 12 }}>Quitar filtro: {formatDate(selectedDate)}</Button>}
      </SectionCard></Grid>
      <Grid item xs={12} lg={4}><SectionCard title="En el horizonte" subtitle="Los siguientes encuentros de la comunidad">
        <Stack spacing={0}>{upcoming.sort((a, b) => a.date.localeCompare(b.date)).slice(0, 4).map((event, i) => <Box key={event.id} sx={{ display: 'flex', gap: 1.4, py: 1.5, borderBottom: i < Math.min(upcoming.length, 4) - 1 ? '1px solid #edf0ee' : 0 }}><Box sx={{ width: 43, height: 46, bgcolor: '#e8f2ec', borderRadius: 1.6, color: '#38765c', display: 'grid', placeItems: 'center', flexShrink: 0, fontWeight: 800, fontSize: 16 }}>{formatDate(event.date, { day: '2-digit' })}</Box><Box><Typography sx={{ fontSize: 12.5, fontWeight: 800 }}>{event.title}</Typography><Typography color="text.secondary" sx={{ fontSize: 11.5, mt: .3 }}>{formatDate(event.date, { month: 'short' })} · {event.time} · {event.location}</Typography></Box></Box>)}{!upcoming.length && <Typography color="text.secondary" fontSize={13}>No hay eventos próximos.</Typography>}</Stack>
      </SectionCard></Grid>
    </Grid>
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 1.5 }}><TextField select size="small" label="Estado" value={filter} onChange={(e) => setFilter(e.target.value)} sx={{ minWidth: 160 }}>{['Todos', 'Confirmado', 'Pendiente', 'Cerrado'].map((v) => <MenuItem key={v} value={v}>{v}</MenuItem>)}</TextField></Box>
    <DataTable title="Todos los eventos" subtitle={`${filtered.length} actividades encontradas`} rows={filtered} emptyText="No hay eventos con estos filtros." columns={[
      { key: 'title', label: 'Evento', render: (e) => <Box><Typography sx={{ fontSize: 12.5, fontWeight: 800 }}>{e.title}</Typography><Typography color="text.secondary" sx={{ fontSize: 11 }}>{e.category}</Typography></Box> },
      { key: 'date', label: 'Fecha y hora', render: (e) => `${formatDate(e.date)} · ${e.time}` }, { key: 'location', label: 'Lugar' }, { key: 'registered', label: 'Cupos', render: (e) => `${e.registered} / ${e.capacity}` }, { key: 'status', label: 'Estado', render: (e) => <StatusChip label={e.status} /> },
      { key: 'actions', label: 'Acciones', align: 'right', render: (e) => <Stack direction="row" justifyContent="flex-end"><Tooltip title="Editar evento"><IconButton size="small" aria-label={`Editar ${e.title}`} onClick={() => setDraft({ ...e })}><EditOutlined fontSize="small" /></IconButton></Tooltip><Tooltip title="Eliminar evento"><IconButton size="small" color="error" aria-label={`Eliminar ${e.title}`} onClick={() => setToDelete(e)}><DeleteOutline fontSize="small" /></IconButton></Tooltip></Stack> },
    ]} />
    <Dialog open={Boolean(draft)} onClose={() => setDraft(null)} fullWidth maxWidth="sm"><Box component="form" onSubmit={save}><DialogTitle sx={{ fontWeight: 800, pt: 3 }}>{draft?.id ? 'Editar evento' : 'Crear evento'}</DialogTitle><DialogContent sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, pt: '12px !important' }}>
      <TextField label="Nombre del evento" required value={draft?.title ?? ''} onChange={(e) => update({ title: e.target.value })} sx={{ gridColumn: { sm: 'span 2' } }} inputProps={{ maxLength: 100 }} />
      <TextField label="Fecha" type="date" required value={draft?.date ?? ''} onChange={(e) => update({ date: e.target.value })} InputLabelProps={{ shrink: true }} /><TextField label="Hora" type="time" required value={draft?.time ?? ''} onChange={(e) => update({ time: e.target.value })} InputLabelProps={{ shrink: true }} />
      <TextField label="Lugar" required value={draft?.location ?? ''} onChange={(e) => update({ location: e.target.value })} sx={{ gridColumn: { sm: 'span 2' } }} inputProps={{ maxLength: 100 }} />
      <TextField select label="Categoría" value={draft?.category ?? 'Servicio'} onChange={(e) => update({ category: e.target.value })}>{['Servicio', 'Oración', 'Jóvenes', 'Comunidad', 'Formación', 'Otro'].map((v) => <MenuItem key={v} value={v}>{v}</MenuItem>)}</TextField>
      <TextField select label="Estado" value={draft?.status ?? 'Confirmado'} onChange={(e) => update({ status: e.target.value as EventItem['status'] })}>{['Confirmado', 'Pendiente', 'Cerrado'].map((v) => <MenuItem key={v} value={v}>{v}</MenuItem>)}</TextField>
      <TextField label="Capacidad" type="number" required value={draft?.capacity ?? 0} onChange={(e) => update({ capacity: Number(e.target.value) })} inputProps={{ min: 1 }} /><TextField label="Registrados" type="number" required value={draft?.registered ?? 0} onChange={(e) => update({ registered: Number(e.target.value) })} error={Boolean(draft && draft.registered > draft.capacity)} helperText={draft && draft.registered > draft.capacity ? 'No puede superar la capacidad' : ''} inputProps={{ min: 0, max: draft?.capacity }} />
    </DialogContent><DialogActions sx={{ p: 3, pt: 1 }}><Button color="inherit" onClick={() => setDraft(null)}>Cancelar</Button><Button variant="contained" type="submit" disabled={Boolean(draft && draft.registered > draft.capacity)}>{draft?.id ? 'Guardar cambios' : 'Crear evento'}</Button></DialogActions></Box></Dialog>
    <ConfirmDialog open={Boolean(toDelete)} title="Eliminar evento" message={`¿Seguro que deseas eliminar “${toDelete?.title ?? 'este evento'}”? Esta acción no se puede deshacer.`} onClose={() => setToDelete(null)} onConfirm={() => { if (toDelete) deleteEvent(toDelete.id); setToDelete(null); setNotice('Evento eliminado'); }} />
    <Snackbar open={Boolean(notice)} autoHideDuration={3500} onClose={() => setNotice('')} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}><Alert onClose={() => setNotice('')} severity="success" variant="filled">{notice}</Alert></Snackbar>
  </Box>;
}
