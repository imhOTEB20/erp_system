-- V2__add_active_column_to_products.sql
-- Agrega la columna 'active' a la tabla products

ALTER TABLE price_lists
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;