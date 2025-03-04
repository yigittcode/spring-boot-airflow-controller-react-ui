import { useState, useEffect, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  CircularProgress,
  Alert,
  Stack,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Tooltip,
  Divider
} from '@mui/material';
import { Refresh, Download } from '@mui/icons-material';
import { getLogService } from '../utils/api';

// Log color settings for better readability
const LOG_STYLES = {
  container: {
    p: 1, 
    maxHeight: '500px', 
    overflow: 'auto',
    bgcolor: 'black',
    color: 'lightgreen',
    fontFamily: 'monospace',
    fontSize: '0.875rem',
    whiteSpace: 'pre-wrap',
    wordBreak: 'break-all'
  }
};

interface TaskLogProps {
  dagId?: string;
  dagRunId?: string;
  taskId?: string;
  maxTryNumber?: number;
}

export default function TaskLog({ dagId, dagRunId, taskId, maxTryNumber = 1 }: TaskLogProps) {
  const params = useParams();
  const finalDagId = dagId || params.dagId;
  const finalDagRunId = dagRunId || params.dagRunId;
  const finalTaskId = taskId || params.taskId;
  
  const [logs, setLogs] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tryNumber, setTryNumber] = useState(1);
  
  const tryOptions = useMemo(() => {
    const options = [];
    for (let i = 1; i <= maxTryNumber; i++) {
      options.push(i);
    }
    return options;
  }, [maxTryNumber]);
  
  const fetchLogs = async () => {
    if (!finalDagId || !finalDagRunId || !finalTaskId) {
      setError('Missing required parameters to fetch logs');
      return;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const logService = getLogService();
      const fetchedLogs = await logService.getTaskLogs(
        finalDagId, 
        finalDagRunId, 
        finalTaskId, 
        tryNumber
      );
      setLogs(fetchedLogs || 'No logs available for this try number');
    } catch (err: any) {
      setError(err.message || 'Failed to fetch logs');
      console.error('Error fetching logs:', err);
    } finally {
      setLoading(false);
    }
  };
  
  const handleDownload = () => {
    if (!logs) return;
    
    const blob = new Blob([logs], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${finalDagId}_${finalTaskId}_try${tryNumber}.log`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };
  
  useEffect(() => {
    fetchLogs();
  }, [finalDagId, finalDagRunId, finalTaskId, tryNumber]);
  
  return (
    <Box sx={{ width: '100%' }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
          <Typography variant="h6">Task Logs</Typography>
          <Box sx={{ flexGrow: 1 }} />
          
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Try Number</InputLabel>
            <Select
              value={tryNumber}
              label="Try Number"
              onChange={(e) => setTryNumber(Number(e.target.value))}
            >
              {tryOptions.map((num) => (
                <MenuItem key={num} value={num}>
                  Try {num}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          
          <Tooltip title="Refresh Logs">
            <IconButton onClick={fetchLogs} size="small" disabled={loading}>
              <Refresh />
            </IconButton>
          </Tooltip>
          
          <Tooltip title="Download Logs">
            <IconButton onClick={handleDownload} size="small" disabled={!logs}>
              <Download />
            </IconButton>
          </Tooltip>
        </Stack>
        
        <Divider sx={{ mb: 2 }} />
        
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        
        {loading && <CircularProgress size={24} sx={{ mb: 2, display: 'block' }} />}
        
        <Paper 
          variant="outlined" 
          sx={LOG_STYLES.container}
        >
          {logs || 'No logs available'}
        </Paper>
      </Paper>
    </Box>
  );
} 