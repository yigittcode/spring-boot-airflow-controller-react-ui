import { Base64 } from 'js-base64';

const AUTH_KEY = 'airflow_auth';

export interface AuthCredentials {
  username: string;
  password: string;
  serverUrl: string;
  token?: string;
  role?: string;
}

export const saveCredentials = (credentials: AuthCredentials): void => {
  try {
    console.log('--- SAVING CREDENTIALS ---');
    console.log('Username:', credentials.username);
    console.log('Token present:', !!credentials.token);
    console.log('------------------------');
    localStorage.setItem(AUTH_KEY, JSON.stringify(credentials));
  } catch (error) {
    console.error('Error saving credentials to localStorage:', error);
  }
};

export const getCredentials = (): AuthCredentials | null => {
  try {
    const stored = localStorage.getItem(AUTH_KEY);
    const credentials = stored ? JSON.parse(stored) : null;
    
    // Only log at important points to avoid console spam
    // console.log('Retrieved credentials for user:', credentials?.username);
    
    return credentials;
  } catch (error) {
    console.error('Error retrieving credentials from localStorage:', error);
    return null;
  }
};

export const clearCredentials = (): void => {
  console.log('Clearing user credentials');
  // First get current credentials for logging
  const currentCreds = getCredentials();
  if (currentCreds) {
    console.log(`Removing credentials for user: ${currentCreds.username}`);
  }
  
  // Remove from localStorage
  localStorage.removeItem(AUTH_KEY);
  
  // Verify credentials are cleared
  const afterClear = getCredentials();
  if (afterClear) {
    console.error('Failed to clear credentials!', afterClear);
  } else {
    console.log('Credentials successfully cleared');
  }
};

export const isAuthenticated = (): boolean => {
  return getCredentials() !== null;
};

export const getAuthHeader = (): string | null => {
  const credentials = getCredentials();
  if (!credentials) return null;
  
  // Create and log the authentication header
  let authHeader: string;
  
  // If a JWT token is available, use it
  if (credentials.token) {
    authHeader = `Bearer ${credentials.token}`;
    console.log('Using Bearer token authentication for user:', credentials.username);
  } else {
    // Fallback to basic auth (for older implementation)
    const { username, password } = credentials;
    authHeader = `Basic ${Base64.encode(`${username}:${password}`)}`;
    console.log('Using Basic authentication for user:', username);
  }
  
  return authHeader;
};

export const getServerUrl = (): string => {
  const credentials = getCredentials();
  return credentials?.serverUrl || 'http://localhost:8008';
};

/**
 * Get the role of the current user
 */
export const getUserRole = (): string | null => {
  const credentials = getCredentials();
  return credentials?.role || null;
};

/**
 * Check if the current user has any of the specified roles
 */
export const hasAnyRole = (roles: string[]): boolean => {
  const userRole = getUserRole();
  return userRole !== null && roles.includes(userRole);
};

/**
 * Role hierarchy structure based on Apache Airflow RBAC
 * Each role inherits permissions from roles below it
 */
// Define a type for the valid role keys
type RoleType = 'ADMIN' | 'OP' | 'USER' | 'VIEWER' | 'PUBLIC';

const ROLE_HIERARCHY: Record<RoleType, string[]> = {
  'ADMIN': ['ADMIN', 'OP', 'USER', 'VIEWER', 'PUBLIC'],
  'OP': ['OP', 'USER', 'VIEWER', 'PUBLIC'],
  'USER': ['USER', 'VIEWER', 'PUBLIC'],
  'VIEWER': ['VIEWER', 'PUBLIC'],
  'PUBLIC': ['PUBLIC']
};

/**
 * Check if user has a role that has the specified permission
 * Takes into account the role hierarchy
 */
export const hasPermission = (requiredRole: string): boolean => {
  const userRole = getUserRole();
  if (!userRole) return false;
  
  // Get all roles that the user effectively has based on hierarchy
  // Check if userRole is a valid key in ROLE_HIERARCHY
  const effectiveRoles = userRole in ROLE_HIERARCHY 
    ? ROLE_HIERARCHY[userRole as RoleType] 
    : [];
  return effectiveRoles.includes(requiredRole);
};

/**
 * Check if the current user is an admin
 */
export const isAdmin = (): boolean => {
  return getUserRole() === 'ADMIN';
};

/**
 * Check if the current user can modify DAGs (ADMIN or OP)
 */
export const canModifyDags = (): boolean => {
  return hasPermission('OP');
};

/**
 * Check if the current user can run DAGs (ADMIN, OP, or USER)
 */
export const canRunDags = (): boolean => {
  return hasPermission('USER');
};

/**
 * Check if the current user can view DAGs (all authenticated users)
 */
export const canViewDags = (): boolean => {
  return hasPermission('VIEWER');
};

/**
 * Check if user can control task instances (ADMIN, OP, or USER)
 */
export const canControlTasks = (): boolean => {
  return hasPermission('USER');
};

/**
 * Check if user can view task instance logs (currently requires ADMIN role)
 * Update this function if logs permissions change in the backend
 */
export const canViewTaskLogs = (): boolean => {
  // Currently only ADMIN users can view logs based on the issue description
  return isAdmin();
};

/**
 * Format user role for display
 */
export const formatUserRole = (role: string | null | undefined): string => {
  if (!role) return 'Unknown';
  
  // Capitalize role and add description
  switch (role) {
    case 'ADMIN':
      return 'Admin (Full Access)';
    case 'OP':
      return 'Operator (DAG Manager)';
    case 'USER':
      return 'User (DAG Runner)';
    case 'VIEWER':
      return 'Viewer (Read Only)';
    case 'PUBLIC':
      return 'Public (Limited)';
    default:
      return role;
  }
}; 