-- =================================================================================
-- V3: Auditoría y trazabilidad en Módulo de Cajas
-- =================================================================================

-- 1. Trazabilidad de quién cerró el turno (Puede ser el cajero, un admin o el CronJob)
ALTER TABLE cash_shifts
ADD COLUMN closed_by_user_id BIGINT NULL;

ALTER TABLE cash_shifts
ADD CONSTRAINT fk_cash_shifts_closed_by
FOREIGN KEY (closed_by_user_id) REFERENCES users (id) ON DELETE RESTRICT;

-- 2. Trazabilidad de quién ejecuta un movimiento (Fundamental para SYSTEM_ADJUSTMENT)
ALTER TABLE cash_movements
ADD COLUMN user_id BIGINT NULL;

ALTER TABLE cash_movements
ADD CONSTRAINT fk_cash_movements_user
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;