package com.erts.controller;

import com.erts.dto.ApiResponse;
import com.erts.dto.RegistrationRequest;
import com.erts.model.Registration;
import com.erts.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    // POST /api/registrations  — create a new registration (any logged-in user)
    @PostMapping
    public ResponseEntity<ApiResponse<Registration>> create(
            @Valid @RequestBody RegistrationRequest req,
            Authentication auth) {

        String userId = (String) auth.getPrincipal();
        Registration reg = registrationService.create(req, userId);
        return ResponseEntity.ok(ApiResponse.ok("Registration submitted successfully", reg));
    }

    // GET /api/registrations/my  — logged-in user's own registrations
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Registration>>> mine(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(registrationService.getByUser(userId)));
    }

    // GET /api/registrations/:id  — single registration
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Registration>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(registrationService.getById(id)));
    }

    // GET /api/registrations/all  — admin: all registrations
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Registration>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(registrationService.getAll()));
    }

    // GET /api/registrations/event/:eventId  — admin: registrations for one event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<Registration>>> byEvent(
            @PathVariable String eventId) {
        return ResponseEntity.ok(ApiResponse.ok(registrationService.getByEvent(eventId)));
    }
}
