import { useState, useEffect, useCallback } from 'react';
import { getTaskInstanceService } from '../services';
import { TaskInstance } from '../types';
import { extractErrorMessage, logApiError } from '../utils/errorHandling';

export function useTaskInstances(dagId?: string, dagRunId?: string) {
  const [tasks, setTasks] = useState<TaskInstance[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchTaskInstances = useCallback(async () => {
    if (!dagId || !dagRunId) return;
    
    setLoading(true);
    try {
      const taskInstanceService = getTaskInstanceService();
      const response = await taskInstanceService.getTaskInstances(dagId, dagRunId);
      
      // Extract task_instances array from API response
      const taskInstances = Array.isArray(response) 
        ? response 
        : response?.task_instances || [];
      
      setTasks(taskInstances);
      setError(null);
    } catch (error: any) {
      const errorMessage = extractErrorMessage(error);
      setError(`Error fetching task instances: ${errorMessage}`);
      logApiError(error, 'Fetch Task Instances');
    } finally {
      setLoading(false);
    }
  }, [dagId, dagRunId]);

  useEffect(() => {
    if (dagId && dagRunId) {
      fetchTaskInstances();
    }
  }, [dagId, dagRunId, fetchTaskInstances]);

  return { tasks, loading, error, fetchTaskInstances };
}