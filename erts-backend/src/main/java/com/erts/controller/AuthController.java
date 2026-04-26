package com.erts.controller;

import com.erts.dto.*;
import com.erts.model.User;
import com.erts.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register  — register a regular USER
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(
            @Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req);
        user.setPasswordHash(null);
        return ResponseEntity.ok(ApiResponse.ok("Account created successfully", user));
    }

    // POST /api/auth/register-organiser  — register as ORGANISER (needs admin approval)
    @PostMapping("/register-organiser")
    public ResponseEntity<ApiResponse<User>> registerOrganiser(
            @Valid @RequestBody OrganiserRegisterRequest req) {
        User user = authService.registerOrganiser(req);
        user.setPasswordHash(null);
        return ResponseEntity.ok(ApiResponse.ok(
            "Organiser account created. Awaiting admin approval before you can log in.", user));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest req) {
        LoginResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> me(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        User user = authService.getById(userId);
        user.setPasswordHash(null);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    // ── Admin: user management ────────────────────────────────────────────────

    // GET /api/users  — all users (admin)
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> allUsers() {
        List<User> users = authService.getAllUsers();
        users.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    // GET /api/organisers/pending  — pending organiser approvals (admin)
    @GetMapping("/organisers/pending")
    public ResponseEntity<ApiResponse<List<User>>> pendingOrganisers() {
        List<User> users = authService.getPendingOrganisers();
        users.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    // PUT /api/organisers/:id/approve  — approve organiser (admin)
    @PutMapping("/organisers/{id}/approve")
    public ResponseEntity<ApiResponse<User>> approveOrganiser(@PathVariable String id) {
        User user = authService.approveOrganiser(id);
        user.setPasswordHash(null);
        return ResponseEntity.ok(ApiResponse.ok("Organiser approved", user));
    }

    // PUT /api/organisers/:id/reject  — reject organiser (admin)
    @PutMapping("/organisers/{id}/reject")
    public ResponseEntity<ApiResponse<User>> rejectOrganiser(@PathVariable String id) {
        User user = authService.rejectOrganiser(id);
        user.setPasswordHash(null);
        return ResponseEntity.ok(ApiResponse.ok("Organiser rejected", user));
    }
}
