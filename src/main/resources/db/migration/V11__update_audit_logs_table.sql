-- V11: Update audit_logs table schema
-- Target columns:
--   action      : the action performed
--   description : human-readable explanation
--   created_at  : when the action happened

-- 1. Create table audit_logs if not exists
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    action      VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- 2. If table was created earlier with details/performed_at, migrate columns
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'audit_logs' AND column_name = 'details'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'audit_logs' AND column_name = 'description'
        ) THEN
            ALTER TABLE audit_logs RENAME COLUMN details TO description;
        ELSE
            UPDATE audit_logs SET description = details WHERE description IS NULL;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'audit_logs' AND column_name = 'performed_at'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'audit_logs' AND column_name = 'created_at'
        ) THEN
            ALTER TABLE audit_logs RENAME COLUMN performed_at TO created_at;
        ELSE
            UPDATE audit_logs SET created_at = performed_at WHERE created_at IS NULL;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'audit_logs' AND column_name = 'admin_id'
    ) THEN
        ALTER TABLE audit_logs ALTER COLUMN admin_id DROP NOT NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_audit_logs_action     ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
