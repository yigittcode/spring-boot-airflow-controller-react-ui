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