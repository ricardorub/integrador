/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.peloteros.controller.api;

import com.example.peloteros.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Spring's UserDetailsService
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Request and Response classes (can be inner classes or separate files)
// Making them public static inner classes or separate public classes is generally better practice.
// For this subtask, keeping them as non-static inner classes as per prompt.
class AuthenticationRequest {
    private String username;
    private String password;

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class AuthenticationResponse {
    private final String jwt;
    public AuthenticationResponse(String jwt) { this.jwt = jwt; }
    public String getJwt() { return jwt; } // Getter
}

@RestController
@RequestMapping("/api/authenticate") 
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    // Spring will inject the CustomUserDetailsService bean here as it implements UserDetailsService
    private UserDetailsService userDetailsService; 

    @PostMapping
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (DisabledException e) {
            // It's generally better to return specific HTTP error codes than throwing generic Exception
            // For example, ResponseEntity.status(HttpStatus.FORBIDDEN).body("USER_DISABLED");
            throw new Exception("USER_DISABLED: " + e.getMessage(), e);
        } catch (BadCredentialsException e) {
            // Example: ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID_CREDENTIALS");
            throw new Exception("INVALID_CREDENTIALS: " + e.getMessage(), e);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}


