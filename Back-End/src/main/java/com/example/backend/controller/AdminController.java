package com.example.backend.controller;

import com.example.backend.dto.UserResponse;
import com.example.backend.service.AdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // All users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
                adminService.getAllUsers().stream().map(UserResponse::new).toList());
    }

    // Users waiting for approval
    @GetMapping("/pending")
    public ResponseEntity<List<UserResponse>> getPendingUsers() {
        return ResponseEntity.ok(
                adminService.getPendingUsers().stream().map(UserResponse::new).toList());
    }

    @PatchMapping("/{userId}/approve")
    public ResponseEntity<UserResponse> approveUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new UserResponse(adminService.approveUser(userId)));
    }

    @PatchMapping("/{userId}/reject")
    public ResponseEntity<UserResponse> rejectUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new UserResponse(adminService.rejectUser(userId)));
    }

    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<UserResponse> suspendUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new UserResponse(adminService.suspendUser(userId)));
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new UserResponse(adminService.activateUser(userId)));
    }
}
