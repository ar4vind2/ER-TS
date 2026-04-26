package com.erts.controller;

import com.erts.dto.ApiResponse;
import com.erts.model.Event;
import com.erts.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // GET /api/events  — public, supports ?category=, ?search=, ?all=true, ?mine=true
    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean all,
            @RequestParam(required = false, defaultValue = "false") boolean mine,
            Authentication auth) {

        List<Event> events;

        if (mine && auth != null) {
            // Organiser fetching their own events
            events = eventService.getByOrganiser((String) auth.getPrincipal());
        } else if (all && auth != null) {
            // Admin fetching all events
            events = eventService.getAll();
        } else if (category != null && !category.isBlank()) {
            events = eventService.getByCategory(category);
        } else if (search != null && !search.isBlank()) {
            events = eventService.search(search);
        } else {
            events = eventService.getAllActive();
        }

        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    // GET /api/events/pending-approval  — admin only
    @GetMapping("/pending-approval")
    public ResponseEntity<ApiResponse<List<Event>>> pendingApproval() {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getPendingApproval()));
    }

    // GET /api/events/:id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getById(id)));
    }

    // POST /api/events  — organiser or admin creates event
    @PostMapping
    public ResponseEntity<ApiResponse<Event>> create(
            @RequestBody Event event,
            Authentication auth) {

        String userId = (String) auth.getPrincipal();
        String role   = auth.getAuthorities().iterator().next()
                            .getAuthority().replace("ROLE_", "");

        Event saved = "ADMIN".equals(role)
                ? eventService.createByAdmin(event)
                : eventService.create(event, userId);

        String msg = "ADMIN".equals(role)
                ? "Event created and published"
                : "Event submitted for admin approval";

        return ResponseEntity.ok(ApiResponse.ok(msg, saved));
    }

    // PUT /api/events/:id  — organiser edits own event / admin edits any
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> update(
            @PathVariable String id,
            @RequestBody Event event,
            Authentication auth) {

        String userId = (String) auth.getPrincipal();
        String role   = auth.getAuthorities().iterator().next()
                            .getAuthority().replace("ROLE_", "");

        return ResponseEntity.ok(ApiResponse.ok(
            "Event updated", eventService.update(id, event, userId, role)));
    }

    // PUT /api/events/:id/approve  — admin approves organiser event
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Event>> approveEvent(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Event approved and published",
            eventService.approveEvent(id)));
    }

    // PUT /api/events/:id/reject  — admin rejects organiser event
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Event>> rejectEvent(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok("Event rejected",
            eventService.rejectEvent(id, reason)));
    }

    // DELETE /api/events/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            Authentication auth) {

        String userId = (String) auth.getPrincipal();
        String role   = auth.getAuthorities().iterator().next()
                            .getAuthority().replace("ROLE_", "");

        eventService.delete(id, userId, role);
        return ResponseEntity.ok(ApiResponse.ok("Event deleted", null));
    }
}
