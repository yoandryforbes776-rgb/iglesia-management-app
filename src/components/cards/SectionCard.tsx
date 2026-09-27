import type { ReactNode } from 'react';
import { Card, Box, Typography } from '@mui/material';

export default function SectionCard({ title, subtitle, action, children, sx }: { title: string; subtitle?: string; action?: ReactNode; children: ReactNode; sx?: object }) {
  return <Card sx={{ p: { xs: 2.3, md: 3 }, height: '100%', ...sx }}>
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1, mb: 2.5 }}>
      <Box><Typography variant="h6" sx={{ fontSize: 17 }}>{title}</Typography>{subtitle && <Typography color="text.secondary" sx={{ fontSize: 12.5, mt: .4 }}>{subtitle}</Typography>}</Box>
      {action}
    </Box>
    {children}
  </Card>;
}
