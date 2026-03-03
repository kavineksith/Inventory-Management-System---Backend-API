package com.example.inventory.Controllers;

import com.example.inventory.Services.EmailService;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String message) {
        try {
            emailService.sendSimpleEmail(to, subject, message);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcomeEmail(@RequestParam String to, @RequestParam String username) {
        try {
            emailService.sendWelcomeEmail(to, username);
            return ResponseEntity.ok("Welcome email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send welcome email: " + e.getMessage());
        }
    }
}