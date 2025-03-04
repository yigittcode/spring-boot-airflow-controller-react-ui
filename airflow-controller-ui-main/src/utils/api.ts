import { getDagService, getDagRunService, getTaskInstanceService } from '../services';
import logService from '../services/logService'; // Create if not already created

// Service accessors
export const getLogService = () => logService;

// Service accessors for backward compatibility
export {
  getDagService,
  getDagRunService,
  getTaskInstanceService
};