package com.example.backend.dto;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The only user shape that is ever sent to the browser.
 * It never contains the password hash.
 */
public class UserResponse {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final Role role;
    private final UserStatus status;
    private final BigDecimal credit;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime lastLoginAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.credit = user.getCredit();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.lastLoginAt = user.getLastLoginAt();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
