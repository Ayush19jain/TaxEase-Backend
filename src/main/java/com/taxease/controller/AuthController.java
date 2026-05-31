package com.taxease.controller;

import com.taxease.dto.AuthDTO;
import com.taxease.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<AuthDTO.AuthResponse> signup(@RequestBody AuthDTO.SignupRequest request) {
        AuthDTO.AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.AuthResponse> login(@RequestBody AuthDTO.LoginRequest request) {
        AuthDTO.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<AuthDTO.UserDTO> getMe(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        AuthDTO.UserDTO user = authService.getMe(userId);
        return ResponseEntity.ok(user);
    }
}
