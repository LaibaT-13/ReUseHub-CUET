package com.reusehubJava.backend.service;

import com.reusehubJava.backend.model.User;
import com.reusehubJava.backend.model.Admin;
import com.reusehubJava.backend.repository.UserRepository;
import com.reusehubJava.backend.repository.AdminRepository;
import com.reusehubJava.backend.security.jwt.JwtUtil;
import com.reusehubJava.backend.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public String registerUser(User user) {
        String email = user.getUCusMail() == null ? "" : user.getUCusMail().toLowerCase();
        user.setUCusMail(email);

        if (!email.endsWith("@cuet.ac.bd") && !email.endsWith("@student.cuet.ac.bd")) {
            throw new RuntimeException("Only CUET email addresses are allowed (@cuet.ac.bd or @student.cuet.ac.bd)");
        }

        if (userRepository.findByUCusMail(email).isPresent()) {
            throw new RuntimeException("Email is already in use.");
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));

        User newUser = new User();
        newUser.setUName(user.getUName());
        newUser.setUPhone(user.getUPhone());
        newUser.setUCusMail(email);
        newUser.setAddress(user.getAddress());
        newUser.setUPassword(passwordEncoder.encode(user.getUPassword()));
        newUser.setDateJoined(new Date());
        newUser.setStatus(User.UserStatus.ACTIVE);
        newUser.setResetOtp(otp);
        newUser.setOtpExpiry(new Date(System.currentTimeMillis() + 10 * 60 * 1000));

        userRepository.save(newUser);
        emailService.sendRegistrationOtpEmail(email, otp);

        System.out.println("==========================================");
        System.out.println("REGISTRATION OTP FOR: " + email);
        System.out.println("OTP: " + otp);
        System.out.println("==========================================");

        return "OTP sent to your CUET email. Please verify to complete registration.";
    }

    @Transactional
    public String verifyRegistrationOTP(String email, String otp) {
        email = email.toLowerCase();
        User user = userRepository.findByUCusMail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP. Please check your email.");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().before(new Date())) {
            throw new RuntimeException("OTP has expired. Please register again.");
        }

        user.setStatus(User.UserStatus.ACTIVE);
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        System.out.println("✅ Email verified successfully for: " + email);
        return "Email verified successfully! You can now log in.";
    }

    @Transactional
    public String resendRegistrationOTP(String email) {
        email = email.toLowerCase();
        User user = userRepository.findByUCusMail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.format("%06d", new Random().nextInt(1000000));
        user.setResetOtp(otp);
        user.setOtpExpiry(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
        userRepository.save(user);

        emailService.sendRegistrationOtpEmail(email, otp);

        System.out.println("==========================================");
        System.out.println("RESENT REGISTRATION OTP FOR: " + email);
        System.out.println("OTP: " + otp);
        System.out.println("==========================================");

        return "OTP resent successfully.";
    }

    public String authenticateAndGenerateToken(String email, String password) {
        System.out.println("🔐 LOGIN ATTEMPT for: " + email);

        User user = userRepository.findByUCusMail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getUPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new RuntimeException("Account is banned");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String jwt = jwtUtil.generateToken(userDetails);

        System.out.println("✅ LOGIN SUCCESS for: " + email);
        return jwt;
    }

    public AuthResponse authenticateAndGenerateTokenWithDetails(String email, String password) {
        System.out.println("🔐 LOGIN ATTEMPT for: " + email);

        User user = userRepository.findByUCusMail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getUPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new RuntimeException("Account is banned");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String jwt = jwtUtil.generateToken(userDetails);

        if (user.getStatus() == User.UserStatus.WARNED) {
            String warningMessage = "Your account has been warned. Reason: " +
                (user.getWarningReason() != null ? user.getWarningReason() : "Policy violation");
            System.out.println("⚠️ LOGIN SUCCESS with WARNING for: " + email);
            return new AuthResponse(jwt, warningMessage, true, user.getWarningReason());
        } else {
            System.out.println("✅ LOGIN SUCCESS for: " + email);
            return new AuthResponse(jwt);
        }
    }

    public String authenticateAdminAndGenerateToken(String email, String password) {
        System.out.println("👑 ADMIN LOGIN ATTEMPT for: " + email);

        Admin admin = adminRepository.findByAEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(password, admin.getAPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (admin.getStatus() != Admin.AdminStatus.ACTIVE) {
            throw new RuntimeException("Admin account is not active");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String jwt = jwtUtil.generateToken(userDetails);

        System.out.println("✅ ADMIN LOGIN SUCCESS for: " + email);
        return jwt;
    }

    public void initiatePasswordReset(String email) {
        User user = userRepository.findByUCusMail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.format("%06d", new Random().nextInt(1000000));
        user.setResetOtp(otp);
        user.setOtpExpiry(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
        userRepository.save(user);

        try {
            emailService.sendPasswordResetOtpEmail(user.getUCusMail(), otp);
            System.out.println("SUCCESS: Password reset OTP sent to: " + email);
        } catch (Exception e) {
            System.out.println("==========================================");
            System.out.println("PASSWORD RESET OTP FOR: " + email);
            System.out.println("YOUR RESET OTP: " + otp);
            System.out.println("==========================================");
        }
    }

    public void verifyOtpAndResetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByUCusMail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().before(new Date())) {
            throw new RuntimeException("OTP has expired");
        }

        user.setUPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp("");
        user.setOtpExpiry(null);
        userRepository.save(user);

        System.out.println("✅ Password reset successful for: " + email);
    }

    public User getUserByUsername(String email) {
        return userRepository.findByUCusMail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean verifyEmail(String token) {
        try {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean manualVerifyEmail(String email) {
        return true;
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByUCusMail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Would resend verification email to: " + user.getUCusMail());
    }
}