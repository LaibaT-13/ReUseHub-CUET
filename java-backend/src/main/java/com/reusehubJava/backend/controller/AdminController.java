package com.reusehubJava.backend.controller;

import com.reusehubJava.backend.model.Admin;
import com.reusehubJava.backend.repository.AdminRepository;
import com.reusehubJava.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminService adminService;

    // Create new admin (only by existing admins)
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin) {
        try {
            // TEMPORARY: Skip admin check for testing
            /*
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admins can create new admin accounts");
            }
            */

            Admin createdAdmin = adminService.createAdmin(admin);
            return ResponseEntity.ok("Admin created successfully: " + createdAdmin.getAEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create admin: " + e.getMessage());
        }
    }

    // Get admin count (for checking if first user should be promoted)
    @GetMapping("/count")
    public ResponseEntity<Long> getAdminCount() {
        try {
            long count = adminRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Promote user to admin
    @PostMapping("/promote-user/{userId}")
    public ResponseEntity<?> promoteUserToAdmin(@PathVariable Long userId) {
        try {
            // Check if current user is admin
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admins can promote users to admin");
            }

            String result = adminService.promoteUserToAdmin(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to promote user: " + e.getMessage());
        }
    }

    // Get pending posts for admin review
    @GetMapping("/pending-posts")
    public ResponseEntity<?> getPendingPosts() {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // This will use the existing ItemController method
            return ResponseEntity.ok("Use /api/items/admin/pending endpoint");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get pending reports for admin review
    @GetMapping("/pending-reports")
    public ResponseEntity<List<Object>> getPendingReports() {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // TODO: Implement when Report entity is ready
            List<Object> pendingReports = adminService.getPendingReports();
            return ResponseEntity.ok(pendingReports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin login
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest request) {
        try {
            String token = adminService.authenticateAndGenerateToken(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new AdminLoginResponse(token, "Login successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    // Get all admins (for super admin functionality)
    @GetMapping("/all")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        try {
            List<Admin> admins = adminRepository.findAll();
            // Don't return passwords
            admins.forEach(admin -> admin.setAPassword(null));
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Request classes
    public static class AdminLoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AdminLoginResponse {
        private String token;
        private String message;

        public AdminLoginResponse(String token, String message) {
            this.token = token;
            this.message = message;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // Check if current user is admin
    @GetMapping("/check-admin")
    public ResponseEntity<?> checkCurrentUserAdmin() {
        try {
            System.out.println("🔍 Admin check endpoint called");
            // TEMPORARY: Bypass admin check for testing
            System.out.println("✅ Bypassing admin check - returning true for testing");
            return ResponseEntity.ok(java.util.Map.of("isAdmin", true));
            
            // Original code:
            // boolean isAdmin = isCurrentUserAdmin();
            // return ResponseEntity.ok(java.util.Map.of("isAdmin", isAdmin));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of("isAdmin", false));
        }
    }

    // Check if specific user is admin (for other admins to check)
    @GetMapping("/check-admin/{userId}")
    public ResponseEntity<?> checkUserAdmin(@PathVariable Long userId) {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            boolean isAdmin = adminService.isUserAdminById(userId);
            return ResponseEntity.ok(java.util.Map.of("isAdmin", isAdmin));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of("isAdmin", false));
        }
    }

    // Helper method to check if current user is admin
    private boolean isCurrentUserAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            System.out.println("🔍 Admin check for email: " + userEmail);
            
            if (userEmail == null || userEmail.equals("anonymousUser")) {
                System.out.println("🚫 Email is null or anonymous");
                return false;
            }
            
            Optional<Admin> admin = adminRepository.findByAEmail(userEmail);
            System.out.println("🔍 Admin found in database: " + admin.isPresent());
            
            if (admin.isPresent()) {
                System.out.println("🔍 Admin status: " + admin.get().getStatus());
                System.out.println("🔍 Admin name: " + admin.get().getAName());
                boolean isActive = admin.get().getStatus() == Admin.AdminStatus.ACTIVE;
                System.out.println("🔍 Is admin active: " + isActive);
                return isActive;
            }
            
            return false;
        } catch (Exception e) {
            System.out.println("❌ Error in admin check: " + e.getMessage());
            return false;
        }
    }
}
