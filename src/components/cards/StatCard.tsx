import type { ReactNode } from 'react';
import { Card, Typography, Box } from '@mui/material';

export default function StatCard({ label, value, detail, icon, tone = 'green' }: { label: string; value: string; detail: string; icon: ReactNode; tone?: 'green' | 'blue' | 'orange' | 'purple' }) {
  const tones = { green: ['#e3f2eb', '#338163'], blue: ['#e5f0f6', '#4689a4'], orange: ['#fff0df', '#c88748'], purple: ['#f0ebf7', '#8a75aa'] };
  return <Card sx={{ p: 2.5, minHeight: 158, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
      <Typography color="text.secondary" sx={{ fontSize: 13, fontWeight: 600 }}>{label}</Typography>
      <Box sx={{ width: 39, height: 39, display: 'grid', placeItems: 'center', borderRadius: 2.2, bgcolor: tones[tone][0], color: tones[tone][1], '& svg': { fontSize: 20 } }}>{icon}</Box>
    </Box>
    <Box><Typography sx={{ fontFamily: 'Manrope', fontSize: { xs: 27, md: 30 }, fontWeight: 800, lineHeight: 1.2, letterSpacing: '-.05em' }}>{value}</Typography><Typography color="text.secondary" sx={{ fontSize: 11.5, mt: .5 }}>{detail}</Typography></Box>
  </Card>;
}
