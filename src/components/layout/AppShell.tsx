import { useState, type FormEvent } from 'react';
import { AppBar, Avatar, Badge, Box, Button, Divider, Drawer, IconButton, InputBase, List, ListItemButton, ListItemIcon, ListItemText, Menu, MenuItem, Toolbar, Tooltip, Typography, useMediaQuery, useTheme } from '@mui/material';
import { AccountBalanceWalletOutlined, CalendarMonthOutlined, ChevronRight, ChurchOutlined, DashboardOutlined, GroupsOutlined, HelpOutline, Menu as MenuIcon, NotificationsNoneOutlined, Search, SummarizeOutlined } from '@mui/icons-material';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useChurch } from '../../context/ChurchContext';
import { todayISO } from '../../data/mockData';
import { formatDate } from '../../utils/format';

const drawerWidth = 252;
const navItems = [
  { label: 'Resumen', to: '/', icon: <DashboardOutlined /> },
  { label: 'Miembros', to: '/members', icon: <GroupsOutlined /> },
  { label: 'Eventos', to: '/events', icon: <CalendarMonthOutlined /> },
  { label: 'Finanzas', to: '/finance', icon: <AccountBalanceWalletOutlined /> },
  { label: 'Reportes', to: '/reports', icon: <SummarizeOutlined /> },
];

export default function AppShell() {
  const theme = useTheme();
  const mobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [notificationAnchor, setNotificationAnchor] = useState<HTMLElement | null>(null);
  const [search, setSearch] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const { members, events } = useChurch();
  const pending = members.filter((m) => m.status === 'En revisión');
  const upcoming = events.filter((e) => e.date >= todayISO() && e.status !== 'Cerrado').sort((a, b) => a.date.localeCompare(b.date));
  const pageName = navItems.find((item) => item.to === location.pathname)?.label ?? 'Resumen';

  const sidebar = <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: '#16322f', color: '#fff' }}>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, px: 3.2, pt: 3.3, pb: 4.3 }}>
      <Box sx={{ width: 37, height: 37, borderRadius: '11px', bgcolor: '#d7ebd4', color: '#205649', display: 'grid', placeItems: 'center' }}><ChurchOutlined sx={{ fontSize: 23 }} /></Box>
      <Box><Typography sx={{ fontFamily: 'Manrope', fontSize: 18, fontWeight: 800, lineHeight: 1.15, letterSpacing: '-.04em' }}>IglesiaFlow<span style={{ color: '#b6d99b' }}>.</span></Typography><Typography sx={{ fontSize: 10, color: '#a6bdb6', letterSpacing: '.06em', mt: .2 }}>GESTIÓN DE COMUNIDAD</Typography></Box>
    </Box>
    <Typography sx={{ pl: 3.5, pb: 1.5, fontSize: 10, fontWeight: 800, color: '#8daaa1', letterSpacing: '.16em' }}>MENÚ PRINCIPAL</Typography>
    <List sx={{ px: 1.5, py: 0 }}>
      {navItems.map((item) => <ListItemButton key={item.to} component={NavLink} to={item.to} end={item.to === '/'} onClick={() => setMobileOpen(false)} sx={{ borderRadius: 2, mb: .5, minHeight: 46, px: 2, color: '#a9beb7', '& .MuiListItemIcon-root': { color: 'inherit' }, '&:hover': { bgcolor: 'rgba(255,255,255,.07)' }, '&.active': { color: '#fff', bgcolor: '#315d52', '& .MuiListItemText-primary': { fontWeight: 700 }, '&:before': { content: '""', position: 'absolute', left: 0, top: 12, bottom: 12, width: 3, borderRadius: 2, bgcolor: '#c3e3a7' } } }}>
        <ListItemIcon sx={{ minWidth: 36, '& svg': { fontSize: 21 } }}>{item.icon}</ListItemIcon><ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 13.5 }} />
        {item.to === '/members' && pending.length > 0 && <Box sx={{ color: '#d6e6c8', bgcolor: '#497165', borderRadius: 2, px: .85, fontSize: 11, fontWeight: 800 }}>{pending.length}</Box>}
      </ListItemButton>)}
    </List>
    <Box sx={{ mt: 'auto', p: 2 }}>
      <Box sx={{ p: 2, borderRadius: 2.5, bgcolor: '#24453e', border: '1px solid rgba(255,255,255,.06)' }}>
        <Box sx={{ width: 30, height: 30, bgcolor: '#3d6758', borderRadius: 1.5, display: 'grid', placeItems: 'center', mb: 1.4 }}><HelpOutline sx={{ fontSize: 18, color: '#d7eabf' }} /></Box>
        <Typography sx={{ fontSize: 12, fontWeight: 700 }}>Espacio de demostración</Typography>
        <Typography sx={{ fontSize: 11, color: '#abc3b9', mt: .6, lineHeight: 1.5 }}>Tus cambios se guardan en este navegador.</Typography>
      </Box>
      <Divider sx={{ borderColor: 'rgba(255,255,255,.1)', my: 2 }} />
      <Typography sx={{ px: 1, color: '#8ba79e', fontSize: 11 }}>© {new Date().getFullYear()} IglesiaFlow</Typography>
    </Box>
  </Box>;

  const submitSearch = (e: FormEvent) => { e.preventDefault(); navigate(`/members?q=${encodeURIComponent(search.trim())}`); };

  return <Box sx={{ display: 'flex', minHeight: '100vh' }}>
    {mobile ? <Drawer open={mobileOpen} onClose={() => setMobileOpen(false)} ModalProps={{ keepMounted: true }} sx={{ '& .MuiDrawer-paper': { width: drawerWidth, border: 0 } }}>{sidebar}</Drawer>
      : <Drawer variant="permanent" open sx={{ width: drawerWidth, flexShrink: 0, '& .MuiDrawer-paper': { width: drawerWidth, border: 0 } }}>{sidebar}</Drawer>}
    <Box sx={{ flexGrow: 1, minWidth: 0 }}>
      <AppBar position="sticky" elevation={0} sx={{ bgcolor: '#fff', color: 'text.primary', borderBottom: '1px solid #e9eeeb' }}>
        <Toolbar sx={{ minHeight: '70px !important', px: { xs: 2, md: 4 } }}>
          {mobile && <IconButton aria-label="Abrir menú" onClick={() => setMobileOpen(true)} sx={{ mr: 1 }}><MenuIcon /></IconButton>}
          <Box sx={{ display: { xs: 'none', sm: 'flex' }, alignItems: 'center', gap: 1, color: 'text.secondary', fontSize: 12.5 }}><span>Mi iglesia</span><ChevronRight sx={{ fontSize: 15 }} /><strong style={{ color: '#244a43' }}>{pageName}</strong></Box>
          <Typography sx={{ display: { xs: 'block', sm: 'none' }, fontWeight: 800, fontFamily: 'Manrope' }}>{pageName}</Typography>
          <Box sx={{ flexGrow: 1 }} />
          <Box component="form" onSubmit={submitSearch} sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center', bgcolor: '#f6f8f6', border: '1px solid #edf0ed', borderRadius: 2, px: 1.5, width: 246, height: 37, mr: 2 }}><Search sx={{ fontSize: 18, color: '#8c9b94', mr: 1 }} /><InputBase placeholder="Buscar miembros..." inputProps={{ 'aria-label': 'Buscar miembros' }} value={search} onChange={(e) => setSearch(e.target.value)} sx={{ fontSize: 12.5, flex: 1 }} /></Box>
          <Tooltip title="Notificaciones"><IconButton aria-label="Notificaciones" onClick={(e) => setNotificationAnchor(e.currentTarget)} sx={{ mr: { xs: 1, md: 2 }, color: '#4b615b' }}><Badge badgeContent={pending.length} color="error" overlap="circular"><NotificationsNoneOutlined sx={{ fontSize: 22 }} /></Badge></IconButton></Tooltip>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2, pl: { xs: 0, sm: 2 }, borderLeft: { xs: 0, sm: '1px solid #e9eeeb' } }}><Avatar sx={{ width: 34, height: 34, bgcolor: '#e0eee5', color: '#336d56', fontWeight: 800, fontSize: 12 }}>AD</Avatar><Box sx={{ display: { xs: 'none', sm: 'block' } }}><Typography sx={{ fontSize: 12.5, fontWeight: 700, lineHeight: 1.25 }}>Administrador</Typography><Typography color="text.secondary" sx={{ fontSize: 10.5 }}>Vista local</Typography></Box></Box>
        </Toolbar>
      </AppBar>
      <Menu anchorEl={notificationAnchor} open={Boolean(notificationAnchor)} onClose={() => setNotificationAnchor(null)} PaperProps={{ sx: { width: 290, mt: 1, p: 1, borderRadius: 2 } }}>
        <Typography sx={{ px: 1.5, py: 1, fontSize: 13, fontWeight: 800 }}>Notificaciones</Typography>
        {pending.length ? <MenuItem onClick={() => { setNotificationAnchor(null); navigate('/members?status=En revisión'); }} sx={{ whiteSpace: 'normal', fontSize: 12.5, borderRadius: 1.5, py: 1.2 }}>{pending.length} miembro{pending.length !== 1 ? 's' : ''} pendiente{pending.length !== 1 ? 's' : ''} de revisión</MenuItem> : <Typography sx={{ p: 1.5, fontSize: 12, color: 'text.secondary' }}>No hay miembros pendientes.</Typography>}
        {upcoming[0] && <MenuItem onClick={() => { setNotificationAnchor(null); navigate('/events'); }} sx={{ whiteSpace: 'normal', fontSize: 12.5, borderRadius: 1.5, py: 1.2 }}>Próximo evento: {upcoming[0].title} · {formatDate(upcoming[0].date, { day: 'numeric', month: 'short' })}</MenuItem>}
      </Menu>
      <Box component="main" sx={{ maxWidth: 1560, mx: 'auto', px: { xs: 2, sm: 3, lg: 4 }, py: { xs: 3, md: 3.7 } }}><Outlet /></Box>
    </Box>
  </Box>;
}
