package com.erts.service;

import com.erts.dto.RegistrationRequest;
import com.erts.model.Event;
import com.erts.model.Registration;
import com.erts.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService           eventService;

    // ── Create a new registration ─────────────────────────────────────────────
    public Registration create(RegistrationRequest req, String userId) {

        // Validate event exists and has capacity
        Event event = eventService.getById(req.getEventId());

        if (event.getRegisteredCount() >= event.getCapacity()) {
            throw new RuntimeException("Sorry, this event is fully booked.");
        }

        // Validate group size
        if ("group".equals(req.getType()) && req.getMembers() != null) {
            int maxGroup = event.getMaxGroupSize() > 0 ? event.getMaxGroupSize() : 10;
            if (req.getMembers().size() >= maxGroup) {
                throw new RuntimeException("Group size exceeds the maximum of " + maxGroup);
            }
        }

        // Map DTO → model
        Registration reg = new Registration();
        reg.setEventId(req.getEventId());
        reg.setUserId(userId);
        reg.setType(req.getType());
        reg.setReferenceNumber(generateRef());
        reg.setStatus("pending");
        reg.setCreatedAt(LocalDateTime.now());

        // Primary attendee
        Registration.Attendee primary = new Registration.Attendee();
        primary.setFirstName(req.getPrimaryAttendee().getFirstName());
        primary.setLastName(req.getPrimaryAttendee().getLastName());
        primary.setEmail(req.getPrimaryAttendee().getEmail());
        primary.setPhone(req.getPrimaryAttendee().getPhone());
        primary.setOrganisation(req.getPrimaryAttendee().getOrganisation());
        primary.setRequirements(req.getPrimaryAttendee().getRequirements());
        reg.setPrimaryAttendee(primary);

        // Group members (may be null for solo)
        List<Registration.Attendee> members = new ArrayList<>();
        if ("group".equals(req.getType()) && req.getMembers() != null) {
            for (RegistrationRequest.AttendeeDto dto : req.getMembers()) {
                Registration.Attendee m = new Registration.Attendee();
                m.setFirstName(dto.getFirstName());
                m.setLastName(dto.getLastName());
                m.setEmail(dto.getEmail());
                m.setPhone(dto.getPhone());
                m.setOrganisation(dto.getOrganisation());
                members.add(m);
            }
        }
        reg.setMembers(members);

        // Calculate total amount
        int totalPeople = 1 + members.size();
        reg.setTotalAmount(event.getPrice() * totalPeople);

        return registrationRepository.save(reg);
    }

    // ── Get single registration ───────────────────────────────────────────────
    public Registration getById(String id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found: " + id));
    }

    // ── Get all registrations for a user ──────────────────────────────────────
    public List<Registration> getByUser(String userId) {
        return registrationRepository.findByUserId(userId);
    }

    // ── Get all registrations (admin) ─────────────────────────────────────────
    public List<Registration> getAll() {
        return registrationRepository.findAll();
    }

    // ── Get registrations for an event (admin) ────────────────────────────────
    public List<Registration> getByEvent(String eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    // ── Mark confirmed (called when payment approved) ─────────────────────────
    public Registration confirm(String registrationId) {
        Registration reg = getById(registrationId);
        reg.setStatus("confirmed");
        return registrationRepository.save(reg);
    }

    // ── Mark rejected ─────────────────────────────────────────────────────────
    public Registration reject(String registrationId) {
        Registration reg = getById(registrationId);
        reg.setStatus("rejected");
        return registrationRepository.save(reg);
    }

    // ── Generate unique reference number ─────────────────────────────────────
    private String generateRef() {
        int year   = LocalDateTime.now().getYear();
        int number = 1000 + new Random().nextInt(9000);
        return "EVH-" + year + "-" + number;
    }
}
