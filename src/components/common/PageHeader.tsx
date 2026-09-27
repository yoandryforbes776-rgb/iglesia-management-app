import type { ReactNode } from 'react';
import { Box, Stack, Typography } from '@mui/material';

export default function PageHeader({ title, subtitle, action, eyebrow }: { title: string; subtitle?: string; action?: ReactNode; eyebrow?: string }) {
  return <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} spacing={2} sx={{ mb: 3.5 }}>
    <Box>
      {eyebrow && <Typography sx={{ color: 'primary.main', fontSize: 11, fontWeight: 800, letterSpacing: '0.13em', textTransform: 'uppercase', mb: .8 }}>{eyebrow}</Typography>}
      <Typography variant="h4" sx={{ fontSize: { xs: 26, md: 30 } }}>{title}</Typography>
      {subtitle && <Typography color="text.secondary" sx={{ mt: .6, fontSize: 14 }}>{subtitle}</Typography>}
    </Box>
    {action && <Box sx={{ flexShrink: 0 }}>{action}</Box>}
  </Stack>;
}
