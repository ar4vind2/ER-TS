package com.erts.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "events")
public class Event {

    @Id
    private String id;

    // ── Owner ──────────────────────────────────────────────────────────────────
    private String organiserId;
    private String organiserName;

    // ── Event details ──────────────────────────────────────────────────────────
    private String title;
    private String description;
    private String category;
    private String emoji;

    // Image URL — organiser provides a publicly accessible image URL
    // e.g. https://example.com/event-banner.jpg
    private String imageUrl;

    // Optional multiple event images for slideshow rendering
    private List<String> imageUrls;

    private String date;
    private String dateDisplay;
    private String time;

    private String venue;
    private String address;

    private int    capacity;
    private int    registeredCount;
    private int    maxGroupSize;

    private double price;
    private String upiId;

    // ── Status ─────────────────────────────────────────────────────────────────
    // pending_approval | active | upcoming | closed | draft | rejected
    private String status;
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
