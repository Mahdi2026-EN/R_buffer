package com.example.backend.service;

import com.example.backend.dto.ChangePasswordRequest;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UpdateProfileRequest;
import com.example.backend.entity.User;
import com.example.backend.entity.UserStatus;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Emails are compared in lower case so "Ali@x.com" and "ali@x.com" are the same account
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    // =========================
    // REGISTER USER
    // =========================
    @Transactional
    public User registerUser(RegisterRequest request) {

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw emailTaken();
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());

        // Hash the password before saving it
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // The User entity provides the default role (USER), status (PENDING) and credit (0)
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            // Two people registered the same email at the same moment
            throw emailTaken();
        }
    }

    // =========================
    // LOGIN USER
    // =========================
    @Transactional
    public User loginUser(LoginRequest request) {

        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserService::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw invalidCredentials();
        }

        // The password is correct: now tell the user why they cannot enter, if so
        if (user.getStatus() == UserStatus.PENDING) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_PENDING",
                    "Your account is waiting for admin approval");
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_REJECTED",
                    "Your registration was rejected");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED",
                    "Your account has been suspended");
        }

        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // =========================
    // CURRENT USER
    // =========================
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND", "User not found"));
    }

    @Transactional
    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = getByEmail(email);
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CURRENT_PASSWORD",
                    "The current password is not correct");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Invalid email or password");
    }

    private static ApiException emailTaken() {
        return new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN",
                "Email is already registered");
    }
}
