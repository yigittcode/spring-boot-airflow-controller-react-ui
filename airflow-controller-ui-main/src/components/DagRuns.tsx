import { useState, useEffect, useCallback, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Chip,
  Typography,
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  Alert,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  Grid,
  LinearProgress,
  Tooltip,
} from '@mui/material';
import { PlayArrow, Stop, Delete, Refresh, ListAlt, FilterList, SportsScore, Visibility } from '@mui/icons-material';
import { getDagRunService } from '../utils/api';
import { extractErrorMessage, logApiError } from '../utils/errorHandling';
import BackButton from './common/BackButton';

interface DagRun {
  dag_run_id: string;
  dag_id: string;
  logical_date: string;
  start_date: string | null;
  end_date: string | null;
  state: 'queued' | 'running' | 'success' | 'failed';
  external_trigger: boolean;
  conf: {
    message: string;
    parameters?: {
      source?: string;
      trigger_time?: string;
      custom_param?: any;
      environment?: string;
      priority?: number;
    };
  };
  note?: string;
  data_interval_start: string | null;
  data_interval_end: string | null;
  last_scheduling_decision: string | null;
  run_type: string;
}

interface DagRunConfig {
  environment: string;
  custom_param?: string;
  priority?: number;
  notes?: string;
  start_date?: string;
  runId?: string;
}

interface DagRunFilters {
  state: string;
  dagRunId: string;
}

export default function DagRuns() {
  const { dagId } = useParams<{ dagId: string }>();
  const navigate = useNavigate();
  const [dagRuns, setDagRuns] = useState<DagRun[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [openNewRun, setOpenNewRun] = useState(false);
  const [newRunConfig, setNewRunConfig] = useState<DagRunConfig>({
    environment: 'prod',
    custom_param: '',
    priority: 1,
    notes: '',
    start_date: new Date().toISOString().slice(0, 16),
    runId: ''
  });
  
  const [filters, setFilters] = useState<DagRunFilters>({
    state: '',
    dagRunId: '',
  });
  
  const [showFilters, setShowFilters] = useState(false);

  const fetchDagRuns = useCallback(async () => {
    if (!dagId) return;
    
    setLoading(true);
    try {
      const dagRunService = getDagRunService();
      const response = await dagRunService.getDagRuns(dagId, {
        state: filters.state !== 'all' ? filters.state : undefined,
      });
      
      const runs = Array.isArray(response.data) 
        ? response.data 
        : response.data?.dag_runs || [];
      
      setDagRuns(runs);
      setError(null);
    } catch (error: any) {
      const errorMessage = extractErrorMessage(error);
      setError(`Error fetching DAG runs: ${errorMessage}`);
      logApiError(error, 'Fetch DAG Runs');
    } finally {
      setLoading(false);
    }
  }, [dagId, filters.state]);
  
  useEffect(() => {
    fetchDagRuns();
  }, [fetchDagRuns]);
  
  const runStatusCounts = useMemo(() => {
    return {
      all: dagRuns.length,
      queued: dagRuns.filter(run => run.state === 'queued').length,
      running: dagRuns.filter(run => run.state === 'running').length,
      success: dagRuns.filter(run => run.state === 'success').length,
      failed: dagRuns.filter(run => run.state === 'failed').length,
    };
  }, [dagRuns]);
  
  const getFilteredDagRuns = () => {
    return dagRuns.filter(run => {
      if (filters.state && run.state !== filters.state) {
        return false;
      }
      
      if (filters.dagRunId && !run.dag_run_id.toLowerCase().includes(filters.dagRunId.toLowerCase())) {
        return false;
      }
      
      return true;
    });
  };

  const handleFilterChange = (name: string, value: string) => {
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleCreateRun = async () => {
    if (!dagId) return;
    try {
      const startDate = newRunConfig.start_date 
        ? new Date(newRunConfig.start_date) 
        : new Date();

      const runId = newRunConfig.runId 
        ? newRunConfig.runId 
        : `manual_${new Date().getTime()}`;

      const dagRunService = getDagRunService();
      await dagRunService.createDagRun(dagId, {
        runId: runId,
        logicalDate: startDate.toISOString(),
        conf: {
          message: newRunConfig.notes || 'Triggered from UI',
          parameters: {
            source: 'frontend_app',
            environment: newRunConfig.environment,
            custom_param: newRunConfig.custom_param,
            priority: newRunConfig.priority,
            trigger_time: new Date().toISOString()
          }
        }
      });
      
      setOpenNewRun(false);
      setNewRunConfig({
        environment: 'prod',
        custom_param: '',
        priority: 1,
        notes: '',
        start_date: new Date().toISOString().slice(0, 16),
        runId: ''
      });
      fetchDagRuns();
    } catch (error: any) {
      const errorMessage = extractErrorMessage(error);
      setError(`Error creating DAG run: ${errorMessage}`);
      logApiError(error, 'Create DAG Run');
    }
  };

  const handleDeleteRun = async (runId: string) => {
    if (!dagId) return;
    try {
      const dagRunService = getDagRunService();
      await dagRunService.deleteDagRun(dagId, runId);
      fetchDagRuns();
    } catch (error: any) {
      const errorMessage = extractErrorMessage(error);
      setError(`Error deleting DAG run: ${errorMessage}`);
      logApiError(error, 'Delete DAG Run');
    }
  };

  const handleClearRun = async (runId: string) => {
    if (!dagId) return;
    setLoading(true);
    try {
      const dagRunService = getDagRunService();
      await dagRunService.clearDagRun(dagId, runId);
      await fetchDagRuns();
      setError(null);
    } catch (error: any) {
      const errorMessage = extractErrorMessage(error);
      setError(`Error clearing DAG run: ${errorMessage}`);
      logApiError(error, 'Clear DAG Run');
    } finally {
      setLoading(false);
    }
  };

  const handleStopRun = async (runId: string) => {
    if (!dagId) return;
    try {
      const dagRunService = getDagRunService();
      await dagRunService.updateDagRunState(dagId, runId, 'failed');
      fetchDagRuns();
    } catch (error: any) {
      const errorMessage = extractErrorMessage(error);
      setError(`Error stopping DAG run: ${errorMessage}`);
      logApiError(error, 'Stop DAG Run');
    }
  };

  const handleViewTasks = (dagId: string, runId: string) => {
    navigate(`/dags/${dagId}/runs/${runId}/tasks`);
  };

  const getStateColor = (state: string): "success" | "info" | "error" | "default" => {
    switch (state.toLowerCase()) {
      case 'success':
        return 'success';
      case 'running':
        return 'info';
      case 'failed':
        return 'error';
      default:
        return 'default';
    }
  };

  const renderFilterSection = () => {
    if (!showFilters) return null;
    
    return (
      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={5}>
            <FormControl fullWidth size="small">
              <InputLabel id="state-filter-label">State</InputLabel>
              <Select
                labelId="state-filter-label"
                value={filters.state}
                label="State"
                onChange={(e) => handleFilterChange('state', e.target.value)}
              >
                <MenuItem value="">All States</MenuItem>
                <MenuItem value="queued">Queued</MenuItem>
                <MenuItem value="running">Running</MenuItem>
                <MenuItem value="success">Success</MenuItem>
                <MenuItem value="failed">Failed</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={5}>
            <TextField
              fullWidth
              size="small"
              label="DAG Run ID"
              value={filters.dagRunId}
              onChange={(e) => handleFilterChange('dagRunId', e.target.value)}
              placeholder="Search by DAG Run ID"
            />
          </Grid>
          <Grid item xs={12} sm={2}>
            <Button 
              variant="contained" 
              onClick={() => setFilters({state: '', dagRunId: ''})} 
              color="secondary"
              fullWidth
            >
              Clear Filters
            </Button>
          </Grid>
        </Grid>
      </Paper>
    );
  };

  const renderDagRunsTable = () => {
    const filteredRuns = getFilteredDagRuns();
    
    if (loading) {
      return (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
          <CircularProgress />
        </Box>
      );
    }

    if (filteredRuns.length === 0) {
      return (
        <Typography variant="body1" sx={{ p: 2 }}>
          No DAG runs found{filters.state || filters.dagRunId ? ' matching the filter criteria' : ''}.
        </Typography>
      );
    }

    return (
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Run ID</TableCell>
              <TableCell>Logical Date</TableCell>
              <TableCell>Start</TableCell>
              <TableCell>End</TableCell>
              <TableCell>Duration</TableCell>
              <TableCell>State</TableCell>
              <TableCell>Note</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredRuns.map((run) => (
              <TableRow key={run.dag_run_id}>
                <TableCell>{run.dag_run_id}</TableCell>
                <TableCell>
                  {run.logical_date ? new Date(run.logical_date).toLocaleString() : '-'}
                </TableCell>
                <TableCell>
                  {run.start_date ? new Date(run.start_date).toLocaleString() : '-'}
                </TableCell>
                <TableCell>
                  {run.end_date ? new Date(run.end_date).toLocaleString() : '-'}
                </TableCell>
                <TableCell>
                  {run.end_date && run.start_date ? (
                    <Typography variant="body2" color="textSecondary">
                      {(new Date(run.end_date).getTime() - new Date(run.start_date).getTime()) / 1000} seconds
                    </Typography>
                  ) : '-'}
                </TableCell>
                <TableCell>
                  <Chip
                    label={run.state}
                    color={getStateColor(run.state)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Tooltip title={(run.note || run.conf?.message || '-')} arrow>
                    <Typography 
                      sx={{ 
                        maxWidth: 150, 
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}
                    >
                      {run.note || run.conf?.message || '-'}
                    </Typography>
                  </Tooltip>
                </TableCell>
                <TableCell>
                  <IconButton
                    size="small"
                    onClick={() => handleViewTasks(dagId!, run.dag_run_id)}
                    title="View Tasks"
                  >
                    <ListAlt />
                  </IconButton>
                  {run.state === 'running' && (
                    <IconButton
                      size="small"
                      onClick={() => handleStopRun(run.dag_run_id)}
                      title="Stop Run"
                    >
                      <Stop />
                    </IconButton>
                  )}
                  <IconButton
                    size="small"
                    onClick={() => handleClearRun(run.dag_run_id)}
                    title="Clear Run"
                  >
                    <SportsScore />
                  </IconButton>
                  <IconButton
                    size="small"
                    onClick={() => handleDeleteRun(run.dag_run_id)}
                    title="Delete Run"
                  >
                    <Delete />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    );
  };

  if (!dagId) {
    return <Typography>No DAG ID provided</Typography>;
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <BackButton to="/" />
        <Typography variant="h5">DAG Runs - {dagId}</Typography>
        <Box sx={{ flexGrow: 1 }} />
        <Box>
          <Button
            variant="outlined"
            startIcon={<FilterList />}
            onClick={() => setShowFilters(!showFilters)}
            sx={{ mr: 1 }}
          >
            {showFilters ? 'Hide Filters' : 'Show Filters'}
          </Button>
          <Button
            variant="contained"
            startIcon={<Refresh />}
            onClick={fetchDagRuns}
            sx={{ mr: 1 }}
            disabled={loading}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            color="primary"
            startIcon={<PlayArrow />}
            onClick={() => setOpenNewRun(true)}
            disabled={loading}
          >
            Trigger DAG
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {renderFilterSection()}

      {renderDagRunsTable()}

      <Dialog 
        open={openNewRun} 
        onClose={() => setOpenNewRun(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Trigger New DAG Run</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <Typography variant="subtitle2" gutterBottom>Run ID (Optional)</Typography>
            <TextField
              fullWidth
              size="small"
              value={newRunConfig.runId}
              onChange={(e) => setNewRunConfig({
                ...newRunConfig,
                runId: e.target.value
              })}
              placeholder="Leave empty for auto-generated ID"
              sx={{ mb: 2 }}
            />

            <Typography variant="subtitle2" gutterBottom>Environment</Typography>
            <TextField
              select
              fullWidth
              size="small"
              value={newRunConfig.environment}
              onChange={(e) => setNewRunConfig({
                ...newRunConfig,
                environment: e.target.value
              })}
              sx={{ mb: 2 }}
            >
              <MenuItem value="prod">Production</MenuItem>
              <MenuItem value="stage">Staging</MenuItem>
              <MenuItem value="dev">Development</MenuItem>
              <MenuItem value="test">Test</MenuItem>
            </TextField>

            <Typography variant="subtitle2" gutterBottom>Custom Parameter</Typography>
            <TextField
              fullWidth
              size="small"
              value={newRunConfig.custom_param}
              onChange={(e) => setNewRunConfig({
                ...newRunConfig,
                custom_param: e.target.value
              })}
              placeholder="Enter custom parameter"
              sx={{ mb: 2 }}
            />

            <Typography variant="subtitle2" gutterBottom>Priority</Typography>
            <TextField
              type="number"
              fullWidth
              size="small"
              value={newRunConfig.priority}
              onChange={(e) => setNewRunConfig({
                ...newRunConfig,
                priority: parseInt(e.target.value) || 1
              })}
              inputProps={{ min: 1, max: 10 }}
              sx={{ mb: 2 }}
            />

            <Typography variant="subtitle2" gutterBottom>Start Date</Typography>
            <TextField
              type="datetime-local"
              fullWidth
              size="small"
              value={newRunConfig.start_date}
              onChange={(e) => setNewRunConfig({
                ...newRunConfig,
                start_date: e.target.value
              })}
              inputProps={{ step: 1 }}
              sx={{ mb: 2 }}
            />

            <Typography variant="subtitle2" gutterBottom>Notes</Typography>
            <TextField
              fullWidth
              multiline
              rows={3}
              size="small"
              value={newRunConfig.notes}
              onChange={(e) => setNewRunConfig({
                ...newRunConfig,
                notes: e.target.value
              })}
              placeholder="Add notes about this run"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={() => {
              setOpenNewRun(false);
              setNewRunConfig({
                environment: 'prod',
                custom_param: '',
                priority: 1,
                notes: '',
                start_date: new Date().toISOString().slice(0, 16),
                runId: ''
              });
            }}
          >
            Cancel
          </Button>
          <Button 
            onClick={handleCreateRun} 
            variant="contained" 
            color="primary"
          >
            Trigger
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
} 