/**
 * Unified error handling utilities for the Airflow Controller UI
 */

// Standard error response structure from the backend
export interface ApiErrorResponse {
  error?: {
    status: number;
    title: string;
    type: string;
    detail: string;
  };
  detail?: string;
  message?: string;
  timestamp?: string;
  path?: string;
}

/**
 * Extracts a user-friendly error message from API errors
 */
export const extractErrorMessage = (error: any): string => {
  // If it's an axios error response
  if (error.response) {
    // Check for specific status codes and provide meaningful messages
    switch (error.response.status) {
      case 401:
        return 'Authentication failed. Please log in again.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'The requested resource was not found.';
      case 500:
        return 'Server error occurred. Please try again later.';
      default:
        // Try to extract error message from response body
        if (error.response.data) {
          if (typeof error.response.data === 'string') {
            return error.response.data;
          }
          if (error.response.data.message) {
            return error.response.data.message;
          }
          if (error.response.data.error) {
            return error.response.data.error;
          }
        }
        return `Error ${error.response.status}: ${error.response.statusText || 'Unknown error'}`;
    }
  }
  
  // Network errors
  if (error.request && !error.response) {
    return 'Network error. Please check your connection.';
  }
  
  // Other errors
  return error.message || 'An unknown error occurred';
};

/**
 * Logs API errors to console with context
 */
export const logApiError = (error: any, context: string = 'API Request'): void => {
  // Create a structured error message for consistent logging
  const errorDetails = {
    message: error.response?.data?.message || error.message || 'Unknown error',
    status: error.response?.status,
    statusText: error.response?.statusText,
    data: error.response?.data || null,
    url: error.config?.url || null
  };

  console.error(`[${context}] API Error:`, errorDetails);
}; 