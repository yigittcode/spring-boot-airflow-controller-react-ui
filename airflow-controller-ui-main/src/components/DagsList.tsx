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
import { getCredentials } from '../utils/auth';
import { format, parseISO, isValid } from 'date-fns';

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
      console.error('Error fetching DAGs:', err);
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
    console.log(`--- TOGGLE PAUSE ACTION FOR ${dagId} ---`);
    console.log('Current user:', getCredentials()?.username);
    console.log('Current pause state:', currentState);
    console.log('New pause state:', !currentState);
    
    try {
      await getDagService().togglePause(dagId, !currentState);
      console.log('Toggle pause action completed successfully');
      fetchDags(); // Refresh list after toggle
    } catch (err: any) {
      console.error(`Error toggling pause state for DAG ${dagId}:`, err);
    } finally {
      console.log('--- END TOGGLE PAUSE ACTION ---');
    }
  };

  const handleDeleteClick = (dagId: string) => {
    setDagToDelete(dagId);
    setDeleteConfirmOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!dagToDelete) return;
    
    try {
      await getDagService().deleteDag(dagToDelete);
      fetchDags();
    } catch (err: any) {
      console.error(`Error deleting DAG ${dagToDelete}:`, err);
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
    navigate(`/dags/${dagId}/runs`);
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
        return format(isoDate, 'dd.MM.yyyy HH:mm:ss');
      }
      
      // Try manual parsing
      // Airflow date format is usually: "2023-11-20T14:30:00+00:00" or similar
      if (dateString.includes('T') && (dateString.includes('+') || dateString.includes('Z'))) {
        // Clean ISO format and try again
        const cleanedDate = dateString.replace(/\.\d+/, ''); // Remove milliseconds
        const date = new Date(cleanedDate);
        if (isValid(date) && !isNaN(date.getTime())) {
          return format(date, 'dd.MM.yyyy HH:mm:ss');
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
            return format(date, 'dd.MM.yyyy');
          }
        }
      }
      
      // Last resort: We're showing the original value in the tooltip anyway
      return "Date could not be formatted";
    } catch (error) {
      return "Date could not be formatted";
    }
  };

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
                    dags.map((dag) => (
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
                          <Stack direction="row" spacing={1} justifyContent="center">
                            <Tooltip title="View DAG Runs">
                              <IconButton 
                                size="small" 
                                onClick={() => handleViewRuns(dag.dag_id)}
                              >
                                <BarChart />
                              </IconButton>
                            </Tooltip>
                            
                            <Tooltip title={dag.is_paused ? "Unpause DAG" : "Pause DAG"}>
                              <IconButton 
                                size="small" 
                                onClick={() => handleTogglePause(dag.dag_id, dag.is_paused)}
                                color={dag.is_paused ? "warning" : "default"}
                              >
                                {dag.is_paused ? <PlayArrow /> : <Pause />}
                              </IconButton>
                            </Tooltip>
                            
                            <Tooltip title="Delete DAG">
                              <IconButton 
                                size="small" 
                                onClick={() => handleDeleteClick(dag.dag_id)}
                                color="error"
                              >
                                <Delete />
                              </IconButton>
                            </Tooltip>
                          </Stack>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
              <Pagination 
                count={pageResponse.totalPages} 
                page={page} 
                onChange={handlePageChange} 
                color="primary" 
              />
            </Box>
          </>
        )}
      </Paper>
      
      {/* Delete confirmation dialog */}
      <Dialog
        open={deleteConfirmOpen}
        onClose={handleCancelDelete}
      >
        <DialogTitle>Delete DAG</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete DAG "{dagToDelete}"? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelDelete}>Cancel</Button>
          <Button onClick={handleConfirmDelete} color="error">Delete</Button>
        </DialogActions>
      </Dialog>

      {expandedRow && (
        <Box p={2}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={8}>
              <Typography variant="h6" gutterBottom>
                DAG Details
              </Typography>
              <Paper sx={{ p: 2 }}>
                {dags.filter(dag => dag.dag_id === expandedRow).map(dag => (
                  <Box key={dag.dag_id} sx={{ my: 1 }}>
                    <Grid container spacing={2}>
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">DAG ID:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Typography variant="body2">{dag.dag_id}</Typography>
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">Description:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Typography variant="body2">{dag.description || "No description"}</Typography>
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">Active:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Chip 
                          size="small" 
                          label={dag.is_active ? "Active" : "Inactive"} 
                          color={dag.is_active ? "success" : "error"}
                        />
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">Status:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Chip 
                          size="small" 
                          label={dag.is_paused ? "Paused" : "Running"} 
                          color={dag.is_paused ? "warning" : "info"}
                        />
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">Owners:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Typography variant="body2">
                          {dag.owners && dag.owners.length > 0 
                            ? dag.owners.join(", ") 
                            : "Not specified"}
                        </Typography>
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">Schedule:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Typography variant="body2">
                          {dag.schedule_interval 
                            ? `${dag.schedule_interval.type || dag.schedule_interval.__type || 'Cron'}: ${dag.schedule_interval.value}` 
                            : "Not specified"}
                        </Typography>
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">Tags:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                          {dag.tags && dag.tags.length > 0 
                            ? dag.tags.map(tag => (
                                <Chip 
                                  key={tag.name} 
                                  label={tag.name} 
                                  size="small" 
                                  variant="outlined"
                                  sx={{ m: 0.25 }}
                                />
                              ))
                            : <Typography variant="body2">No tags</Typography>
                          }
                        </Box>
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">File Location:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Typography variant="body2" sx={{ wordBreak: 'break-all' }}>
                          {dag.fileloc || "Not specified"}
                        </Typography>
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">Last Update:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Tooltip 
                          title={
                            <Box>
                              <Typography variant="caption">Original format:</Typography>
                              <Typography variant="body2">{dag.last_parsed_time}</Typography>
                            </Box>
                          }
                          arrow
                        >
                          <Chip
                            label={formatDate(dag.last_parsed_time)}
                            size="small"
                            color="info"
                            variant="outlined"
                            sx={{ cursor: 'help' }}
                          />
                        </Tooltip>
                      </Grid>
                      
                      <Grid item xs={4}>
                        <Typography variant="subtitle2" color="text.secondary">Timetable:</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Typography variant="body2">
                          {dag.timetable_description || "Not specified"}
                        </Typography>
                      </Grid>
                    </Grid>
                  </Box>
                ))}
              </Paper>
            </Grid>
            <Grid item xs={12} md={4}>
              <DagActivityLogs dagId={expandedRow} limit={5} />
            </Grid>
          </Grid>
        </Box>
      )}
    </Box>
  );
} 