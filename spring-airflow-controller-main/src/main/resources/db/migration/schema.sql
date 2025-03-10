CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(50) NOT NULL,
    airflow_username VARCHAR(50),
    airflow_password VARCHAR(255)
); 

CREATE TABLE IF NOT EXISTS dag_action_logs (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    dag_id VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_details TEXT,
    timestamp TIMESTAMP NOT NULL,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    run_id VARCHAR(255)
); 