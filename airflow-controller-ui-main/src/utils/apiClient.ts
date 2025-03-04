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
    baseURL: `${credentials.serverUrl}/api/v1`,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': getAuthHeader() || ''
    }
  });
};

// Lazily initialize API client when needed
let apiClient: AxiosInstance | null = null;

// Get or create an API client instance
export const getApiClient = (): AxiosInstance => {
  if (!apiClient) {
    try {
      apiClient = createApiClient();
      setupInterceptors();
    } catch (error) {
      console.error('Failed to create API client:', error);
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
        console.warn('Unauthorized request detected. Redirecting to login...');
        window.location.href = '/';
      }
      
      return Promise.reject(error);
    }
  );
  return client;
};

// Reset the API client (useful after login/logout)
export const resetApiClient = (): void => {
  apiClient = null;
};

// Default export for backward compatibility
export default getApiClient;