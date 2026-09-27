import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#285e57', light: '#e3f1ec', dark: '#194941', contrastText: '#fff' },
    secondary: { main: '#b66a46' },
    background: { default: '#f7f8f6', paper: '#fff' },
    text: { primary: '#172b2a', secondary: '#778582' },
    success: { main: '#32836b' }, warning: { main: '#c28935' }, error: { main: '#d16d62' },
    divider: '#e9eeeb',
  },
  shape: { borderRadius: 16 },
  typography: {
    fontFamily: '"DM Sans", sans-serif',
    h4: { fontFamily: '"Manrope", sans-serif', fontWeight: 800, letterSpacing: '-0.045em' },
    h5: { fontFamily: '"Manrope", sans-serif', fontWeight: 800, letterSpacing: '-0.04em' },
    h6: { fontFamily: '"Manrope", sans-serif', fontWeight: 750, letterSpacing: '-0.025em' },
    button: { textTransform: 'none', fontWeight: 700, letterSpacing: 0 },
  },
  components: {
    MuiCssBaseline: { styleOverrides: { body: { backgroundColor: '#f7f8f6' } } },
    MuiCard: { styleOverrides: { root: { border: '1px solid #e9eeeb', borderRadius: 18, boxShadow: '0 3px 18px rgba(23, 43, 42, .025)' } } },
    MuiButton: { styleOverrides: { root: { borderRadius: 10, boxShadow: 'none', padding: '9px 18px', '&:hover': { boxShadow: 'none' } } } },
    MuiOutlinedInput: { styleOverrides: { root: { borderRadius: 10, backgroundColor: '#fff' } } },
    MuiDialog: { styleOverrides: { paper: { borderRadius: 20 } } },
    MuiTableCell: { styleOverrides: { root: { borderBottomColor: '#edf0ee' } } },
  },
});

export default theme;
