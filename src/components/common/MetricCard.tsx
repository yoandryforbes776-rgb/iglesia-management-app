import type { ReactNode } from 'react';
import { Box, Card, Typography } from '@mui/material';
export default function MetricCard({ label, value, detail, icon, color = '#32836b' }: { label: string; value: string; detail: string; icon: ReactNode; color?: string }) {
  return <Card sx={{ p: 2.5, display: 'flex', gap: 2, alignItems: 'center', minHeight: 116 }}><Box sx={{ width: 48, height: 48, flexShrink: 0, borderRadius: 2.3, display: 'grid', placeItems: 'center', bgcolor: `${color}18`, color }}>{icon}</Box><Box><Typography color="text.secondary" sx={{ fontSize: 12.5 }}>{label}</Typography><Typography sx={{ fontFamily: 'Manrope', fontWeight: 800, fontSize: 24, letterSpacing: '-.04em', lineHeight: 1.35 }}>{value}</Typography><Typography color="text.secondary" sx={{ fontSize: 11 }}>{detail}</Typography></Box></Card>;
}
