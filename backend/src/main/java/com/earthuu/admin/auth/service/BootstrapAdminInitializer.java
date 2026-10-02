package com.earthuu.admin.auth.service;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.global.config.BootstrapAdminProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final BootstrapAdminProperties properties;
    private final AdminUserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminInitializer(
            BootstrapAdminProperties properties,
            AdminUserRepository userRepository,
            PasswordCredentialRepository credentialRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.password())) {
            throw new IllegalStateException("Bootstrap admin email and password are required when enabled");
        }
        if (credentialRepository.existsByLoginEmailIgnoreCase(properties.email())) {
            return;
        }

        var admin = userRepository.save(AdminUser.createAdmin());
        credentialRepository.save(PasswordCredential.create(
                admin.getId(),
                properties.email(),
                passwordEncoder.encode(properties.password())
        ));
    }
}
