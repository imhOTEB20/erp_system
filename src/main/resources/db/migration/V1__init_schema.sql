CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ==========================================
-- FASE 1: INFRAESTRUCTURA BASE (SIN DEPENDENCIAS)
-- ==========================================

CREATE TABLE warehouses (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    type VARCHAR(20) NOT NULL DEFAULT 'STORE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL
);

-- ==========================================
-- FASE 2: SEGURIDAD (DEPENDE DE WAREHOUSES)
-- ==========================================

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    default_warehouse_id BIGINT NULL, -- CORRECCIÓN 3: Sucursal Base
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'CASHIER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT fk_users_warehouse FOREIGN KEY (default_warehouse_id) REFERENCES warehouses (id) ON DELETE SET NULL
);
CREATE INDEX idx_users_role ON users (role) WHERE deleted_at IS NULL;

-- ==========================================
-- FASE 3: CAJAS Y TURNOS (DEPENDE DE WAREHOUSES Y USERS)
-- ==========================================

CREATE TABLE cash_registers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    warehouse_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT fk_cash_registers_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id) ON DELETE RESTRICT,
    CONSTRAINT uq_warehouse_register_name UNIQUE (warehouse_id, name)
);

CREATE TABLE cash_shifts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    cash_register_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMPTZ NULL,
    opening_balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    expected_balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    reported_balance NUMERIC(14, 2) NULL,
    declared_balance NUMERIC(14, 2),
    discrepancy NUMERIC(14, 2),
    ended_at TIMESTAMPTZ,
    observations TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    CONSTRAINT fk_cash_shifts_register FOREIGN KEY (cash_register_id) REFERENCES cash_registers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_cash_shifts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE cash_movements (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cash_shift_id BIGINT NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    description VARCHAR(255) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cash_movements_shift FOREIGN KEY (cash_shift_id) REFERENCES cash_shifts (id) ON DELETE CASCADE
);

-- ==========================================
-- FASE 4: CATÁLOGOS MAESTROS (SIN DEPENDENCIAS)
-- ==========================================

CREATE TABLE categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL
);

CREATE TABLE brands (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL
);

CREATE TABLE tax_types (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name VARCHAR(50) NOT NULL,
    default_rate NUMERIC(6, 4) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL
);

-- ==========================================
-- FASE 5: PRODUCTOS (DEPENDE DE CATÁLOGOS)
-- ==========================================

CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    sku VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(64) NULL,
    name VARCHAR(200) NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    tax_type_id BIGINT NOT NULL, -- CORRECCIÓN 1: Enlace con impuesto maestro
    is_service BOOLEAN NOT NULL DEFAULT FALSE,
    is_kit BOOLEAN NOT NULL DEFAULT FALSE,
    cost_price NUMERIC(14, 4) NOT NULL DEFAULT 0.0000,
    sale_price NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id) ON DELETE RESTRICT,
    CONSTRAINT fk_products_tax FOREIGN KEY (tax_type_id) REFERENCES tax_types (id) ON DELETE RESTRICT
);
CREATE INDEX idx_products_sku ON products (sku) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_name_trgm ON products USING GIN (name gin_trgm_ops) WHERE deleted_at IS NULL;

CREATE TABLE product_cross_references (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    product_id BIGINT NOT NULL,
    external_brand VARCHAR(100) NOT NULL,
    external_sku VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_cross_ref_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE TABLE kit_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_product_id BIGINT NOT NULL,
    child_product_id BIGINT NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_kit_parent FOREIGN KEY (parent_product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_kit_child FOREIGN KEY (child_product_id) REFERENCES products (id) ON DELETE RESTRICT
);

-- ==========================================
-- FASE 6: INVENTARIO (DEPENDE DE PRODUCTOS Y USERS)
-- ==========================================

CREATE TABLE stocks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    min_stock NUMERIC(14, 2) NOT NULL DEFAULT 2.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT fk_stocks_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_stocks_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id) ON DELETE RESTRICT,
    CONSTRAINT uq_product_warehouse UNIQUE (product_id, warehouse_id)
);

CREATE TABLE stock_movements (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL CHECK (quantity > 0),
    balance_after NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    movement_type VARCHAR(30) NOT NULL,
    reference_document UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sm_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sm_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sm_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE transfers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    tracking_code VARCHAR(50) NOT NULL UNIQUE,
    origin_warehouse_id BIGINT NOT NULL,
    target_warehouse_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- CORRECCIÓN 2: Trazabilidad
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfers_origin FOREIGN KEY (origin_warehouse_id) REFERENCES warehouses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfers_target FOREIGN KEY (target_warehouse_id) REFERENCES warehouses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfers_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE transfer_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transfer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_transfer_items_transfer FOREIGN KEY (transfer_id) REFERENCES transfers (id) ON DELETE CASCADE,
    CONSTRAINT fk_transfer_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
);

-- ==========================================
-- FASE 7: COMPRAS Y VENTAS
-- ==========================================

CREATE TABLE suppliers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    legal_name VARCHAR(150) NOT NULL,
    tax_id VARCHAR(20) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL
);

CREATE TABLE purchases (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    supplier_id BIGINT NOT NULL,
    destination_warehouse_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- CORRECCIÓN 2: Trazabilidad
    invoice_number VARCHAR(50) NOT NULL,
    total_amount NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_purchases_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE price_lists (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name VARCHAR(50) NOT NULL,
    margin_percentage NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL
);

CREATE TABLE customers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    doc_number VARCHAR(20) NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    current_balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    credit_limit NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    price_list_id BIGINT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT fk_customers_price_list FOREIGN KEY (price_list_id) REFERENCES price_lists (id) ON DELETE RESTRICT
);

CREATE TABLE sales (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    cash_shift_id BIGINT NULL, -- CORRECCIÓN 4: Opcional para ventas B2B
    user_id BIGINT NOT NULL,   -- CORRECCIÓN 4: Vendedor responsable
    customer_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    total_amount NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sales_shift FOREIGN KEY (cash_shift_id) REFERENCES cash_shifts (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sales_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT
);

-- (Resto de la Fase 5: Historial de precios que faltaba)
CREATE TABLE product_price_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    product_id BIGINT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    old_cost NUMERIC(14, 4) NOT NULL,
    new_cost NUMERIC(14, 4) NOT NULL,
    old_price NUMERIC(14, 2) NOT NULL,
    new_price NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_product_price_history PRIMARY KEY (id, created_at),
    CONSTRAINT fk_price_hist_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) PARTITION BY RANGE (created_at);

CREATE TABLE product_price_history_default PARTITION OF product_price_history DEFAULT;

-- (Resto de la Fase 7: Ventas, Pagos, Cuentas Corrientes y Notas de Crédito)
CREATE TABLE sale_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(14, 2) NOT NULL,
    subtotal NUMERIC(14, 2) NOT NULL,
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
);

CREATE TABLE sale_taxes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    tax_type_id BIGINT NOT NULL,
    tax_amount NUMERIC(14, 2) NOT NULL,
    CONSTRAINT fk_st_sale FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE CASCADE,
    CONSTRAINT fk_st_tax FOREIGN KEY (tax_type_id) REFERENCES tax_types (id) ON DELETE RESTRICT
);

CREATE TABLE sale_payments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    sale_id BIGINT NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_payments_sale FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE CASCADE
);

CREATE TABLE customer_ledger (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    sale_id BIGINT NULL,
    movement_type VARCHAR(20) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    balance_after NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ledger_sale FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE RESTRICT
);

CREATE TABLE credit_notes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    credit_note_number VARCHAR(30) NOT NULL UNIQUE,
    sale_id BIGINT NOT NULL,
    customer_id BIGINT NULL,
    total_amount NUMERIC(14, 2) NOT NULL,
    refund_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cn_sale FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE RESTRICT,
    CONSTRAINT fk_cn_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT
);

CREATE TABLE credit_note_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    credit_note_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(14, 2) NOT NULL,
    subtotal NUMERIC(14, 2) NOT NULL,
    CONSTRAINT fk_cni_cn FOREIGN KEY (credit_note_id) REFERENCES credit_notes (id) ON DELETE CASCADE,
    CONSTRAINT fk_cni_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
);

CREATE TABLE purchase_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    purchase_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL CHECK (quantity > 0),
    unit_cost NUMERIC(14, 4) NOT NULL,
    CONSTRAINT fk_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
);

CREATE TABLE purchase_taxes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    purchase_id BIGINT NOT NULL,
    tax_type_id BIGINT NOT NULL,
    tax_amount NUMERIC(14, 2) NOT NULL,
    CONSTRAINT fk_pt_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_tax FOREIGN KEY (tax_type_id) REFERENCES tax_types (id) ON DELETE RESTRICT
);