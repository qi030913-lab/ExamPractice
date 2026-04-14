CREATE TABLE IF NOT EXISTS auth_session (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id INT NOT NULL,
    role VARCHAR(32) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_auth_session_expires_at (expires_at),
    INDEX idx_auth_session_issued_at (issued_at),
    INDEX idx_auth_session_user_role (user_id, role)
);
