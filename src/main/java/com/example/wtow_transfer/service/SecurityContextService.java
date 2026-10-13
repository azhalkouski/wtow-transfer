package com.example.wtow_transfer.service;

import com.example.wtow_transfer.exception.NotAuthenticatedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SecurityContextService {

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

    public UUID getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new NotAuthenticatedException();
        }
        return (UUID) auth.getPrincipal();
    }
}
