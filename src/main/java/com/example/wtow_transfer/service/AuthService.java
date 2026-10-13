package com.example.wtow_transfer.service;

import com.example.wtow_transfer.exception.AuthenticationException;
import com.example.wtow_transfer.exception.NotAuthenticatedException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    @PersistenceContext(unitName = "userManager")
    private EntityManager userManagerEntityManager;

    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional("userManagerTransactionManager")
    public UUID authenticate(String email, String password) {
        Object[] result = (Object[]) userManagerEntityManager.createNativeQuery(
                    "SELECT id, password_hash FROM core.get_user_for_auth(:pEmail)"
                )
                .setParameter("pEmail", email)
                .getSingleResultOrNull();

        if (result == null || !passwordEncoder.matches(password, (String) result[1])) {
            throw new AuthenticationException(email);
        }

        return (UUID) result[0];
    }

    public UUID getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new NotAuthenticatedException();
        }
        return (UUID) auth.getPrincipal();
    }

    public SecurityContext createSecurityContext(UUID userId) {
        var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
        return securityContext;
    }

    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
