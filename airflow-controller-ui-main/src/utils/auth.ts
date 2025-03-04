import { Base64 } from 'js-base64';

const AUTH_KEY = 'airflow_auth';

export interface AuthCredentials {
  username: string;
  password: string;
  serverUrl: string;
}

export const saveCredentials = (credentials: AuthCredentials): void => {
  try {
    localStorage.setItem(AUTH_KEY, JSON.stringify(credentials));
  } catch (error) {
    console.error('Error saving credentials to localStorage:', error);
  }
};

export const getCredentials = (): AuthCredentials | null => {
  try {
    const stored = localStorage.getItem(AUTH_KEY);
    return stored ? JSON.parse(stored) : null;
  } catch (error) {
    console.error('Error retrieving credentials from localStorage:', error);
    return null;
  }
};

export const clearCredentials = (): void => {
  localStorage.removeItem(AUTH_KEY);
};

export const isAuthenticated = (): boolean => {
  return getCredentials() !== null;
};

export const getAuthHeader = (): string | null => {
  const credentials = getCredentials();
  if (!credentials) return null;
  
  const { username, password } = credentials;
  return `Basic ${Base64.encode(`${username}:${password}`)}`;
};

export const getServerUrl = (): string => {
  const credentials = getCredentials();
  return credentials?.serverUrl || 'http://localhost:8008';
}; 