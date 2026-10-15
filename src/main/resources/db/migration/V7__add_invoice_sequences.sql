-- Secuencia para Facturas A (Ventas B2B / Responsables Inscriptos)
CREATE SEQUENCE seq_invoice_type_a START 1 INCREMENT 1;

-- Secuencia para Facturas B (Consumidor Final / Monotributistas)
CREATE SEQUENCE seq_invoice_type_b START 1 INCREMENT 1;

-- Opcional: Si manejas Notas de Crédito, también necesitan su secuencia
CREATE SEQUENCE seq_credit_note_type_a START 1 INCREMENT 1;
CREATE SEQUENCE seq_credit_note_type_b START 1 INCREMENT 1;