package com.yigit.airflow_spring_rest_controller.entity;

/**
 * Apache Airflow RBAC (Role Based Access Control) Roles
 * 
 * Based on official Airflow documentation:
 * https://airflow.apache.org/docs/apache-airflow/stable/security/access-control.html
 * 
 * Admin: Full system access, can manage users, roles, connections, etc.
 * Op: Can manage DAGs, trigger runs, clear tasks, but cannot manage users, roles, etc.
 * User: Can view all DAGs and run them, but cannot edit DAGs or access configurations
 * Viewer: Can only view DAGs, cannot run or modify them
 * Public: Limited access to public interfaces
 */
public enum Role {
    ADMIN,       // Full system access
    OP,          // Operator role - Can manage DAGs and trigger runs
    USER,        // User role - Can view and run DAGs, but not edit
    VIEWER,      // Viewer role - Can only view DAGs, no execution
    PUBLIC       // Public role - Limited access
} 