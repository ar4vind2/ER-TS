package com.erts.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.erts.model.Event;
import com.erts.model.User;
import com.erts.repository.EventRepository;
import com.erts.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository  userRepository;

    // ── Public: get all active/upcoming events ────────────────────────────────
    public List<Event> getAllActive() {
        return eventRepository.findByStatusIn(List.of("active", "upcoming"));
    }

    // ── Admin: get all events ─────────────────────────────────────────────────
    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    // ── Admin: get events pending approval ────────────────────────────────────
    public List<Event> getPendingApproval() {
        return eventRepository.findByStatus("pending_approval");
    }

    // ── Organiser: get only their events ──────────────────────────────────────
    public List<Event> getByOrganiser(String organiserId) {
        return eventRepository.findByOrganiserId(organiserId);
    }

    // ── Get single event ──────────────────────────────────────────────────────
    public Event getById(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
    }

    // ── Filter by category ────────────────────────────────────────────────────
    public List<Event> getByCategory(String category) {
        return eventRepository.findByStatusInAndCategory(
                List.of("active", "upcoming"), category);
    }

    // ── Search by keyword ─────────────────────────────────────────────────────
    public List<Event> search(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // ── Organiser creates event — goes to pending_approval ────────────────────
    public Event create(Event event, String organiserId) {
        User organiser = userRepository.findById(organiserId)
                .orElseThrow(() -> new RuntimeException("Organiser not found"));

        normalizeImages(event);

        event.setOrganiserId(organiserId);
        event.setOrganiserName(
            organiser.getOrganiserName() != null
                ? organiser.getOrganiserName()
                : organiser.getFirstName() + " " + organiser.getLastName()
        );
        event.setRegisteredCount(0);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        // Admin-created events go live immediately; organiser events need approval
        if (event.getStatus() == null || !event.getStatus().equals("active")) {
            event.setStatus("pending_approval");
        }

        return eventRepository.save(event);
    }

    // ── Admin creates event directly (goes live) ──────────────────────────────
    public Event createByAdmin(Event event) {
        normalizeImages(event);
        event.setRegisteredCount(0);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        if (event.getStatus() == null) event.setStatus("active");
        return eventRepository.save(event);
    }

    // ── Update event ──────────────────────────────────────────────────────────
    public Event update(String id, Event updated, String requesterId, String requesterRole) {
        Event existing = getById(id);

        normalizeImages(updated);

        // Organisers can only edit their own events
        if ("ORGANISER".equals(requesterRole) &&
            !existing.getOrganiserId().equals(requesterId)) {
            throw new RuntimeException("You can only edit your own events");
        }

        updated.setId(existing.getId());
        updated.setOrganiserId(existing.getOrganiserId());
        updated.setOrganiserName(existing.getOrganiserName());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        updated.setRegisteredCount(existing.getRegisteredCount());

        // If organiser edits an active event, put back to pending_approval
        if ("ORGANISER".equals(requesterRole) && "active".equals(existing.getStatus())) {
            updated.setStatus("pending_approval");
        }

        return eventRepository.save(updated);
    }

    private void normalizeImages(Event event) {
        List<String> normalized = event.getImageUrls();
        if (normalized != null) {
            normalized = normalized.stream()
                    .filter(v -> v != null && !v.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        if ((normalized == null || normalized.isEmpty()) && event.getImageUrl() != null
                && !event.getImageUrl().trim().isEmpty()) {
            normalized = List.of(event.getImageUrl().trim());
        }

        event.setImageUrls((normalized == null || normalized.isEmpty()) ? null : normalized);
        event.setImageUrl(event.getImageUrls() != null ? event.getImageUrls().get(0) : null);
    }

    // ── Admin approves an organiser's event ───────────────────────────────────
    public Event approveEvent(String id) {
        Event event = getById(id);
        event.setStatus("active");
        event.setRejectionReason(null);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // ── Admin rejects an organiser's event ───────────────────────────────────
    public Event rejectEvent(String id, String reason) {
        Event event = getById(id);
        event.setStatus("rejected");
        event.setRejectionReason(reason);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // ── Delete event ──────────────────────────────────────────────────────────
    public void delete(String id, String requesterId, String requesterRole) {
        Event existing = getById(id);
        if ("ORGANISER".equals(requesterRole) &&
            !existing.getOrganiserId().equals(requesterId)) {
            throw new RuntimeException("You can only delete your own events");
        }
        eventRepository.deleteById(id);
    }

    // ── Increment registered count ────────────────────────────────────────────
    public void incrementRegistered(String eventId) {
        Event event = getById(eventId);
        event.setRegisteredCount(event.getRegisteredCount() + 1);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
    }
}
