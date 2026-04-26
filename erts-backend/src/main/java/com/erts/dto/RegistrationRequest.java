package com.erts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class RegistrationRequest {

    @NotBlank
    private String eventId;

    // "solo" or "group"
    @NotBlank
    private String type;

    private AttendeeDto primaryAttendee;
    private List<AttendeeDto> members; // empty for solo

    @Data
    public static class AttendeeDto {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @Email @NotBlank private String email;
        private String phone;
        private String organisation;
        private String requirements;
    }
}
