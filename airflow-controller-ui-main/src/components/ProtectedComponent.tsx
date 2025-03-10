import React from 'react';
import { getUserRole, hasPermission } from '../utils/auth';

interface ProtectedComponentProps {
  allowedRoles: string[];
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

/**
 * A component that only renders its children if the current user has one of the allowed roles.
 * Takes into account the Airflow RBAC role hierarchy.
 * 
 * For example, if allowedRoles includes 'USER', then 'ADMIN' and 'OP' will also have access
 * because they have all the permissions of 'USER'.
 * 
 * @param allowedRoles Array of roles that are allowed to see the content
 * @param children Content to render if user has permission
 * @param fallback Optional content to render if user lacks permission
 */
const ProtectedComponent: React.FC<ProtectedComponentProps> = ({ 
  allowedRoles, 
  children, 
  fallback = null 
}) => {
  const userRole = getUserRole();
  
  // If no roles defined, always show
  if (!allowedRoles || allowedRoles.length === 0) {
    return <>{children}</>;
  }

  // Check if user has at least one of the required roles (considering hierarchy)
  const hasAccess = allowedRoles.some(role => hasPermission(role));
  
  if (hasAccess) {
    return <>{children}</>;
  }
  
  return <>{fallback}</>;
};

export default ProtectedComponent; 