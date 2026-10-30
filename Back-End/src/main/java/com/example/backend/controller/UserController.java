package com.example.backend.controller;

import com.example.backend.dto.ChangePasswordRequest;
import com.example.backend.dto.UpdateProfileRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Who am I? (used by the front end to restore the session)
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(
                new UserResponse(userService.getByEmail(authentication.getName())));
    }

    // Update my name and phone (the email is the login identity and cannot be changed)
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(new UserResponse(
                userService.updateProfile(authentication.getName(), request)));
    }

    // Change my password
    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
