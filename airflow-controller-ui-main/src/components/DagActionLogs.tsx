import { useState, useEffect } from 'react';
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
  TablePagination,
  Chip,
  IconButton,
  Tooltip,
  TextField,
  InputAdornment,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  Skeleton,
  LinearProgress
} from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import RefreshIcon from '@mui/icons-material/Refresh';
import SearchIcon from '@mui/icons-material/Search';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import { Link } from 'react-router-dom';
import { format, parseISO, isValid } from 'date-fns';
import { DagActionLog, DagActionLogResponse, DAG_ACTION_TYPES } from '../types';
import { getDagActionLogs, getDagActionLogsByDagId, getDagActionLogsByType } from '../services/logService';

// Helper function to get appropriate color for action type
const getActionColor = (actionType: string): 'primary' | 'success' | 'error' | 'warning' | 'info' | 'secondary' | 'default' => {
  switch (actionType) {
    case DAG_ACTION_TYPES.TRIGGERED:
      return 'primary';
    case DAG_ACTION_TYPES.PAUSED:
      return 'warning';
    case DAG_ACTION_TYPES.UNPAUSED:
      return 'success';
    case DAG_ACTION_TYPES.DELETED:
      return 'error';
    case DAG_ACTION_TYPES.CLEARED:
      return 'secondary';
    case DAG_ACTION_TYPES.TASK_STATE_CHANGED:
      return 'info';
    default:
      return 'default';
  }
};

export default function DagActionLogs() {
  const [logs, setLogs] = useState<DagActionLog[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(20);
  const [totalCount, setTotalCount] = useState<number>(0);
  const [filterType, setFilterType] = useState<string>('all');
  const [searchValue, setSearchValue] = useState<string>('');
  const [filterDagId, setFilterDagId] = useState<string>('');

  const fetchLogs = async () => {
    setLoading(true);
    try {
      let response: DagActionLogResponse | DagActionLog[];

      if (filterType === 'all') {
        response = await getDagActionLogs(page, rowsPerPage);
        if ('logs' in response) {
          setLogs(response.logs);
          setTotalCount(response.totalCount);
        }
      } else if (filterType === 'dagId' && filterDagId) {
        response = await getDagActionLogsByDagId(filterDagId);
        setLogs(response as DagActionLog[]);
        setTotalCount((response as DagActionLog[]).length);
      } else if (filterType === 'actionType' && searchValue) {
        response = await getDagActionLogsByType(searchValue);
        setLogs(response as DagActionLog[]);
        setTotalCount((response as DagActionLog[]).length);
      } else {
        // Default to all logs if no filter is selected
        response = await getDagActionLogs(page, rowsPerPage);
        if ('logs' in response) {
          setLogs(response.logs);
          setTotalCount(response.totalCount);
        }
      }
    } catch (error) {
      console.error('Error fetching DAG action logs:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [page, rowsPerPage, filterType, filterDagId]);

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleSearch = () => {
    if (filterType === 'dagId') {
      setFilterDagId(searchValue);
    } else {
      fetchLogs();
    }
  };

  const handleRefresh = () => {
    fetchLogs();
  };

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
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        DAG Action Logs
      </Typography>
      
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={3}>
            <FormControl fullWidth size="small">
              <InputLabel id="filter-type-label">Filter Type</InputLabel>
              <Select
                labelId="filter-type-label"
                value={filterType}
                label="Filter Type"
                onChange={(e) => setFilterType(e.target.value)}
              >
                <MenuItem value="all">All Logs</MenuItem>
                <MenuItem value="dagId">By DAG ID</MenuItem>
                <MenuItem value="actionType">By Action Type</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={7}>
            {filterType !== 'all' && (
              <TextField
                fullWidth
                placeholder={filterType === 'dagId' ? 'Enter DAG ID' : 'Enter action type (e.g. TRIGGERED, PAUSED)'}
                size="small"
                value={searchValue}
                onChange={(e) => setSearchValue(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                }}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSearch();
                  }
                }}
              />
            )}
          </Grid>
          <Grid item xs={12} sm={2} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Tooltip title="Refresh">
              <IconButton onClick={handleRefresh} color="primary">
                <RefreshIcon />
              </IconButton>
            </Tooltip>
            {filterType !== 'all' && (
              <Tooltip title="Filter">
                <IconButton onClick={handleSearch} color="primary">
                  <FilterListIcon />
                </IconButton>
              </Tooltip>
            )}
          </Grid>
        </Grid>
      </Paper>

      <Paper>
        {loading && <LinearProgress />}
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Date</TableCell>
                <TableCell>User</TableCell>
                <TableCell>DAG ID</TableCell>
                <TableCell>Action Type</TableCell>
                <TableCell>Action Details</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Run ID</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                Array.from(new Array(5)).map((_, index) => (
                  <TableRow key={index}>
                    <TableCell><Skeleton variant="text" /></TableCell>
                    <TableCell><Skeleton variant="text" /></TableCell>
                    <TableCell><Skeleton variant="text" /></TableCell>
                    <TableCell><Skeleton variant="text" /></TableCell>
                    <TableCell><Skeleton variant="text" /></TableCell>
                    <TableCell><Skeleton variant="text" /></TableCell>
                    <TableCell><Skeleton variant="text" /></TableCell>
                  </TableRow>
                ))
              ) : logs.length > 0 ? (
                logs.map((log) => (
                  <TableRow key={log.id}>
                    <TableCell>{formatTimestamp(log.timestamp)}</TableCell>
                    <TableCell>{log.username}</TableCell>
                    <TableCell>
                      <Link to={`/dags/${log.dagId}`} style={{ textDecoration: 'none' }}>
                        {log.dagId}
                      </Link>
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={log.actionType} 
                        color={getActionColor(log.actionType)} 
                        size="small" 
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>{log.actionDetails}</TableCell>
                    <TableCell>
                      {log.success ? (
                        <Tooltip title="Successful">
                          <CheckCircleOutlineIcon color="success" />
                        </Tooltip>
                      ) : (
                        <Tooltip title="Failed">
                          <ErrorOutlineIcon color="error" />
                        </Tooltip>
                      )}
                    </TableCell>
                    <TableCell>
                      {log.runId ? (
                        <Tooltip title={log.runId} arrow placement="top">
                          <Link to={`/dags/${log.dagId}/runs/${log.runId}`} style={{ textDecoration: 'none' }}>
                            {log.runId.length > 15 ? `${log.runId.substring(0, 15)}...` : log.runId}
                          </Link>
                        </Tooltip>
                      ) : (
                        '-'
                      )}
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography variant="body1" sx={{ py: 2 }}>
                      {filterType !== 'all' ? 'No logs found matching the filter' : 'No log records found'}
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={totalCount}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          rowsPerPageOptions={[10, 20, 50, 100]}
          labelRowsPerPage="Rows per page:"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} / ${count}`}
        />
      </Paper>
    </Box>
  );
} 