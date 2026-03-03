package com.example.inventory.DTO;

import com.example.inventory.Model.Role;
import java.util.UUID;
import java.time.LocalDateTime;

public class UserResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private boolean isActive;

    public UserResponseDTO(UUID id, String username, String email, Role role, LocalDateTime createdAt, boolean isActive) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}