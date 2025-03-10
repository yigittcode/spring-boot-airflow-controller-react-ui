import { Base64 } from 'js-base64';

const AUTH_KEY = 'airflow_auth';

export interface AuthCredentials {
  username: string;
  password: string;
  serverUrl: string;
  token?: string;
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