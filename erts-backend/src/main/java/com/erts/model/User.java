package com.erts.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String phone;
    private String passwordHash;
    private String organisation;

    // USER | ORGANISER | ADMIN
    private String role;

    // For ORGANISER: name of their organisation shown on events
    private String organiserName;

    // For ORGANISER: short bio or description
    private String organiserBio;

    private boolean approved; // ORGANISER accounts need admin approval

    private LocalDateTime createdAt;
}
