package com.example.wtow_transfer.controller;

import com.example.wtow_transfer.dto.LoginRequest;
import com.example.wtow_transfer.service.AuthService;
import com.example.wtow_transfer.service.SecurityContextService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextService securityCtxService;

    public AuthController(AuthService authService, SecurityContextService securityCtxService) {
        this.authService = authService;
        this.securityCtxService = securityCtxService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, UUID>> login(@Valid @RequestBody LoginRequest loginRequest,
                                         HttpSession httpSession) {
        UUID userId = authService.authenticate(loginRequest.email(), loginRequest.password());
        SecurityContext securityContext = securityCtxService.createSecurityContext(userId);
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        return ResponseEntity.ok().body(Map.of("userId", userId));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        securityCtxService.clearSecurityContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
