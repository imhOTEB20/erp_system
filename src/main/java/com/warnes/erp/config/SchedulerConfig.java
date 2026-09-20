package com.warnes.erp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Spring Boot configurará automáticamente un ThreadPoolTaskScheduler por defecto.
    // Para este ERP, el pool por defecto es suficiente.
}