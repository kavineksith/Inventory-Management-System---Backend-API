package com.example.inventory.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ═══════════════════════════════════════════════════════════
    //  CORE EMAIL METHODS
    // ═══════════════════════════════════════════════════════════

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // ═══════════════════════════════════════════════════════════
    //  USER EMAILS (Async)
    // ═══════════════════════════════════════════════════════════

    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String to, String username) {
        String subject = "🎉 Welcome to Inventory Management System";
        try {
            sendHtmlEmail(to, subject, buildWelcomeEmailTemplate(username));
            logger.info("Welcome email sent to user: {}", username);
        } catch (MessagingException e) {
            logger.error("Failed to send welcome email to '{}': {}", username, e.getMessage());
            try {
                sendSimpleEmail(to, subject,
                        "Welcome to Inventory Management System, " + username + "!\n\n" +
                        "Your account has been successfully created.\n" +
                        "You can now log in using your credentials.\n\n" +
                        "Best regards,\nInventory Management Team");
            } catch (Exception fallbackEx) {
                logger.error("Fallback email also failed for '{}': {}", username, fallbackEx.getMessage());
            }
        }
    }

    @Async("emailTaskExecutor")
    public void sendAccountUpdatedEmail(String to, String username) {
        String subject = "✅ Account Updated — Inventory Management System";
        try {
            sendHtmlEmail(to, subject, buildAccountUpdatedEmailTemplate(username));
            logger.info("Account updated email sent to user: {}", username);
        } catch (MessagingException e) {
            logger.error("Failed to send account updated email to '{}': {}", username, e.getMessage());
            try {
                sendSimpleEmail(to, subject,
                        "Hello " + username + ",\n\n" +
                        "Your account details have been successfully updated.\n" +
                        "If you did not make this change, contact support immediately.\n\n" +
                        "Best regards,\nInventory Management Team");
            } catch (Exception fallbackEx) {
                logger.error("Fallback email also failed for '{}': {}", username, fallbackEx.getMessage());
            }
        }
    }

    @Async("emailTaskExecutor")
    public void sendAccountDeletedEmail(String to, String username) {
        String subject = "⚠️ Account Deleted — Inventory Management System";
        try {
            sendHtmlEmail(to, subject, buildAccountDeletedEmailTemplate(username));
            logger.info("Account deleted email sent to user: {}", username);
        } catch (MessagingException e) {
            logger.error("Failed to send account deleted email to '{}': {}", username, e.getMessage());
            try {
                sendSimpleEmail(to, subject,
                        "Hello " + username + ",\n\n" +
                        "Your account has been deleted from the Inventory Management System.\n" +
                        "If you did not request this, contact support immediately.\n\n" +
                        "Best regards,\nInventory Management Team");
            } catch (Exception fallbackEx) {
                logger.error("Fallback email also failed for '{}': {}", username, fallbackEx.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  INVENTORY EMAILS (Async)
    // ═══════════════════════════════════════════════════════════

    @Async("emailTaskExecutor")
    public void sendInventoryCreatedEmail(String to, String itemName, String pluCode, int qty, double price) {
        String subject = "📦 New Inventory Item Created — " + itemName;
        try {
            sendHtmlEmail(to, subject, buildInventoryCreatedTemplate(itemName, pluCode, qty, price));
            logger.info("Inventory created email sent for item: {} (PLU: {})", itemName, pluCode);
        } catch (MessagingException e) {
            logger.error("Failed to send inventory created email for '{}': {}", pluCode, e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    public void sendInventoryUpdatedEmail(String to, String itemName, String pluCode, int qty, double price) {
        String subject = "🔄 Inventory Item Updated — " + itemName;
        try {
            sendHtmlEmail(to, subject, buildInventoryUpdatedTemplate(itemName, pluCode, qty, price));
            logger.info("Inventory updated email sent for item: {} (PLU: {})", itemName, pluCode);
        } catch (MessagingException e) {
            logger.error("Failed to send inventory updated email for '{}': {}", pluCode, e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    public void sendInventoryDeletedEmail(String to, String itemName, String pluCode) {
        String subject = "🗑️ Inventory Item Deleted — " + itemName;
        try {
            sendHtmlEmail(to, subject, buildInventoryDeletedTemplate(itemName, pluCode));
            logger.info("Inventory deleted email sent for item: {} (PLU: {})", itemName, pluCode);
        } catch (MessagingException e) {
            logger.error("Failed to send inventory deleted email for '{}': {}", pluCode, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PROFESSIONAL EMAIL TEMPLATES
    // ═══════════════════════════════════════════════════════════

    private String wrapInBaseTemplate(String headerGradient, String iconEmoji, String headerTitle,
                                       String headerSubtitle, String bodyContent) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f0f2f5; color: #1a1a2e; line-height: 1.7; }" +
                ".wrapper { max-width: 640px; margin: 30px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 40px rgba(0,0,0,0.08); }" +
                ".header { background: " + headerGradient + "; padding: 40px 30px; text-align: center; }" +
                ".header-icon { font-size: 48px; margin-bottom: 12px; display: block; }" +
                ".header h1 { color: #ffffff; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; margin-bottom: 6px; }" +
                ".header p { color: rgba(255,255,255,0.85); font-size: 14px; font-weight: 400; }" +
                ".body { padding: 36px 30px; }" +
                ".body h2 { font-size: 20px; color: #1a1a2e; margin-bottom: 16px; }" +
                ".body p { font-size: 15px; color: #4a4a68; margin-bottom: 14px; }" +
                ".info-card { background: #f8f9fc; border-left: 4px solid #4361ee; border-radius: 8px; padding: 18px 20px; margin: 20px 0; }" +
                ".info-card p { margin-bottom: 6px; color: #2d2d44; font-size: 14px; }" +
                ".info-card strong { color: #1a1a2e; }" +
                ".divider { height: 1px; background: linear-gradient(to right, transparent, #e0e0e0, transparent); margin: 24px 0; }" +
                ".cta-btn { display: inline-block; background: " + headerGradient + "; color: #ffffff; padding: 12px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 14px; margin-top: 10px; }" +
                ".footer { background: #1a1a2e; padding: 24px 30px; text-align: center; }" +
                ".footer p { color: rgba(255,255,255,0.55); font-size: 12px; margin-bottom: 4px; }" +
                ".footer a { color: #4361ee; text-decoration: none; }" +
                ".badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }" +
                ".badge-success { background: #d4edda; color: #155724; }" +
                ".badge-warning { background: #fff3cd; color: #856404; }" +
                ".badge-danger { background: #f8d7da; color: #721c24; }" +
                ".badge-info { background: #d1ecf1; color: #0c5460; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='wrapper'>" +
                "<div class='header'>" +
                "<span class='header-icon'>" + iconEmoji + "</span>" +
                "<h1>" + headerTitle + "</h1>" +
                "<p>" + headerSubtitle + "</p>" +
                "</div>" +
                "<div class='body'>" +
                bodyContent +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2025 Inventory Management System</p>" +
                "<p>This is an automated notification. Please do not reply to this email.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    // ─── User Templates ───────────────────────────────────────

    private String buildWelcomeEmailTemplate(String username) {
        String body =
                "<h2>Hello " + username + " \uD83D\uDC4B</h2>" +
                "<p>Your account has been <strong>successfully created</strong> in our Inventory Management System.</p>" +
                "<div class='info-card'>" +
                "<p><strong>Username:</strong> " + username + "</p>" +
                "<p><strong>Status:</strong> <span class='badge badge-success'>Active</span></p>" +
                "<p><strong>Access Level:</strong> Full system access</p>" +
                "</div>" +
                "<p>You can now log in using your credentials and start managing inventory items, generate reports, and track stock levels in real time.</p>" +
                "<div class='divider'></div>" +
                "<p><strong>What you can do:</strong></p>" +
                "<p>\u2705 Create, update, and delete inventory items</p>" +
                "<p>\u2705 Generate professional PDF reports</p>" +
                "<p>\u2705 Track stock levels and pricing</p>" +
                "<p>\u2705 Manage user accounts (Admin)</p>" +
                "<div class='divider'></div>" +
                "<p style='color: #6c757d; font-size: 13px;'>If you did not create this account, please contact our support team immediately.</p>";

        return wrapInBaseTemplate(
                "linear-gradient(135deg, #4361ee 0%, #3a0ca3 100%)",
                "\uD83C\uDF1F", "Welcome Aboard!", "Your account is ready to go", body);
    }

    private String buildAccountUpdatedEmailTemplate(String username) {
        String body =
                "<h2>Hello " + username + ",</h2>" +
                "<p>Your account details have been <strong>successfully updated</strong> in the Inventory Management System.</p>" +
                "<div class='info-card'>" +
                "<p><strong>Action:</strong> <span class='badge badge-info'>Account Updated</span></p>" +
                "<p><strong>Updated By:</strong> System Administrator</p>" +
                "<p><strong>Timestamp:</strong> Just now</p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p>\u26A0\uFE0F If you did not request this change, please contact our support team <strong>immediately</strong> to secure your account.</p>";

        return wrapInBaseTemplate(
                "linear-gradient(135deg, #06d6a0 0%, #118ab2 100%)",
                "\u2705", "Account Updated", "Your profile changes have been saved", body);
    }

    private String buildAccountDeletedEmailTemplate(String username) {
        String body =
                "<h2>Hello " + username + ",</h2>" +
                "<p>Your account has been <strong>permanently deleted</strong> from the Inventory Management System.</p>" +
                "<div class='info-card'>" +
                "<p><strong>Action:</strong> <span class='badge badge-danger'>Account Deleted</span></p>" +
                "<p><strong>Status:</strong> Permanently removed</p>" +
                "<p><strong>Data:</strong> All associated data has been purged</p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p>If you did not request this deletion, please contact our support team <strong>immediately</strong>.</p>" +
                "<p style='color: #6c757d; font-size: 13px;'>We're sorry to see you go. Thank you for using our system.</p>";

        return wrapInBaseTemplate(
                "linear-gradient(135deg, #ef476f 0%, #d00000 100%)",
                "\u26A0\uFE0F", "Account Deleted", "Your account has been removed", body);
    }

    // ─── Inventory Templates ──────────────────────────────────

    private String buildInventoryCreatedTemplate(String itemName, String pluCode, int qty, double price) {
        String body =
                "<h2>New Item Added \uD83D\uDCE6</h2>" +
                "<p>A new inventory item has been successfully added to the system.</p>" +
                "<div class='info-card'>" +
                "<p><strong>Item Name:</strong> " + itemName + "</p>" +
                "<p><strong>PLU Code:</strong> " + pluCode + "</p>" +
                "<p><strong>Quantity:</strong> " + qty + " units</p>" +
                "<p><strong>Unit Price:</strong> $" + String.format("%.2f", price) + "</p>" +
                "<p><strong>Total Value:</strong> $" + String.format("%.2f", qty * price) + "</p>" +
                "<p><strong>Status:</strong> <span class='badge badge-success'>In Stock</span></p>" +
                "</div>";

        return wrapInBaseTemplate(
                "linear-gradient(135deg, #4361ee 0%, #4895ef 100%)",
                "\uD83D\uDCE6", "Inventory Item Created", "New item added to your inventory", body);
    }

    private String buildInventoryUpdatedTemplate(String itemName, String pluCode, int qty, double price) {
        String body =
                "<h2>Item Updated \uD83D\uDD04</h2>" +
                "<p>An existing inventory item has been modified.</p>" +
                "<div class='info-card'>" +
                "<p><strong>Item Name:</strong> " + itemName + "</p>" +
                "<p><strong>PLU Code:</strong> " + pluCode + "</p>" +
                "<p><strong>Updated Quantity:</strong> " + qty + " units</p>" +
                "<p><strong>Updated Price:</strong> $" + String.format("%.2f", price) + "</p>" +
                "<p><strong>New Total Value:</strong> $" + String.format("%.2f", qty * price) + "</p>" +
                "<p><strong>Status:</strong> <span class='badge badge-warning'>Modified</span></p>" +
                "</div>";

        return wrapInBaseTemplate(
                "linear-gradient(135deg, #f77f00 0%, #fcbf49 100%)",
                "\uD83D\uDD04", "Inventory Item Updated", "Item details have been changed", body);
    }

    private String buildInventoryDeletedTemplate(String itemName, String pluCode) {
        String body =
                "<h2>Item Removed \uD83D\uDDD1\uFE0F</h2>" +
                "<p>An inventory item has been permanently removed from the system.</p>" +
                "<div class='info-card'>" +
                "<p><strong>Item Name:</strong> " + itemName + "</p>" +
                "<p><strong>PLU Code:</strong> " + pluCode + "</p>" +
                "<p><strong>Status:</strong> <span class='badge badge-danger'>Deleted</span></p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p style='color: #6c757d; font-size: 13px;'>This action cannot be undone. If this was a mistake, the item must be re-created manually.</p>";

        return wrapInBaseTemplate(
                "linear-gradient(135deg, #ef476f 0%, #d00000 100%)",
                "\uD83D\uDDD1\uFE0F", "Inventory Item Deleted", "An item has been removed from stock", body);
    }
}
