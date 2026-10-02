package com.earthuu.admin.auth.security;

import com.earthuu.admin.auth.entity.UserRole;
import com.earthuu.admin.auth.entity.UserStatus;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final PasswordCredentialRepository credentialRepository;
    private final AdminUserRepository userRepository;

    public AdminUserDetailsService(
            PasswordCredentialRepository credentialRepository,
            AdminUserRepository userRepository
    ) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var credential = credentialRepository.findByLoginEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
        var user = userRepository.findById(credential.getUserId())
                .filter(found -> found.getRole() == UserRole.ADMIN)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        return User.withUsername(credential.getLoginEmail())
                .password(credential.getPasswordHash())
                .roles("ADMIN")
                .disabled(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }
}
