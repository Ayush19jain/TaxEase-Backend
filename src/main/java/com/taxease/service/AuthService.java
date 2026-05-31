package com.taxease.service;

import com.taxease.dto.AuthDTO;
import com.taxease.model.User;
import com.taxease.repository.UserRepository;
import com.taxease.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthDTO.AuthResponse signup(AuthDTO.SignupRequest request) {
        // Check if user exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new RuntimeException("User already exists with this email");
        }
        
        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setTaxRegime(User.TaxRegime.NEW);
        
        user = userRepository.save(user);
        
        // Generate JWT
        String token = jwtUtil.generateToken(user.getId());
        
        // Build response
        AuthDTO.UserDTO userDTO = new AuthDTO.UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTaxRegime().getValue(),
                user.getPan(),
                user.getPhoneNumber()
        );
        
        return new AuthDTO.AuthResponse(true, token, userDTO);
    }
    
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        // Generate JWT
        String token = jwtUtil.generateToken(user.getId());
        
        // Build response
        AuthDTO.UserDTO userDTO = new AuthDTO.UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTaxRegime().getValue(),
                user.getPan(),
                user.getPhoneNumber()
        );
        
        return new AuthDTO.AuthResponse(true, token, userDTO);
    }
    
    @SuppressWarnings("null")
    public AuthDTO.UserDTO getMe(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new AuthDTO.UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTaxRegime().getValue(),
                user.getPan(),
                user.getPhoneNumber()
        );
    }
}
