package com.example.wtow_transfer.service;

import com.example.wtow_transfer.exception.AuthenticationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
