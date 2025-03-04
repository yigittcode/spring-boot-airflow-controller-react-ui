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
 * Extracts a human-readable error message from various API error response formats
 * @param error The error object from an API call
 * @returns A user-friendly error message string
 */
export const extractErrorMessage = (error: any): string => {
  // Handle axios error response with our API error structure
  if (error.response?.data) {
    const data = error.response.data;
    
    // Handle structured error format
    if (data.error?.detail) {
      return data.error.detail;
    }
    
    // Handle simple detail field
    if (data.detail) {
      return data.detail;
    }
    
    // Handle message field
    if (data.message) {
      return data.message;
    }
  }
  
  // Handle direct error message
  if (error.message) {
    return error.message;
  }
  
  // Default fallback
  return 'An unexpected error occurred';
};

/**
 * Logs API errors to console with consistent formatting
 * @param error The error object to log
 * @param context Optional context information (e.g., the operation that failed)
 */
export const logApiError = (error: any, context?: string): void => {
  const contextPrefix = context ? `[${context}] ` : '';
  
  if (error.response?.data) {
    console.error(`${contextPrefix}API Error:`, error.response.data);
  } else {
    console.error(`${contextPrefix}Error:`, error);
  }
}; 