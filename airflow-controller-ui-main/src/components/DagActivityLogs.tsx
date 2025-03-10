import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Chip,
  Skeleton,
  Alert,
  IconButton,
  Tooltip,
  LinearProgress,
  Stack
} from '@mui/material';
import { formatDistanceToNow, parseISO, isValid } from 'date-fns';
import { format } from 'date-fns';
import RefreshIcon from '@mui/icons-material/Refresh';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import PauseIcon from '@mui/icons-material/Pause';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import BuildIcon from '@mui/icons-material/Build';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { DagActionLog, DAG_ACTION_TYPES } from '../types';
import { getDagActionLogsByDagId } from '../services/logService';
import { Link as RouterLink } from 'react-router-dom';

interface DagActivityLogsProps {
  dagId: string;
  limit?: number;
}

// Helper function to determine icon based on action type
const getActionIcon = (actionType: string) => {
  switch (actionType) {
    case DAG_ACTION_TYPES.TRIGGERED:
      return <PlayArrowIcon color="primary" />;
    case DAG_ACTION_TYPES.PAUSED:
      return <PauseIcon color="warning" />;
    case DAG_ACTION_TYPES.UNPAUSED:
      return <PlayArrowIcon color="success" />;
    case DAG_ACTION_TYPES.DELETED:
      return <DeleteIcon color="error" />;
    case DAG_ACTION_TYPES.CLEARED:
      return <CheckCircleOutlineIcon color="secondary" />;
    case DAG_ACTION_TYPES.TASK_STATE_CHANGED:
      return <BuildIcon color="info" />;
    default:
      return <MoreHorizIcon />;
  }
};

export default function DagActivityLogs({ dagId, limit = 10 }: DagActivityLogsProps) {
  const [logs, setLogs] = useState<DagActionLog[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchLogs = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getDagActionLogsByDagId(dagId);
      // Check if response is an array, if not, create an empty array
      const logsArray = Array.isArray(response) ? response : [];
      // Limit the number of logs based on the prop
      setLogs(logsArray.slice(0, limit));
    } catch (err) {
      console.error('Error fetching DAG logs:', err);
      setError('Failed to load DAG activity logs. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (dagId) {
      fetchLogs();
    }
  }, [dagId]);

  const formatTimestamp = (timestamp: string) => {
    if (!timestamp) {
      return 'No Date';
    }
    
    try {
      // First try with parseISO directly
      let date = parseISO(timestamp);
      
      // If not valid, try different formats
      if (!isValid(date)) {
        // If it's a Unix timestamp (number)
        if (!isNaN(Number(timestamp))) {
          date = new Date(Number(timestamp));
        } 
        // If it's a date string
        else {
          date = new Date(timestamp);
        }
      }
      
      // Final check
      if (!isValid(date)) {
        return 'Invalid Date';
      }
      
      return format(date, 'MM/dd/yyyy HH:mm:ss');
    } catch (error) {
      console.error("Error formatting date:", error);
      return 'Invalid Date';
    }
  };

  return (
    <Paper sx={{ p: 2, my: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">
          Recent Activities
        </Typography>
        <Tooltip title="Refresh">
          <IconButton onClick={fetchLogs} size="small">
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>
      
      {loading && <LinearProgress />}
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      <List sx={{ maxHeight: 400, overflow: 'auto' }}>
        {loading ? (
          Array.from(new Array(3)).map((_, index) => (
            <Box key={index}>
              <ListItem>
                <ListItemIcon>
                  <Skeleton variant="circular" width={24} height={24} />
                </ListItemIcon>
                <ListItemText 
                  primary={<Skeleton variant="text" width="80%" />} 
                  secondary={<Skeleton variant="text" width="40%" />} 
                />
              </ListItem>
              {index < 2 && <Divider component="li" />}
            </Box>
          ))
        ) : logs.length > 0 ? (
          logs.map((log, index) => (
            <Box key={log.id}>
              <ListItem>
                <ListItemIcon>
                  {getActionIcon(log.actionType)}
                </ListItemIcon>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography variant="body1" component="span">
                        {log.username}
                      </Typography>
                      <Chip 
                        label={log.actionType} 
                        size="small" 
                        variant="outlined" 
                        sx={{ fontSize: '0.7rem' }}
                      />
                      {log.dagId && log.dagId !== dagId && (
                        <Tooltip title={`Go to DAG: ${log.dagId}`}>
                          <Chip
                            label={log.dagId}
                            size="small"
                            color="primary"
                            component={RouterLink}
                            to={`/dags/${log.dagId}?details=true`}
                            clickable
                            sx={{ fontSize: '0.7rem' }}
                          />
                        </Tooltip>
                      )}
                    </Box>
                  }
                  secondaryTypographyProps={{ component: 'div' }}
                  secondary={
                    <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 0.5 }}>
                      <Typography variant="body2" component="span" color="text.primary">
                        {log.actionDetails}
                      </Typography>
                      
                      {log.runId && (
                        <Tooltip title={`Run ID: ${log.runId}`} arrow placement="top">
                          <Chip
                            label={log.runId.length > 10 ? `Run: ${log.runId.substring(0, 10)}...` : `Run: ${log.runId}`}
                            size="small"
                            variant="outlined"
                            color="info"
                            sx={{ fontSize: '0.7rem' }}
                          />
                        </Tooltip>
                      )}
                      
                      <Typography variant="caption" component="span" color="text.secondary">
                        ({formatTimestamp(log.timestamp)})
                      </Typography>
                    </Stack>
                  }
                />
              </ListItem>
              {index < logs.length - 1 && <Divider component="li" />}
            </Box>
          ))
        ) : (
          <ListItem>
            <ListItemText 
              primary={
                <Typography variant="body1" align="center">
                  No activity records for this DAG yet.
                </Typography>
              } 
            />
          </ListItem>
        )}
      </List>
    </Paper>
  );
} 