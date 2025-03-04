import { getApiClient } from '../utils/apiClient';
import { TaskInstance } from '../types';

// Task Instance Service Types
export interface TaskInstanceQuery {
  page?: number;
  size?: number;
  state?: string;
}

export interface TaskInstanceActionResponse {
  status: 'success' | 'error';
  message: string;
  details?: {
    taskId: string;
    state: string;
    timestamp: string;
  };
}

// Service for Task Instance operations
const taskInstanceService = {
  getTaskInstances: (dagId: string, dagRunId: string, params?: TaskInstanceQuery) => 
    getApiClient().get(`/dags/${dagId}/dagRuns/${dagRunId}/taskInstances`, { params })
      .then(response => response.data),
  
  getTaskInstance: (dagId: string, dagRunId: string, taskId: string) => 
    getApiClient().get(`/dags/${dagId}/dagRuns/${dagRunId}/taskInstances/${taskId}`)
      .then(response => response.data),
  
  setTaskInstanceState: (dagId: string, dagRunId: string, taskId: string, state: string) => {
    const payload = { state };
    return getApiClient().post(
      `/dags/${dagId}/dagRuns/${dagRunId}/taskInstances/${taskId}/updateState`,
      payload
    );
  },
  
  clearTaskInstance: (dagId: string, dagRunId: string, taskId: string) =>
    getApiClient().post(`/dags/${dagId}/dagRuns/${dagRunId}/taskInstances/${taskId}/clear`),
  
  getTaskLogs: (dagId: string, dagRunId: string, taskId: string, tryNumber: number) =>
    getApiClient().get(`/logs/${dagId}/dagRuns/${dagRunId}/taskInstances/${taskId}`, {
      params: { tryNumber },
      transformResponse: [(data) => data] // Keep as text
    }).then(response => response.data)
};

export default taskInstanceService; 