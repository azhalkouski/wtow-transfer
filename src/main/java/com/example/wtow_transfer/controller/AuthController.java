package com.example.wtow_transfer.controller;

import com.example.wtow_transfer.dto.LoginRequest;
import com.example.wtow_transfer.dto.UserDto;
import com.example.wtow_transfer.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest loginRequest,
                                         HttpSession httpSession) {
        UserDto user = authService.authenticate(loginRequest.email(), loginRequest.password());
        SecurityContext securityContext = authService.createSecurityContext(user.id());
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.clearSecurityContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
