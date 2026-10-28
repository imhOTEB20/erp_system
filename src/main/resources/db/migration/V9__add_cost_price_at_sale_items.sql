-- 1. Agregar la columna permitiendo valores NULL temporalmente
ALTER TABLE sale_items
ADD COLUMN cost_price NUMERIC(14, 2);

-- 2. Copiar los valores desde la tabla product usando la FK product_id
UPDATE sale_items si
SET cost_price = p.cost_price
FROM products p
WHERE si.product_id = p.id;

-- 3. Aplicar la restricción NOT NULL ahora que todos los registros tienen datos
ALTER TABLE sale_items
ALTER COLUMN cost_price SET NOT NULL;
