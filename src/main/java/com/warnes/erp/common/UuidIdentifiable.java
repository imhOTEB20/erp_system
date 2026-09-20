package com.warnes.erp.common;

import java.util.UUID;

/**
 * Interfaz para estandarizar el acceso al UUID en las entidades que lo implementan.
 * Esto será útil si luego queremos crear clases genéricas (ej. un BaseService)
 * que reciba un UUID para realizar búsquedas sin conocer la entidad concreta.
 */
public interface UuidIdentifiable {
    UUID getUuid();
    void setUuid(UUID uuid);
}