import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
export default function ConfirmDialog({ open, title, message, onClose, onConfirm }: { open: boolean; title: string; message: string; onClose: () => void; onConfirm: () => void }) {
  return <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth><DialogTitle sx={{ fontWeight: 800 }}>{title}</DialogTitle><DialogContent><Typography color="text.secondary" fontSize={14}>{message}</Typography></DialogContent><DialogActions sx={{ p: 2.5, pt: 1 }}><Button onClick={onClose} color="inherit">Cancelar</Button><Button onClick={onConfirm} color="error" variant="contained">Eliminar</Button></DialogActions></Dialog>;
}
