import { Chip } from '@mui/material';

type StatusChipProps = {
  label: string;
  color?: 'success' | 'warning' | 'error' | 'primary' | 'secondary';
};

export default function StatusChip({ label, color = 'primary' }: StatusChipProps) {
  return (
    <Chip
      label={label}
      color={color}
      variant="filled"
      sx={{
        fontWeight: 600,
        borderRadius: 1.5,
      }}
    />
  );
}
