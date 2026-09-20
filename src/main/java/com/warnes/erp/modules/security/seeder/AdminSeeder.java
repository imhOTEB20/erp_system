package com.warnes.erp.modules.security.seeder;

import com.warnes.erp.modules.security.entity.Role;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Solo inyectamos el administrador si la tabla de usuarios está completamente vacía
        if (userRepository.count() == 0) {
            log.info("Iniciando inyección de datos semilla (Seed)...");

            User admin = new User();
            admin.setUsername("admin");
            // En un entorno de desarrollo está bien; en producción el password inicial vendría por variable de entorno
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrador del Sistema");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);

            log.info("Usuario administrador creado por defecto -> Username: 'admin' | Password: 'admin123'");
        } else {
            log.info("Base de datos ya inicializada. Omitiendo Seeder.");
        }
    }
}