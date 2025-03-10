import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Button,
  Tooltip,
  Grid,
  Stack,
  Pagination,
  CircularProgress,
  Alert
} from '@mui/material';
import { Edit, Delete, PlayArrow, Pause, BarChart } from '@mui/icons-material';
import { getDagService } from '../utils/api';
import { SelectChangeEvent } from '@mui/material/Select';
import { Dag, PageResponse } from '../types';
import DagActivityLogs from './DagActivityLogs';
import { getCredentials, canRunDags, canModifyDags, canViewDags, hasPermission, formatUserRole } from '../utils/auth';
import { format, parseISO, isValid } from 'date-fns';
import ProtectedComponent from './ProtectedComponent';

// Constants for styling and configuration
const TABLE_STYLES = {
  actionCell: { width: 200 }
};

const STATUS_COLORS = {
  active: 'success',
  paused: 'warning',
  inactive: 'error'
};

export default function DagsList() {
  const navigate = useNavigate();
  
  // State management
  const [dags, setDags] = useState<Dag[]>([]);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [dagToDelete, setDagToDelete] = useState<string | null>(null);
  const [isPaused, setIsPaused] = useState<string>('all');
  const [page, setPage] = useState(1);
  const [pageResponse, setPageResponse] = useState<PageResponse>({
    currentPage: 0,
    totalPages: 0,
    pageSize: 10,
    totalElements: 0
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expandedRow, setExpandedRow] = useState<string | null>(null);

  // Fetch DAGs with filtering options
  const fetchDags = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await getDagService().getDags({
        isPaused: isPaused === 'all' ? undefined : isPaused === 'true',
        search: searchTerm || undefined,
        page: page - 1,
        size: 10
      });

      setDags(response.data.dags || []);
      
      const totalEntries = response.data.total_entries || 0;
      const totalPages = Math.ceil(totalEntries / 10);
      
      setPageResponse({
        currentPage: page,
        totalPages,
        pageSize: 10,
        totalElements: totalEntries
      });

      // Adjust page if current page is beyond total pages
      if (page > totalPages && totalPages > 0) {
        setPage(1);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to fetch DAGs');
    } finally {
      setLoading(false);
    }
  };

  // Event handlers
  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
    setPage(1); // Reset to first page on new search
  };

  const handlePausedChange = (event: SelectChangeEvent) => {
    setIsPaused(event.target.value);
    setPage(1);
  };
  
  const handlePageChange = (_: React.ChangeEvent<unknown>, value: number) => {
    setPage(value);
  };

  const handleTogglePause = async (dagId: string, currentState: boolean) => {
    // Only OP and ADMIN can pause/unpause DAGs
    if (!canModifyDags()) {
      setError("You don't have permission to pause/unpause DAGs. This requires ADMIN or OP role.");
      setTimeout(() => setError(null), 5000);
      return;
    }
    
    try {
      await getDagService().togglePause(dagId, !currentState);
      fetchDags(); // Refresh list after toggle
    } catch (err: any) {
      setError(`Failed to ${currentState ? 'unpause' : 'pause'} DAG: ${err.message || 'Unknown error'}`);
      setTimeout(() => setError(null), 5000);
    }
  };

  const handleDeleteClick = (dagId: string) => {
    // Only OP and ADMIN can delete DAGs
    if (!canModifyDags()) {
      setError("You don't have permission to delete DAGs. This requires ADMIN or OP role.");
      setTimeout(() => setError(null), 5000);
      return;
    }
    
    setDagToDelete(dagId);
    setDeleteConfirmOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!dagToDelete) return;
    
    try {
      await getDagService().deleteDag(dagToDelete);
      fetchDags();
    } catch (err: any) {
      setError(`Failed to delete DAG: ${err.message || 'Unknown error'}`);
      setTimeout(() => setError(null), 5000);
    } finally {
      setDeleteConfirmOpen(false);
      setDagToDelete(null);
    }
  };

  const handleCancelDelete = () => {
    setDeleteConfirmOpen(false);
    setDagToDelete(null);
  };

  const handleViewRuns = (dagId: string) => {
    // Only USER, OP and ADMIN can view and manage DAG runs
    if (canRunDags()) {
      navigate(`/dags/${dagId}/runs`);
    } else {
      setError("You don't have permission to view DAG runs. This requires ADMIN, OP, or USER role.");
      setTimeout(() => setError(null), 5000);
    }
  };

  // Load DAGs when dependencies change
  useEffect(() => {
    fetchDags();
  }, [page, isPaused, searchTerm]);

  // Helper function for status chip
  const getStatusChip = (dag: Dag) => {
    if (!dag.is_active) {
      return <Chip label="Inactive" color="default" size="small" />;
    }
    return dag.is_paused ? 
      <Chip label="Paused" color="warning" size="small" /> : 
      <Chip label="Active" color="success" size="small" />;
  };

  // Helper function to format dates safely
  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return "Not specified";
    
    try {
      // First try standard methods
      const isoDate = new Date(dateString);
      if (isValid(isoDate) && !isNaN(isoDate.getTime())) {
        return format(isoDate, 'MM/dd/yyyy HH:mm:ss');
      }
      
      // Try manual parsing
      // Airflow date format is usually: "2023-11-20T14:30:00+00:00" or similar
      if (dateString.includes('T') && (dateString.includes('+') || dateString.includes('Z'))) {
        // Clean ISO format and try again
        const cleanedDate = dateString.replace(/\.\d+/, ''); // Remove milliseconds
        const date = new Date(cleanedDate);
        if (isValid(date) && !isNaN(date.getTime())) {
          return format(date, 'MM/dd/yyyy HH:mm:ss');
        }
      }
      
      // Try simple yyyy-MM-dd format
      if (/^\d{4}-\d{2}-\d{2}/.test(dateString)) {
        const parts = dateString.split(/[T\s]/)[0].split('-');
        if (parts.length === 3) {
          const year = parseInt(parts[0]);
          const month = parseInt(parts[1]) - 1; // Months in JavaScript are 0-11
          const day = parseInt(parts[2]);
          const date = new Date(year, month, day);
          if (isValid(date) && !isNaN(date.getTime())) {
            return format(date, 'MM/dd/yyyy');
          }
        }
      }
      
      // Last resort: We're showing the original value in the tooltip anyway
      return "Date could not be formatted";
    } catch (error) {
      return "Date could not be formatted";
    }
  };

  // Render components for table
  const renderDagRow = (dag: Dag) => (
    <TableRow
      key={dag.dag_id}
      hover
      onClick={() => setExpandedRow(prev => prev === dag.dag_id ? null : dag.dag_id)}
      sx={{
        cursor: 'pointer',
        backgroundColor: expandedRow === dag.dag_id ? 'action.hover' : 'inherit'
      }}
    >
      <TableCell>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          {dag.dag_id}
          {expandedRow === dag.dag_id && (
            <Typography variant="caption" color="primary" sx={{ ml: 1 }}>
              (Details shown)
            </Typography>
          )}
        </Box>
      </TableCell>
      <TableCell>{getStatusChip(dag)}</TableCell>
      <TableCell>{dag.description || '-'}</TableCell>
      <TableCell>{dag.owners?.join(', ') || '-'}</TableCell>
      <TableCell align="center" sx={TABLE_STYLES.actionCell}>
        <Stack direction="row" spacing={1} justifyContent="flex-end">
          {/* Pause/Resume button - Only for ADMIN and OP roles */}
          <ProtectedComponent 
            allowedRoles={['ADMIN', 'OP']}
            fallback={
              <Tooltip title="You need ADMIN or OP role to pause/unpause DAGs">
                <span>
                  <IconButton 
                    size="small"
                    color="default"
                    disabled={true}
                  >
                    {dag.is_paused ? <PlayArrow /> : <Pause />}
                  </IconButton>
                </span>
              </Tooltip>
            }
          >
            <Tooltip title={dag.is_paused ? 'Resume DAG' : 'Pause DAG'}>
              <IconButton 
                onClick={(e) => {
                  e.stopPropagation();
                  handleTogglePause(dag.dag_id, dag.is_paused);
                }} 
                size="small"
                color={dag.is_paused ? 'warning' : 'primary'}
              >
                {dag.is_paused ? <PlayArrow /> : <Pause />}
              </IconButton>
            </Tooltip>
          </ProtectedComponent>
          
          {/* View DAG Runs button - Show tooltip based on role */}
          <Tooltip title={canRunDags() ? "View DAG Runs" : "You need ADMIN, OP, or USER role to view runs"}>
            <span>
              <IconButton
                onClick={(e) => {
                  e.stopPropagation();
                  handleViewRuns(dag.dag_id);
                }} 
                size="small"
                color="primary"
                disabled={!canRunDags()}
              >
                <BarChart />
              </IconButton>
            </span>
          </Tooltip>
          
          {/* Delete DAG button - Only for ADMIN and OP roles */}
          <ProtectedComponent 
            allowedRoles={['ADMIN', 'OP']}
            fallback={
              <Tooltip title="You need ADMIN or OP role to delete DAGs">
                <span>
                  <IconButton 
                    size="small"
                    color="default"
                    disabled={true}
                  >
                    <Delete />
                  </IconButton>
                </span>
              </Tooltip>
            }
          >
            <Tooltip title="Delete DAG">
              <IconButton
                onClick={(e) => {
                  e.stopPropagation();
                  handleDeleteClick(dag.dag_id);
                }}
                size="small"
                color="error"
              >
                <Delete />
              </IconButton>
            </Tooltip>
          </ProtectedComponent>
        </Stack>
      </TableCell>
    </TableRow>
  );

  // Render expanded detail row for the selected DAG
  const renderExpandedRow = (dag: Dag) => (
    <TableRow key={`${dag.dag_id}-details`}>
      <TableCell colSpan={5}>
        <Box sx={{ p: 2, backgroundColor: 'background.paper' }}>
          <Typography variant="subtitle1" gutterBottom>DAG Details</Typography>
          
          <Grid container spacing={2}>
            {/* Schedule Information */}
            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Schedule Interval</Typography>
                <Typography variant="body1">
                  {typeof dag.schedule_interval === 'object' && dag.schedule_interval !== null
                    ? `${dag.schedule_interval.__type || dag.schedule_interval.type || 'Cron'}: ${dag.schedule_interval.value}`
                    : dag.schedule_interval || 'None'}
                </Typography>
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Timetable Description</Typography>
                <Typography variant="body1">
                  {dag.timetable_description || 'Not specified'}
                </Typography>
              </Box>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Default View</Typography>
                <Typography variant="body1" sx={{ textTransform: 'capitalize' }}>
                  {dag.default_view || 'Grid'}
                </Typography>
              </Box>
            </Grid>
            
            {/* Timing Information */}
            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Next Run</Typography>
                <Tooltip title={dag.next_dagrun || 'Not scheduled'}>
                  <Typography variant="body1">
                    {formatDate(dag.next_dagrun)}
                  </Typography>
                </Tooltip>
              </Box>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Last Parsed</Typography>
                <Tooltip title={dag.last_parsed_time || 'Not available'}>
                  <Typography variant="body1">
                    {formatDate(dag.last_parsed_time)}
                  </Typography>
                </Tooltip>
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Start Date</Typography>
                <Tooltip title={dag.start_date || 'Not specified'}>
                  <Typography variant="body1">
                    {formatDate(dag.start_date)}
                  </Typography>
                </Tooltip>
              </Box>
            </Grid>

            {/* Configuration Limits */}
            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Max Active Runs</Typography>
                <Typography variant="body1">
                  {dag.max_active_runs || 'Default'}
                </Typography>
              </Box>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Max Active Tasks</Typography>
                <Typography variant="body1">
                  {dag.max_active_tasks || 'Default'}
                </Typography>
              </Box>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Max Failed Runs</Typography>
                <Typography variant="body1">
                  {dag.max_consecutive_failed_dag_runs !== undefined ? dag.max_consecutive_failed_dag_runs : 'Default'}
                </Typography>
              </Box>
            </Grid>

            {/* Status Information */}
            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Is Active</Typography>
                <Chip 
                  label={dag.is_active ? "Active" : "Inactive"}
                  color={dag.is_active ? "success" : "default"}
                  size="small"
                />
              </Box>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Is Paused</Typography>
                <Chip 
                  label={dag.is_paused ? "Paused" : "Not Paused"}
                  color={dag.is_paused ? "warning" : "success"}
                  size="small"
                />
              </Box>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Box>
                <Typography variant="body2" color="textSecondary">Is Subdag</Typography>
                <Chip 
                  label={dag.is_subdag ? "Yes" : "No"}
                  color={dag.is_subdag ? "info" : "default"}
                  size="small"
                />
              </Box>
            </Grid>

            {/* File Information */}
            <Grid item xs={12} sm={12} md={12}>
              <Box>
                <Typography variant="body2" color="textSecondary">File Location</Typography>
                <Tooltip title={dag.fileloc || 'Unknown'}>
                  <Typography variant="body1" sx={{ 
                    wordBreak: 'break-all', 
                    textOverflow: 'ellipsis',
                    overflow: 'hidden',
                    whiteSpace: 'nowrap',
                    maxWidth: '100%'
                  }}>
                    {dag.fileloc || 'Unknown'}
                  </Typography>
                </Tooltip>
              </Box>
            </Grid>
            
            {/* Tags */}
            <Grid item xs={12}>
              <Box>
                <Typography variant="body2" color="textSecondary">Tags</Typography>
                <Box sx={{ mt: 0.5 }}>
                  {dag.tags && dag.tags.length > 0 ? (
                    dag.tags.map((tag) => (
                      <Chip 
                        key={tag.name} 
                        label={tag.name} 
                        size="small" 
                        sx={{ mr: 0.5, mb: 0.5 }} 
                      />
                    ))
                  ) : (
                    <Typography variant="body2">No tags</Typography>
                  )}
                </Box>
              </Box>
            </Grid>
          </Grid>
          
          <Typography variant="subtitle1" sx={{ mt: 3, mb: 1 }}>Recent Activities</Typography>
          <Box sx={{ maxHeight: 300, overflow: 'auto' }}>
            <DagActivityLogs dagId={dag.dag_id} limit={5} />
          </Box>
        </Box>
      </TableCell>
    </TableRow>
  );

  return (
    <Box>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Typography variant="h5" sx={{ mb: 3 }}>DAGs</Typography>
        
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Search DAGs"
              variant="outlined"
              value={searchTerm}
              onChange={handleSearchChange}
              size="small"
              placeholder="Search by DAG ID or description"
            />
          </Grid>
          
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth size="small">
              <InputLabel>Paused Status</InputLabel>
              <Select
                value={isPaused}
                label="Paused Status"
                onChange={handlePausedChange}
              >
                <MenuItem value="all">All</MenuItem>
                <MenuItem value="true">Paused</MenuItem>
                <MenuItem value="false">Unpaused</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
        
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>DAG ID</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Owners</TableCell>
                    <TableCell align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {dags.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        No DAGs found
                      </TableCell>
                    </TableRow>
                  ) : (
                    dags.map((dag) => [
                      renderDagRow(dag),
                      expandedRow === dag.dag_id && renderExpandedRow(dag)
                    ])
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            
            {pageResponse.totalPages > 1 && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                <Pagination
                  count={pageResponse.totalPages}
                  page={page}
                  onChange={handlePageChange}
                  color="primary"
                />
              </Box>
            )}
          </>
        )}
      </Paper>
      
      {/* Delete confirmation dialog */}
      <Dialog open={deleteConfirmOpen} onClose={handleCancelDelete}>
        <DialogTitle>Confirm Delete DAG</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete this DAG? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelDelete}>Cancel</Button>
          <Button onClick={handleConfirmDelete} color="error">Delete</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
} 