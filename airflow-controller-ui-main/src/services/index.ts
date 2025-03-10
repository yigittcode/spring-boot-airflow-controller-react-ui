import dagService from './dagService';
import dagRunService from './dagRunService';
import taskInstanceService from './taskInstanceService';
import * as logServiceExports from './logService';

/**
 * Service accessor functions - get singleton instances
 * These are the preferred way to access services
 */
export const getDagService = () => dagService;
export const getDagRunService = () => dagRunService;
export const getTaskInstanceService = () => taskInstanceService;
export const getLogService = () => ({
  getTaskLogs: logServiceExports.getTaskLogs,
  getDagActionLogs: logServiceExports.getDagActionLogs,
  getDagActionLogsByDagId: logServiceExports.getDagActionLogsByDagId,
  getDagActionLogsByType: logServiceExports.getDagActionLogsByType
});

// Direct exports (for backwards compatibility)
export { dagService, dagRunService, taskInstanceService };

// Re-export types and interfaces from each service
export * from './logService';
export * from './dagService';
export * from './dagRunService';
export * from './taskInstanceService'; 