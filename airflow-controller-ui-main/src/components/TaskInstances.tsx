import { useState } from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Typography,
  Box,
  Alert,
  LinearProgress,
  IconButton,
  Modal,
  Button
} from '@mui/material';
import { useParams } from 'react-router-dom';
import { Refresh } from '@mui/icons-material';
import { useTaskInstances } from '../hooks/useTaskInstances';
import { TaskInstance } from '../types';
import VisibilityIcon from '@mui/icons-material/Visibility';
import TaskLog from './TaskLog';
import BackButton from './common/BackButton';

export default function TaskInstances() {
  const { dagId, runId } = useParams<{ dagId: string; runId: string }>();
  const { tasks, loading, error, fetchTaskInstances } = useTaskInstances(dagId, runId);
  const [selectedTask, setSelectedTask] = useState<TaskInstance | null>(null);
  const [logModalOpen, setLogModalOpen] = useState<boolean>(false);

  const getStateColor = (state: string | null): "success" | "info" | "error" | "warning" | "default" => {
    if (!state) return 'default';
    
    switch (state.toLowerCase()) {
      case 'success': return 'success';
      case 'running': return 'info';
      case 'failed': return 'error';
      case 'upstream_failed': return 'warning';
      default: return 'default';
    }
  };

  const handleViewLogs = (task: TaskInstance) => {
    setSelectedTask(task);
    setLogModalOpen(true);
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <BackButton to={dagId ? `/dags/${dagId}/runs` : undefined} />
        <Typography variant="h5">Task Instances</Typography>
        <Box sx={{ flexGrow: 1 }} />
        <IconButton onClick={fetchTaskInstances} size="small">
          <Refresh />
        </IconButton>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Task ID</TableCell>
              <TableCell>State</TableCell>
              <TableCell>Start Date</TableCell>
              <TableCell>End Date</TableCell>
              <TableCell>Duration</TableCell>
              <TableCell>Operator</TableCell>
              <TableCell>Try Number</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {tasks.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  No task instances found
                </TableCell>
              </TableRow>
            ) : (
              tasks.map((task) => (
                <TableRow key={task.task_id}>
                  <TableCell>{task.task_id}</TableCell>
                  <TableCell>
                    <Chip
                      label={task.state || 'unknown'}
                      color={getStateColor(task.state)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {task.start_date ? new Date(task.start_date).toLocaleString() : '-'}
                  </TableCell>
                  <TableCell>
                    {task.end_date ? new Date(task.end_date).toLocaleString() : '-'}
                  </TableCell>
                  <TableCell>
                    {task.duration ? `${task.duration.toFixed(2)}s` : '-'}
                  </TableCell>
                  <TableCell>{task.operator}</TableCell>
                  <TableCell>{task.try_number}</TableCell>
                  <TableCell>
                    <IconButton 
                      size="small" 
                      onClick={() => handleViewLogs(task)}
                      title="View logs"
                    >
                      <VisibilityIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {selectedTask && (
        <Modal
          open={logModalOpen}
          onClose={() => setLogModalOpen(false)}
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Box sx={{ width: '80%', maxHeight: '80vh', bgcolor: 'background.paper', p: 3, borderRadius: 1, overflow: 'auto' }}>
            <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
              Logs for {selectedTask.task_id}
            </Typography>
            <TaskLog 
              dagId={dagId} 
              dagRunId={runId} 
              taskId={selectedTask.task_id} 
              maxTryNumber={selectedTask.try_number || 1} 
            />
            <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
              <Button onClick={() => setLogModalOpen(false)}>Close</Button>
            </Box>
          </Box>
        </Modal>
      )}
    </Box>
  );
} 