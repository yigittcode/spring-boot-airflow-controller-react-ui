import { Chip, ChipProps } from '@mui/material';
import { useMemo } from 'react';

type StatusType = 'success' | 'running' | 'failed' | 'upstream_failed' | 'queued' | 'skipped' | null;

interface StatusChipProps extends Omit<ChipProps, 'color'> {
  status: string | null;
}

export default function StatusChip({ status, ...props }: StatusChipProps) {
  const color = useMemo((): ChipProps['color'] => {
    if (!status) return 'default';
    
    switch (status.toLowerCase()) {
      case 'success': return 'success';
      case 'running': return 'info';
      case 'failed': return 'error';
      case 'upstream_failed': return 'warning';
      case 'queued': return 'secondary';
      case 'skipped': return 'default';
      default: return 'default';
    }
  }, [status]);
  
  return <Chip 
    label={status || 'unknown'} 
    color={color} 
    size="small" 
    {...props} 
  />;
}