package com.warnes.erp.modules.reporting.entity;

import java.time.ZonedDateTime;

public record PeriodKey(
        ZonedDateTime start,
        ZonedDateTime end
) {
}
