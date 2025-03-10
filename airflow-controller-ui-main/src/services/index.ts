import dagService from './dagService';
import dagRunService from './dagRunService';
import taskInstanceService from './taskInstanceService';
import * as logServiceExports from './logService';

// Service accessor functions
export const getDagService = () => dagService;
export const getDagRunService = () => dagRunService;
export const getTaskInstanceService = () => taskInstanceService;
export const getLogService = () => ({
  getTaskLogs: logServiceExports.getTaskLogs,
  getDagActionLogs: logServiceExports.getDagActionLogs,
  getDagActionLogsByDagId: logServiceExports.getDagActionLogsByDagId,
  getDagActionLogsByType: logServiceExports.getDagActionLogsByType
});

export { dagService, dagRunService, taskInstanceService };
export * from './logService';

// Re-export from the other services if needed
export * from './dagService';
export * from './dagRunService';
export * from './taskInstanceService'; 