package com.example.wtow_transfer.service;

import com.example.wtow_transfer.dto.UserDto;
import com.example.wtow_transfer.exception.AuthenticationException;
import com.example.wtow_transfer.exception.NotAuthenticatedException;
import com.example.wtow_transfer.jpa.repository.usermanager.UserRepository;
import com.example.wtow_transfer.mapper.UserMapper;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional("userManagerTransactionManager")
    public UserDto authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .map(UserMapper::toDto)
                .orElseThrow(() -> new AuthenticationException(email));
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
