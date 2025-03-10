import axios, { AxiosInstance } from 'axios';
import { logApiError } from './errorHandling';
import { getAuthHeader, getCredentials, isAuthenticated } from './auth';

// Create an axios instance with default configuration
const createApiClient = (): AxiosInstance => {
  // First, check if the user is authenticated
  if (!isAuthenticated()) {
    throw new Error('User not authenticated. Please log in first.');
  }
  
  const credentials = getCredentials();
  
  if (!credentials) {
    throw new Error('No credentials available. Please log in again.');
  }
  
  return axios.create({
    baseURL: `${credentials.serverUrl}/api`,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': getAuthHeader() || ''
    }
  });
};

// Lazily initialize API client when needed
let apiClient: AxiosInstance | null = null;

// Store the username that the API client was created for
let apiClientUser: string | null = null;

// Get or create an API client instance
export const getApiClient = (): AxiosInstance => {
  // Force recreation of the API client if active user changes
  const currentUserCredentials = getCredentials();
  const currentUser = currentUserCredentials?.username || null;
  
  if (apiClient && apiClientUser !== currentUser) {
    apiClient = null;
    apiClientUser = null;
  }
  
  if (!apiClient) {
    try {
      apiClient = createApiClient();
      apiClientUser = currentUser;
      setupInterceptors();
    } catch (error) {
      // If we are not on the login page, redirect the user to the login page
      if (window.location.pathname !== '/login' && window.location.pathname !== '/') {
        window.location.href = '/';
      }
      // Return a simple dummy axios instance so that the application does not crash
      return axios.create({
        baseURL: '',
        validateStatus: () => false // will always fail
      });
    }
  }
  return apiClient;
};

// Add response interceptor to the client
const setupInterceptors = () => {
  const client = apiClient as AxiosInstance;
  client.interceptors.response.use(
    response => response,
    error => {
      logApiError(error, 'API Request');
      
      // In case of 401 Unauthorized error, terminate the session and redirect to the login page
      if (error.response && error.response.status === 401) {
        window.location.href = '/';
      }
      
      // For 403 Forbidden errors, add user-friendly messaging
      if (error.response && error.response.status === 403) {
        // Get current role for better debugging
        const creds = getCredentials();
        
        // We add custom info to the error object for our components to use
        error.isPermissionError = true;
        error.permissionMessage = "You don't have sufficient permissions to access this resource.";
      }
      
      return Promise.reject(error);
    }
  );
  return client;
};

// Reset the API client (useful after login/logout)
export const resetApiClient = (): void => {
  apiClient = null;
  apiClientUser = null;
};

// Default export for backward compatibility
export default getApiClient;