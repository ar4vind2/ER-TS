package com.erts.service;

import com.erts.model.Payment;
import com.erts.model.Registration;
import com.erts.model.Ticket;
import com.erts.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository    ticketRepository;
    private final RegistrationService registrationService;

    // ── Issue ticket when payment is approved ─────────────────────────────────
    public Ticket issue(Payment payment) {
        Registration reg = registrationService.getById(payment.getRegistrationId());

        Ticket ticket = new Ticket();
        ticket.setRegistrationId(payment.getRegistrationId());
        ticket.setPaymentId(payment.getId());
        ticket.setEventId(payment.getEventId());
        ticket.setUserId(payment.getUserId());
        ticket.setReferenceNumber(reg.getReferenceNumber());

        // QR code encodes the reference number
        // In production you could encode a signed URL or unique token here
        ticket.setQrCodeData(reg.getReferenceNumber());
        ticket.setStatus("active");
        ticket.setIssuedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    // ── Get all tickets for the logged-in user ────────────────────────────────
    public List<Ticket> getByUser(String userId) {
        return ticketRepository.findByUserId(userId);
    }

    // ── Get single ticket ─────────────────────────────────────────────────────
    public Ticket getById(String id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
    }

    // ── Get ticket by reference number (used at venue entry) ──────────────────
    public Ticket getByRef(String ref) {
        return ticketRepository.findByReferenceNumber(ref)
                .orElseThrow(() -> new RuntimeException("Ticket not found for ref: " + ref));
    }

    // ── Get all tickets (admin) ───────────────────────────────────────────────
    public List<Ticket> getAll() {
        return ticketRepository.findAll();
    }

    // ── Revoke a ticket (admin) ───────────────────────────────────────────────
    public Ticket revoke(String ticketId, String adminUserId) {
        Ticket ticket = getById(ticketId);
        ticket.setStatus("revoked");
        ticket.setRevokedAt(LocalDateTime.now());
        ticket.setRevokedBy(adminUserId);
        return ticketRepository.save(ticket);
    }
}
