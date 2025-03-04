import dagService from './dagService';
import dagRunService from './dagRunService';
import taskInstanceService from './taskInstanceService';

// Service accessor functions
export const getDagService = () => dagService;
export const getDagRunService = () => dagRunService;
export const getTaskInstanceService = () => taskInstanceService;

export { dagService, dagRunService, taskInstanceService }; 