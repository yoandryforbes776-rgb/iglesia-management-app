import React, { type ReactNode } from 'react';
import {
  AppBar,
  Box,
  Drawer,
  Divider,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import {
  AccountBalance,
  CalendarMonth,
  Dashboard,
  Groups,
  Menu,
  Report,
} from '@mui/icons-material';
import { NavLink, Outlet } from 'react-router-dom';

const drawerWidth = 260;

const navItems = [
  { label: 'Dashboard', to: '/', icon: <Dashboard /> },
  { label: 'Miembros', to: '/members', icon: <Groups /> },
  { label: 'Eventos', to: '/events', icon: <CalendarMonth /> },
  { label: 'Finanzas', to: '/finance', icon: <AccountBalance /> },
  { label: 'Reportes', to: '/reports', icon: <Report /> },
];

const drawerContent = (
  <Box sx={{ height: '100%', bgcolor: 'background.paper' }}>
    <Toolbar>
      <Typography variant="h6" fontWeight={700} color="primary.main">
        IglesiaFlow
      </Typography>
    </Toolbar>
    <Divider />
    <List sx={{ px: 1.5, py: 2 }}>
      {navItems.map((item) => (
        <ListItem key={item.label} disablePadding sx={{ mb: 1 }}>
          <ListItemButton
            component={NavLink}
            to={item.to}
            end={item.to === '/'}
            sx={({ palette }) => ({
              borderRadius: 2,
              color: 'text.primary',
              '&.active': {
                bgcolor: palette.primary.main,
                color: '#fff',
                '& .MuiListItemIcon-root': {
                  color: '#fff',
                },
              },
            })}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItemButton>
        </ListItem>
      ))}
    </List>
  </Box>
);

export default function AppShell() {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = React.useState(false);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {!isMobile && (
        <Box component="nav" sx={{ width: drawerWidth, flexShrink: 0 }}>
          <Drawer
            variant="permanent"
            open
            sx={{
              '& .MuiDrawer-paper': {
                width: drawerWidth,
                boxSizing: 'border-box',
                borderRight: '1px solid rgba(0,0,0,0.08)',
              },
            }}
          >
            {drawerContent}
          </Drawer>
        </Box>
      )}

      {isMobile && (
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            '& .MuiDrawer-paper': {
              width: drawerWidth,
              boxSizing: 'border-box',
            },
          }}
        >
          {drawerContent}
        </Drawer>
      )}

      <Box component="main" sx={{ flexGrow: 1, minWidth: 0 }}>
        <AppBar
          position="sticky"
          color="transparent"
          elevation={0}
          sx={{
            backdropFilter: 'blur(12px)',
            borderBottom: '1px solid rgba(0,0,0,0.08)',
            bgcolor: 'rgba(245,247,251,0.8)',
          }}
        >
          <Toolbar>
            {isMobile && (
              <IconButton onClick={() => setMobileOpen(true)} sx={{ mr: 1 }}>
                <Menu />
              </IconButton>
            )}
            <Typography variant="h6" fontWeight={700}>
              Panel de administración
            </Typography>
          </Toolbar>
        </AppBar>

        <Box sx={{ p: { xs: 2, md: 3 } }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
