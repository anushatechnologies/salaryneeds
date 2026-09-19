-- V9: Create AUDIT_LOGS table

CREATE TABLE IF NOT EXISTS AUDIT_LOGS (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id     BINARY(16)   NOT NULL,
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(100),
    entity_id    VARCHAR(100),
    details      TEXT,
    ip_address   VARCHAR(50),
    performed_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_admin FOREIGN KEY (admin_id) REFERENCES ADMINS(id) ON DELETE RESTRICT
);

CREATE INDEX idx_audit_log_admin_id     ON AUDIT_LOGS(admin_id);
CREATE INDEX idx_audit_log_action       ON AUDIT_LOGS(action);
CREATE INDEX idx_audit_log_performed_at ON AUDIT_LOGS(performed_at);
