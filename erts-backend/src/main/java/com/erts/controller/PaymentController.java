package com.erts.controller;

import com.erts.dto.ApiResponse;
import com.erts.dto.PaymentSubmitRequest;
import com.erts.model.Payment;
import com.erts.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/payments/submit  — user submits transaction ID
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Payment>> submit(
            @Valid @RequestBody PaymentSubmitRequest req,
            Authentication auth) {

        String userId = (String) auth.getPrincipal();
        Payment payment = paymentService.submit(req, userId);
        return ResponseEntity.ok(ApiResponse.ok("Payment details submitted. Awaiting verification.", payment));
    }

    // GET /api/payments/pending  — admin: list all pending payments
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Payment>>> pending() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPending()));
    }

    // GET /api/payments  — admin: all payments
    @GetMapping
    public ResponseEntity<ApiResponse<List<Payment>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getAll()));
    }

    // GET /api/payments/:id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getById(id)));
    }

    // PUT /api/payments/:id/approve  — admin: approve payment and issue ticket
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Payment>> approve(
            @PathVariable String id,
            Authentication auth) {

        String adminId = (String) auth.getPrincipal();
        Payment payment = paymentService.approve(id, adminId);
        return ResponseEntity.ok(ApiResponse.ok("Payment approved. Ticket issued.", payment));
    }

    // PUT /api/payments/:id/reject  — admin: reject payment
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Payment>> reject(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {

        String adminId = (String) auth.getPrincipal();
        String reason  = body != null ? body.get("reason") : null;
        Payment payment = paymentService.reject(id, reason, adminId);
        return ResponseEntity.ok(ApiResponse.ok("Payment rejected.", payment));
    }
}
