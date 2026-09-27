import type { ReactNode } from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';

type Column<T> = {
  key: keyof T;
  label: string;
  align?: 'left' | 'right' | 'center';
  render?: (row: T) => ReactNode;
};

type DataTableProps<T extends Record<string, unknown>> = {
  title: string;
  columns: Column<T>[];
  rows: T[];
};

export default function DataTable<T extends Record<string, unknown>>({
  title,
  columns,
  rows,
}: DataTableProps<T>) {
  return (
    <Paper variant="outlined" sx={{ borderRadius: 3, overflow: 'hidden' }}>
      <Typography variant="h6" sx={{ p: 2, pb: 1, fontWeight: 700 }}>
        {title}
      </Typography>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell key={String(column.key)} align={column.align || 'left'}>
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row, index) => (
              <TableRow key={index} hover>
                {columns.map((column) => (
                  <TableCell key={`${String(column.key)}-${index}`} align={column.align || 'left'}>
                    {column.render ? column.render(row) : String(row[column.key] ?? '')}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}
