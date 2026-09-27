import type { ReactNode } from 'react';
import { Card, CardContent, Typography, Box, Stack } from '@mui/material';

type StatCardProps = {
  label: string;
  value: string;
  delta: string;
  tone?: 'primary' | 'secondary' | 'success' | 'warning';
};

export default function StatCard({ label, value, delta, tone = 'primary' }: StatCardProps) {
  const tones = {
    primary: { bg: '#E9E4FD', color: '#4F378B' },
    secondary: { bg: '#DDF6F9', color: '#006874' },
    success: { bg: '#E5F6EA', color: '#2E7D32' },
    warning: { bg: '#FFF1D9', color: '#ED6C02' },
  };

  return (
    <Card>
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
          <Box
            sx={{
              width: 44,
              height: 44,
              borderRadius: 2,
              display: 'grid',
              placeItems: 'center',
              background: tones[tone].bg,
              color: tones[tone].color,
              fontWeight: 700,
            }}
          >
            {delta}
          </Box>
        </Stack>
        <Typography variant="body2" color="text.secondary">
          {label}
        </Typography>
        <Typography variant="h4" fontWeight={700} mt={1}>
          {value}
        </Typography>
      </CardContent>
    </Card>
  );
}
