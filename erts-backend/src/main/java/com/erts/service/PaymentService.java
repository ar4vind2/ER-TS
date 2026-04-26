package com.erts.service;

import com.erts.dto.PaymentSubmitRequest;
import com.erts.model.Payment;
import com.erts.model.Registration;
import com.erts.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository    paymentRepository;
    private final RegistrationService  registrationService;
    private final TicketService        ticketService;
    private final EventService         eventService;

    // ── User submits transaction ID after UPI payment ─────────────────────────
    public Payment submit(PaymentSubmitRequest req, String userId) {

        Registration reg = registrationService.getById(req.getRegistrationId());

        // Prevent duplicate submissions
        paymentRepository.findByRegistrationId(req.getRegistrationId()).ifPresent(p -> {
            if ("approved".equals(p.getStatus())) {
                throw new RuntimeException("Payment already approved for this registration.");
            }
        });

        Payment payment = new Payment();
        payment.setRegistrationId(req.getRegistrationId());
        payment.setEventId(reg.getEventId());
        payment.setUserId(userId);
        payment.setTransactionId(req.getTransactionId());
        payment.setScreenshotUrl(req.getScreenshotUrl());
        payment.setAmount(reg.getTotalAmount());
        payment.setStatus("pending");
        payment.setSubmittedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    // ── Admin: get all pending payments ───────────────────────────────────────
    public List<Payment> getPending() {
        return paymentRepository.findByStatus("pending");
    }

    // ── Admin: get all payments ───────────────────────────────────────────────
    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    // ── Admin: approve a payment → confirm registration + issue ticket ────────
    public Payment approve(String paymentId, String adminUserId) {
        Payment payment = getById(paymentId);

        payment.setStatus("approved");
        payment.setReviewedAt(LocalDateTime.now());
        payment.setReviewedBy(adminUserId);
        paymentRepository.save(payment);

        // Confirm the registration
        registrationService.confirm(payment.getRegistrationId());

        // Increment event registered count
        eventService.incrementRegistered(payment.getEventId());

        // Generate the digital ticket
        ticketService.issue(payment);

        return payment;
    }

    // ── Admin: reject a payment ───────────────────────────────────────────────
    public Payment reject(String paymentId, String reason, String adminUserId) {
        Payment payment = getById(paymentId);

        payment.setStatus("rejected");
        payment.setRejectionReason(reason);
        payment.setReviewedAt(LocalDateTime.now());
        payment.setReviewedBy(adminUserId);
        paymentRepository.save(payment);

        // Mark registration as rejected too
        registrationService.reject(payment.getRegistrationId());

        return payment;
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────
    public Payment getById(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }
}
