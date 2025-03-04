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

// Constants for styling and configuration
const TABLE_STYLES = {
  actionCell: { width: 200 }
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
    try {
      await getDagService().togglePause(dagId, !currentState);
      fetchDags(); // Refresh list after toggle
    } catch (err: any) {
      console.error(`Error toggling pause state for DAG ${dagId}:`, err);
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
                      <TableRow key={dag.dag_id}>
                        <TableCell>{dag.dag_id}</TableCell>
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
    </Box>
  );
} 