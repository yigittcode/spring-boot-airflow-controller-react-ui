import { getApiClient } from '../utils/apiClient';

const logService = {
  getTaskLogs: (dagId: string, dagRunId: string, taskId: string, tryNumber: number = 1) =>
    getApiClient().get(`/logs/${dagId}/dagRuns/${dagRunId}/taskInstances/${taskId}`, {
      params: { tryNumber },
      transformResponse: [(data) => data] // Keep as text
    }).then(response => response.data)
};

export default logService;