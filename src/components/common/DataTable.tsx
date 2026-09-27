import type { ReactNode } from 'react';
import { Box, Card, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';

type Column<T> = { key: keyof T | string; label: string; align?: 'left' | 'right' | 'center'; render?: (row: T) => ReactNode };
export default function DataTable<T extends { id: number }>({ title, subtitle, columns, rows, action, emptyText = 'No hay registros para mostrar.' }: { title: string; subtitle?: string; columns: Column<T>[]; rows: T[]; action?: ReactNode; emptyText?: string }) {
  return <Card sx={{ overflow: 'hidden' }}>
    <Box sx={{ px: { xs: 2, md: 3 }, pt: 2.5, pb: 2.2, display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
      <Box><Typography variant="h6" sx={{ fontSize: 17 }}>{title}</Typography>{subtitle && <Typography color="text.secondary" sx={{ fontSize: 12.5, mt: .3 }}>{subtitle}</Typography>}</Box>{action}
    </Box>
    <TableContainer><Table sx={{ minWidth: 680 }}><TableHead><TableRow sx={{ bgcolor: '#f8faf9' }}>{columns.map((column) => <TableCell key={String(column.key)} align={column.align || 'left'} sx={{ py: 1.55, px: 3, color: '#81908b', fontWeight: 700, fontSize: 11, textTransform: 'uppercase', letterSpacing: '.07em', whiteSpace: 'nowrap' }}>{column.label}</TableCell>)}</TableRow></TableHead>
    <TableBody>{rows.length ? rows.map((row) => <TableRow key={row.id} hover sx={{ '&:last-child td': { borderBottom: 0 } }}>{columns.map((column) => <TableCell key={String(column.key)} align={column.align || 'left'} sx={{ py: 1.6, px: 3, fontSize: 13, whiteSpace: 'nowrap' }}>{column.render ? column.render(row) : String(row[column.key as keyof T] ?? '')}</TableCell>)}</TableRow>) : <TableRow><TableCell colSpan={columns.length} sx={{ textAlign: 'center', color: 'text.secondary', py: 6 }}>{emptyText}</TableCell></TableRow>}</TableBody></Table></TableContainer>
  </Card>;
}
