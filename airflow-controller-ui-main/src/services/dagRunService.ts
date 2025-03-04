import { getApiClient } from '../utils/apiClient';

// DagRun Query Parameters
export interface DagRunQueryParams {
  page?: number;
  size?: number;
  state?: string;
  startDate?: string;
  endDate?: string;
  orderBy?: string;
}

// DagRun Create Parameters
export interface DagRunCreateParams {
  runId: string;
  logicalDate: string;
  conf?: {
    message: string;
    parameters?: Record<string, any>;
  };
  note?: string;
  state?: string;
}

// DagRun Clear Parameters
export interface DagRunClearParams {
  dry_run?: boolean;
  reset_dag_runs?: boolean;
  only_failed?: boolean;
  only_running?: boolean;
  include_subdags?: boolean;
  include_parentdag?: boolean;
}

// Service for DagRun operations
const dagRunService = {
  getDagRuns: (dagId: string, params?: DagRunQueryParams) => 
    getApiClient().get(`/dags/${dagId}/dagRuns`, { params }),
  
  getDagRun: (dagId: string, dagRunId: string) => 
    getApiClient().get(`/dags/${dagId}/dagRuns/${dagRunId}`),
  
  createDagRun: (dagId: string, params: DagRunCreateParams) => {
    // Format date to ISO without milliseconds
    const formattedDate = new Date(params.logicalDate)
      .toISOString()
      .replace(/\.\d{3}/, '');
    
    // Build request payload
    const payload = {
      dag_run_id: params.runId,
      logical_date: formattedDate,
      conf: params.conf || { message: "Triggered from UI" },
      external_trigger: true,
      ...(params.note && { note: params.note }),
      ...(params.state && { state: params.state })
    };
    
    return getApiClient().post(`/dags/${dagId}/dagRuns`, payload);
  },
  
  updateDagRunState: (dagId: string, dagRunId: string, state: string) =>
    getApiClient().patch(`/dags/${dagId}/dagRuns/${dagRunId}`, { state }),
  
  clearDagRun: (dagId: string, dagRunId: string, params: DagRunClearParams = { dry_run: false }) =>
    getApiClient().post(`/dags/${dagId}/dagRuns/${dagRunId}/clear`, params),
  
  deleteDagRun: (dagId: string, dagRunId: string) =>
    getApiClient().delete(`/dags/${dagId}/dagRuns/${dagRunId}`),
  
  setDagRunNote: (dagId: string, dagRunId: string, note: string) =>
    getApiClient().patch(`/dags/${dagId}/dagRuns/${dagRunId}/setNote`, { note }),
  
  retryFailedTasks: (dagId: string, dagRunId: string) =>
    getApiClient().post(`/dags/${dagId}/dagRuns/${dagRunId}/retryFailed`),
  
  getDagRunTaskGroups: (dagId: string, dagRunId: string) =>
    getApiClient().get(`/dags/${dagId}/dagRuns/${dagRunId}/taskGroups`)
};

export default dagRunService; 