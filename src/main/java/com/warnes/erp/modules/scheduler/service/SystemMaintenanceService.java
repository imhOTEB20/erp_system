package com.warnes.erp.modules.scheduler.service;

public interface SystemMaintenanceService {
    void cancelAbandonedSales();
    void autoCloseForgottenCashShifts();
}