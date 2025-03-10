import { getApiClient } from '../utils/apiClient';
import { AxiosResponse } from 'axios';
import { DagActionLog, DagActionLogResponse } from '../types';

/**
 * Get task logs
 */
export const getTaskLogs = async (dagId: string, dagRunId: string, taskId: string, tryNumber: number = 1): Promise<string> => {
  try {
    const response = await getApiClient().get(`/v1/logs/${dagId}/dagRuns/${dagRunId}/taskInstances/${taskId}`, {
      params: { tryNumber }
    });
    return typeof response.data === 'string' ? response.data : 'Log data is not available or in an unexpected format.';
  } catch (error) {
    console.error('Error fetching task logs:', error);
    return 'Failed to fetch task logs. Please try again later.';
  }
};

/**
 * Get all DAG action logs with pagination
 */
export const getDagActionLogs = async (page: number = 0, size: number = 20): Promise<DagActionLogResponse> => {
  try {
    const response = await getApiClient().get('/logs/dag-actions', {
      params: { page, size }
    });
    
    // Default empty response if data is not in expected format
    if (!response.data || typeof response.data !== 'object') {
      return { logs: [], totalCount: 0, page, size };
    }
    
    return response.data;
  } catch (error) {
    console.error('Error fetching DAG action logs:', error);
    return { logs: [], totalCount: 0, page, size };
  }
};

/**
 * Get DAG action logs for a specific DAG
 */
export const getDagActionLogsByDagId = async (dagId: string): Promise<DagActionLog[]> => {
  try {
    const response = await getApiClient().get(`/logs/dag-actions/dag/${dagId}`);
    // Ensure we return an array, handle possible formats from reactive backend
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error('Error fetching DAG action logs by DAG ID:', error);
    return [];
  }
};

/**
 * Get DAG action logs by action type
 */
export const getDagActionLogsByType = async (actionType: string): Promise<DagActionLog[]> => {
  try {
    const response = await getApiClient().get(`/logs/dag-actions/type/${actionType}`);
    // Ensure we return an array, handle possible formats from reactive backend
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error('Error fetching DAG action logs by action type:', error);
    return [];
  }
};