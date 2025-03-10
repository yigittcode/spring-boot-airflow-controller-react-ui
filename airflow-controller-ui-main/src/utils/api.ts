import { getDagService, getDagRunService, getTaskInstanceService } from '../services';
import * as logServiceExports from '../services/logService';

// Service accessors
export const getLogService = () => ({
  getTaskLogs: logServiceExports.getTaskLogs,
  getDagActionLogs: logServiceExports.getDagActionLogs,
  getDagActionLogsByDagId: logServiceExports.getDagActionLogsByDagId,
  getDagActionLogsByType: logServiceExports.getDagActionLogsByType
});

// Service accessors for backward compatibility
export {
  getDagService,
  getDagRunService,
  getTaskInstanceService
};