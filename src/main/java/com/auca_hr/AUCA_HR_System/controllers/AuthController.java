package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.dtos.AuthResponse;
import com.auca_hr.AUCA_HR_System.dtos.JwtResponse;
import com.auca_hr.AUCA_HR_System.dtos.LoginRequest;
import com.auca_hr.AUCA_HR_System.dtos.RefreshTokenRequest;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.exceptions.ResourceNotFoundException;
import com.auca_hr.AUCA_HR_System.services.CustomUserDetailsService;
import com.auca_hr.AUCA_HR_System.services.UserService;
import com.auca_hr.AUCA_HR_System.utils.JwtAuthFilter;
import com.auca_hr.AUCA_HR_System.utils.JwtUtil;
import com.auca_hr.AUCA_HR_System.utils.StandardResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;


    @PostMapping(value = "/login")
    public ResponseEntity<StandardResponse<AuthResponse>> login(@RequestBody LoginRequest loginRequest,
                                                                HttpServletRequest request) {

        System.out.println("Login attempt for email: " + loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(StandardResponse.<AuthResponse>builder()
                                .message("Unauthorized")
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .data(null)
                                .build());
            }

            User user = (User) authentication.getPrincipal();

//            if (user.getAccountStatus() != Status.ACTIVE) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(StandardResponse.<AuthResponse>builder()
//                                .message("Your account is not active. Please contact administration for activation.")
//                                .statusCode(HttpStatus.FORBIDDEN.value())
//                                .data(new AuthResponse(null, user.getEmail(), user.getRole().name(),
//                                        user.getFirstName(), user.getLastName(), "Your account is not active."))
//                                .build());
//            }

            String token = jwtTokenService.generateToken(user);

            // ✅ Store JWT in session
            HttpSession session = request.getSession();
            session.setAttribute("jwt_token", token);

            AuthResponse response = new AuthResponse(token, user.getEmail(), user.getRole().name(),
                    user.getFullNames(), "Login successful");

            logger.info("User {} successfully authenticated with role {}",
                    user.getEmail(),
                    user.getRole());

            return ResponseEntity.ok(
                    StandardResponse.<AuthResponse>builder()
                            .message("Login successful")
                            .statusCode(HttpStatus.OK.value())
                            .data(response)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(StandardResponse.<AuthResponse>builder()
                            .message("Invalid credentials")
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .data(new AuthResponse(null, loginRequest.getEmail(), null, null, "Invalid credentials"))
                            .build());
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            if (!jwtTokenService.isRefreshToken(refreshToken) || jwtTokenService.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResourceNotFoundException("Invalid refresh token"));
            }

            String username = jwtTokenService.extractUsername(refreshToken);
            User user = userService.getUserByEmail(username);

            String newAccessToken = jwtTokenService.generateToken(user);
            String newRefreshToken = jwtTokenService.generateRefreshToken(user);

            return ResponseEntity.ok(new JwtResponse(
                    newAccessToken,
                    newRefreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResourceNotFoundException("Token refresh failed"));
        }
    }
}

