package com.reusehubJava.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendRegistrationOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Account Verification OTP - ReuseHub");
            
            String emailBody = String.format(
                "Welcome to ReuseHub!\n\n" +
                "Thank you for registering your account. To complete your registration, please verify your email address.\n\n" +
                "Your verification OTP (One-Time Password) is: %s\n\n" +
                "This OTP will expire in 10 minutes for security reasons.\n\n" +
                "If you didn't register for ReuseHub, please ignore this email.\n\n" +
                "Best regards,\n" +
                "ReuseHub Team", 
                otp
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("SUCCESS: Registration OTP email sent successfully to: " + toEmail);
            System.out.println("EMAIL: Check your email inbox for verification OTP: " + otp);
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to send registration OTP email to " + toEmail + ": " + e.getMessage());
            
            // Console fallback with clear formatting
            System.out.println("==========================================");
            System.out.println("REGISTRATION EMAIL FALLBACK (CHECK CONSOLE)");
            System.out.println("==========================================");
            System.out.println("TO: " + toEmail);
            System.out.println("SUBJECT: Account Verification OTP - ReuseHub");
            System.out.println("------------------------------------------");
            System.out.println("Your verification OTP is: " + otp);
            System.out.println("==========================================");
            System.out.println("YOUR REGISTRATION OTP: " + otp);
            System.out.println("==========================================");
        }
    }

    public void sendPasswordResetOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP - ReuseHub");
            
            String emailBody = String.format(
                "Hello,\n\n" +
                "You requested to reset your password for your ReuseHub account.\n\n" +
                "Your password reset OTP (One-Time Password) is: %s\n\n" +
                "This OTP will expire in 10 minutes for security reasons.\n\n" +
                "If you didn't request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "ReuseHub Team", 
                otp
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("SUCCESS: Password reset OTP email sent successfully to: " + toEmail);
            System.out.println("EMAIL: Check your email inbox for reset OTP: " + otp);
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to send password reset OTP email to " + toEmail + ": " + e.getMessage());
            
            // Console fallback with clear formatting
            System.out.println("==========================================");
            System.out.println("PASSWORD RESET EMAIL FALLBACK (CHECK CONSOLE)");
            System.out.println("==========================================");
            System.out.println("TO: " + toEmail);
            System.out.println("SUBJECT: Password Reset OTP - ReuseHub");
            System.out.println("------------------------------------------");
            System.out.println("Your password reset OTP is: " + otp);
            System.out.println("==========================================");
            System.out.println("YOUR PASSWORD RESET OTP: " + otp);
            System.out.println("==========================================");
        }
    }

    // Keep the old method for backward compatibility (defaults to password reset)
    public void sendOtpEmail(String toEmail, String otp) {
        sendPasswordResetOtpEmail(toEmail, otp);
    }

}
