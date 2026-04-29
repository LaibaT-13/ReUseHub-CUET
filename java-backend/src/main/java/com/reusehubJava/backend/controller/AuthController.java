package com.reusehubJava.backend.controller;

import com.reusehubJava.backend.dto.AuthRequest;
import com.reusehubJava.backend.dto.AuthResponse;
import com.reusehubJava.backend.model.User;
import com.reusehubJava.backend.service.AuthService;
import com.reusehubJava.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Enhanced debug logging
            System.out.println("==========================================");
            System.out.println("📝 SIGNUP REQUEST RECEIVED");
            System.out.println("==========================================");
            System.out.println("Name: " + user.getUName());
            System.out.println("Email: " + user.getUCusMail());
            System.out.println("Phone: " + user.getUPhone());
            System.out.println("Address: " + user.getAddress());
            System.out.println("Password provided: " + (user.getUPassword() != null && !user.getUPassword().isEmpty()));
            System.out.println("==========================================");
            
            String result = authService.registerUser(user);
            System.out.println("✅ User registered successfully: " + user.getUCusMail());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            System.out.println("❌ SIGNUP ERROR: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            
            // Return the actual error message to help with debugging
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("💥 UNEXPECTED ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<?> verifyRegistration(@RequestBody VerifyRegistrationRequest request) {
        try {
            String result = authService.verifyRegistrationOTP(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
}

    @PostMapping("/resend-registration-otp")
    public ResponseEntity<?> resendRegistrationOTP(@RequestBody ResendOTPRequest request) {
        try {
            String result = authService.resendRegistrationOTP(request.getEmail());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authenticationRequest) throws Exception {
        try {
            System.out.println("🔐 LOGIN ATTEMPT for: " + authenticationRequest.getEmail());
            
            AuthResponse authResponse = authService.authenticateAndGenerateTokenWithDetails(
                authenticationRequest.getEmail(),
                authenticationRequest.getPassword()
            );
            
            System.out.println("✅ LOGIN SUCCESS for: " + authenticationRequest.getEmail());
            return ResponseEntity.ok(authResponse);
            
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.out.println("❌ LOGIN FAILED - Bad credentials: " + authenticationRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            
        } catch (RuntimeException e) {
            System.out.println("❌ LOGIN FAILED - Runtime error: " + e.getMessage());
            // These are our custom validation errors (email verification, banned users, etc.)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            
        } catch (Exception e) {
            System.out.println("💥 LOGIN FAILED - Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody AuthRequest authenticationRequest) throws Exception {
        try {
            String jwt = authService.authenticateAdminAndGenerateToken(
                authenticationRequest.getEmail(),
                authenticationRequest.getPassword()
            );
            
            // Get user profile to return with JWT
            User user = userRepository.findByUCusMail(authenticationRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Mask password before returning
            user.setUPassword(null);
            
            return ResponseEntity.ok(new AdminAuthResponse(jwt, user));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.out.println("❌ ADMIN LOGIN FAILED - Bad credentials: " + authenticationRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (RuntimeException e) {
            System.out.println("❌ ADMIN LOGIN FAILED - Runtime error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("💥 ADMIN LOGIN FAILED - Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Admin login failed: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok("OTP sent successfully (check console for simulated OTP)");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/verify-otp-reset-password")
    public ResponseEntity<String> verifyOtpAndResetPassword(@RequestBody OtpResetPasswordRequest request) {
        try {
            authService.verifyOtpAndResetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Email verification endpoint
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody VerifyEmailRequest request) {
        try {
            boolean isVerified = authService.verifyEmail(request.getToken());
            if (isVerified) {
                return ResponseEntity.ok("Email verified successfully! You can now log in.");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired verification token");
            }
        } catch (Exception e) {
            System.out.println("Email verification error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Verification failed");
        }
    }

    // Manual verification endpoint for development
    @PostMapping("/manual-verify")
    public ResponseEntity<String> manualVerifyEmail(@RequestBody ManualVerifyRequest request) {
        try {
            boolean isVerified = authService.manualVerifyEmail(request.getEmail());
            if (isVerified) {
                return ResponseEntity.ok("Email manually verified successfully! You can now log in.");
            } else {
                return ResponseEntity.badRequest().body("User not found or already verified");
            }
        } catch (Exception e) {
            System.out.println("Manual verification error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Manual verification failed: " + e.getMessage());
        }
    }

    // Resend verification email endpoint
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody ResendVerificationRequest request) {
        try {
            authService.resendVerificationEmail(request.getEmail());
            return ResponseEntity.ok("Verification email resent successfully! Check your email and console for the verification link.");
        } catch (Exception e) {
            System.out.println("Resend verification error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to resend verification email: " + e.getMessage());
        }
    }

    // Inner class for forgot password request
    public static class ForgotPasswordRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }    // Inner class for OTP verification and password reset
    public static class OtpResetPasswordRequest {
        private String email;
        private String otp;
        private String newPassword;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    // Debug endpoint to see what users exist (REMOVE IN PRODUCTION)
    @GetMapping("/debug/users")
    public ResponseEntity<List<String>> getUsers() {
        try {
            List<String> emails = userRepository.findAll()
                .stream()
                .map(User::getUCusMail)
                .collect(Collectors.toList());
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    // Inner class for admin authentication response
    public static class AdminAuthResponse {
        private final String jwt;
        private final User user;
        private final String message = "Admin authentication successful";

        public AdminAuthResponse(String jwt, User user) {
            this.jwt = jwt;
            this.user = user;
        }

        public String getJwt() { return jwt; }
        public User getUser() { return user; }
        public String getMessage() { return message; }
    }

    // Inner class for email verification
    public static class VerifyEmailRequest {
        private String token;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class ManualVerifyRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResendVerificationRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class VerifyRegistrationRequest {
        private String email;
        private String otp;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }

    public static class ResendOTPRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}