CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    activity_type VARCHAR(50) NOT NULL,
    module VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    reference_uuid UUID NULL,
    reference_code VARCHAR(80) NULL,
    amount NUMERIC(19, 2) NULL,
    warehouse_id BIGINT NULL REFERENCES warehouses(id),
    target_warehouse_id BIGINT NULL REFERENCES warehouses(id),
    customer_id BIGINT NULL REFERENCES customers(id),
    cash_register_id BIGINT NULL REFERENCES cash_registers(id),
    performed_by VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NULL,
    deleted_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_activity_logs_uuid ON activity_logs(uuid);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_activity_logs_warehouse ON activity_logs(warehouse_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_activity_logs_customer ON activity_logs(customer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_activity_logs_cash_register ON activity_logs(cash_register_id) WHERE deleted_at IS NULL;