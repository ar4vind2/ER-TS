package com.erts.controller;

import com.erts.dto.ApiResponse;
import com.erts.model.Ticket;
import com.erts.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // GET /api/tickets/my  — logged-in user's own tickets
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Ticket>>> mine(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getByUser(userId)));
    }

    // GET /api/tickets/all  — admin: every ticket
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Ticket>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getAll()));
    }

    // GET /api/tickets/:id  — single ticket by MongoDB ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Ticket>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getById(id)));
    }

    // GET /api/tickets/ref/:ref  — look up ticket by reference number (venue scan)
    @GetMapping("/ref/{ref}")
    public ResponseEntity<ApiResponse<Ticket>> getByRef(@PathVariable String ref) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getByRef(ref)));
    }

    // PUT /api/tickets/:id/revoke  — admin: revoke a ticket
    @PutMapping("/{id}/revoke")
    public ResponseEntity<ApiResponse<Ticket>> revoke(
            @PathVariable String id,
            Authentication auth) {

        String adminId = (String) auth.getPrincipal();
        Ticket ticket = ticketService.revoke(id, adminId);
        return ResponseEntity.ok(ApiResponse.ok("Ticket revoked.", ticket));
    }
}
