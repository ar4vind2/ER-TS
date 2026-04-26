package com.erts.service;

import com.erts.dto.LoginRequest;
import com.erts.dto.LoginResponse;
import com.erts.dto.RegisterRequest;
import com.erts.dto.OrganiserRegisterRequest;
import com.erts.model.User;
import com.erts.repository.UserRepository;
import com.erts.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    // ── Register a regular USER ───────────────────────────────────────────────
    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered: " + req.getEmail());
        }
        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail().toLowerCase());
        user.setPhone(req.getPhone());
        user.setOrganisation(req.getOrganisation());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");
        user.setApproved(true); // users are auto-approved
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // ── Register an ORGANISER (needs admin approval) ──────────────────────────
    public User registerOrganiser(OrganiserRegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered: " + req.getEmail());
        }
        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail().toLowerCase());
        user.setPhone(req.getPhone());
        user.setOrganisation(req.getOrganisation());
        user.setOrganiserName(req.getOrganiserName());
        user.setOrganiserBio(req.getOrganiserBio());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole("ORGANISER");
        user.setApproved(false); // must be approved by admin before login
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Organisers must be approved by admin before they can log in
        if ("ORGANISER".equals(user.getRole()) && !user.isApproved()) {
            throw new RuntimeException("Your organiser account is pending admin approval.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new LoginResponse(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );
    }

    // ── Get user by ID ────────────────────────────────────────────────────────
    public User getById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    // ── Admin: get all users ──────────────────────────────────────────────────
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ── Admin: get all pending organiser approvals ────────────────────────────
    public List<User> getPendingOrganisers() {
        return userRepository.findByRoleAndApproved("ORGANISER", false);
    }

    // ── Admin: approve an organiser ───────────────────────────────────────────
    public User approveOrganiser(String userId) {
        User user = getById(userId);
        if (!"ORGANISER".equals(user.getRole())) {
            throw new RuntimeException("User is not an organiser");
        }
        user.setApproved(true);
        return userRepository.save(user);
    }

    // ── Admin: reject/block an organiser ─────────────────────────────────────
    public User rejectOrganiser(String userId) {
        User user = getById(userId);
        user.setApproved(false);
        return userRepository.save(user);
    }
}
