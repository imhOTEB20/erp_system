-- Tabla para configurar tasas de inter s din micas sin redesplegar
CREATE TABLE payment_method_surcharges (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    payment_method VARCHAR(30) NOT NULL,
    installments INTEGER NULL, -- NULL = 1 pago / d bito
    rate_percentage NUMERIC(6, 4) NOT NULL DEFAULT 0.0000,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_method_installments UNIQUE (payment_method, installments)
);

-- Ampliaci n de la tabla sale_payments para soportar facturaci n AFIP y auditor a de Posnet
ALTER TABLE sale_payments
ADD COLUMN installments INTEGER DEFAULT 1,
ADD COLUMN applied_rate NUMERIC(6, 4) DEFAULT 0.0000,
ADD COLUMN surcharge_amount NUMERIC(14, 2) DEFAULT 0.00,
ADD COLUMN surcharge_tax_amount NUMERIC(14, 2) DEFAULT 0.00, -- IVA 21% exclusivo sobre el inter s
ADD COLUMN total_billed NUMERIC(14, 2) NOT NULL DEFAULT 0.00, -- Lo que realmente pas  por la tarjeta
ADD COLUMN authorization_code VARCHAR(50) NULL,
ADD COLUMN posnet_transaction_id VARCHAR(100) NULL;

-- Aseguramos que los registros hist ricos (V1) tengan el total_billed igual al amount (deuda saldada)
UPDATE sale_payments SET total_billed = amount WHERE total_billed = 0.00;